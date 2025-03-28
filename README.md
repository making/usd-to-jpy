# USD to JPY Converter API

The `usd-to-jpy` is an API for converting between US Dollars (USD) and Japanese Yen (JPY). It supports three types of rates: TTS (Telegraphic Transfer Selling), TTB (Telegraphic Transfer Buying), and TTM (Telegraphic Transfer Middle).

## URL Format

To use the API, structure your request URL as follows:

```
https://imadeit.github.io/usd-to-jpy/yyyy/mm/dd/{TTS,TTB,TTM}
```

Replace `yyyy`, `mm`, and `dd` with the year, month, and day respectively. Choose between `TTS`, `TTB`, and `TTM` for the rate type.

## Example

To get the TTM rate for December 15, 2023, use a command like this:

```
$ curl https://imadeit.github.io/usd-to-jpy/2023/12/15/TTM
142.45
```

This command will return the TTM rate for USD to JPY conversion as of that date.

## Usage in Google Sheets

This API can also be used in Google Sheets with the `IMPORTDATA` function. To integrate it, simply use the URL format in your `IMPORTDATA` formula. For example:

```
=IMPORTDATA("https://imadeit.github.io/usd-to-jpy/2023/12/15/TTM")
```

This will import the TTM rate for USD to JPY conversion on December 15, 2023, directly into your Google Sheets document.

<img width="831" alt="image" src="https://github.com/making/usd-to-jpy/assets/106908/1eeaeaf5-bb3e-4875-8c76-eabf812d6e7e">


## OpenAPI Spec

* [Swagger UI](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/making/usd-to-jpy/main/openapi.yaml)
* [Redoc](https://redocly.github.io/redoc/?url=https://raw.githubusercontent.com/making/usd-to-jpy/main/openapi.yaml)
