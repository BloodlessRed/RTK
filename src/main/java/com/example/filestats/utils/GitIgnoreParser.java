package com.example.filestats.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GitIgnoreParser {
    private List<Pattern> ignorePatterns;

    public GitIgnoreParser(Path gitIgnoreFilePath) throws IOException {
        ignorePatterns = new ArrayList<>();
        List<String> lines = Files.readAllLines(gitIgnoreFilePath);
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                ignorePatterns.add(Pattern.compile(globToRegex(line)));
            }
        }
    }

    public boolean isIgnored(Path filePath) {
        String path = filePath.toString().replace("\\", "/");
        for (Pattern pattern : ignorePatterns) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    private String globToRegex(String glob) {
        StringBuilder regex = new StringBuilder();
        boolean escaped = false;

        for (char currentChar : glob.toCharArray()) {
            if (escaped) {
                regex.append("\\").append(currentChar);
                escaped = false;
            } else {
                switch (currentChar) {
                    case '*':
                        regex.append(".*");
                        break;
                    case '?':
                        regex.append(".");
                        break;
                    case '\\':
                        escaped = true;
                        break;
                    case '.':
                    case '$':
                    case '^':
                    case '{':
                    case '[':
                    case '(':
                    case '|':
                    case ')':
                    case '+':
                    case '#':
                        regex.append("\\").append(currentChar);
                        break;
                    case '/':
                        regex.append("/");
                        break;
                    default:
                        regex.append(currentChar);
                }
            }
        }

        if (glob.endsWith("/")) {
            regex.append(".*");
        }

        return "^" + regex.toString() + "$";
    }
}
