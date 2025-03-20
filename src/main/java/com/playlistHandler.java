package com;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
        File file = new File("C:\\playlists\\" + name+".json");
        try {
            if(file.createNewFile()) {
                Playlist playlist = new Playlist();
                playlist.setName(name);
                playlist.setPlaylist(file);
                return playlist;
            }else {
                JOptionPane.showMessageDialog(null, "Playlist already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error creating playlist: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

    }
    public void importPlaylists(){
        playlists.clear();
        File directory = new File("C:\\playlists\\");
        File[] files = directory.listFiles();
        if(!directory.exists()){
            directory.mkdir();
            return;
        }
        createDefaultPlaylistIfNeeded();

        for (File file : files){
            Playlist playlist  = new Playlist();
            playlist.setName(file.getName().replace(".json", ""));
            playlist.setPlaylist(file);
            playlists.add(playlist);
        }

    }

    public void addSong(Playlist playlist, Song song){
        JsonArray jsonFile = new JsonArray();
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
                    jsonFile = rootElement.getAsJsonArray();
                } else if (rootElement.isJsonObject()) {
                    jsonFile.add(rootElement.getAsJsonObject());
                }
            } catch (IOException e) {
                System.err.println("Error reading the JSON file: " + e.getMessage());
            }
        }
        if(!jsonFile.contains(newSong)) {
            jsonFile.add(newSong);
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(jsonFile, writer);
            } catch (IOException e) {
                System.err.println("Error writing the JSON file: " + e.getMessage());
            }
        }
        else System.err.println("Song already exists");

    }
    private void createDefaultPlaylistIfNeeded() {
        File defaultPlaylist = new File("C:\\playlists\\", "Liked Songs" + ".json");
        if (!defaultPlaylist.exists()) {
            try {
                defaultPlaylist.createNewFile();
                Playlist playlist = new Playlist();
                playlist.setName("Liked Songs");
                playlist.setPlaylist(defaultPlaylist);
                playlists.add(playlist);
            } catch (IOException e) {
                System.err.println("Error creating default playlist: " + e.getMessage());
            }
        }
    }
    public Song[] initialisePlaylist(Playlist playlist) {
        try {
            String contents = Files.readString(playlist.getPlaylist().toPath());
            Song[] songs = gson.fromJson(contents, Song[].class);
            return songs != null ? songs : new Song[0];
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "Error loading playlist:\n" + e.getMessage(),
                            "Loading Error", JOptionPane.ERROR_MESSAGE));
            return new Song[0];

        }
    }

    public void deleteSongFromPlaylist(Playlist playlist, Song song){
        JsonArray jsonFile = new JsonArray();
        File file = playlist.getPlaylist();
        Path filePath = file.toPath();
        JsonObject songJson = new JsonObject();
        songJson.addProperty("VideoId", song.getVideoId());
        songJson.addProperty("title", song.getTitle());
        songJson.addProperty("artist", song.getArtist());
        songJson.addProperty("duration", song.getDuration());
        if (Files.exists(playlist.getPlaylist().toPath())){
            try (Reader reader = Files.newBufferedReader(filePath)) {
                JsonElement rootElement = JsonParser.parseReader(reader);
                if (rootElement.isJsonArray()) {
                    jsonFile = rootElement.getAsJsonArray();
                } else if (rootElement.isJsonObject()) {
                    jsonFile.add(rootElement.getAsJsonObject());
                }
            } catch (IOException e) {
                System.err.println("Error reading the JSON file: " + e.getMessage());
            }
        }
        if(jsonFile.contains(songJson)){
            jsonFile.remove(songJson);
        }
        else System.err.println("Song Not Found");
    }

    public void deletePlaylist(Playlist playlist){
        if(!playlists.remove(playlist)){
            System.err.println("Playlist could not be removed");
        }
    }

}