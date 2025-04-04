from ytmusicapi import YTMusic  
from flask import Flask, jsonify
import yt_dlp
ytmusic = YTMusic()
app = Flask("musicAPI")
@app.route('/search/<name>')
def getSongDetails(name):
    results = ytmusic.search(name, "songs")
    limited_results = results[:3]
    filtered_results = []
    for result in limited_results:
        artist_name = result.get("artists", [{}])[0].get("name", "Unknown")
        filtered_results.append({
            "title": result.get("title", ""),
            "artist": artist_name,
            "duration": result.get("duration_seconds", ""),
            "videoId": result.get("videoId", "")
        })
    
    return jsonify(filtered_results)

@app.route('/play/<videoId>')
def getSongLink(videoId):
    video_url = "https://www.youtube.com/watch?v=" + videoId
    ydl_opts = {
    "format": "bestaudio",
    "quiet": True,}
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info_dict = ydl.extract_info(video_url, download=False)
        audio_url = info_dict["url"]
        return audio_url
