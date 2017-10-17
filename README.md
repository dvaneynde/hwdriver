# elm-domotica-ui

## Installatie

1. In domotic.elm, zet urlBase naar ip domotica3 host
1. Voer volgende commandos uit:

```bash
$ elm make domotic.elm --output domotic.js
$ scp domotic.js domotica3:/home/dirk/domotic/static
```