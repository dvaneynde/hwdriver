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

type alias Model = { statuses: List StatusRecord, errorMsg: String, test: String, robotOn: Bool, mdl : Material.Model }

init : (Model, Cmd Msg)
init = ( { statuses=[], errorMsg="No worries...", test="nothing tested", robotOn=True, mdl=Material.model }, Cmd.none )

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
          | Click Int

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    PutModelInTestAsString -> ({model | test = (toString {model|test=""})}, Cmd.none)
    Click nr -> ( model, Cmd.none)
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
    div [] [ button [ onClick (Click 0) ] [text "Check..."]],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "SPECIAAL"]],
    div [] [ Button.render Mdl [1] model.mdl [ Button.fab, Button.ripple ] [ Icon.i "arrow_downward"] ],
    div [] [text "-"],
    div [] [text "Zon: ", meter [ Html.Attributes.min "0", (attribute "low" "20"), (attribute "high" "80"), Html.Attributes.max "120", Html.Attributes.value (toString (statusByName "LichtScreen" model.statuses).level) ] [], text ((toString (statusByName "LichtScreen" model.statuses).level)++"%") ],
    div [] [text "Wind: ", meter [ Html.Attributes.min "0", (attribute "low" "5"), (attribute "high" "15"), Html.Attributes.max "20", Html.Attributes.value (toString (statusByName "Windmeter" model.statuses).level) ] [], text (toString (statusByName "Windmeter" model.statuses).level) ],
    div [] [ input [ type' "checkbox", checked model.robotOn {-, onCheck (Click 0)-} ] [], text " zonne-automaat" ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Nutsruimtes"]],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Inkom"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Gang Boven"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Garage (Poort)"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Garage (Tuin)"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Badkamer +1"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Badkamer"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "WC"] ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Beneden"]],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Keuken"] ],
    div [] [
       input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [
      Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Veranda"] ,
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [
      Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Eetkamer"],
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Circante Tafel"] ],
    div [] [
      Toggles.switch Mdl [0] model.mdl [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Zithoek"] ,
      input [ type' "range", Html.Attributes.min "0", Html.Attributes.max "100",Html.Attributes.value "25"] []
    ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Bureau"] ],

    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Kinderen"]],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Tomas Spots"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Dries Wand"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Dries Spots"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Roos Wand"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Roos Spots"] ],
    div [][ Html.hr [] [] ],
    div [] [Html.h3 [] [text "Buiten"]],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Licht terras en zijkant"] ],
    div [] [ Toggles.switch Mdl [0] model.mdl  [ Toggles.onClick (Click 0), Toggles.value model.robotOn ] [text "Stopcontact buiten"] ],
    div [][ Html.hr [] [] ]
  ]
  |> Scheme.topWithScheme Color.Green Color.Red
