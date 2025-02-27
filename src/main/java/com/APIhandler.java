package com;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class APIhandler {
        public Song[] search(String name) throws IOException, InterruptedException {
            name = name.replaceAll("\\s", "+");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/" + name))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String result = response.body();
            Gson gson = new Gson();
            Song[] searchResults = gson.fromJson(result, Song[].class);
            for (int i = 0; i<3; i++){
                System.out.println(searchResults[i].getTitle() + " - " + searchResults[i].getArtist());
            }
            return searchResults;
        }
    public static void main(String[] args) throws IOException, InterruptedException {
        APIhandler a = new APIhandler();
        System.out.println(a.search("ss"));
    }
}


