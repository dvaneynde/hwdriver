import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode exposing (Decoder, decodeString, int, float, string, bool, object3, (:=))
import Json.Encode as Encode

-- Domotics user interface

{- in Safari, Develop, "Disable Cross-Origin Restrictions"
-- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
-- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}

-- GLOBAL
urlBase = "http://localhost:8080/domo/"
--url = "http://192.168.0.10:8080/domo/actuators"
--url="http://localhost:8080/domo/actuators"
urlGetRobotInfo = urlBase ++ "screenRobot"
urlPostRobotUpdate = urlBase ++ "screenRobotUpdate"

main =
  Html.App.program { init = init, view = view, update = update, subscriptions = (always Sub.none) }


-- MODEL
type alias Model = { actuators: String, sunLevel: Int, windLevel: Float, robotOn: Bool, errorMsg: String, test: String }

init : (Model, Cmd Msg)
init = ( { actuators="Click Status button...", sunLevel=0, windLevel=0.0, robotOn=False, errorMsg="No worries...", test="nothing tested" }, Cmd.none )


-- UPDATE
type Msg = CheckError Http.Error | CheckErrorRaw Http.RawError| CheckInfo | CheckInfoOk ReceivedInfoRecord |
    RobotClick Bool | Test | ShowResult String | DecodeUpdateResponse Http.Response

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    Test -> ({model | test = (toString {model|test=""})}, Cmd.none)
    CheckInfo -> (model, receiveInfoCmd)
    CheckInfoOk info -> ({ model | robotOn = info.robotOn, sunLevel = info.sunLevel, windLevel = info.windLevel }, Cmd.none)
    CheckError error -> ({ model | errorMsg = toString error }, Cmd.none)
    CheckErrorRaw error -> ({ model | actuators = toString error }, Cmd.none)
    RobotClick value -> ( model, updateInfoCmd value)
    ShowResult value -> ({model | test = value}, Cmd.none)
    DecodeUpdateResponse response -> ({model | test = (toString response.value), robotOn = (decodeUpdateResponse response)}, Cmd.none)

-- checkActuatorsCmd : Cmd Msg
-- checkActuatorsCmd = Task.perform CheckError CheckActuatorsOk (Http.getString url)

type alias ReceivedInfoRecord = { robotOn : Bool, sunLevel : Int, windLevel : Float }

receivedInfoDecoder : Decoder ReceivedInfoRecord
receivedInfoDecoder =
  object3 ReceivedInfoRecord ("robotOn" := bool) ("sunLevel" := int) ("windLevel" := float)

receiveInfoCmd : Cmd Msg
receiveInfoCmd =
  Task.perform CheckError CheckInfoOk (Http.get receivedInfoDecoder urlGetRobotInfo)

-- newValue -> [send and recv] -> newValue -> [update model] -> newModel

updateInfoEncoder : Bool -> String
updateInfoEncoder newState =
  let info =
    Encode.object [ ("robotOn", Encode.bool newState)]
  in
    Encode.encode 0 info

updateInfoCmd : Bool -> Cmd Msg
updateInfoCmd newState =
  let
    request : Http.Request
    request =
      { verb = "POST"
      , headers = [ ( "Content-Type", "application/json" ) ]
      , url = urlPostRobotUpdate
      , body = Http.string (updateInfoEncoder newState)
      }
  in
    Task.perform CheckErrorRaw DecodeUpdateResponse (Http.send Http.defaultSettings request)

decodeUpdateResponse : Http.Response ->  Bool
decodeUpdateResponse response =
  case response.value of
    Http.Text value -> Result.withDefault False (Decode.decodeString Decode.bool value)
    Http.Blob blob -> False

-- VIEW
view : Model -> Html Msg
view model =
  div [][
    div [] [ text "Test: " , button [ onClick Test ] [ text "Test"], text model.test],
    div [][ Html.hr [] [] ],
    div [] [text "Error: ", text model.errorMsg],
    div [][ Html.hr [] [] ],
    div [] [ button [ onClick CheckInfo ] [text "Check..."]],
    div [] [ input [ type' "checkbox", checked model.robotOn, onCheck RobotClick ] [], text " zonne-automaat" ],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString model.sunLevel) ] [], text ((toString (round (toFloat model.sunLevel/3650.0*100)))++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString model.windLevel) ] [], text (toString model.windLevel) ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Nutsruimtes"]],
    div [] [input[type' "checkbox"][], text "inkom"],
    div [] [input[type' "checkbox"][], text "Gang Boven"],
    div [] [input[type' "checkbox"][], text "Garage (Poort)"],
    div [] [input[type' "checkbox"][], text "Garage (Tuin)"],
    div [] [input[type' "checkbox"][], text "Badkamer +1"],
    div [] [input[type' "checkbox"][], text "Badkamer +0"],
    div [] [input[type' "checkbox"][], text "WC"]
    ]
