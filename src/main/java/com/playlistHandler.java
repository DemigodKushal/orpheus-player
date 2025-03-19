package com;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import com.google.gson.*;

public class playlistHandler{
    public ArrayList<Playlist> playlists = new ArrayList<>();
    Gson gson = new Gson();
    public Playlist createPlaylist(String name){
        File file = new File("C:\\playlists\\" + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Playlist creation not possible " + e.getMessage());
        }
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setPlaylist(file);
        return playlist;
    }
    public void importPlaylists(){
        File directory = new File("C:\\playlists\\");
        File[] files = directory.listFiles();
        if(!directory.exists()){
            directory.mkdir();
            return;
        }
        if(files == null){
            return;
        }
        for (File file : files){
            Playlist playlist  = new Playlist();
            playlist.setName(file.getName());
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
        jsonFile.add(newSong);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(jsonFile, writer);
        } catch (IOException e) {
            System.err.println("Error writing the JSON file: " + e.getMessage());
        }

    }
    public Song[] initialisePlaylist(Playlist playlist) {
        try {
            String contents = Files.readString(playlist.getPlaylist().toPath());
            return gson.fromJson(contents, Song[].class);
        } catch (IOException e) {
            System.err.println("Error opening playlist: " + e.getMessage());
            return new Song[0];

        }
    }

}
