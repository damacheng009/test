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
            writer.println("msg_create_date,msg_type,IO,amount,sender,receiver");

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
        String year = folderName.startsWith("Jan") ? "2024" : "2023";
        String monthAbbreviation = folderName.substring(0, 3).toLowerCase();
        String monthNumber = String.format("%02d", getMonthNumber(monthAbbreviation));
        String dayOfMonth = folderName.substring(3);

        Map<String, Map<String, Map<String, Integer>>> identifierCountMap = new HashMap<>();

        File[] csvFiles = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles != null) {
            for (File csvFile : csvFiles) {
                processCSVFile(csvFile, identifierCountMap);
            }
        }

        // Output the result
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : identifierCountMap.entrySet()) {
            String identifier = entry.getKey();
            Map<String, Map<String, Integer>> ioCountMap = entry.getValue();

            for (Map.Entry<String, Map<String, Integer>> ioEntry : ioCountMap.entrySet()) {
                String io = ioEntry.getKey();
                Map<String, Integer> senderReceiverCountMap = ioEntry.getValue();

                for (Map.Entry<String, Integer> senderReceiverEntry : senderReceiverCountMap.entrySet()) {
                    String senderReceiver = senderReceiverEntry.getKey();
                    int count = senderReceiverEntry.getValue();
                    String[] senderReceiverParts = senderReceiver.split(" ");
                    String sender = senderReceiverParts[0].substring(0, senderReceiverParts[0].length() - 3);
                    String receiver = senderReceiverParts[1].substring(0, senderReceiverParts[1].length() - 3);
                    writer.println(formatOutputLine(year, monthNumber, dayOfMonth, identifier, io, sender, receiver, count));
                }
            }
        }
    }

    private static void processCSVFile(File csvFile, Map<String, Map<String, Map<String, Integer>>> identifierCountMap) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            // Skip the first line (metadata)
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    String ioField = fields[0].trim();
                    String identifierField = fields[2].trim();
                    String senderReceiverField = fields[7].trim();

                    if (isValidIdentifier(identifierField)) {
                        identifierCountMap
                                .computeIfAbsent(identifierField, k -> new HashMap<>())
                                .computeIfAbsent(ioField, k -> new HashMap<>())
                                .merge(senderReceiverField, 1, Integer::sum);
                    }
                }
            }
        }
    }

    private static boolean isValidIdentifier(String identifier) {
        return identifier.matches("fin\\.\\d{3}");
    }

    private static String formatOutputLine(String year, String monthNumber, String dayOfMonth, String identifier, String io, String sender, String receiver, int count) {
        return year + "/" + monthNumber + "/" + dayOfMonth + "," + identifier + "," + io + "," + count + "," + sender + "," + receiver;
    }

    private static int getMonthNumber(String monthAbbreviation) {
        switch (monthAbbreviation) {
            case "dec":
                return 12;
            case "jan":
                return 1;
            // Add more cases for other months if needed
            default:
                return 0;
        }
    }
}
