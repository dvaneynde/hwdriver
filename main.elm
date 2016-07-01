import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode exposing (Decoder, decodeString, int, float, string, bool, object3, (:=))
import Json.Encode as Encode

-- Domotics user interface

{- in Safari, Develop, "Disable Cross-Origin Restrictions"
-- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
-- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}

-- GLOBAL
url : String
--url = "http://192.168.0.10:8080/domo/actuators"
--url="http://localhost:8080/domo/actuators"
url = "http://localhost:8080/domo/screenRobot"

urlPost :  String
urlPost = "http://localhost:8080/domo/screenRobotUpdate"


main =
  Html.App.program { init = init, view = view, update = update, subscriptions = (always Sub.none) }


-- MODEL
type alias Model = { actuators: String, sunLevel: Int, windLevel: Float, robotOn: Bool, errorMsg: String, test: String }

init : (Model, Cmd Msg)
init = ( { actuators="Click Status button...", sunLevel=0, windLevel=0.0, robotOn=False, errorMsg="No worries...", test="nothing tested" }, Cmd.none )


-- UPDATE
type Msg = CheckActuators | CheckActuatorsOk String | CheckError Http.Error | CheckErrorRaw Http.RawError| CheckInfo | CheckInfoOk DecodedInfo |
    RobotClick Bool | Test | ShowResult String | ShowResponse Http.Response

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    CheckActuators -> (model, checkActuatorsCmd)
    CheckActuatorsOk text -> ({ model | actuators = text }, Cmd.none)
    CheckInfo -> (model, checkInfoCmd)
    CheckInfoOk info -> ({ model | robotOn = info.robotOn, sunLevel = info.sunLevel, windLevel = info.windLevel }, Cmd.none)
    CheckError error -> ({ model | actuators = toString error }, Cmd.none)
    CheckErrorRaw error -> ({ model | actuators = toString error }, Cmd.none)
    RobotClick value -> ({ model | robotOn = value}, updateInfoCmd model)
    Test -> ({model | test = (toString(sendInfoEncoded model))}, Cmd.none)
    ShowResult value -> ({model | test = value}, Cmd.none)
    ShowResponse value -> ({model | test = (toString value)}, Cmd.none)

checkActuatorsCmd : Cmd Msg
checkActuatorsCmd = Task.perform CheckError CheckActuatorsOk (Http.getString url)

type alias DecodedInfo = { robotOn : Bool, sunLevel : Int, windLevel : Float }
requestDecoder : Decoder DecodedInfo
requestDecoder =
  object3 DecodedInfo ("robotOn" := bool) ("sunLevel" := int) ("windLevel" := float)

checkInfoCmd : Cmd Msg
checkInfoCmd =
  Task.perform CheckError CheckInfoOk (Http.get requestDecoder url)

sendInfoEncoded : Model -> String
sendInfoEncoded model =
  let info =
    Encode.object [ ("robotOn", Encode.bool model.robotOn)]
  in
    Encode.encode 0 info

updateInfoCmd : Model -> Cmd Msg
updateInfoCmd model =
  Task.perform CheckErrorRaw ShowResponse (updateInfo model)

--updateInfo: Model -> Task Http.Error String
--updateInfo model =
  --Http.post string urlPost (Http.string """{"robotOn": false}""")
  -- (Http.string ("{ \"robotOn\": " ++ (toString model.robotOn) ++ " }"))
  -- (Http.string """{ "robotOn": "false" }""")
  --(Http.string (sendInfoEncoded model))--Http.empty --(Http.string (sendInfoEncoded model))
updateInfo: Model -> Task Http.RawError Http.Response
updateInfo model =
  Http.send Http.defaultSettings
      { verb = "POST"
      , headers =
          [ ( "Content-Type", "application/json" )
        
          ]
      , url = urlPost
      , body = Http.string (sendInfoEncoded model)
      }

-- VIEW
view : Model -> Html Msg
view model =
  div [][
    div [] [ text "Test: " , button [ onClick Test ] [ text "Test"], text model.test],
    div [][ Html.hr [] [] ],
    div [] [ text "Actuators: ", text model.actuators ],
    div [] [ text "Check Actuators: " , button [ onClick CheckActuators ] [ text "status"]],
    div [][ Html.hr [] [] ],
    div [] [ button [ onClick CheckInfo ] [text "Check..."]],
    div [] [text "Error: ", text model.errorMsg],
    div [] [ input [ type' "checkbox", checked model.robotOn, onCheck RobotClick ] [], text " zonne-automaat" ],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString model.sunLevel) ] [], text ((toString (round (toFloat model.sunLevel/3650.0*100)))++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString model.windLevel) ] [], text (toString model.windLevel) ]
  ]
