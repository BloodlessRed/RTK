package com.example.filestats.entities;

public class FileStats {
    public String extension;

    public int count;

    public long totalSize;

    public int totalLines;

    public int nonEmptyLines;

    public int commentLines;

    public FileStats(String extension, int count, long totalSize, int totalLines, int nonEmptyLines, int commentLines) {
        this.extension = extension;
        this.count = count;
        this.totalSize = totalSize;
        this.totalLines = totalLines;
        this.nonEmptyLines = nonEmptyLines;
        this.commentLines = commentLines;
    }
}