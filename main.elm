import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, object4, object5, (:=))
import Json.Encode as Encode
import WebSocket
import Material
import Material.Button as Button
import Material.Scheme as Scheme
import Material.Options exposing (css)
import Material.Toggles as Toggles
import Material.Icon as Icon
import Material.Color as Color


-- Domotics user interface

{- in Safari, Develop, "Disable Cross-Origin Restrictions"
-- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
-- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}

-- GLOBAL
urlBase = "http://localhost:8080/"
-- urlGetActuators = urlBase ++ "rest/actuators"
-- /rest/act/{name}/{action}
urlUpdateActuators = urlBase ++ "rest/act/"
wsStatus = "ws://localhost:8080/" ++ "time/"

main =
  Html.App.program { init = init, view = view, update = update, subscriptions = subscriptions }


-- MODEL
type alias StatusRecord = { name: String, kind: String, on: Bool, level: Int, status: String }

type alias Model = { statuses: List StatusRecord, errorMsg: String, test: String, mdl : Material.Model }

init : (Model, Cmd Msg)
init = ( { statuses=[], errorMsg="No worries...", test="nothing tested", mdl=Material.model }, Cmd.none )

initialStatus : StatusRecord
initialStatus = { name="", kind="", on=False, level=0, status="" }

statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
  let
    checkName = (\rec -> rec.name == name)
    filteredList = List.filter checkName listOfRecords
  in
    Maybe.withDefault initialStatus (List.head filteredList)


-- UPDATE
type Msg = PutModelInTestAsString
          | Mdl (Material.Msg Msg)
          | NewStatus String
          | Clicked String
          | Checked String Bool
          | Down String
          | Up String
          | Check
          | RestError Http.Error
          | RestStatus (List StatusRecord)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    Check -> ( model, Cmd.none)
    PutModelInTestAsString -> ({model | test = (toString {model|test=""})}, Cmd.none)
    Clicked what -> ( model, toggleBlock what model )
    Checked what value -> ( model, updateStatusViaRestCmd what (if value then "on" else "off"))
    Down what -> ( model, updateStatusViaRestCmd what "down" )
    Up what -> ( model, updateStatusViaRestCmd what "up" )
    NewStatus str ->
        ({ model | statuses = (decodeStatuses str) }, Cmd.none)
    RestStatus statuses' -> ( {model | statuses = statuses', errorMsg="OK"}, Cmd.none)
    RestError error -> ({ model | errorMsg = toString error }, Cmd.none)
    Mdl message' -> Material.update message' model

-- TODO tuple teruggeven dat fout bevat, en dan met (value,error)=... en dan in model.error dat zetten als nodig
decodeStatuses : String -> List StatusRecord
decodeStatuses strJson =
  let
    result = decodeString statusesDecoder strJson
  in
    case result of
      Ok value -> value
      Err error -> []

statusesDecoder : Decoder (List StatusRecord)
statusesDecoder = Decode.list statusDecoder

statusDecoder : Decoder StatusRecord
statusDecoder =
  object5 StatusRecord
    ("name" := string)
    ("type" := string)
    ("on" := bool)
    ("level" := int)
    ("status" := string)

toggleBlock: String -> Model -> Cmd Msg
toggleBlock what model =
  let
    onOff = not (statusByName what model.statuses).on
    onOffText = if onOff then "on" else "off"
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
levelByName name model = (toString (statusByName name model.statuses).level)

isOnByName : String -> Model -> Bool
isOnByName name model = (statusByName name model.statuses).on

screenStatus : String -> Model -> String
screenStatus name model =
  let
    status = (statusByName name model.statuses).status
  in
    (toString status)

view : Model -> Html Msg
view model =
  div [ Html.Attributes.style [ ("padding", "2rem"), ("background", "azure") ] ]
  [
    div [Html.Attributes.style[ ("background","DarkSlateGrey"), ("color","white")]] [
      text "Model: ", text (toString model.statuses) ],
    div [][ Html.hr [] [] ],
    div [Html.Attributes.style[ ("background","DarkSlateGrey"), ("color","white")]] [
      button [ onClick PutModelInTestAsString ] [ text "Test"],
      text model.test ],
    div [][ Html.hr [] [] ],
    div [] [text "Error: ", text model.errorMsg],
    div [] [ button [ onClick Check ] [text "Check..."]],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Automaat"]],
    div [] [text "-"],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", (attribute "low" "20"), (attribute "high" "80"), Html.Attributes.max "120", Html.Attributes.value (levelByName "LichtScreen" model) ] [], text ((toString (statusByName "LichtScreen" model.statuses).level)++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", (attribute "low" "5"), (attribute "high" "15"), Html.Attributes.max "20", Html.Attributes.value (levelByName "Windmeter" model) ] [], text (toString (statusByName "Windmeter" model.statuses).level) ],
    div [] [ input [ type' "checkbox", checked (isOnByName "ZonneAutomaat" model), onCheck (Checked "ZonneAutomaat") ] [], text " zonne-automaat" ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Nutsruimtes"]],
    div [] [ Toggles.switch Mdl [1] model.mdl  [ Toggles.onClick (Clicked "LichtInkom"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Inkom"] ],
    div [] [ Toggles.switch Mdl [2] model.mdl  [ Toggles.onClick (Clicked "Gang Boven"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Gang Boven"] ],
    div [] [ Toggles.switch Mdl [3] model.mdl  [ Toggles.onClick (Clicked "Garage (Poort)"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Garage (Poort)"] ],
    div [] [ Toggles.switch Mdl [4] model.mdl  [ Toggles.onClick (Clicked "Garage (Tuin)"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Garage (Tuin)"] ],
    div [] [ Toggles.switch Mdl [5] model.mdl  [ Toggles.onClick (Clicked "Badkamer +1"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Badkamer +1"] ],
    div [] [ Toggles.switch Mdl [6] model.mdl  [ Toggles.onClick (Clicked "Badkamer"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Badkamer"] ],
    div [] [ Toggles.switch Mdl [7] model.mdl  [ Toggles.onClick (Clicked "WC"), Toggles.value (isOnByName "LichtInkom" model) ] [text "WC"] ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Beneden"]],
    div [] [ Toggles.switch Mdl [8] model.mdl  [ Toggles.onClick (Clicked "Keuken"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Keuken"] ],
    div [] [
       input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [
      Toggles.switch Mdl [9] model.mdl  [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Veranda"] ,
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [
      Toggles.switch Mdl [10] model.mdl  [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Eetkamer"],
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [ Toggles.switch Mdl [11] model.mdl  [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Circante Tafel"] ],
    div [] [
      Toggles.switch Mdl [0] model.mdl [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Zithoek"] ,
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [ Toggles.switch Mdl [12] model.mdl  [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Bureau"] ],

    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Kinderen"]],
    div [] [ Toggles.switch Mdl [13] model.mdl  [ Toggles.onClick (Clicked ""), Toggles.value (isOnByName "LichtInkom" model) ] [text "Tomas Spots"] ],
    div [] [ Toggles.switch Mdl [14] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Dries Wand"] ],
    div [] [ Toggles.switch Mdl [15] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Dries Spots"] ],
    div [] [ Toggles.switch Mdl [16] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Roos Wand"] ],
    div [] [ Toggles.switch Mdl [17] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Roos Spots"] ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Buiten"]],
    div [] [ Toggles.switch Mdl [18] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Licht terras en zijkant"] ],
    div [] [ Toggles.switch Mdl [19] model.mdl  [ Toggles.onClick (Clicked "0"), Toggles.value (isOnByName "LichtInkom" model) ] [text "Stopcontact buiten"] ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Screens"]],
    div [ ]
    [
      Button.render Mdl [20] model.mdl [ Button.minifab, Button.ripple, Button.onClick (Down "ScreenKeuken") ] [ Icon.i "arrow_downward"],
      Button.render Mdl [20] model.mdl [ Button.minifab, Button.ripple, Button.onClick (Up "ScreenKeuken") ] [ Icon.i "arrow_upward"],
      text (screenStatus "ScreenKeuken" model),
      text " | Screen Keuken"
      ],
    --div [] [ Toggles.switch Mdl [20] model.mdl  [  ] [text (screenStatus "ScreenKeuken" model), text "Screen Keuken"] ],
    div [][ Html.hr [] [] ]
  ]
  |> Scheme.topWithScheme Color.Green Color.Red
