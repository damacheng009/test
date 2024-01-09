import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSVAnalyzer {

    public static void main(String[] args) {
        String rootFolderPath = "C:/Users/45247935/Desktop/IMO_msg_stat";
        String outputCsvPath = "C:/Users/45247935/Desktop/IMO_msg_stat.csv";

        try {
            analyzeCSVFiles(rootFolderPath, outputCsvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void analyzeCSVFiles(String rootFolderPath, String outputCsvPath) throws IOException {
        File rootFolder = new File(rootFolderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            System.err.println("Invalid root folder path.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputCsvPath))) {
            writer.println("msg_create_date,msg_type,amount");

            File[] subFolders = rootFolder.listFiles(File::isDirectory);
            if (subFolders != null) {
                for (File subFolder : subFolders) {
                    analyzeSubFolder(subFolder, writer);
                }
            }
        }
    }

    private static void analyzeSubFolder(File subFolder, PrintWriter writer) throws IOException {
        String folderName = subFolder.getName();
        String monthAbbreviation = folderName.substring(0, 3).toLowerCase();
        String monthNumber = folderName.substring(3);

        Map<String, Integer> identifierCountMap = new HashMap<>();

        File[] csvFiles = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles != null) {
            for (File csvFile : csvFiles) {
                processCSVFile(csvFile, identifierCountMap);
            }
        }

        // Output the result
        for (Map.Entry<String, Integer> entry : identifierCountMap.entrySet()) {
            String identifier = entry.getKey();
            int count = entry.getValue();
            writer.println(formatOutputLine(monthAbbreviation, monthNumber, identifier, count));
        }
    }

    private static void processCSVFile(File csvFile, Map<String, Integer> identifierCountMap) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            // Skip the first line (metadata)
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    String identifierField = fields[2].trim();

                    if (isValidIdentifier(identifierField)) {
                        identifierCountMap.merge(identifierField, 1, Integer::sum);
                    }
                }
            }
        }
    }

    private static boolean isValidIdentifier(String identifier) {
        return identifier.matches("fin\\.\\d{3}");
    }

    private static String formatOutputLine(String monthAbbreviation, String monthNumber, String identifier, int count) {
        return "2023/" + monthAbbreviation + "/" + String.format("%02d", Integer.parseInt(monthNumber))
                + "," + identifier + "," + count;
    }
}
