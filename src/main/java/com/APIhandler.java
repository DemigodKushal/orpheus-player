	package com;
	
	import java.io.IOException;
	import java.net.URI;
	import java.net.http.HttpClient;
	import java.net.http.HttpRequest;
	import java.net.http.HttpResponse;
	
	import com.google.gson.Gson;
	import com.google.gson.annotations.SerializedName;
	
	public class APIhandler {
		        public Song[] search(String name) throws IOException, InterruptedException {
		            name = name.replaceAll("\\s", "+");
		            HttpClient client = HttpClient.newHttpClient();
		            HttpRequest request = HttpRequest.newBuilder()
		                    .uri(URI.create("http://127.0.0.1:5000/search/" + name))
		                    .GET()
		                    .build();
		            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		            String result = response.body();
		            Gson gson = new Gson();
		            Song[] searchResults = gson.fromJson(result, Song[].class);
		            for (Song song : searchResults) {
		                String thumbnailUrl = returnThumbnailUrl(song.getVideoId());
		                song.setThumbnailUrl(thumbnailUrl); // Ensure you have a setter method
		            }
		            return searchResults;
		        }
		        public String returnURL(String videoId) throws IOException, InterruptedException {
		            HttpClient client = HttpClient.newHttpClient();
		            HttpRequest request = HttpRequest.newBuilder()
		                    .uri(URI.create("http://127.0.0.1:5000/play/" + videoId))
		                    .GET()
		                    .build();
		            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		            String result = response.body();
		            return result;
		        }
		        
		        public String returnThumbnailUrl(String videoId){
		            return ("https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg");
		        }
		        
		        // Inner class representing a Song
		        public  class Song {
		        	
		        	private String title;
		            private String videoId;
		            private String artist;
		            private int duration;
		            @SerializedName("thumbnail")
		            private String thumbnailUrl;
		            @Override
		            public String toString() {
		                return title + " - " + artist;
		            }
		            
		            
	
					public Song(String title, String videoId, String songArtist,String thumbnailUrl, int songLength) {
		                this.title = title;
		                this.artist = songArtist;
		                this.videoId = videoId;
		                this.duration = songLength;
		                this.thumbnailUrl=thumbnailUrl;
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
		                return videoId;
		            }
		            public String getThumbnailUrl() {
		            	return this.thumbnailUrl;
		            }
		
		            public void setTitle(String title) {
		                this.title = title;
		            }
		
		            public void setVideoId(String videoId) {
		                this.videoId = videoId;
		            }
		
		            public void setArtist(String artist) {
		                this.artist = artist;
		            }
		            
		            public void setThumbnailUrl(String thumbnailUrl) {
		            	this.thumbnailUrl = thumbnailUrl;
						
					}
		            
		            public void setDuration(int duration) {
		                this.duration = duration;
		            }
		        }
		    }
