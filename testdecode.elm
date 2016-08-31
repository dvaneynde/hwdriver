import Html exposing (text)
import Json.Decode exposing (..)

status: String
status = """
[
{
  "name": "Windmeter",
  "type": "WindSensor",
  "description": "No Description",
  "on": false,
  "level": 18
},
{
  "name": "Zonnesensor",
  "type": "SunSensor",
  "description": "Again No Description",
  "on": true,
  "level": 3567
}
]
"""

type alias StatusRecord = {
  name: String,
  kind: String,
  on: Bool,
  level: Int
}

init : StatusRecord
init = { name="", kind="", on=False, level=0 }

decodeStatuses : String -> (List StatusRecord, String)
decodeStatuses strJson =
  let
    result = decodeString statusesDecoder strJson
  in
    case result of
      Ok value -> (value, "")
      Err error -> ([], error)

statusesDecoder: Decoder (List StatusRecord)
statusesDecoder = list statusDecoder

statusDecoder: Decoder StatusRecord
statusDecoder =
  object4 StatusRecord
    ("name" := string)
    ("type" := string)
    ("on" := bool)
    ("level" := int)

statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
  let
    checkName = (\rec -> rec.name == name)
    filteredList = List.filter checkName listOfRecords
  in
    Maybe.withDefault init (List.head filteredList)

main =
  --text ("Decoded: " ++ (toString (decodeStatuses status)))
  let
    (value, _) = decodeStatuses status
    wind = statusByName "Windmeter" value
    sun = statusByName "Zonnesensor" value
  in
    text ("Wind : " ++ (toString wind) ++ " |    Sun:" ++ (toString sun))
  {--
  let
    (value, _) = decodeStatuses status
    --datte = List.head value
    datte = List.head (List.drop 1 value)
  in
    text ("Decoded: " ++ (toString (Maybe.withDefault init datte)))
    --}
