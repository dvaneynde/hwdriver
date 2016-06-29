import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode exposing (Decoder, decodeString, int, float, string, bool, object3, (:=))

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


main =
  Html.App.program { init = init, view = view, update = update, subscriptions = (always Sub.none) }


-- MODEL
type alias Model = { actuators: String, sunLevel: Int, windLevel: Float, robotOn: Bool, errorMsg: String }

init : (Model, Cmd Msg)
init = ( { actuators="Click Status button...", sunLevel=0, windLevel=0.0, robotOn=False, errorMsg="No worries..." }, Cmd.none )


-- UPDATE
type Msg = CheckActuators | CheckActuatorsOk String | CheckError Http.Error | CheckInfo | CheckInfoOk DecodedInfo

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    CheckActuators -> (model, checkActuatorsCmd)
    CheckActuatorsOk text -> ({ model | actuators = text }, Cmd.none)
    CheckInfo -> (model, checkInfoCmd)
    CheckInfoOk info -> ({ model | robotOn = info.robotOn, sunLevel = info.sunLevel, windLevel = info.windLevel }, Cmd.none)
    CheckError error -> ({ model | actuators = toString error }, Cmd.none)

checkActuatorsCmd : Cmd Msg
checkActuatorsCmd = Task.perform CheckError CheckActuatorsOk (Http.getString url)

type alias DecodedInfo = { robotOn : Bool, sunLevel : Int, windLevel : Float }
requestDecoder : Decoder DecodedInfo
requestDecoder =
  object3 DecodedInfo ("robotOn" := bool) ("sunLevel" := int) ("windLevel" := float)

checkInfoCmd : Cmd Msg
checkInfoCmd =
  Task.perform CheckError CheckInfoOk (Http.get requestDecoder url)


-- VIEW
view : Model -> Html Msg
view model =
  div [][
    div [] [ text "Actuators: ", text model.actuators ],
    div [] [ text "Check Actuators: " , button [ onClick CheckActuators ] [ text "status"]],
    div [][ Html.hr [] [] ],
    div [] [ button [ onClick CheckInfo ] [text "Check..."]],
    div [] [text "Error: ", text model.errorMsg],
    div [] [ input [ type' "checkbox", checked model.robotOn ] [], text " zonne-automaat" ],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString model.sunLevel) ] [], text ((toString (round (toFloat model.sunLevel/3650.0*100)))++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString model.windLevel) ] [], text (toString model.windLevel) ]
  ]
