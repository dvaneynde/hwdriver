module Main exposing (..)

import Html exposing (text)
import Json.Decode exposing (..)


chain : String -> ( String, Int ) -> ( String, Int )
chain extra ( already, seq ) =
    ( already ++ " " ++ extra ++ ":" ++ (toString seq), seq + 1 )


chain2 : (Int -> a) -> ( List a, Int ) -> ( List a, Int )
chain2 extraFunction ( already, seq ) =
    let
        extra =
            extraFunction seq
    in
        ( List.append already [ extra ], seq + 1 )


type alias Model =
    String


v : Int -> Model -> String
v nr model =
    "another " ++ model ++ " with index " ++ (toString nr)


main =
    -- text (toString (chain "C" (chain "B" (chain "A" ("",0)))))
    -- text (toString (("",0) |> chain "A" |> chain "B" |> chain "C"))
    -- text (toString ( chain2 w (chain2 w ([""],0))))
    let
        ( operations, _ ) =
            chain2 v (chain2 v ( [ \m -> (v 0) m ], 1 ))
    in
        text (toString (List.map (\f -> f "modelletje") operations))
