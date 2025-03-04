package com;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Playlist {
    public File playlist;
    private String name;

    public Playlist(String name){
//        this.playlist = new File("C:\\playlists:\\"+name+".txt");
        this.playlist = new File("name.txt");
    }

    public void addSong(String songId){
        try (FileWriter writer = new FileWriter(name + ".txt", true)) { // 'true' enables append mode
            writer.write(songId + "\n");
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());

    }
}
//    public Song[] getSongsFromPlaylist(String playlistName){}



    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        Playlist test = new Playlist("test");



    }
}
