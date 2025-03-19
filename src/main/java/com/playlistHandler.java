package com;

import javax.tools.FileObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.*;

public class playlistHandler{
    public ArrayList<Playlist> playlists = new ArrayList<>();
    Gson gson = new Gson();
    public Playlist createPlaylist(String name){
        File file = new File("C:\\playlists\\" + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Playlist creation not possible " + e.getMessage());;
        }
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setPlaylist(file);
        return playlist;
    }
    public void importPlaylists(){
        File directory = new File("C:\\playlists\\");
        File[] files = directory.listFiles();
        for (File file : files){
            Playlist playlist  = new Playlist();
            playlist.setName(file.getName());
            playlist.setPlaylist(file);
            playlists.add(playlist);
        }

    }

    public void addSong(Playlist playlist, Song song){
        JsonArray jsonfile = new JsonArray();
        File file = playlist.getPlaylist();
        Path filePath = file.toPath();
        JsonObject newSong = new JsonObject();
        newSong.addProperty("VideoId", song.getVideoId());
        newSong.addProperty("title", song.getTitle());
        newSong.addProperty("artist", song.getArtist());
        newSong.addProperty("duration", song.getDuration());
        if (Files.exists(playlist.getPlaylist().toPath())){
            try (Reader reader = Files.newBufferedReader(filePath)) {
                JsonElement rootElement = JsonParser.parseReader(reader);
                if (rootElement.isJsonArray()) {
                    jsonfile = rootElement.getAsJsonArray();
                } else if (rootElement.isJsonObject()) {
                    jsonfile.add(rootElement.getAsJsonObject());
                }
            } catch (IOException e) {
                System.err.println("Error reading the JSON file: " + e.getMessage());
            }
        }
        jsonfile.add(newSong);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(jsonfile, writer);
//            System.out.println("Song added successfully!");
        } catch (IOException e) {
            System.err.println("Error writing the JSON file: " + e.getMessage());
        }

    }
    public Song[] initialisePlaylist(Playlist playlist) throws FileNotFoundException {
        try {
            String contents = Files.readString(playlist.getPlaylist().toPath());
            return gson.fromJson(contents, Song[].class);
        } catch (IOException e) {
            System.err.println("Error opening playlist: " + e.getMessage());
            return null;

        }
    }
}
