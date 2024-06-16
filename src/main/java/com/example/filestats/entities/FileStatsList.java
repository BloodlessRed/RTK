package com.example.filestats.entities;


import org.simpleframework.xml.ElementList;

public class FileStatsList {
    @ElementList(inline = true)
    public java.util.List<FileStats> fileStats;

    public FileStatsList(java.util.List<FileStats> fileStats) {
        this.fileStats = fileStats;
    }
}