import Html exposing (text)
import Json.Decode exposing (..)

-- TYPES ===============================

type alias StatusRecord = {
  name: String,
  kind: String,
  on: Bool,
  level: Int
}


-- DECODING ===============================

-- Decodes a Json string into list of StatusRecords
decodeStatuses : String -> (List StatusRecord, String)
decodeStatuses strJson =
  let
    result = decodeString statusesDecoder strJson
  in
    case result of
      Ok value -> (value, "")
      Err error -> ([], error)

-- Decoder: decodes the list
statusesDecoder: Decoder (List StatusRecord)
statusesDecoder = list statusDecoder

-- Decoder: decodes one statusrecord Json object
statusDecoder: Decoder StatusRecord
statusDecoder =
  object4 StatusRecord
    ("name" := string)
    ("type" := string)
    ("on" := bool)
    ("level" := int)


-- HELPER ===============================
-- find StatusRecord by name
statusByName : String -> List StatusRecord -> StatusRecord
statusByName name listOfRecords =
  let
    checkName = (\rec -> rec.name == name)
    filteredList = List.filter checkName listOfRecords
  in
    Maybe.withDefault emptyStatus (List.head filteredList)

emptyStatus : StatusRecord
emptyStatus = { name="", kind="", on=False, level=0 }


-- TEST ===============================

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


main =
  --text ("Decoded: " ++ (toString (decodeStatuses status)))
  let
    (value, _) = decodeStatuses status
    wind = statusByName "Windmeter" value
    sun = statusByName "Zonnesensor" value
  in
    text ("Wind : " ++ (toString wind) ++ " |    Sun:" ++ (toString sun))
