# usd-to-jpy

`usd-to-jpy` is an API for converting between US Dollars (USD) and Japanese Yen (JPY). It supports three types of rates: TTS (Telegraphic Transfer Selling), TTB (Telegraphic Transfer Buying), and TTM (Telegraphic Transfer Middle).

## URL Format

To use the API, structure your request URL as follows:

```
https://making.github.io/usd-to-jpy/yyyy/mm/dd/{TTS,TTB,TTM}
```

Replace `yyyy`, `mm`, and `dd` with the year, month, and day respectively. Choose between `TTS`, `TTB`, and `TTM` for the rate type.

## Example

To get the TTM rate for December 15, 2023, use a command like this:

```
$ curl https://making.github.io/usd-to-jpy/2023/12/15/TTM
142.45
```

This command will return the TTM rate for USD to JPY conversion as of that date.
