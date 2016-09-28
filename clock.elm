import Html exposing (Html)
import Html.App as App
import Svg exposing (..)
import Svg.Attributes exposing (..)
import Time exposing (Time, second)



main =
  App.program
  { init = init
  , view = view
  , update = update
  , subscriptions = subscriptions
  }



-- MODEL


type alias Model = Time


init : (Model, Cmd Msg)
init =
  (0, Cmd.none)



-- UPDATE


type Msg
  = Tick Time


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
  Tick newTime ->
    (newTime, Cmd.none)



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Time.every second Tick



-- VIEW

-- diff js: can't take time here, must be fed from 'event pipe' (note: clock in domotic backend drives everything)
secondsHand : Model -> Svg Msg
secondsHand model =
  let
    rotation =  (floor (Time.inSeconds model)) % 60 * 6
    rotationString = "rotate(" ++ (toString rotation) ++ ")"
  in
    g [transform rotationString] [
      line [y1 "10", y2 "-38", stroke "#B40000"] []
      , line [y1 "10", y2 "2", stroke "#B40000", strokeWidth "3"] []
    ]

minutesHand : Model -> Svg Msg
minutesHand model =
  let
    rotation =  (floor (Time.inMinutes model)) % 60 * 6
    rotationString = "rotate(" ++ (toString rotation) ++ ")"
  in
    g [transform rotationString] [
      line [y1 "10", y2 "-38", stroke "#666"] []
    ]

hoursHand : Model -> Svg Msg
hoursHand model =
  let
    rotation =  (floor (Time.inHours model)) % 12 * 5
    rotationString = "rotate(" ++ (toString rotation) ++ ")"
  in
    g [transform rotationString] [
      line [y1 "2", y2 "-20", stroke "#333"] []
    ]


minor : Int -> Svg Msg
minor i =
  let
    rotation = 360 * (toFloat i) / 60
    rotationString = "rotate( " ++ (toString rotation) ++ " )"
  in
    line [ y1 "42", y2 "45", transform rotationString, stroke "#999", strokeWidth "0.5" ] []

major : Int -> Svg Msg
major i =
  let
    rotation = 360 * (toFloat i) / 12
    rotationString = "rotate( " ++ (toString rotation) ++ " )"
  in
    line [ y1 "35", y2 "45", transform rotationString, stroke "#333", strokeWidth "1" ] []


view : Model -> Html Msg
view model =
  svg [ viewBox "0 0 100 100", width "300px" ] [
    g [ transform "translate(50,50)" ]
      (List.concat [
        [ circle [ cx "0", cy "0", r "48", fill "white", stroke "#333" ] [] ]
        , List.map minor [0..59]
        , List.map major [0..11]
        , [
          hoursHand model
          , minutesHand model
          , secondsHand model
        ]
        ])
   ]

