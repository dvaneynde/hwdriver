module Domotic exposing (..)

import Dict
import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Navigation exposing (Location)
import Http
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, field, oneOf, succeed)
import WebSocket
import Material
import Material.Button as Button
import Material.Scheme as Scheme
import Material.Options as Options exposing (css)
import Material.Icon as Icon
import Material.Color as Color
import Material.Toggles as Toggles
import Material.Slider as Slider
import Material.Menu as Menu


----------------------------------------------------------
-- Domotics user interface
----------------------------------------------------------
{- in Safari, Develop, "Disable Cross-Origin Restrictions"
   But when on same server no problem.
   https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}
----------------------------------------------------------
-- URL's and WebSocket address

{-
   Set to Nothing for production use, set to backend if using Elm Reactor.
   TODO make parameter of program, so that it is set to Nothing from index.html - see Navigation.programWithFlags
-}
fixHost : Maybe String
fixHost =
    --Just "192.168.0.10:8080"
    Just "127.0.0.1:8080"
    -- Nothing

{-
   Determines host and port to used for backend; see also fixHost
-}
getHost : Location -> String
getHost location =
    case fixHost of
        Just hostAndPort ->
            hostAndPort

        Nothing ->
            location.host


urlUpdateActuators : Model -> String
urlUpdateActuators model =
    "http://" ++ model.host ++ "/rest/act/"



--"/rest/act/"


wsStatus : Model -> String
wsStatus model =
    "ws://" ++ model.host ++ "/status/"



--"/status/"
----------------------------------------------------------
-- GLOBALS


lichtmeterName : String
lichtmeterName =
    "LichtmeterZonnewering"



----------------------------------------------------------
-- MAIN


main : Program Never Model Msg
main =
    Navigation.program LocationChanged { init = init, view = view, update = update, subscriptions = subscriptions }



----------------------------------------------------------
-- MODEL

-- Which groups are expanded or collapsed
type alias Group2ExpandedDict =
    Dict.Dict String Bool


type ExtraStatus
    = None
    | OnOff Bool
    | OnOffLevel Bool Int
    | OnOffEco Bool Bool


type alias StatusRecord =
    { name : String, kind : String, groupName : String, groupSeq : Int, description : String, status : String, extra : ExtraStatus }

type alias Groups =
    Dict.Dict String (List StatusRecord)

type alias Model =
    { statuses : List StatusRecord, groups : Groups, group2Expanded : Group2ExpandedDict, errorMsg : String, test : String, mdl : Material.Model, host : String }


initialStatus : StatusRecord
initialStatus =
    { name = "", kind = "", groupName = "", groupSeq = 0, description = "", status = "", extra = None }


init : Location -> ( Model, Cmd Msg )
init location =
    ( { statuses = [], groups = Dict.empty, group2Expanded = initGroups, errorMsg = "No worries...", test = "nothing tested", mdl = Material.model, host = getHost location }, Cmd.none )


initGroups : Group2ExpandedDict
initGroups =
    Dict.fromList [ ( "ScreensZ", False ), ( "ScreensW", False ), ( "Beneden", True ), ( "Nutsruimtes", True ), ( "Kinderen", True ), ( "Buiten", False ) ]


statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
    let
        filteredList =
            List.filter (\rec -> rec.name == name) listOfRecords
    in
        Maybe.withDefault initialStatus (List.head filteredList)



----------------------------------------------------------
-- UPDATE


type Msg
    = PutModelInTestAsString
    | Mdl (Material.Msg Msg)
    | Clicked String
    | ClickedEco String
    | Checked String Bool
    | Down String
    | Up String
    | SliderMsg String Float
    | ToggleShowBlock String
    | NewStatusViaWs String
    | NewStatusViaRest (Result Http.Error (List StatusRecord))
    | LocationChanged Location


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        PutModelInTestAsString ->
--            ( { model | test = (toString { model | test = "" }.groups) }, Cmd.none )
--            ( { model | test = (toString (Dict.get "Beneden" { model | test = "" }.groups)) }, Cmd.none )
            ( { model | test = (toString { model | test = "" }) }, Cmd.none )

        Mdl message_ ->
            Material.update Mdl message_ model

        Clicked what ->
            ( model
            , let
                extra =
                    (statusByName what model.statuses).extra

                onOff =
                    not (isOn extra)

                --not (isOnByName what model.statuses)
                onOffText =
                    if onOff then
                        "on"
                    else
                        "off"
              in
                updateStatusViaRestCmd model what onOffText
            )

        ClickedEco what ->
            ( model, updateStatusViaRestCmd model what "ecoToggle" )

        Checked what value ->
            ( model
            , updateStatusViaRestCmd model
                what
                (if value then
                    "on"
                 else
                    "off"
                )
            )

        Down what ->
            ( model, updateStatusViaRestCmd model what "down" )

        Up what ->
            ( model, updateStatusViaRestCmd model what "up" )

        SliderMsg what level ->
            ( model, updateStatusViaRestCmd model what (toString level) )

        NewStatusViaWs str ->
            let
                ( newStatuses, error ) =
                    decodeStatuses str
            in
                ( { model | statuses = newStatuses, groups = createGroups newStatuses, errorMsg = error }, Cmd.none )

        ToggleShowBlock name ->
            ( { model | group2Expanded = (toggleGroup2Open model.group2Expanded name) }, Cmd.none )

        NewStatusViaRest (Ok newStatuses) ->
            ( { model | statuses = newStatuses, groups = createGroups newStatuses, errorMsg = "OK" }, Cmd.none )

        NewStatusViaRest (Err message) ->
            ( { model | errorMsg = ("NewStatusViaRest: " ++ (toString message)) }, Cmd.none )

        LocationChanged location ->
            ( { model | host = getHost location }, Cmd.none )


-- Takes list of StatusRecords and creates the Groups
createGroups : List StatusRecord -> Groups
createGroups statuses =
    let
        -- Gather doet iets raars, lijst van lijst en in inner lijst komt het eerste record en dan de lijst van de rest. Slim eigenlijk.
        listOfGroups = gatherWith (\a b -> a.groupName == b.groupName) statuses
        listOfTuplesWithJustGroupNameAndSortedBySeqList = List.map (\(r,l) -> (r.groupName, List.sortBy .groupSeq (r::l))) listOfGroups
    in
        Dict.fromList listOfTuplesWithJustGroupNameAndSortedBySeqList


isGroupOpen : Group2ExpandedDict -> String -> Bool
isGroupOpen blocks blockName =
    Maybe.withDefault True (Dict.get blockName blocks)


toggleGroup2Open : Group2ExpandedDict -> String -> Group2ExpandedDict
toggleGroup2Open group2Expanded name =
    let
        func =
            \maybe ->
                case maybe of
                    Maybe.Just b ->
                        Just (not b)

                    Nothing ->
                        Just True
    in
        Dict.update name func group2Expanded


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
    Decode.map7 StatusRecord
        (field "name" string)
        (field "type" string)
        (field "groupName" string)
        (field "groupSeq" int)
        (field "description" string)
        (field "status" string)
        (oneOf [ decoderExtraOnOffLevel, decoderExtraOnOffEco, decoderExtraOnOff, succeed None ])


decoderExtraOnOffLevel : Decoder ExtraStatus
decoderExtraOnOffLevel =
    Decode.map2 OnOffLevel (field "on" bool) (field "level" int)


decoderExtraOnOff : Decoder ExtraStatus
decoderExtraOnOff =
    Decode.map OnOff (field "on" bool)


decoderExtraOnOffEco : Decoder ExtraStatus
decoderExtraOnOffEco =
    Decode.map2 OnOffEco (field "on" bool) (field "eco" bool)


toggleBlock : String -> Model -> Cmd Msg
toggleBlock what model =
    let
        onOff =
            not (isOnByName what model.statuses)

        onOffText =
            if onOff then
                "on"
            else
                "off"
    in
        updateStatusViaRestCmd model what onOffText


updateStatusViaRestCmd : Model -> String -> String -> Cmd Msg
updateStatusViaRestCmd model name value =
    let
        url =
            urlUpdateActuators model ++ name ++ "/" ++ value

        request =
            Http.get url statusesDecoder
    in
        --Task.perform RestError NewStatusViaRest ()
        Http.send NewStatusViaRest request



----------------------------------------------------------
-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen (wsStatus model) NewStatusViaWs



----------------------------------------------------------
-- VIEW
-- https://design.google.com/icons/ - klik op icon, en dan onderaan klik op "< > Icon Font"
-- https://debois.github.io/elm-mdl/


levelByName : String -> List StatusRecord -> Float
levelByName name statuses =
    let
        status =
            statusByName name statuses
    in
        case status.extra of
            OnOffLevel isOn level ->
                (toFloat level)

            _ ->
                0.0


isOnByName : String -> List StatusRecord -> Bool
isOnByName name statuses =
    isOn (statusByName name statuses).extra


isOn : ExtraStatus -> Bool
isOn extraStatus =
    case extraStatus of
        OnOff isOn ->
            isOn

        OnOffLevel isOn level ->
            isOn

        OnOffEco isOn eco ->
            isOn

        None ->
            False


isEco : ExtraStatus -> Bool
isEco extraStatus =
    case extraStatus of
        OnOff isOn ->
            False

        OnOffLevel isOn level ->
            False

        OnOffEco isOn eco ->
            eco

        None ->
            False


screenStatus : String -> Model -> String
screenStatus name model =
    let
        status =
            (statusByName name model.statuses).status
    in
        (toString status)



-- toggleDiv ( "LichtInkom", "Inkom" ) 1 model


toggleDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleDiv ( name, desc ) nr model =
    let
        record =
            statusByName name model.statuses
    in
        case record.extra of
            None ->
                Html.text ("BUG toggleDiv for " ++ name)

            OnOff status ->
                div [] [ Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value status ] [ text desc ] ]

            OnOffLevel status level ->
                toggleWithSliderDiv ( name, desc ) nr model

            OnOffEco status eco ->
                div [ style [ ( "width", "300px" ) ] ]
                    [ div [ style [ ( "float", "left" ) ] ] [ Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value status ] [ text desc ] ]
                    , div [ style [ ( "float", "right" ) ] ] [ Toggles.switch Mdl [ (nr + 100) ] model.mdl [ Options.onToggle (ClickedEco name), Toggles.value eco ] [ text "eco" ] ]
                    , div [ style [ ( "clear", "both" ) ] ] []
                    ]


{-
viewSwitches : String -> Int -> Model -> Html Msg
viewSwitches groupName nr model =
    let
        switchStatuses = Dict.get groupName model.groups |> Maybe.withDefault[]
    in
        -- toggleDiv ( "LichtInkom", "Inkom" ) 1 model
        List.map (\s -> (toggleDiv (s.name, s.description) (nr+s.groupSeq*2) model)) switchStatuses
-}


toggleWithSliderDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleWithSliderDiv ( name, desc ) nr model =
    div [ style [ ( "width", "300px" ) ] ]
        [ div [ style [ ( "float", "left" ) ] ] [ Toggles.switch Mdl [ nr ] model.mdl [ Options.onToggle (Clicked name), Toggles.value (isOnByName name model.statuses) ] [ text desc ] ]
        , div [ style [ ( "float", "right" ) ] ]
            [ Slider.view
                ([ Slider.onChange (SliderMsg name), Slider.value (levelByName name model.statuses) ]
                    ++ (if (isOnByName name model.statuses) then
                            []
                        else
                            [ Slider.disabled ]
                       )
                )
            ]
        , div [ style [ ( "clear", "both" ) ] ] []
        ]


screenDiv : ( String, String ) -> Model -> Int -> Html Msg
screenDiv ( name, desc ) model nr =
    div []
        [ Button.render Mdl [ nr ] model.mdl [ Button.minifab, Button.ripple, Options.onClick (Down name) ] [ Icon.i "arrow_downward" ]
        , Button.render Mdl [ nr + 1 ] model.mdl [ Button.minifab, Button.ripple, Options.onClick (Up name) ] [ Icon.i "arrow_upward" ]
        , text (screenStatus name model)
        , text (" | " ++ desc)
        ]


screenWidgets : String -> Int -> Model -> Html Msg
screenWidgets groupName mdlID model =
    let
        statuses = Dict.get groupName model.groups |> Maybe.withDefault (List.singleton initialStatus)

        auto = List.filter(\s -> s.kind == "SunWindController") statuses
        autoHtml = case auto of
            [] ->
                []
            first::_ ->
                [ toggleDiv ( first.name, first.description ) mdlID model ]

        wind = List.filter(\s -> s.kind == "WindSensor") statuses
        windHtml = case wind of
            [] ->
                []
            first::_ ->
                let
                    sensorName = first.name
                in
                    [ div [{- style [ ( "display", "inline-block" ) ] -}]
                        [ text "Wind: "
                        , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "0", (attribute "low" "0"), (attribute "high" "900"), Html.Attributes.max "1200", Html.Attributes.value (toString (levelByName sensorName model.statuses)) ] []
                        , text ((toString ((levelByName sensorName model.statuses) / 100.0)) ++ "RPM - " ++ (toString (statusByName sensorName model.statuses).status))
                        ]
                    ]

        light = List.filter(\s -> s.kind == "LightSensor") statuses
        lightHtml = case light of
            [] ->
                []
            first::_ ->
                let
                    sensorName = first.name
                in
                    [ div [{- style [ ( "display", "inline-block" ) ] -}]
                            [ text "Zon: "
                            , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "3000", (attribute "low" "3400"), (attribute "high" "3600"), Html.Attributes.max "4000", Html.Attributes.value (toString (levelByName sensorName model.statuses)) ] []
                            , text (toString (levelByName sensorName model.statuses) ++ " - " ++ (toString (statusByName sensorName model.statuses).status))
                              --                            , text (toString (lightPercentage (levelByName lichtmeterName model.statuses)) ++ "% - " ++ (toString (statusByName lichtmeterName model.statuses).status))
                            ]
                    ]

        statusToScreen :  StatusRecord -> Html Msg
        statusToScreen status =
            screenDiv (status.name, status.description) model status.groupSeq

        screens = List.filter (\s -> s.kind == "Screen") statuses |>  List.map (\s -> (statusToScreen s))

    in
        div [] (autoHtml ++ windHtml ++ lightHtml ++ screens)


viewScreens : String -> Int -> Model -> Html Msg
viewScreens groupName nr model =
    if (isGroupOpen model.group2Expanded groupName) then
        div []
            [screenWidgets groupName nr model]
    else
        div [] []


-- true iff at least one actuator is on in the given group

somethingOn : Model -> String -> Bool
somethingOn model groupName =
    let
        groupStatuses =
            List.filter (\status -> (status.groupName == groupName)) model.statuses
    in
        List.foldl (\status -> \soFar -> (isOn status.extra) || soFar) False groupStatuses



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
            [ style [ ( "background-color", colorOfBlock model groupName ), ( "width", "250px" ), ( "margin", "0px 0px 10px 0px" ), ( "padding", "10px 10px 10px 10px" ) ] ]
            [ Button.render Mdl
                [ nr ]
                model.mdl
                [ Button.raised, Button.colored, Button.ripple, Options.onClick (ToggleShowBlock groupName) ]
                [ text
                    (if (isGroupOpen model.group2Expanded groupName) then
                        "Verberg"
                     else
                        "Toon"
                    )
                ]
            , Html.span [ style [ ( "padding-left", "20px" ), ( "font-size", "120%" ) ] ] [ text groupName ]
            ]
        , content model
        ]


viewGroup : (String -> Int -> Model -> Html Msg) -> String -> Int -> Model -> Html Msg
viewGroup subView groupName nr model =
    groupToggleBar groupName nr model (\model -> subView groupName nr model)


lightPercentage : Float -> Float
lightPercentage level =
    (level - 3000) / 40


view : Model -> Html Msg
view model =
    div [ Html.Attributes.style [ ( "padding", "2rem" ), ( "background", "azure" ) ] ]
        ([ Menu.render Mdl
            [ 1000 ]
            model.mdl
            [ Menu.bottomLeft ]
            [ Menu.item
                [ Menu.onSelect PutModelInTestAsString ]
                [ text "English (US)" ]
            , Menu.item
                [ Menu.onSelect PutModelInTestAsString ]
                [ text "français" ]
            ]
         , viewGroup viewScreens "ScreensZ" 100 model
         , viewGroup viewScreens "ScreensW" 200 model
         , groupToggleBar "Beneden"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Expanded "Beneden") then
                    div []
                        [ div [] [ Toggles.switch Mdl [ 8 ] model.mdl [ Options.onToggle (Clicked "LichtKeuken"), Toggles.value (isOnByName "LichtKeuken" model.statuses) ] [ text "Keuken" ] ]
                        , toggleWithSliderDiv ( "LichtVeranda", "Licht Veranda" ) 9 model
                        , div [] []
                        , toggleWithSliderDiv ( "LichtCircanteRondom", "Eetkamer" ) 10 model
                        , div [] [ Toggles.switch Mdl [ 11 ] model.mdl [ Options.onToggle (Clicked "LichtCircante"), Toggles.value (isOnByName "LichtCircante" model.statuses) ] [ text "Circante Tafel" ] ]
                        , toggleWithSliderDiv ( "LichtZithoek", "Zithoek" ) 21 model
                        , div [] [ Toggles.switch Mdl [ 12 ] model.mdl [ Options.onToggle (Clicked "LichtBureau"), Toggles.value (isOnByName "LichtBureau" model.statuses) ] [ text "Bureau" ] ]
                        ]
                else
                    div [] []
            )
--         , viewGroup viewSwitches "Nutsruimtes" 500 model
         , groupToggleBar "Nutsruimtes"
            101
            model
            (\model ->
                if (isGroupOpen model.group2Expanded "Nutsruimtes") then
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
                if (isGroupOpen model.group2Expanded "Kinderen") then
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
                if (isGroupOpen model.group2Expanded "Buiten") then
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


-- Copied from List.Extra in Elm Community

{-| Group equal elements together. A function is applied to each element of the list
and then the equality check is performed against the results of that function evaluation.
Elements will be grouped in the same order as they appear in the original list. The
same applies to elements within each group.
    gatherEqualsBy .age [{age=25},{age=23},{age=25}]
    --> [({age=25},[{age=25}]),({age=23},[])]
-}
gatherEqualsBy : (a -> b) -> List a -> List (a, List a)
gatherEqualsBy extract list =
    gatherWith (\a b -> (extract a) == (extract b)) list


{-| Group equal elements together using a custom equality function. Elements will be
grouped in the same order as they appear in the original list. The same applies to
elements within each group.
    gatherWith (==) [1,2,1,3,2]
    --> [(1,[1]),(2,[2]),(3,[])]
-}
gatherWith : (a -> a -> Bool) -> List a -> List (a, List a)
gatherWith testFn list =
    let
        helper : List a -> List (a,List a) -> List (a, List a)
        helper scattered gathered =
            case scattered of
                [] ->
                    List.reverse gathered

                toGather :: population ->
                    let
                        ( gathering, remaining ) =
                            List.partition (testFn toGather) population
                    in
                    helper remaining <| (toGather, gathering) :: gathered
    in
    helper list []