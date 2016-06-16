import Html exposing (Html, button, div, text, span, input, label, br)
import Html.App
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)
import Http
import Task exposing (Task)
import Json.Decode as Decode

-- Domotics user interface

-- GLOBAL

-- in Safari, Develop, "Disable Cross-Origin Restrictions"
-- maar als elm op zelfde server wordt aangeboden, misschien geen probleem
-- anders: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Allow-Origin
-- dit werkt: url = "http://localhost:8000/main.elm"
url : String
url = "http://192.168.0.10:8080/domo/actuators"

main =
  Html.App.program { init = init, view = view, update = update, subscriptions = (always Sub.none) }


-- MODEL

type alias Model = { actuators:String, keuken:Bool, badkamer:Bool }

init : (Model, Cmd Msg)
init = ( { actuators="Click Status button...", keuken=False, badkamer=False }, Cmd.none )


-- UPDATE

type Msg = CheckActuators | CheckOk String | CheckError Http.Error | Keuken Bool | Badkamer Bool

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    Keuken checked -> ({ model | keuken = checked }, Cmd.none)
    Badkamer checked -> ({ model | badkamer = checked }, Cmd.none)
    CheckActuators -> (model, checkCmd)
    CheckOk text -> ({ model | actuators = text }, Cmd.none)
    CheckError error -> ({ model | actuators = toString error }, Cmd.none)

checkTask : Task Http.Error String
checkTask = Http.getString url

checkCmd : Cmd Msg
checkCmd = Task.perform CheckError CheckOk checkTask

-- VIEW

view : Model -> Html Msg
view model =
  div [][
    div [] [ text "Actuators: ", text model.actuators ],
    div [] [ text "Check Actuators: " , button [ onClick CheckActuators ] [ text "status"]],
    div [][ Html.hr [] [] ],
    div []
    [ span [] [text "Domotics"]
    , label []
    [ br [] []
    , input [ type' "checkbox", checked model.keuken, onCheck Keuken ] []
    , text "Keuken"
    ]
    , label []
    [ br [] []
    , input [ type' "checkbox", checked model.badkamer, onCheck Badkamer ] []
    , text "Badkamer"
    ]
    ]
  ]
