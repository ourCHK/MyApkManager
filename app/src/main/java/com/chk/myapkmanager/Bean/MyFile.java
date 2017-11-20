package com.chk.myapkmanager.Bean;

/**
 * Created by chk on 17-11-20.
 */

public class MyFile {
    String originalName;
    String fileName;
    boolean isFolder;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
}
