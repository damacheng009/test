import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSVAnalyzer {

    public static void main(String[] args) {
        String rootFolderPath = "path/to/IMO_msg_stat";
        try {
            analyzeCSVFiles(rootFolderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void analyzeCSVFiles(String rootFolderPath) throws IOException {
        File rootFolder = new File(rootFolderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            System.err.println("Invalid root folder path.");
            return;
        }

        File[] subFolders = rootFolder.listFiles(File::isDirectory);
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                analyzeSubFolder(subFolder);
            }
        }
    }

    private static void analyzeSubFolder(File subFolder) throws IOException {
        File[] csvFiles = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles != null) {
            Map<String, Map<String, Integer>> dateIdentifierCountMap = new HashMap<>();

            for (File csvFile : csvFiles) {
                processCSVFile(csvFile, dateIdentifierCountMap);
            }

            // Output the result
            for (Map.Entry<String, Map<String, Integer>> entry : dateIdentifierCountMap.entrySet()) {
                String valueDate = entry.getKey();
                Map<String, Integer> identifierCountMap = entry.getValue();

                System.out.println("Date: " + valueDate);
                for (Map.Entry<String, Integer> identifierCountEntry : identifierCountMap.entrySet()) {
                    String identifier = identifierCountEntry.getKey();
                    int count = identifierCountEntry.getValue();
                    System.out.println("  Identifier: " + identifier + ", Count: " + count);
                }
                System.out.println();
            }
        }
    }

    private static void processCSVFile(File csvFile, Map<String, Map<String, Integer>> dateIdentifierCountMap)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip the first line (metadata)
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 8) {
                    String ioField = fields[0].trim();
                    String identifierField = fields[2].trim();
                    String valueDateField = fields[7].trim();

                    if (isValidIdentifier(identifierField) && isValidValueDate(valueDateField)) {
                        dateIdentifierCountMap
                                .computeIfAbsent(valueDateField, k -> new HashMap<>())
                                .merge(identifierField, 1, Integer::sum);
                    }
                }
            }
        }
    }

    private static boolean isValidIdentifier(String identifier) {
        return identifier.matches("fin\\.\\d{3}");
    }

    private static boolean isValidValueDate(String valueDate) {
        return valueDate.matches("\\d{4}/\\d{2}/\\d{2}");
    }
}
