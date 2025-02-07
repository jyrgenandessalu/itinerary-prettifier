import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//----------------------------------------------------------------------------------
public class Prettifier {

    public static final String RESET = "\033[0m";  // Text Reset
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN BOLD
    public static final String RED_BOLD = "\033[1;31m";    // RED BOLD for errors
    public static final String L_C = "\033[1;36m"; // Bright CYAN
    public static final String LIGHT_CYAN_ITALIC = "\033[3;36m"; // Italic cyan, might not be supported for some terminals (command prompt windows)
    public static final String DARK_GREEN = "\033[0;32m"; // darker green, not bold
//----------------MAIN FUNCTION + ARGS CONDITIONS FOR RUNNING PROGRAM
    public static void main(String[] args) throws IOException {

        if (args.length > 0 && (args[0].equals("-h")|| args[0].equals("-help")) ) {
            System.err.println(GREEN_BOLD+"==================================================================="+RESET);
            displayGuide(); // guide on $ java main -h or -help
            System.err.println(GREEN_BOLD+"==================================================================="+RESET);
            System.exit(0);  
        }else if(args.length != 3) {
            displayGuide();
            System.exit(0);
        }

        String inputFile = args[0];
        String outputFile = args[1];
        String csvFile = args[2];

        if(!fileExists(inputFile)) {
            System.err.println(RED_BOLD+"==================================================================="+RESET);
            throw new IOException(RED_BOLD + "Input file not found: " + inputFile + RESET);    
        }
        if(!fileExists(csvFile)) {
            System.err.println(RED_BOLD+"==================================================================="+RESET);
            throw new IOException(RED_BOLD + "Airport lookup not found: " + csvFile + RESET);
        }
        File outputFileObj = new File(outputFile);
        if (!outputFileObj.exists()) {
            boolean created = outputFileObj.createNewFile();  //creates new output if file didn't exist.
            System.out.println();//empty line for visual clarity
            System.out.println(RED_BOLD+outputFile + RESET + " does not exist in current folder, creating new file: " +GREEN_BOLD+ outputFile+RESET );
            
            if (!created) {
                System.err.println(RED_BOLD+"==================================================================="+RESET);
                throw new IOException(RED_BOLD+"Could not create output file: " + outputFile+RESET);
            }
        }
//----------------MAPS
        Map<String, Integer> headerMap = new HashMap<>();
        Map<String, String> icaoToAirportMap = new HashMap<>();
        Map<String, String> iataToAirportMap = new HashMap<>();
        Map<String, String> icaoToCityMap = new HashMap<>();
        Map<String, String> iataToCityMap = new HashMap<>();
//----------------CSV READER
        // Read the CSV file to build the maps
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException(RED_BOLD+ "Airport lookup malformed: header line is missing or null" + RESET);
            }
            

            String[] headers = headerLine.split(",");
            if (headers.length != 6) {
                throw new IOException(RED_BOLD+ "Airport lookup malformed: The header line is missing elements. Expected 6 but found " +headers.length + RESET);

            }
            for (String header : headers) {
                    if (header.trim().isEmpty()) {
                        throw new IOException(RED_BOLD+ "Airport lookup malformed: The header is missing or null." + RESET);
                    }
                }

            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }
           
            String line;
            while ((line = br.readLine()) != null) {
               
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length != headers.length) {
                    throw new IOException(RED_BOLD+ "Airport lookup malformed. A line is missing elements. Expected 6 but found " + parts.length + ". Line content:\n" + L_C + line + RESET);
                
                }
                for (String part : parts) {
                    if (part.trim().isEmpty()) {
                        throw new IOException(RED_BOLD+ "Airport lookup malformed: line has has null element\n" + RESET+L_C+line+RESET);
                    }
                }
                if (parts.length == 6) {
                    String name = parts[headerMap.get("name")].trim();
                    String icaoCode = parts[headerMap.get("icao_code")].trim();
                    String iataCode = parts[headerMap.get("iata_code")].trim();
                    String cityName = parts[headerMap.get("municipality")].trim();

                    // Map ICAO and IATA codes to names
                    icaoToAirportMap.put("##" + icaoCode, name);
                    iataToAirportMap.put("#" + iataCode, name);
                    // For city names
                    icaoToCityMap.put("*##" + icaoCode, cityName);
                    iataToCityMap.put("*#" + iataCode, cityName);
                } else {
                    throw new IOException(RED_BOLD+ "Airport lookup malformed. A line is missing elements. Expected 6 but found " + parts.length + ". Line content: " + L_C + line + RESET);
                }
            }
        } catch (IOException e) {
            System.err.println(RED_BOLD+"==================================================================="+RESET);
            System.err.println(RED_BOLD+"Error reading CSV file: " +RESET+ e.getMessage());
            throw new IOException();
        }

        logOldOutput(outputFile); //stores last output in logs before overwriting it

//---------------- INPUT READER AND OUTPUT WRITER
        // Read input and write output
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            boolean prevLineWasEmpty = false;
            // patterns for coloring times and dates
            Pattern datePattern = Pattern.compile("\\b\\d{2}(?=\\s+\\w{3})");  // 09 in 09 May
            Pattern yearPattern = Pattern.compile("\\b\\d{4}\\b");  // year part (e.g., 2022)
            Pattern timePattern = Pattern.compile("\\b\\d{2}:\\d{2}(?:[AP]M)?\\b|\\([+-]\\d{2}:\\d{2}\\)");  // time (with AM/PM) and timezones
            Pattern monthPattern = Pattern.compile("\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\b"); // months

            System.out.println(); //for empty line before output in terminal
            System.err.println(GREEN_BOLD+"==================================================================="+RESET);
            System.err.println(GREEN_BOLD+"=================== "+L_C+"DISPLAYING OUTPUT"+GREEN_BOLD+" ============================="+RESET);
            writer.write("--- PROCESSED ENTRY: " + LocalDateTime.now() + " ---\n\n");
            while ((line = reader.readLine()) != null) {
 
                //System.out.println("Original line: " + line); // Debugg
                
                // swap codes with names or municipalities
                for (String code : icaoToCityMap.keySet()) {
                    line = line.replace(code, icaoToCityMap.get(code));
                }
                for (String code : iataToCityMap.keySet()) {
                    line = line.replace(code, iataToCityMap.get(code));
                }
                for (String code : icaoToAirportMap.keySet()) {
                    line = line.replace(code, icaoToAirportMap.get(code));
                }
                for (String code : iataToAirportMap.keySet()) {
                    line = line.replace(code, iataToAirportMap.get(code));
                }
                // converts vertical white space characters into new-line characters
                line = line.replace("\\v", "\n").replace("\\f", "\n").replace("\\r", "\n").replace("\\n", "\n");
                line = convert(line);
                line = convert(line).trim(); 
                // apply colors
                String coloredLine = applyColor(line, datePattern, DARK_GREEN);
                coloredLine = applyColor(coloredLine, yearPattern, DARK_GREEN);
                coloredLine = applyColor(coloredLine, timePattern, DARK_GREEN);
                coloredLine = applyColor(coloredLine, monthPattern, DARK_GREEN);

                // Manage blank lines: write only if not more than one consecutive blank line
                boolean currentLineIsEmpty = line.isEmpty();

                if (currentLineIsEmpty) {
                    if (!prevLineWasEmpty) {
                        writer.newLine();
                        prevLineWasEmpty = true; // flag for the next iteration
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                    prevLineWasEmpty = false; 
                }
                if (!line.isEmpty()) {
                System.out.println(GREEN_BOLD+" ---> "+RESET + LIGHT_CYAN_ITALIC + coloredLine ); // Debugging print
                }
            }
            System.err.println(GREEN_BOLD+"=================== "+L_C+"FILE PROCESSED"+GREEN_BOLD+" ================================"+RESET);
            System.err.println(GREEN_BOLD+"==================================================================="+RESET);
        } catch (IOException e) {
            System.err.println(RED_BOLD+"==================================================================="+RESET);
            System.err.println(RED_BOLD + "Error during processing: " + RESET + e.getMessage());
        }
    }
//----------------------------------------------------------------------------------
//---------------- TIME CONVERTER
    public static String convert(String input) {
        // regex to match the date/time format within a string
        Pattern pattern = Pattern.compile("(D|T(12|24))\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String type = matcher.group(1);  // Group 1: "D", "T12", or "T24"
            String dateTimeStr = matcher.group(3);  // Group 3: Date/Time string inside parentheses
            String formattedDateTime;

            try {
                OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeStr);

                String offsetStr = dateTime.getOffset().toString();
                // Handle the case where Z should be displayed as (+00:00)
                if (offsetStr.equals("Z") || offsetStr.equals("+00:00")) {
                    offsetStr = "(+00:00)";
                } else {
                    offsetStr = "(" + offsetStr + ")";
                }

                switch (type) {
                    case "D":
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        formattedDateTime = dateTime.format(dateFormatter);
                        break;
                    case "T12":
                        DateTimeFormatter timeFormatter12 = DateTimeFormatter.ofPattern("hh:mma");
                        formattedDateTime = dateTime.format(timeFormatter12).toUpperCase() + " " + offsetStr;
                        break;
                    case "T24":
                        DateTimeFormatter timeFormatter24 = DateTimeFormatter.ofPattern("HH:mm");
                        formattedDateTime = dateTime.format(timeFormatter24) + " " + offsetStr;
                        break;
                    default:
                        formattedDateTime = matcher.group(0); // Return the original match if type is invalid
                }
            } catch (DateTimeParseException e) {
                formattedDateTime = matcher.group(0);
            }

            // Replace the original match with the formatted version
            matcher.appendReplacement(result, formattedDateTime);
        }
        matcher.appendTail(result);
        return result.toString();
    }
//----------------------------------------------------------------------------------
//---------------- HELPER FUNCTION FOR COLORING AND MATCHING PATTERNS
    public static String applyColor(String line, Pattern pattern, String color) {

        Matcher matcher = pattern.matcher(line);
        StringBuffer colorText = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(colorText, color+matcher.group()+LIGHT_CYAN_ITALIC);
        }
        matcher.appendTail(colorText);

        return colorText.toString();
    }
//---------------- DISPLAY GUIDE
    public static void displayGuide() {
        System.out.println("Itinerary usage: \n "+GREEN_BOLD+"---> "+L_C+"java Prettifier.java"+RESET+" <"+L_C +"input_file.txt"+RESET+"> <"+L_C+"output_file.txt"+RESET+"> <"+L_C+"airport-lookup.csv"+RESET+">");
    }
//---------------- FILE CHECK 
    public static boolean fileExists(String path) {

        File file = new File(path);
        return file.exists() && file.isFile();
    }

    public static void logOldOutput(String outputFile) throws IOException {
        File outputFileObj = new File(outputFile);
        if(outputFileObj.exists() && outputFileObj.length() > 0){
            
            try (BufferedReader oldOutputReader = new BufferedReader(new FileReader(outputFile));
                BufferedWriter oldOutputWriter = new BufferedWriter(new FileWriter("output_log.txt", true)) ){
                    oldOutputWriter.write("Old content of " + outputFile + ":\n");

                    String line;

                    while((line = oldOutputReader.readLine()) != null) {
                        oldOutputWriter.write(line + "\n");
                    }
                    oldOutputWriter.write("---END OF ENTRY---\n\n");
                }catch (IOException e) {
                    System.err.println(RED_BOLD + "Failed to log old output: " + e.getMessage() + RESET);
                }
            }    
        }
}
