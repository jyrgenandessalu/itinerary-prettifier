# Prettifier

**Prettifier** is a Java-based application designed to read an itinerary file, format dates and times, replace airport codes with their corresponding city or airport names, and generate a cleaner, more readable output. The program also handles flexible column orders in an airport lookup file.

## Features

- **Airport Code Replacement**: The program replaces ICAO and IATA codes with the respective city or airport names from a CSV lookup file.
- **Date and Time Formatting**: Recognizes and formats specific date and time markers in the input, converting them into human-readable forms.
- **Vertical Whitespace Cleaning**: Removes unwanted vertical whitespace characters.
- **Customizable Input**: The program supports various orders for columns in the airport lookup CSV file.

## Usage

## Cloning the repository and building the applicaton

- git clone https://gitea.kood.tech/jurgenandessalu/itinerary.git
- javac Prettifier.java

## Command to display usage, after cloning the repository

- $ java Prettifier.java -h

## Command to Run in Terminal for colored output.txt

After compiling, write your input in input.txt and then run the following commands to see the output in the terminal:
- $ java Prettifier ./input.txt ./output.txt ./airport-lookup.csv
- $ cat output.txt

## Input and Output Files
- input.txt: The file containing the unformatted itinerary data.
- output.txt: The file where the formatted output will be saved.
- airport-lookup.csv: A CSV file mapping airport codes to city and airport names.

## Input Format
The input file (input.txt) should contain raw itinerary data with special markers, such as:

- Airport codes (ICAO or IATA) prefixed by * for city names.
- Date markers like D() for dates, T12() for 12-hour time, and T24() for 24-hour time.
- Vertical whitespace characters (\v, \f, \r) that the program will replace with proper newlines.


## Airport Lookup CSV
The airport-lookup.csv should have six columns:
- iso_country
- municipality
- coordinates
- icao_code (e.g., EGLL)
- iata_code (e.g., LHR)
- name (e.g., London Heathrow Airport, London)
## Example CSV
- csv
- icao_code,iata_code,name
- EGLL,LHR,London Heathrow Airport, London
- KLAX,LAX,Los Angeles International Airport, Los Angeles

## Output Format
The program will generate a formatted output.txt file, replacing codes with city or airport names and converting date/time markers into readable formats.

## Example Input
Flight Itinerary

Departure: ##EGLL, D(2024-09-29T14:00+01:00) T12(2024-09-29T14:00+01:00)
Arrival: ##EGKK, D(2024-09-29T15:30+01:00) T12(2024-09-29T15:30+01:00)

Flight: *#LHR to *#LGW\f

Important: Please arrive at least 2 hours before your flight.

Some text with vertical whitespace\r
\v
More text
\f
Another line
\r
Yet another line

End of the itinerary



## Example Output
Flight Itinerary

Departure: London Heathrow Airport, 29-Sep-2024 02:00PM (+01:00)
Arrival: London Gatwick Airport, 29-Sep-2024 03:30PM (+01:00)

Flight: London Heathrow Airport to London Gatwick Airport


Important: Please arrive at least 2 hours before your flight.

Some text with vertical whitespace


More text

Another line

Yet another line

