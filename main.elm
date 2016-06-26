import Html exposing (Html, button, div, text, span, input, label, br, meter)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode

-- Domotics user interface

{- in Safari, Develop, "Disable Cross-Origin Restrictions"
-- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
-- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-}

-- GLOBAL
url : String
url = "http://192.168.0.10:8080/domo/actuators"

testJson : String
testJson = """
{
  sunAuto: true,
  sunLevel: 3465,
  windLevel: 4.25
}
"""

main =
  Html.App.program { init = init, view = view, update = update, subscriptions = (always Sub.none) }


-- MODEL
type alias Model = { actuators: String, sunLevel: Int, windLevel: Float, sunScreenAuto: Bool }

init : (Model, Cmd Msg)
init = ( { actuators="Click Status button...", sunLevel=3500, windLevel=1.3, sunScreenAuto=False }, Cmd.none )


-- UPDATE
type Msg = CheckActuators | CheckOk String | CheckError Http.Error | CheckInfo | CheckInfoOk String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    CheckActuators -> (model, checkCmd)
    CheckOk text -> ({ model | actuators = text }, Cmd.none)
    CheckInfo -> (tmpCheckInfo model, Cmd.none)
    CheckInfoOk text -> (model, Cmd.none)
    CheckError error -> ({ model | actuators = toString error }, Cmd.none)

checkTask : Task Http.Error String
checkTask = Http.getString url

checkCmd : Cmd Msg
checkCmd = Task.perform CheckError CheckOk checkTask

tmpCheckInfo : Model -> Model
tmpCheckInfo model =
  { model | windLevel=8, sunLevel = 450}
  
-- VIEW
view : Model -> Html Msg
view model =
  div [][
    div [] [ text "Actuators: ", text model.actuators ],
    div [] [ text "Check Actuators: " , button [ onClick CheckActuators ] [ text "status"]],
    div [][ Html.hr [] [] ],
    div [] [ button [ onClick CheckInfo ] [text "Check..."]],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", Html.Attributes.max "3800", Html.Attributes.value (toString model.sunLevel) ] [], text (toString (round (toFloat model.sunLevel/3650.0*100))) ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", Html.Attributes.max "8.5", Html.Attributes.value (toString model.windLevel) ] [], text (toString (round (model.windLevel/15.0*100))) ]
  ]
