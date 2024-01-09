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
        String folderName = subFolder.getName();
        String dateVal = folderName.toLowerCase();  // Assuming folder name is in lowercase

        File[] csvFiles = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles != null) {
            Map<String, Map<String, Integer>> dateIdentifierCountMap = new HashMap<>();
            int totalPerDay = 0;

            for (File csvFile : csvFiles) {
                int identifierCount = processCSVFile(csvFile, dateVal, dateIdentifierCountMap);
                totalPerDay += identifierCount;
            }

            // Output the result
            System.out.println("Date: " + dateVal);
            for (Map.Entry<String, Map<String, Integer>> entry : dateIdentifierCountMap.entrySet()) {
                String currentDateVal = entry.getKey();
                Map<String, Integer> identifierCountMap = entry.getValue();

                System.out.println("  Date: " + currentDateVal);
                int totalForDate = 0;
                for (Map.Entry<String, Integer> identifierCountEntry : identifierCountMap.entrySet()) {
                    String identifier = identifierCountEntry.getKey();
                    int count = identifierCountEntry.getValue();
                    System.out.println("    Identifier: " + identifier + ", Count: " + count);
                    totalForDate += count;
                }
                System.out.println("    Total for Date: " + totalForDate);
            }
            System.out.println("Total Per Day: " + totalPerDay);
            System.out.println();
        }
    }

    private static int processCSVFile(File csvFile, String dateVal, Map<String, Map<String, Integer>> dateIdentifierCountMap)
            throws IOException {
        int totalPerFile = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip the first line (metadata)
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    String ioField = fields[0].trim();
                    String identifierField = fields[2].trim();

                    if (isValidIdentifier(identifierField)) {
                        dateIdentifierCountMap
                                .computeIfAbsent(dateVal, k -> new HashMap<>())
                                .merge(identifierField, 1, Integer::sum);
                        totalPerFile++;
                    }
                }
            }
        }

        return totalPerFile;
    }

    private static boolean isValidIdentifier(String identifier) {
        return identifier.matches("fin\\.\\d{3}");
    }
}
