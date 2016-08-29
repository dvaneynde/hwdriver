import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, object3, object4, (:=))
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
urlGetRobotInfo = urlBase ++ "rest/screenRobot"
urlPostRobotUpdate = urlBase ++ "rest/screenRobotUpdate"
wsStatus = "ws://localhost:8080/" ++ "time/"

main =
  Html.App.program { init = init, view = view, update = update, subscriptions = subscriptions }


-- MODEL
type alias StatusRecord = { name: String, kind: String, on: Bool, level: Int }

type alias Model = { statuses: List StatusRecord, actuators: String, sunLevel: Int, windLevel: Float, robotOn: Bool, errorMsg: String, test: String, statusAsString: String, mdl : Material.Model }

init : (Model, Cmd Msg)
init = ( { statuses=[], actuators="Click Status button...", sunLevel=0, windLevel=0.0, robotOn=False, errorMsg="No worries...", test="nothing tested", statusAsString="nothing yet...", mdl=Material.model }, Cmd.none )

initialStatus : StatusRecord
initialStatus = { name="", kind="", on=False, level=0 }

statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
  let
    checkName = (\rec -> rec.name == name)
    filteredList = List.filter checkName listOfRecords
  in
    Maybe.withDefault initialStatus (List.head filteredList)


-- UPDATE
type Msg = HandleRestError Http.Error
          | HandleRawRestError Http.RawError
          | RequestSubStatusViaRest
          | DecodeSubStatusViaRest SubStatusRecord
          | RobotClick Bool
          | PutModelInTestAsString
          | ShowResult String
          | DecodeUpdateResponse Http.Response
          | Click Int
          | Mdl (Material.Msg Msg)
          | NewStatus String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    PutModelInTestAsString -> ({model | test = (toString {model|test=""})}, Cmd.none)
    RequestSubStatusViaRest -> (model, getSubStatusViaRestCmd)
    DecodeSubStatusViaRest info -> ({ model | robotOn = info.robotOn, sunLevel = info.sunLevel, windLevel = info.windLevel }, Cmd.none)
    HandleRestError error -> ({ model | errorMsg = toString error }, Cmd.none)
    HandleRawRestError error -> ({ model | actuators = toString error }, Cmd.none)
    RobotClick value -> ( model, robotUpdateCmd value)
    Click nr -> ( model, robotUpdateCmd (not model.robotOn))
    ShowResult value -> ({model | test = value}, Cmd.none)
    DecodeUpdateResponse response -> ({model | test = (toString response.value), robotOn = (robotUpdateResponseDecoder response)}, Cmd.none)
    -- NewStatus str -> ({model | statusAsString = str}, Cmd.none)
    NewStatus str ->
        ({ model | statuses = (decodeStatuses str) }, Cmd.none)
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
  object4 StatusRecord
    ("name" := string)
    ("type" := string)
    ("on" := bool)
    ("level" := int)

-- checkActuatorsCmd : Cmd Msg
-- checkActuatorsCmd = Task.perform HandleRestError CheckActuatorsOk (Http.getString url)

type alias SubStatusRecord = { robotOn : Bool, sunLevel : Int, windLevel : Float }

-- Decodeert {"robotOn":false,"sunLevel":2867,"windLevel":12.394734}
subStatusDecoder : Decoder SubStatusRecord
subStatusDecoder =
  object3 SubStatusRecord ("robotOn" := bool) ("sunLevel" := int) ("windLevel" := float)

-- Typisch antwoord: {"robotOn":false,"sunLevel":2867,"windLevel":12.394734}
getSubStatusViaRestCmd : Cmd Msg
getSubStatusViaRestCmd =
  Task.perform HandleRestError DecodeSubStatusViaRest (Http.get subStatusDecoder urlGetRobotInfo)

-- newValue -> [send and recv] -> newValue -> [update model] -> newModel

robotUpdateEncoder : Bool -> String
robotUpdateEncoder newState =
  let info =
    Encode.object [ ("robotOn", Encode.bool newState)]
  in
    Encode.encode 0 info

robotUpdateCmd : Bool -> Cmd Msg
robotUpdateCmd newState =
  let
    request : Http.Request
    request =
      { verb = "POST"
      , headers = [ ( "Content-Type", "application/json" ) ]
      , url = urlPostRobotUpdate
      , body = Http.string (robotUpdateEncoder newState)
      }
  in
    Task.perform HandleRawRestError DecodeUpdateResponse (Http.send Http.defaultSettings request)

robotUpdateResponseDecoder : Http.Response ->  Bool
robotUpdateResponseDecoder response =
  case response.value of
    Http.Text value -> Result.withDefault False (Decode.decodeString Decode.bool value)
    Http.Blob blob -> False


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  WebSocket.listen wsStatus NewStatus


-- VIEW
-- https://design.google.com/icons/ - klik op icon, en dan onderaan klik op "< > Icon Font"
-- https://debois.github.io/elm-mdl/
view : Model -> Html Msg
view model =
  div [ Html.Attributes.style [ ("padding", "2rem"), ("background", "azure") ] ]
  [
    div [Html.Attributes.style[ ("background","DarkSlateGrey"), ("color","white")]] [
      text "Websocket Status: " ,
      text model.statusAsString ],
    div [][ Html.hr [] [] ],
    div [Html.Attributes.style[ ("background","DarkSlateGrey"), ("color","white")]] [
      button [ onClick PutModelInTestAsString ] [ text "Test"],
      text model.test ],
    div [][ Html.hr [] [] ],
    div [] [
      -- http://stackoverflow.com/questions/33857602/how-to-implement-a-slider-in-elm bevat ook eventhanlder
      Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [ text "Switch" ],
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "3800",Html.Attributes.value "2500"] [],
      Button.render Mdl [0] model.mdl [ Button.ripple, Button.colored, css "margin" "0 24px" ] [text "ALLES UIT!"]
    ],
    div [][ Html.hr [] [] ],
    div [] [text "Error: ", text model.errorMsg],
    div [][ Html.hr [] [] ],
    div [] [ button [ onClick RequestSubStatusViaRest ] [text "Check..."]],
    div [] [ input [ type' "checkbox", checked model.robotOn, onCheck RobotClick ] [], text " zonne-automaat" ],
    {--div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString (zonnesterkte model)) ] [], text ((toString (round (toFloat model.sunLevel/3650.0*100)))++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString (windsterkte model)) ] [], text (toString model.windLevel) ],
    --}
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString (10 * (statusByName "LichtScreen" model.statuses).level)) ] [], text ((toString (round (toFloat model.sunLevel/3650.0*100)))++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString (statusByName "Windmeter" model.statuses).level) ] [], text (toString model.windLevel) ],
    div [] [ Button.render Mdl [0] model.mdl [ Button.fab, Button.ripple, Color.background (Color.color Color.Blue Color.S100) ] [ Icon.i "arrow_downward"] ]
  ]
  |> Scheme.topWithScheme Color.Green Color.Red
