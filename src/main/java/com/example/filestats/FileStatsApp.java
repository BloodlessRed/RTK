package com.example.filestats;

import com.example.filestats.utils.FileStatsCollector;
import com.example.filestats.utils.GitIgnoreParser;
import com.example.filestats.utils.OutputFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class FileStatsApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Mandatory parameter: directory path
        String directoryPath;
        while (true) {
            System.out.print("Enter the directory path: ");
            directoryPath = scanner.nextLine().trim();
            if (!directoryPath.isEmpty()) {
                Path path = Paths.get(directoryPath);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    break;
                } else {
                    System.out.println("Invalid directory path. Please enter a valid path.");
                }
            } else {
                System.out.println("Directory path cannot be empty. Please enter a valid path.");
            }
        }

        // Optional parameters with default values
        boolean recursive = getBooleanInput(scanner, "Do you want to search recursively? (yes/no): ", false);
        int maxDepth = recursive ? getIntInput(scanner, "Enter the max depth for recursive search (default is Integer.MAX_VALUE): ", Integer.MAX_VALUE) : 1;
        int threadCount = getIntInput(scanner, "Enter the number of threads to use (default is 1): ", 1);

        List<String> includeExtensions = getListInput(scanner, "Enter file extensions to include (comma separated, or leave blank for all): ");
        List<String> excludeExtensions = null;
        AtomicReference<List<String>> excludeExtensionsReference = new AtomicReference<>();
        if (includeExtensions == null || includeExtensions.isEmpty()) {
            excludeExtensions = getListInput(scanner, "Enter file extensions to exclude (comma separated, or leave blank for none): ");
        }
        excludeExtensionsReference.set(excludeExtensions);

        boolean gitIgnore = getBooleanInput(scanner, "Do you want to respect .gitignore files? (yes/no): ", false);

        String outputFormat = getStringInput(scanner, "Enter the output format (plain/xml/json, default is plain): ", "plain", Arrays.asList("plain", "xml", "json"));

        Path startPath = Paths.get(directoryPath);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Use concurrent maps to store file counts, total sizes, total lines, non-empty lines, and comment lines by extension
        ConcurrentHashMap<String, Integer> fileCounts = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> fileSizes = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> totalLines = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> nonEmptyLines = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> commentLines = new ConcurrentHashMap<>();

        GitIgnoreParser gitIgnoreParser = null;
        if (gitIgnore) {
            try {
                Path gitIgnoreFilePath = startPath.resolve(".gitignore");
                if (Files.exists(gitIgnoreFilePath)) {
                    gitIgnoreParser = new GitIgnoreParser(gitIgnoreFilePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final GitIgnoreParser finalGitIgnoreParser = gitIgnoreParser;

        try (Stream<Path> paths = recursive ?
                Files.walk(startPath, maxDepth) :
                Files.list(startPath)) {

            paths.filter(Files::isRegularFile)
                    .filter(path -> finalGitIgnoreParser == null || !finalGitIgnoreParser.isIgnored(startPath.relativize(path)))
                    .forEach(path -> executorService.submit(new FileStatsCollector(path, includeExtensions, excludeExtensionsReference.get(), gitIgnore, fileCounts, fileSizes, totalLines, nonEmptyLines, commentLines)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Wait for all tasks to finish
        }

        // Output results based on the specified format
        try {
            String result;
            switch (outputFormat.toLowerCase()) {
                case "xml":
                    result = OutputFormatter.formatXml(fileCounts, fileSizes, totalLines, nonEmptyLines, commentLines);
                    break;
                case "json":
                    result = OutputFormatter.formatJson(fileCounts, fileSizes, totalLines, nonEmptyLines, commentLines);
                    break;
                case "plain":
                default:
                    result = OutputFormatter.formatPlain(fileCounts, fileSizes, totalLines, nonEmptyLines, commentLines);
                    break;
            }
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean getBooleanInput(Scanner scanner, String prompt, boolean defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.isEmpty()) {
                return defaultValue;
            }
            switch (input) {
                case "yes":
                case "y":
                    return true;
                case "no":
                case "n":
                    return false;
                default:
                    System.out.println("Invalid input. Please enter yes or no.");
            }
        }
    }

    private static int getIntInput(Scanner scanner, String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid integer.");
            }
        }
    }

    private static List<String> getListInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        return Arrays.asList(input.split(","));
    }

    private static String getStringInput(Scanner scanner, String prompt, String defaultValue, List<String> validOptions) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.isEmpty()) {
                return defaultValue;
            }
            if (validOptions.contains(input)) {
                return input;
            } else {
                System.out.println("Invalid input. Please enter one of the following options: " + validOptions);
            }
        }
    }
}
