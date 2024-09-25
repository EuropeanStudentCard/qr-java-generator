# QRFactory

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This library provides functionality to retrieve QR codes for European Student Card Numbers (ESCNs), supporting different orientations, color schemes, and sizes.

## Usage

### Creating an Instance of QRFactory
You can create an instance of `QRFactory` with either the default host or a custom host URL.

### Retrieving a QR Code for a Specific ESCN
To retrieve a QR code for a European Student Card Number (ESCN), use the `generateQR` method. This method requires the ESCN, orientation, colors, and size of the QR code.


The `generateQR` method will return the QR code as an SVG string, which can be directly used or saved.

### Parameters

- `cardNumber`: The European Student Card Number (ESCN) for which the QR code is generated.
- `orientation`: Defines the orientation of the QR code. Available values:
    - `VERTICAL`
    - `HORIZONTAL`

- `colours`: Defines the color scheme of the QR code. Available values:
    - `NORMAL` (Standard colors)
    - `INVERTED` (Inverted color scheme)

- `size`: Defines the size of the QR code. Available values:
    - `XS` (41x41px)
    - `S` (61.5x61.5px)
    - `M` (164x164px)

## Enum Types

The `QRFactory` class uses the following enum types:

### `Orientation`
Defines the orientation of the QR code:
- `VERTICAL`
- `HORIZONTAL`

### `Colours`
Defines the color scheme of the QR code:
- `NORMAL`
- `INVERTED`

### `Size`
Defines the size of the QR code:
- `XS` - 41x41px
- `S`  - 61.5x61.5px
- `M`  - 164x164px

## Class Documentation

### `QRFactory`
A factory class for interacting with the European Student Card Router API.

#### Methods:
- `static QRFactory create()`: Creates a new instance of `QRFactory` with the default host URL.
- `static QRFactory create(String host)`: Creates a new instance of `QRFactory` with a custom host URL.
- `String generateQR(String cardNumber, String orientation, String colours, String size)`: Retrieves the QR code as an SVG string based on the provided parameters.