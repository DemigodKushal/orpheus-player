package com;
public class Song {

    private String title;
    private String VideoId;
    private String artist;
    private int duration;

    public Song(String title, String videoId, String songArtist, int songLength){
        this.title = title;
        this.artist = songArtist;
        this.VideoId = videoId;
        this.duration = songLength;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }


    public String getVideoId() {
        return VideoId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideoId(String videoId) {
        this.VideoId = videoId;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
