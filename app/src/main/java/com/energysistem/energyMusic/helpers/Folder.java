package com.energysistem.energyMusic.helpers;

/**
 * Created by Vicente on 20/09/2014.
 */
public class Folder implements Comparable<Folder> {

    String path;
    String name;
    int numberOfSongs;

    public Folder(String path) {
        this.path = path;
        this.name = path.substring(path.lastIndexOf("/")+1);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    @Override
    public int compareTo(Folder other) {
        return getPath().compareTo(other.getPath());
    }
}
