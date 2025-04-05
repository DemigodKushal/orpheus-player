
![orpheus](https://github.com/user-attachments/assets/bc19d63f-a247-48bc-8bf4-df1984bf752c)


https://github.com/DemigodKushal/orpheus-player
## A simple yet functional music streamer that fetches music from YouTube. Consists of basic features such as:
* Search: Allows user to search a song of their preference and get results. Any song from the results can be played at will.
* Progress Indicator: A dynamic progress indicator is visible when a song is being loaded, indicating that the app is working.
* Media Controls: Songs can be played and paused. User can also seek throughout the duration  of the song. The previous button replays the song and the next button plays the next song from queue. 
* Cover Art: The Cover, Title, and the Artist of the current playing song is visible in the app.  Incase the cover art fails to get fetched, a fallback image is displayed.
* Playlist: Playlists or collections of songs have been implemented, which can be created by pressing a button. Songs can then be added to these playlists to be stored for later through the Search Display and the Add Song to Playlist button present near the media controls. Playlists can also be renamed and deleted. These are stored locally in the form of JSON files. Songs can also be added into multiple playlists and removed from playlist through the menu button.
* Queue: A simple Queue system has been implemented which stores order of songs to be played after the current running track has been completed or the next track has been requested. It has been done through a simple linked-list system. Songs can be added to queue from the menu button of songs in a playlist. The queue can also be cleared all at once.  A song can also be prioritized to be played next.

## Tech Stack
* Java : Playlists, Queue, Song, Playlist Handler, APIhandler classes.
* Java - Swing & Graphics2D: GUI Implementation.
* Java - GSon: For handling all json files involved in songs, playlists etc.
* Java - vlcj: For handling all media playback.
* Python - Flask: A flask app was made which acts as the backend of our project, it's responsible for searching and fetching music.
* Python - ytmusicapi: For performing search over YouTube Music.
* Python - ytdlp: For fetching a streamable URL for provided song.
* Obsidian - Creating README.MD
## A brief yet informative overview:
orpheus-player, named after the legendary musician, is a basic music streaming app. It fetches music from YouTube, utilizing the popular python library: yt-dlp. A simple flask app was written in python, meant to perform two functions: First, searching: This was done using the unofficial ytmusicapi. When the project was being researched upon, we were supposed to use this api for both searching and playing, however we eventually learnt that this was not possible, which led us to utilise yt-dlp for our second function, playing the actual song. Our flask app returns search results in a json which are then converted to utilisable objects through Google's gson library. It also returns a simple streamable url for playing a specific song which is then utilised by the media playback library used, which is vlcj.

Now as one would expect in any music player, we have implemented playlists and a queue system. In short, playlists are simple json files stored locally which contain a json array of songs and it's info. A playlist handler is present for various functions which deal with playlists such as creation, deletion, renaming and so on. A simple queue has been implemented in which one can add songs and direct a specific song from queue to be played next. 

The main player gui consists of  basic media playback control such as seeking, pausing and playing,  next and previous, etc. It also displays the name, artist and the thumbnail of the current playing song. A small progress bar is displayed under this piece of ui to show that the app is working  on playing a song and has not gone unresponsive.

The UI has been written in java-swing, and hence may not look and feel like the greatest of interfaces, but it gets the job done. Also due to lack of resources and knowledge, we could not get our hands on a remote hosting service to host our python script and hence it must run locally for now. This will be changed as soon as we figure out how to host stuff online for free. 

P.S: Required python libraries are present in requirements.txt

# Made by:
## Parv Gandhi

* Responsible for implementing all the functions into a working GUI. Implemented several methods to allow normal functions to be integrated in the UI. Played a major role in the debugging of the program. Also designed the logo. Techstack: Java, JavaSwing, and Graphics2D. 
## Kushal Shrivastava 

* Responsible for creating the backend in python and all classes such as Playlist, Song, PlaylistHandler, APIHandler. Performed final clean-up of code and debugging of the entire program. Captured media showcasing the program as well. Also designed the logo. Techstack: Java, Python - Flask. 
## Dishant Sharma 

* Helped in implementing Playlists and Queue functionality.



