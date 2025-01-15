import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Euro2024App {

    static class Match {
        private int id;
        private String team1;
        private String team2;
        private String date;
        private String location;
        private int capacity;

        public Match(int id, String team1, String team2, String date, String location, int capacity) {
            this.id = id;
            this.team1 = team1;
            this.team2 = team2;
            this.date = date;
            this.location = location;
            this.capacity = capacity;
        }

        public String getLocation() {
            return location;
        }

        public String getDate() {
            return date;
        }

        public int getCapacity() {
            return capacity;
        }

        @Override
        public String toString() {
            return team1 + " vs " + team2 + " - Date: " + date + " - Location: " + location;
        }
    }

    static class MatchManager {
        private List<Match> matches;

        public MatchManager(String filePath) throws IOException {
            this.matches = readMatchesFromTSV(filePath);
        }

        private List<Match> readMatchesFromTSV(String filePath) throws IOException {
            List<Match> matches = new ArrayList<>();
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] fields = lines.get(i).split("\t");
                matches.add(new Match(
                        Integer.parseInt(fields[0]),
                        fields[1],
                        fields[2],
                        fields[3],
                        fields[4],
                        Integer.parseInt(fields[5])
                ));
            }
            return matches;
        }

        public void filterMatchesByCapacity(int minCapacity) {
            matches.stream()
                    .filter(match -> match.getCapacity() >= minCapacity)
                    .forEach(System.out::println);
        }

        public void getMatchesInLocationAfterDate(String location, String date) {
            matches.stream()
                    .filter(match -> match.getLocation().equals(location) && match.getDate().compareTo(date) > 0)
                    .sorted(Comparator.comparing(Match::getDate))
                    .forEach(System.out::println);
        }

        public void countMatchesPerLocation(String outputFilePath) throws IOException {
            Map<String, Long> locationCounts = matches.stream()
                    .collect(Collectors.groupingBy(Match::getLocation, Collectors.counting()));

            List<Map.Entry<String, Long>> sortedLocations = locationCounts.entrySet().stream()
                    .sorted((e1, e2) -> {
                        int cmp = e2.getValue().compareTo(e1.getValue());
                        return cmp != 0 ? cmp : e1.getKey().compareTo(e2.getKey());
                    })
                    .collect(Collectors.toList());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (Map.Entry<String, Long> entry : sortedLocations) {
                    writer.write(entry.getKey() + "%" + entry.getValue() + "\n");
                }
            }
        }
    }

    public static void main(String[] args) {
        String inputFilePath = "spielorte.tsv";
        String outputFilePath = "spielanzahl.txt";

        try {
            MatchManager manager = new MatchManager(inputFilePath);

            System.out.println("1. Filtered Matches (Capacity >= 70000):");
            manager.filterMatchesByCapacity(70000);

            System.out.println("\n2. Matches in Munich (After 2024-06-30):");
            manager.getMatchesInLocationAfterDate("München", "2024-06-30");

            System.out.println("\n3. Writing match counts per location to file...");
            manager.countMatchesPerLocation(outputFilePath);
            System.out.println("Match counts saved to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}