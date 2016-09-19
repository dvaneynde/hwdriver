module Main exposing (..)

import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, object6, (:=))
import Json.Encode as Encode
import WebSocket
import Material
import Material.Button as Button
import Material.Scheme as Scheme
import Material.Options exposing (css)
import Material.Toggles as Toggles
import Material.Icon as Icon
import Material.Color as Color
import Material.Slider as Slider


-- Domotics user interface
{- in Safari, Develop, "Disable Cross-Origin Restrictions"
   -- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
   -- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}
-- GLOBAL


urlBase =
    "http://localhost:8080/"



-- urlGetActuators = urlBase ++ "rest/actuators"
-- /rest/act/{name}/{action}


urlUpdateActuators =
    urlBase ++ "rest/act/"


wsStatus =
    "ws://localhost:8080/" ++ "time/"


main =
    Html.App.program { init = init, view = view, update = update, subscriptions = subscriptions }



-- MODEL


type alias StatusRecord =
    { name : String, kind : String, description : String, on : Bool, level : Int, status : String }


type alias Model =
    { statuses : List StatusRecord, errorMsg : String, test : String, mdl : Material.Model }


init : ( Model, Cmd Msg )
init =
    ( { statuses = [], errorMsg = "No worries...", test = "nothing tested", mdl = Material.model }, Cmd.none )


initialStatus : StatusRecord
initialStatus =
    { name = "", kind = "", description = "", on = False, level = 0, status = "" }


statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
    let
        checkName =
            (\rec -> rec.name == name)

        filteredList =
            List.filter checkName listOfRecords
    in
        Maybe.withDefault initialStatus (List.head filteredList)



-- UPDATE


type Msg
    = PutModelInTestAsString
    | Mdl (Material.Msg Msg)
    | NewStatus String
    | Clicked String
    | Checked String Bool
    | Down String
    | Up String
    | SliderMsg String Float
    | Check
    | RestError Http.Error
    | RestStatus (List StatusRecord)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Check ->
            ( model, Cmd.none )

        PutModelInTestAsString ->
            ( { model | test = (toString { model | test = "" }) }, Cmd.none )

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

        NewStatus str ->
            let
                ( newStatuses, error ) =
                    decodeStatuses str
            in
                ( { model | statuses = newStatuses, errorMsg = error }, Cmd.none )

        RestStatus statuses' ->
            ( { model | statuses = statuses', errorMsg = "OK" }, Cmd.none )

        RestError error ->
            ( { model | errorMsg = toString error }, Cmd.none )

        Mdl message' ->
            Material.update message' model



-- TODO tuple teruggeven dat fout bevat, en dan met (value,error)=... en dan in model.error dat zetten als nodig


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
    object6 StatusRecord
        ("name" := string)
        ("type" := string)
        ("description" := string)
        ("on" := bool)
        ("level" := int)
        ("status" := string)


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
    Task.perform RestError RestStatus (Http.get statusesDecoder (urlUpdateActuators ++ name ++ "/" ++ value))



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen wsStatus NewStatus



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
    Toggles.switch Mdl [ nr ] model.mdl [ Toggles.onClick (Clicked name), Toggles.value (isOnByName name model) ] [ text name ]


toggleDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleDiv ( name, desc ) nr model =
    div [] [ Toggles.switch Mdl [ nr ] model.mdl [ Toggles.onClick (Clicked name), Toggles.value (isOnByName name model) ] [ text desc ] ]


toggleWithSliderDiv : ( String, String ) -> Int -> Model -> Html Msg
toggleWithSliderDiv ( name, desc ) nr model =
    div [ style [ ( "display", "table-cell" ) ] ]
        [ Toggles.switch Mdl [ nr ] model.mdl [ Toggles.onClick (Clicked name), Toggles.value (isOnByName name model) ] [ text desc ]
        , Slider.view
            ([ Slider.onChange (SliderMsg name), Slider.value (level name model) ]
                ++ (if (isOnByName name model) then
                        []
                    else
                        [ Slider.disabled ]
                   )
            )
        ]


screenDiv : ( String, String ) -> Int -> Model -> Html Msg
screenDiv ( name, desc ) nr model =
    div []
        [ Button.render Mdl [ nr ] model.mdl [ Button.minifab, Button.ripple, Button.onClick (Down name) ] [ Icon.i "arrow_downward" ]
        , Button.render Mdl [ nr + 1 ] model.mdl [ Button.minifab, Button.ripple, Button.onClick (Up name) ] [ Icon.i "arrow_upward" ]
        , text (screenStatus name model)
        , text (" | " ++ desc)
        ]


view : Model -> Html Msg
view model =
    div [ Html.Attributes.style [ ( "padding", "2rem" ), ( "background", "azure" ) ] ]
        [ div [] [ Html.hr [] [] ]
        , div [] [ Html.h3 [] [ text "Screens" ] ]
        , toggleDiv ( "ZonneAutomaat", "Zon Wind Automaat" ) 30 model
        , div [{- style [ ( "display", "inline-block" ) ] -}]
            [ text "Zon: "
            , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "0", (attribute "low" "0"), (attribute "high" "80"), Html.Attributes.max "120", Html.Attributes.value (levelByName "LichtScreen" model) ] []
            , text ((toString (statusByName "LichtScreen" model.statuses).level) ++ "% - " ++ (toString (statusByName "LichtScreen" model.statuses).status))
            ]
        , div [{- style [ ( "display", "inline-block" ) ] -}]
            [ text "Wind: "
            , meter [ style [ ( "width", "250px" ), ( "height", "15px" ) ], Html.Attributes.min "0", (attribute "low" "0"), (attribute "high" "900"), Html.Attributes.max "1200", Html.Attributes.value (levelByName "Windmeter" model) ] []
            , text ((toString ((toFloat ((statusByName "Windmeter" model.statuses).level)) / 100.0)) ++ "RPM - " ++ (toString (statusByName "Windmeter" model.statuses).status))
            ]
        , screenDiv ( "ScreenKeuken", "Keuken" ) 20 model
        , screenDiv ( "ScreenTomas", "Tomas" ) 22 model
        , screenDiv ( "ScreenDriesTuin", "Dries Tuin" ) 24 model
        , screenDiv ( "ScreenDriesOpzij", "Dries Opzij" ) 26 model
        , screenDiv ( "ScreenRoos", "Roos" ) 28 model
        , screenDiv ( "ScreenBreed", "Breed" ) 30 model
        , screenDiv ( "ScreenLang", "Smal" ) 32 model
        , div [] [ Html.hr [] [] ]
        , div [] [ Html.h3 [] [ text "Nutsruimtes" ] ]
        , toggleDiv ( "LichtInkom", "Inkom" ) 1 model
        , toggleDiv ( "LichtGangBoven", "Gang Boven" ) 2 model
        , toggleDiv ( "LichtGaragePoort", "Garage Poort" ) 3 model
        , toggleDiv ( "LichtGarageTuin", "Garage Tuin" ) 4 model
        , toggleDiv ( "LichtBadk1", "Badkamer Boven" ) 5 model
        , toggleDiv ( "LichtBadk0", "Badkamer Beneden" ) 6 model
        , toggleDiv ( "LichtWC0", "WC" ) 7 model
        , div [] [ Html.hr [] [] ]
        , div [] [ Html.h3 [] [ text "Beneden" ] ]
        , div [] [ Toggles.switch Mdl [ 8 ] model.mdl [ Toggles.onClick (Clicked "LichtKeuken"), Toggles.value (isOnByName "LichtKeuken" model) ] [ text "Keuken" ] ]
        , toggleWithSliderDiv ( "LichtVeranda", "Licht Veranda" ) 9 model
        , div [] []
        , toggleWithSliderDiv ( "LichtCircanteRondom", "Circante Tafel" ) 10 model
        , div [] [ Toggles.switch Mdl [ 11 ] model.mdl [ Toggles.onClick (Clicked "LichtCircante"), Toggles.value (isOnByName "LichtCircante" model) ] [ text "Circante Tafel" ] ]
        , toggleWithSliderDiv ( "LichtZithoek", "Bureau" ) 21 model
        , div [] [ Toggles.switch Mdl [ 12 ] model.mdl [ Toggles.onClick (Clicked "LichtBureau"), Toggles.value (isOnByName "LichtBureau" model) ] [ text "Bureau" ] ]
        , div [] [ Html.hr [] [] ]
        , div [] [ Html.h3 [] [ text "Kinderen" ] ]
        , toggleDiv ( "LichtTomasSpots", "Tomas" ) 13 model
        , toggleDiv ( "LichtDriesWand", "Dries Wand" ) 14 model
        , toggleDiv ( "LichtDries", "Dries Spots" ) 15 model
        , toggleDiv ( "LichtRoosWand", "Roos Wand" ) 16 model
        , toggleDiv ( "LichtRoos", "Roos Spots" ) 17 model
        , div [] [ Html.hr [] [] ]
        , div [] [ Html.h3 [] [ text "Buiten" ] ]
        , toggleDiv ( "LichtTerras", "Licht terras en zijkant" ) 18 model
        , toggleDiv ( "StopkBuiten", "Stopcontact buiten" ) 19 model
        , div [] [ Html.hr [] [] ]
        , div [ Html.Attributes.style [ ( "background", "DarkSlateGrey" ), ( "color", "white" ) ] ]
            [ text "Model: "
            , text (toString model.statuses)
            ]
        , div [] [ Html.hr [] [] ]
        , div [ Html.Attributes.style [ ( "background", "DarkSlateGrey" ), ( "color", "white" ) ] ]
            [ button [ onClick PutModelInTestAsString ] [ text "Test" ]
            , text model.test
            ]
        , div [] [ text "Error: ", text model.errorMsg ]
        , div [] [ button [ onClick Check ] [ text "Check..." ] ]
        , div [] [ Html.hr [] [] ]
        ]
        |> Scheme.topWithScheme Color.Green Color.Red
