package com;

import javax.swing.JOptionPane;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.APIhandler.Song;
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
    public boolean deletePlaylist(Playlist playlist) {
        if (playlists.remove(playlist)) {
            try {
                Files.deleteIfExists(playlist.getPlaylist().toPath());
                return true;
            } catch (IOException ex) {
                System.err.println("Failed to delete file: " + ex.getMessage());
                return false;
            }
        }
        return false;
    }
    public boolean renamePlaylist(Playlist playlist, String newName) {
        File oldFile = playlist.getPlaylist();
        File newFile = new File(oldFile.getParent(), newName + ".json");
        
        if (newFile.exists()) {
            throw new IllegalArgumentException("Playlist name already exists!");
        }
        
        if (oldFile.renameTo(newFile)) {
            playlist.setName(newName);
            playlist.setPlaylist(newFile);
            return true;
        }
        return false;
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

    public boolean addSong(Playlist playlist, Song song) {
        try {
            if (isSongInPlaylist(playlist, song)) {
                return false; // Already exists
            }
            
            JsonArray jsonArray = new JsonArray();
            Path path = playlist.getPlaylist().toPath();
            
            // Read existing content
            if (Files.exists(path) && Files.size(path) > 0) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                }
            }
            
            // Create new song entry
            JsonObject songJson = new JsonObject();
            songJson.addProperty("videoId", song.getVideoId()); // Consistent casing
            songJson.addProperty("title", song.getTitle());
            songJson.addProperty("artist", song.getArtist());
            songJson.addProperty("duration", song.getDuration());
            
            jsonArray.add(songJson);
            
            // Write back to file
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(jsonArray, writer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        	APIhandler sapi=new APIhandler();
            Path path = playlist.getPlaylist().toPath();
            
            // Handle empty/missing files
            if (!Files.exists(path) || Files.size(path) == 0) {
                return new Song[0];
            }
            
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                List<Song> songs = new ArrayList<>();
                
                for (JsonElement element : jsonArray) {
                    JsonObject songObj = element.getAsJsonObject();
                    // Handle case-sensitive JSON properties
                    String videoId = getStringSafe(songObj, "videoId");
                    String title = getStringSafe(songObj, "title");
                    String artist = getStringSafe(songObj, "artist");
                    int duration = getIntSafe(songObj, "duration");
                    
                    if(videoId != null && title != null) {
                    	songs.add(new APIhandler().new Song(
                    		    title, 
                    		    videoId, 
                    		    artist,
                    		    songObj.has("thumbnail") ? songObj.get("thumbnail").getAsString() : sapi.returnThumbnailUrl(videoId),
                    		    duration
                    		));	                        }
                }
                return songs.toArray(new Song[0]);
            }
        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, "Error loading playlist: " + e.getMessage(),
            		"Error", JOptionPane.ERROR_MESSAGE);
            return new Song[0]; // Return empty array instead of null
        }
    }
    private String getStringSafe(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    private int getIntSafe(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsInt() : 0;
    }
    public void savePlaylist(Playlist playlist, List<Song> songs) throws IOException {
        try (Writer writer = Files.newBufferedWriter(playlist.getPlaylist().toPath())) {
            gson.toJson(songs, writer);
        }
    }
    
    public void deleteSongFromPlaylist(Playlist playlist, Song song){
        JsonArray jsonFile = new JsonArray();
        File file = playlist.getPlaylist();
        Path filePath = file.toPath();
        JsonObject songJson = new JsonObject();
        songJson.addProperty("videoId", song.getVideoId()); // Consistent casing
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
            Writer writer = null;
            try {
                writer = Files.newBufferedWriter(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            jsonFile.remove(songJson);
            gson.toJson(jsonFile, writer);
        }
        else System.err.println("Song not Found");
    }

    
    public boolean removeFromPlaylist(Playlist playlist, Song song) throws IOException {
        List<Song> songs = new ArrayList<>(Arrays.asList(initialisePlaylist(playlist)));
        boolean removed = songs.removeIf(s -> s.getVideoId().equals(song.getVideoId()));
        
        if (removed) {
            savePlaylist(playlist, songs);
        }
        return removed;
    }

    public boolean isSongInPlaylist(Playlist playlist, Song song) throws IOException {
        return Arrays.stream(initialisePlaylist(playlist))
                     .anyMatch(s -> s.getVideoId().equals(song.getVideoId()));
    }

    

}
