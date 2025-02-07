
# File Prettifier and Airport Lookup Program

## Description 
This Java program processes an input text file containing flight itinerary details and transforms it into a more readable format, replacing certain airport codes with their respective names and cities. It also prettifies date/time formats by highlighting dates, months, and times using ANSI colors. Additionally, the program can log the previous output file's contents before overwriting it.


## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Airport lookup file](#airport-lookup-file)
- [Logs history](#logs-history)
- [Error handling](#error-handling)

## Features

- Processes airport codes into more readable format.
- Prettifies date/time formats, also highlighting them with ANSI colors.
- Logs previous output file contents to log file before overwriting it. 


## Installation

1. Ensure you have Java installed on your system. You can download and install it from the official Java website.

2. Clone the repository:

   ```bash
   git clone https://gitea.kood.tech/agolaurluik/itinerary

3. Navigate to the project directory:

   ```bash
   cd itinerary

4. Build the application:

   ```bash
   javac Prettifier.java

5. Run the application:

   ```bash
   java Prettifier <input_file.txt> <output_file.txt> <airport-lookup.csv>

6. Use help command in terminal to see instructions on how to run the application:

   ```bash
   java Prettifier -help

## Usage

To use the Java program, create an input.txt file in the itinerary directory with the flight itinerary details. Alternatively, you can place the file anywhere on your computer, but in that case, you must provide the precise path to the input file (this also applies to the other two files: the output file and the airport lookup file).

If the output file does not exist(for example, due to typos), a new one will be created in the program directory, using the file name you specify in the output section of the terminal.

Once the files are set, follow the guide above to run the application. The processed text will be written to your output file and will also appear in the terminal, color-coded with times highlighted. If reused, the old output will be overwritten and copied to the logs file. 

There is an input.txt provided for testing in program folder that you can edit.
Output files are also timestamped.


**Example**:
- Example input: Flight to ##CYSJ T12(2069-04-24T19:18-02:00) - Scheduled for departure.
- Example output: Flight to Saint John Airport 07:18PM (-02:00) - Scheduled for departure.

## Airport lookup file

The airport lookup file contains six different types of elements: name, iso_country, municipality, icao_code, iata_code, and coordinates. This program uses four of these elements: ICAO and IATA codes, municipalities, and names.

To use this program, the input file must have # in front of IATA codes and ## in front of ICAO codes. The program will then process those codes into their corresponding names. If you wish to retrieve the municipality instead of the name, add a * before ## or # (for example, *##AYKM), and it will be processed into the municipality name.

## Logs history

This program keeps a history of old output files. As mentioned earlier, previous output files are overwritten and copied to the logs. With this feature, it's possible to look up information from previous inputs if necessary. The logs also include the timestamp of each previous output.

## Error handling

When an error occurs, the terminal displays highlighted information about the error type. For example, if the airport lookup file happens to have a missing name in its column, that entire line will be printed in the terminal. This makes it significantly easier to find and fix errors wherever they happen to be

Example Error:
```
===================================================================
Error reading CSV file: Airport lookup malformed: line has has null element
,CN,Dandong,ZYDD,DDG,"124.286003, 40.0247"
```
The corrected line in the airport-lookup.csv would be:
```
Dandong Airport,CN,Dandong,ZYDD,DDG,"124.286003, 40.0247"
```