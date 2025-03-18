package com;

import java.io.File;

public class Playlist {
    private File playlist;
    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public File getPlaylist() {
        return playlist;
    }
    public String getName() {
        return name;
    }
    public void setPlaylist(File playlist) {
        this.playlist = playlist;
    }


}
