package com.example.filestats;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileStatsCollector implements Runnable {
    private Path filePath;
    private List<String> includeExtensions;
    private List<String> excludeExtensions;
    private boolean gitIgnore;
    private ConcurrentHashMap<String, Integer> fileCounts;
    private ConcurrentHashMap<String, Long> fileSizes;
    private ConcurrentHashMap<String, Integer> totalLines;
    private ConcurrentHashMap<String, Integer> nonEmptyLines;
    private ConcurrentHashMap<String, Integer> commentLines;

    public FileStatsCollector(Path filePath, List<String> includeExtensions, List<String> excludeExtensions, boolean gitIgnore,
                              ConcurrentHashMap<String, Integer> fileCounts, ConcurrentHashMap<String, Long> fileSizes,
                              ConcurrentHashMap<String, Integer> totalLines, ConcurrentHashMap<String, Integer> nonEmptyLines,
                              ConcurrentHashMap<String, Integer> commentLines) {
        this.filePath = filePath;
        this.includeExtensions = includeExtensions;
        this.excludeExtensions = excludeExtensions;
        this.gitIgnore = gitIgnore;
        this.fileCounts = fileCounts;
        this.fileSizes = fileSizes;
        this.totalLines = totalLines;
        this.nonEmptyLines = nonEmptyLines;
        this.commentLines = commentLines;
    }

    @Override
    public void run() {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return; // No extension
        }

        String extension = fileName.substring(dotIndex + 1);

        if ((includeExtensions != null && !includeExtensions.contains(extension)) ||
                (excludeExtensions != null && excludeExtensions.contains(extension))) {
            return;
        }

        try {
            long fileSize = Files.size(filePath);

            // Update the count and size for this extension in a thread-safe way
            fileCounts.merge(extension, 1, Integer::sum);
            fileSizes.merge(extension, fileSize, Long::sum);

            // Try to read the lines of the file
            try {
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

                // Count total lines, non-empty lines, and comment lines
                int lineCount = lines.size();
                int nonEmptyLineCount = 0;
                int commentLineCount = 0;

                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        nonEmptyLineCount++;
                        if (isCommentLine(line, extension)) {
                            commentLineCount++;
                        }
                    }
                }

                totalLines.merge(extension, lineCount, Integer::sum);
                nonEmptyLines.merge(extension, nonEmptyLineCount, Integer::sum);
                commentLines.merge(extension, commentLineCount, Integer::sum);
            } catch (Exception e) {
                // If reading lines fails, skip line-related statistics
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isCommentLine(String line, String extension) {
        line = line.trim();
        switch (extension) {
            case "java":
                return line.startsWith("//");
            case "sh":
                return line.startsWith("#");
            default:
                return false;
        }
    }
}
