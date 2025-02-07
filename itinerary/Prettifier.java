package itinerary;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Prettifier {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("itinerary usage:");
            System.out.println("$ java Prettifier.java ./input.txt ./output.txt ./airport-lookup.csv");
            return;
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];
        String airportLookupPath = args[2];

        if (!Files.exists(Paths.get(inputFilePath))) {
            System.out.println("Input not found");
            return;
        }

        if (!Files.exists(Paths.get(airportLookupPath))) {
            System.out.println("Airport lookup not found");
            return;
        }

        try {
            // Parse the airport lookup CSV
            Map<String, String> airportLookup = parseAirportLookup(airportLookupPath);
            if (airportLookup == null) {
                System.out.println("Airport lookup malformed");
                return;
            }

            // Read input lines
            List<String> inputLines = Files.readAllLines(Paths.get(inputFilePath));
            List<String> outputLines = new ArrayList<>();

            // Process each line
            for (String line : inputLines) {
                line = processLine(line, airportLookup);
                outputLines.add(line);
            }

            // Trim extra blank lines
            outputLines = trimBlankLines(outputLines);

            // Write to output file only if no errors occurred
            Files.write(Paths.get(outputFilePath), outputLines);

            // Optional: Print the output to console with formatting
            for (String line : outputLines) {
                System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static Map<String, String> parseAirportLookup(String filePath) throws IOException {
        Map<String, String> airportMap = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
    
        if (lines.size() < 2) {
            return null; // Malformed
        }
    
        String[] headers = lines.get(0).split(",");
        int icaoIndex = -1, iataIndex = -1, cityIndex = -1, nameIndex = -1;
    
        // Find indexes
        for (int i = 0; i < headers.length; i++) {
            switch (headers[i].trim().toLowerCase()) {
                case "icao_code":
                    icaoIndex = i;
                    break;
                case "iata_code":
                    iataIndex = i;
                    break;
                case "municipality":
                    cityIndex = i;
                    break;
                case "name":
                    nameIndex = i;
                    break;
            }
        }
    
        if (icaoIndex == -1 || iataIndex == -1 || cityIndex == -1 || nameIndex == -1) {
            return null; // Malformed
        }
    
        // Parse each line 
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> columns = parseCSVLine(line);
    
            if (columns.size() <= Math.max(Math.max(Math.max(icaoIndex, iataIndex), cityIndex), nameIndex)) {
                return null; // Malformed line
            }
    
            String icao = columns.get(icaoIndex).trim();
            String iata = columns.get(iataIndex).trim();
            String city = columns.get(cityIndex).trim();
            String name = columns.get(nameIndex).trim();
    
            // Ensure no empty values
            if (icao.isEmpty() || iata.isEmpty() || city.isEmpty() || name.isEmpty()) {
                return null; // Malformed
            }
            airportMap.put("##" + icao, name); // Store ICAO code with airport name
            airportMap.put("#" + iata, name);  // Store IATA code with airport name
            airportMap.put("*##" + icao, city); // Store ICAO code with city
            airportMap.put("*#" + iata, city);  // Store IATA code with city
        }
    
        return airportMap; // Return populated map
    }
    
    // Helper method to parse CSV lines considering quoted commas
    private static List<String> parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // This regex handles commas inside quotes
    
        while (scanner.hasNext()) {
            String value = scanner.next().trim();
            // Only remove quotes if both start and end with double quotes
            if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1); // Remove quotes
            }
            values.add(value);
        }
    
        scanner.close();
        return values;
    }

    private static String processLine(String line, Map<String, String> airportLookup) {
        line = replaceVerticalWhitespace(line);
        line = replaceAirportCodes(line, airportLookup);
        line = formatDateTimes(line);
        return line; 
    }

    private static String replaceVerticalWhitespace(String line) {
        return line.replaceAll("\\\\[vfr]+", "\n");
    }

    private static String replaceAirportCodes(String line, Map<String, String> airportLookup) {
        line = replaceCodes(line, airportLookup, "*##", 4, true); // City from ICAO
        line = replaceCodes(line, airportLookup, "*#", 3, true);  // City from IATA
        line = replaceCodes(line, airportLookup, "##", 4, false); // Airport from ICAO
        line = replaceCodes(line, airportLookup, "#", 3, false);  // Airport from IATA
        return line; 
    }

    private static String replaceCodes(String line, Map<String, String> airportLookup, String prefix, int codeLength, boolean city) {
        int index = line.indexOf(prefix);
        while (index != -1) {
            int codeStart = index + prefix.length();
            if (codeStart + codeLength <= line.length()) {
                String code = line.substring(codeStart, codeStart + codeLength);
                String fullCode = prefix + code;
                String replacement = airportLookup.getOrDefault(fullCode, fullCode);
                if (!city) {
                    replacement = ANSI_YELLOW + replacement + ANSI_RESET; // Highlight airport names
                }
                line = line.substring(0, index) + replacement + line.substring(codeStart + codeLength);
            }
            index = line.indexOf(prefix, index + 1);
        }
        return line;
    }
    private static String formatDateTimes(String line) {
        line = replaceDateTime(line, "D(", ")", "dd-MMM-yyyy"); // Date format
        line = replaceDateTime(line, "T12(", ")", "hh:mma z");  // 12-hour time
        line = replaceDateTime(line, "T24(", ")", "HH:mm z");   // 24-hour time
        return line; 
    }

    private static String replaceDateTime(String line, String startMarker, String endMarker, String dateFormat) {
        int startIndex = line.indexOf(startMarker);

        while (startIndex != -1) {
            int endIndex = line.indexOf(endMarker, startIndex);
            if (endIndex != -1) {
                String dateTimeStr = line.substring(startIndex + startMarker.length(), endIndex);
                try {
                    ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr);
                    String formattedDateTime = "";
                    String offset = dateTime.getOffset().toString();

                    if (startMarker.equals("D(")) {
                        formattedDateTime = ANSI_GREEN + dateTime.format(DateTimeFormatter.ofPattern(dateFormat)) + ANSI_RESET;
                    } else {
                        if (startMarker.equals("T12(")) {
                            formattedDateTime = ANSI_BLUE + dateTime.format(DateTimeFormatter.ofPattern("hh:mma")) + ANSI_RESET;
                        } else if (startMarker.equals("T24(")) {
                            formattedDateTime = ANSI_BLUE + dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) + ANSI_RESET;
                        }

                        if (offset.equals("Z")) {
                            offset = "(+00:00)";
                        } else {
                            offset = "(" + offset + ")";
                        }
                        formattedDateTime += " " + offset;
                    }

                    line = line.substring(0, startIndex) + formattedDateTime + line.substring(endIndex + endMarker.length());
                } catch (DateTimeParseException e) {
                    // Leave as is if parsing fails
                }
            }
            startIndex = line.indexOf(startMarker, startIndex + startMarker.length());
        }
        return line; 
    }

    private static List<String> trimBlankLines(List<String> lines) {
        List<String> trimmedLines = new ArrayList<>();
        boolean lastLineBlank = false;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                trimmedLines.add(line);
                lastLineBlank = false;
            } else if (!lastLineBlank) {
                trimmedLines.add(line);
                lastLineBlank = true;
            }
        }
        return trimmedLines; 
    }
}