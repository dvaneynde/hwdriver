import Html exposing (Html, button, div, text, span, input, label, br)
import Html.App as Html
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)

-- TODO http://192.168.0.10:8080/domo/actuators om actuators op te roepen

main =
  Html.beginnerProgram { model = model, view = view, update = update }


-- MODEL

type alias Model = { counter:Int, keuken:Bool, badkamer:Bool }

model : Model
model =
  { counter=0, keuken=False, badkamer=False }


-- UPDATE

type Msg = Increment | Decrement | Reset | CheckActuators | Keuken Bool | Badkamer Bool

update : Msg -> Model -> Model
update msg model =
  case msg of
    Increment -> {model | counter = (up model)}
    Decrement -> {model | counter = (down model)}
    Reset -> { model | counter = 0}
    Keuken checked -> { model | keuken = checked }
    Badkamer checked -> { model | badkamer = checked }
    CheckActuators -> model

up : Model -> Int
up model =
  model.counter + 1
down : Model -> Int
down model =
  model.counter - 1


-- VIEW

view : Model -> Html Msg
view model =
  div [][
    div []
    [ button [ onClick Decrement ] [ text "-" ]
    , text (toString model)
    , button [ onClick Increment ] [ text "+" ],
    button [ onClick Reset] [text "Reset"]
    ],
    div [] [ text "Check Actuators: " , button [ onClick CheckActuators ] [ text "status"]],
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
