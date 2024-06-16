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

        // Prompt user for input
        System.out.print("Enter the directory path: ");
        String directoryPath = scanner.nextLine();

        System.out.print("Do you want to search recursively? (yes/no): ");
        boolean recursive = scanner.nextLine().equalsIgnoreCase("yes");

        int maxDepth = Integer.MAX_VALUE;
        if (recursive) {
            System.out.print("Enter the max depth for recursive search: ");
            maxDepth = Integer.parseInt(scanner.nextLine());
        }

        System.out.print("Enter the number of threads to use: ");
        int threadCount = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter file extensions to include (comma separated, or leave blank for all): ");
        String includeExtInput = scanner.nextLine();
        List<String> includeExtensions = includeExtInput.isEmpty() ? null : Arrays.asList(includeExtInput.split(","));

        List<String> excludeExtensions = null;
        AtomicReference<List<String>> excludeExtensionsReference = new AtomicReference<>();
        if (includeExtensions == null){
            System.out.print("Enter file extensions to exclude (comma separated, or leave blank for none): ");
            String excludeExtInput = scanner.nextLine();
            excludeExtensions = excludeExtInput.isEmpty() ? null : Arrays.asList(excludeExtInput.split(","));
        }
        excludeExtensionsReference.set(excludeExtensions);

        System.out.print("Do you want to respect .gitignore files? (yes/no): ");
        boolean gitIgnore = scanner.nextLine().equalsIgnoreCase("yes");

        System.out.print("Enter the output format (plain/xml/json): ");
        String outputFormat = scanner.nextLine();

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
}
