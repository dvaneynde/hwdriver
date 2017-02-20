module Domotic exposing (..)

import Dict
import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, map7, field)
import Json.Encode as Encode
import WebSocket
import Material
import Material.Button as Button
import Material.Scheme as Scheme
import Material.Options as Options exposing (css)
import Material.Icon as Icon
import Material.Color as Color
import Material.Toggles as Toggles
import Material.Slider as Slider


-- Domotics user interface
{- in Safari, Develop, "Disable Cross-Origin Restrictions"
   But when on same server no problem.
   anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}

-- GLOBAL


urlBase =
    "localhost:8080"
    --"192.168.0.10:8080"


urlUpdateActuators =
    "http://" ++ urlBase ++ "/rest/act/"


wsStatus =
    "ws://" ++ urlBase ++ "/status/"


main =
    Html.program { init = init, view = view, update = update, subscriptions = subscriptions }
    --To disable websockets for test:
    --Html.program { init = init, view = view, update = update, subscriptions = (\_ -> Sub.none) }



-- MODEL


type alias Group2OpenDict =
    Dict.Dict String Bool


type alias StatusRecord =
    { name : String, kind : String, group : String, description : String, on : Bool, level : Int, status : String }


type alias Model =
    { statuses : List StatusRecord, group2Open : Group2OpenDict, errorMsg : String, test : String, mdl : Material.Model }


init : ( Model, Cmd Msg )
init =
    ( { statuses = [], group2Open = initGroups, errorMsg = "No worries...", test = "nothing tested", mdl = Material.model }, Cmd.none )


initGroups : Group2OpenDict
initGroups =
    Dict.fromList [ ( "Screens", False ), ( "Beneden", True ), ( "Nutsruimtes", True ), ( "Kinderen", True ), ( "Buiten", False ) ]


initialStatus : StatusRecord
initialStatus =
    { name = "", kind = "", group = "", description = "", on = False, level = 0, status = "" }


statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
    let
        filteredList =
            List.filter (\rec -> rec.name == name) listOfRecords
    in
        Maybe.withDefault initialStatus (List.head filteredList)

nameInGroup : String -> String
nameInGroup group =
    let
        groupSplit = String.split ":" group

    in
        Maybe.withDefault group (List.head groupSplit)

-- UPDATE


type Msg
    = PutModelInTestAsString
    | Mdl (Material.Msg Msg)
    | Clicked String
    | Checked String Bool
    | Down String
    | Up String
    | SliderMsg String Float
    | ToggleShowBlock String
    | NewStatusViaWs String
    | NewStatusViaRest (Result Http.Error (List StatusRecord))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        PutModelInTestAsString ->
            ( { model | test = (toString { model | test = "" }) }, Cmd.none )

        Mdl message_ ->
            Material.update Mdl message_ model

        Clicked what ->
            ( model, toggleBlock what model )

        Checked what value ->
            ( model
            , updateStatusViaRestCmd what
                (if value then
                    "on"
                 else
                    "off"
                )
            )

        Down what ->
            ( model, updateStatusViaRestCmd what "down" )

        Up what ->
            ( model, updateStatusViaRestCmd what "up" )

        SliderMsg what level ->
            ( model, updateStatusViaRestCmd what (toString level) )

        NewStatusViaWs str ->
            let
                ( newStatuses, error ) =
                    decodeStatuses str
            in
                ( { model | statuses = newStatuses, errorMsg = error }, Cmd.none )

        ToggleShowBlock name ->
            ( { model | group2Open = (toggleGroup2Open model.group2Open name) }, Cmd.none )

        NewStatusViaRest (Ok statuses_) ->
            ( {  model | statuses = statuses_, errorMsg = "OK" }, Cmd.none )

        NewStatusViaRest (Err message) ->
            ( { model | errorMsg = ("NewStatusViaRest: "++ (toString message)) }, Cmd.none )


isGroupOpen : Group2OpenDict -> String -> Bool
isGroupOpen blocks blockName =
    Maybe.withDefault True (Dict.get blockName blocks)



toggleGroup2Open : Group2OpenDict -> String -> Group2OpenDict
toggleGroup2Open group2Open name =
    let
        func =
            \maybe ->
                case maybe of
                    Maybe.Just b ->
                        Just (not b)

                    Nothing ->
                        Just True
    in
        Dict.update name func group2Open


decodeStatuses : String -> ( List StatusRecord, String )
decodeStatuses strJson =
    let
        result =
            decodeString statusesDecoder strJson
    in
        case result of
            Ok value ->
                ( value, "" )

            Err error ->
                ( [], error )


statusesDecoder : Decoder (List StatusRecord)
statusesDecoder =
    Decode.list statusDecoder


statusDecoder : Decoder StatusRecord
statusDecoder =
    map7 StatusRecord
        (field "name" string)
        (field "type" string)
        (field "group" string)
        (field "description" string)
        (field "on" bool)
        (field "level" int)
        (field "status" string)


toggleBlock : String -> Model -> Cmd Msg
toggleBlock what model =
    let
        onOff =
            not (statusByName what model.statuses).on

        onOffText =
            if onOff then
                "on"
            else
                "off"
    in
        updateStatusViaRestCmd what onOffText


updateStatusViaRestCmd : String -> String -> Cmd Msg
updateStatusViaRestCmd name value =
    let
        url = urlUpdateActuators ++ name ++ "/" ++ value
        request = Http.get url statusesDecoder
    in
        --Task.perform RestError NewStatusViaRest ()
        Http.send NewStatusViaRest request



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen wsStatus NewStatusViaWs



-- VIEW
-- https://design.google.com/icons/ - klik op icon, en dan onderaan klik op "< > Icon Font"
-- https://debois.github.io/elm-mdl/


levelByName : String -> Model -> String
levelByName name model =
    (toString (statusByName name model.statuses).level)


isOnByName : String -> Model -> Bool
isOnByName name model =
    (statusByName name model.statuses).on


screenStatus : String -> Model -> String
screenStatus name model =
    let
        status =
            (statusByName name model.statuses).status
    in
        (toString status)


level : String -> Model -> Float
level name model =
    (toFloat (statusByName name model.statuses).level)


toggle : String -> Int -> Model -> Html Msg
toggle name nr model =
    Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value (isOnByName name model) ] [ text name ]


toggleDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleDiv ( name, desc ) nr model =
    div [] [ Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value (isOnByName name model) ] [ text desc ] ]


toggleWithSliderDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleWithSliderDiv ( name, desc ) nr model =
    div [ style [ ( "display", "inline-block" ) ] ]
        -- TODO inline-block helpt niet, want slider zelf is block; hoe aanpassen?
        [ Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value (isOnByName name model) ] [ text desc ]
        , Slider.view
            ([ Slider.onChange (SliderMsg name), Slider.value (level name model) ]
                ++ (if (isOnByName name model) then
                        []
                    else
                        [ Slider.disabled ]
                   )
            )
        ]


screenDiv : ( String, String ) -> Model -> Int -> Html Msg
screenDiv ( name, desc ) model nr =
    div []
        [ Button.render Mdl [ nr ] model.mdl [ Button.minifab, Button.ripple, Options.onClick (Down name) ] [ Icon.i "arrow_downward" ]
        , Button.render Mdl [ nr + 1 ] model.mdl [ Button.minifab, Button.ripple, Options.onClick (Up name) ] [ Icon.i "arrow_upward" ]
        , text (screenStatus name model)
        , text (" | " ++ desc)
        ]


chainWidget : (Int -> a) -> ( List a, Int ) -> ( List a, Int )
chainWidget widgetToAddTakingId ( widgets, seq ) =
    let
        widgetToAdd =
            widgetToAddTakingId seq
    in
        ( List.append widgets [ widgetToAdd ], seq + 2 )


screenWidgets : Model -> List (Html Msg)
screenWidgets model =
    let
        --(list, _) = ([],0) |> chainWidget (screenDiv ( "ScreenKeuken", "Keuken" ) model)
        first =
            screenDiv ( "ScreenKeuken", "Keuken" ) model 0

        --(result, _) = chainWidget (screenDiv ( "ScreenTomas", "Tomas" )) ([first], 2)
        ( result, _ ) =
            ( [ first ], 2 )
                |> chainWidget (screenDiv ( "ScreenTomas", "Tomas" ) model)
                |> chainWidget (screenDiv ( "ScreenDriesTuin", "Dries Tuin" ) model)
                |> chainWidget (screenDiv ( "ScreenDriesOpzij", "Dries Opzij" ) model)
                |> chainWidget (screenDiv ( "ScreenRoos", "Roos" ) model)
                |> chainWidget (screenDiv ( "ScreenBreed", "Breed" ) model)
                |> chainWidget (screenDiv ( "ScreenLang", "Smal" ) model)
    in
        result

-- true iff at least one actuator is on in the given group
somethingOn : Model -> String -> Bool
somethingOn model groupName =
    let
        groupStatuses = List.filter (\status -> ((nameInGroup status.group) == groupName)) model.statuses
    in
        List.foldl (\status -> \soFar -> status.on || soFar) False groupStatuses

-- other color depending on wether something is on or off
colorOfBlock : Model -> String -> String
colorOfBlock model groupName =
    if (somethingOn model groupName) then
        "orange"
    else
        "green"


groupToggleBar : String -> Int -> Model -> (Model -> Html Msg) -> Html Msg
groupToggleBar groupName nr model content =
    div []
        [ div
            [ style [ ( "background-color", colorOfBlock model groupName ), ( "width", "250px" ), ("margin","0px 0px 10px 0px"), ("padding","10px 10px 10px 10px") ] ]
            [ Button.render Mdl
                [ nr ]
                model.mdl
                [ Button.raised, Button.colored, Button.ripple, Options.onClick (ToggleShowBlock groupName) ]
                [ text
                    (if (isGroupOpen model.group2Open groupName) then
                        "Verberg"
                     else
                        "Toon"
                    )
                ]
            , Html.span [ style [ ( "padding-left", "20px" ), ( "font-size", "120%" ) ] ] [ text groupName ]
            ]
        , content model
        ]

lightPercentage level = (level - 3000) // 40

view : Model -> Html Msg
view model =
    div [ Html.Attributes.style [ ( "padding", "2rem" ), ( "background", "azure" ) ] ]
        ([ groupToggleBar "Screens"
            100
            model
            (\model ->
                if (isGroupOpen model.group2Open "Screens") then
                    div []
                        ([ toggleDiv ( "ZonWindAutomaat", "Zon Wind Automaat" ) 30 model
                         , div [{- style [ ( "display", "inline-block" ) ] -}]
                            [ text "Zon: "
                            , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "3000", (attribute "low" "3400"), (attribute "high" "3600"), Html.Attributes.max "4000", Html.Attributes.value (levelByName "Lichtmeter" model) ] []
                            , text (toString (lightPercentage((statusByName "Lichtmeter" model.statuses).level)) ++ "% - " ++ (toString (statusByName "Lichtmeter" model.statuses).status))
                            ]
                         , div [{- style [ ( "display", "inline-block" ) ] -}]
                            [ text "Wind: "
                            , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "0", (attribute "low" "0"), (attribute "high" "900"), Html.Attributes.max "1200", Html.Attributes.value (levelByName "Windmeter" model) ] []
                            , text ((toString (toFloat ((statusByName "Windmeter" model.statuses).level) / 100.0)) ++ "RPM - " ++ (toString (statusByName "Windmeter" model.statuses).status))
                            ]
                         ]
                            ++ screenWidgets model
                        )
                else
                    div [] []
            )
         , groupToggleBar "Beneden"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Open "Beneden") then
                    div []
                        [ div [] [ Toggles.switch Mdl [ 8 ] model.mdl [ Options.onToggle (Clicked "LichtKeuken"), Toggles.value (isOnByName "LichtKeuken" model) ] [ text "Keuken" ] ]
                        , toggleWithSliderDiv ( "LichtVeranda", "Licht Veranda" ) 9 model
                        , div [] []
                        , toggleWithSliderDiv ( "LichtCircanteRondom", "Eetkamer" ) 10 model
                        , div [] [ Toggles.switch Mdl [ 11 ] model.mdl [ Options.onToggle (Clicked "LichtCircante"), Toggles.value (isOnByName "LichtCircante" model) ] [ text "Circante Tafel" ] ]
                        , toggleWithSliderDiv ( "LichtZithoek", "Zithoek" ) 21 model
                        , div [] [ Toggles.switch Mdl [ 12 ] model.mdl [ Options.onToggle (Clicked "LichtBureau"), Toggles.value (isOnByName "LichtBureau" model) ] [ text "Bureau" ] ]
                        ]
                else
                    div [] []
            )
         , groupToggleBar "Nutsruimtes"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Open "Nutsruimtes") then
                    div []
                        [ toggleDiv ( "LichtInkom", "Inkom" ) 1 model
                        , toggleDiv ( "LichtGaragePoort", "Garage Poort" ) 3 model
                        , toggleDiv ( "LichtGarageTuin", "Garage Tuin" ) 4 model
                        , toggleDiv ( "LichtBadk0", "Badkamer Beneden" ) 6 model
                        , toggleDiv ( "LichtWC0", "WC" ) 7 model
                        ]
                else
                    div [] []
            )
         , groupToggleBar "Kinderen"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Open "Kinderen") then
                    div []
                        [ toggleDiv ( "LichtGangBoven", "Gang Boven" ) 2 model
                        , toggleDiv ( "LichtBadk1", "Badkamer Boven" ) 5 model
                        , toggleDiv ( "LichtTomasSpots", "Tomas" ) 13 model
                        , toggleDiv ( "LichtDriesWand", "Dries Wand" ) 14 model
                        , toggleDiv ( "LichtDries", "Dries Spots" ) 15 model
                        , toggleDiv ( "LichtRoosWand", "Roos Wand" ) 16 model
                        , toggleDiv ( "LichtRoos", "Roos Spots" ) 17 model
                        ]
                else
                    div [] []
            )
         , groupToggleBar "Buiten"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Open "Buiten") then
                    div []
                        [ toggleDiv ( "LichtTerras", "Licht terras en zijkant" ) 18 model
                        , toggleDiv ( "StopkBuiten", "Stopcontact buiten" ) 19 model
                        ]
                else
                    div [] []
            )
         , div [] [ Html.hr [] [] ]
         , div [] [ text "Error: ", text model.errorMsg ]
         , div [ Html.Attributes.style [ ( "background", "DarkSlateGrey" ), ( "color", "white" ) ] ]
            [ button [ onClick PutModelInTestAsString ] [ text "Test" ]
            , text model.test
            ]
         ]
        )
        |> Scheme.topWithScheme Color.Green Color.Red
