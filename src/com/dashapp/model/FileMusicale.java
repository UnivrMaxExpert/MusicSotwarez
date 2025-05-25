package com.dashapp.model;

import java.io.Serializable;

public class FileMusicale implements Serializable
{
    private FileType fileType;
    private String filePath;

    public FileMusicale(FileType fileType, String filePath) {
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }
}

enum FileType {
    MP4,
    MP3,
    PDF,
    JPG,
    JPEG,
    MIDI,
    WAV
}