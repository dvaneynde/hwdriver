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

type alias Model = { statuses: List StatusRecord, errorMsg: String, test: String, mdl : Material.Model }

init : (Model, Cmd Msg)
init = ( { statuses=[], errorMsg="No worries...", test="nothing tested", mdl=Material.model }, Cmd.none )

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
type Msg = PutModelInTestAsString
          | Mdl (Material.Msg Msg)
          | NewStatus String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    PutModelInTestAsString -> ({model | test = (toString {model|test=""})}, Cmd.none)
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
      text "Model: ", text (toString model.statuses) ],
    div [][ Html.hr [] [] ],
    div [Html.Attributes.style[ ("background","DarkSlateGrey"), ("color","white")]] [
      button [ onClick PutModelInTestAsString ] [ text "Test"],
      text model.test ],
    div [][ Html.hr [] [] ],
    div [] [text "Error: ", text model.errorMsg],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", (attribute "low" "20"), (attribute "high" "80"), Html.Attributes.max "120", Html.Attributes.value (toString (statusByName "LichtScreen" model.statuses).level) ] [], text ((toString (statusByName "LichtScreen" model.statuses).level)++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", (attribute "low" "5"), (attribute "high" "15"), Html.Attributes.max "20", Html.Attributes.value (toString (statusByName "Windmeter" model.statuses).level) ] [], text (toString (statusByName "Windmeter" model.statuses).level) ]
  ]
  |> Scheme.topWithScheme Color.Green Color.Red
