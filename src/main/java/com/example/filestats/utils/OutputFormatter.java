package com.example.filestats.utils;

import com.example.filestats.entities.FileStats;
import com.example.filestats.entities.FileStatsList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutputFormatter {

    public static String formatPlain(Map<String, Integer> fileCounts, Map<String, Long> fileSizes, Map<String, Integer> totalLines,
                                     Map<String, Integer> nonEmptyLines, Map<String, Integer> commentLines) {
        StringBuilder sb = new StringBuilder();
        fileCounts.forEach((ext, count) -> {
            long size = fileSizes.getOrDefault(ext, 0L);
            int lines = totalLines.getOrDefault(ext, 0);
            int nonEmpty = nonEmptyLines.getOrDefault(ext, 0);
            int comments = commentLines.getOrDefault(ext, 0);
            sb.append(String.format("Extension: %s, Count: %d, Total Size (bytes): %d, Total Lines: %d, Non-Empty Lines: %d, Comment Lines: %d%n",
                    ext, count, size, lines, nonEmpty, comments));
        });
        return sb.toString();
    }

    public static String formatJson(Map<String, Integer> fileCounts, Map<String, Long> fileSizes, Map<String, Integer> totalLines,
                                    Map<String, Integer> nonEmptyLines, Map<String, Integer> commentLines) {
        List<FileStats> fileStatsList = new ArrayList<>();
        fileCounts.forEach((ext, count) -> {
            long size = fileSizes.getOrDefault(ext, 0L);
            int lines = totalLines.getOrDefault(ext, 0);
            int nonEmpty = nonEmptyLines.getOrDefault(ext, 0);
            int comments = commentLines.getOrDefault(ext, 0);
            fileStatsList.add(new FileStats(ext, count, size, lines, nonEmpty, comments));
        });
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(fileStatsList);
    }

    public static String formatXml(Map<String, Integer> fileCounts, Map<String, Long> fileSizes, Map<String, Integer> totalLines,
                                   Map<String, Integer> nonEmptyLines, Map<String, Integer> commentLines) throws Exception {
        List<FileStats> fileStatsList = new ArrayList<>();
        fileCounts.forEach((ext, count) -> {
            long size = fileSizes.getOrDefault(ext, 0L);
            int lines = totalLines.getOrDefault(ext, 0);
            int nonEmpty = nonEmptyLines.getOrDefault(ext, 0);
            int comments = commentLines.getOrDefault(ext, 0);
            fileStatsList.add(new FileStats(ext, count, size, lines, nonEmpty, comments));
        });
        FileStatsList statsList = new FileStatsList(fileStatsList);
        Persister persister = new Persister();
        StringWriter writer = new StringWriter();
        persister.write(statsList, writer);
        return writer.toString();
    }
}
