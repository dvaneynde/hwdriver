import Html exposing (text)
import Json.Decode exposing (..)

chainFront : String -> Int -> Int -> String
chainFront front seq until =
  if (seq == until) then
    front
  else
    (chainFront (front ++ "...another_" ++ (toString seq)) (seq+1) until)

chainBack : String -> Int -> Int -> String
chainBack back seq until =
  if (seq == until) then
    back
  else
    (chainBack ("...another_" ++ (toString seq) ++ " " ++ back) (seq+1) until)

chain : (String, Int) -> String -> (String, Int)
chain (already, seq) extra =
  (already ++ " " ++ extra ++ ":" ++ (toString seq), seq + 1)

main =
  -- text (("Chain Back: " ++ (chainBack "" 0 3)) ++ " ===== " ++ ("Chain Front: " ++ (chainFront "" 0 3)))
  text (toString (chain (chain (chain ("", 0) "A") "B") "C"))
