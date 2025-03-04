package org.sample.sampleGUI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;	

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.sample.sampleGUI.MusicPlayerGUI.APIhandler.Song;

import javax.sound.sampled.*;


import java.awt.*;
import java.awt.event.*;
import java.util.Queue;
import java.net.URL;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
import org.eclipse.wb.swing.FocusTraversalOnArray;

public class MusicPlayerGUI extends JFrame {

    private List<String> playlist;
    private int currentIndex;
    private long totalDuration;
    private EmbeddedMediaPlayerComponent mediaComponent;
    private EmbeddedMediaPlayer mediaPlayer;
    private Timer updateTimer;
//    private JSlider seekSlider;
    private JLabel timeLabel;
    private boolean isPaused;
    private String[] vidIDs;
    private JButton previous;
    private JButton next;
    private JToggleButton play_pause;
    private JTextField searchbox;
    private JButton searchbtn;
    private DefaultTableModel model;
    private JTable searchDisplay;
    
       
  

    public MusicPlayerGUI() throws IOException {
        super("Swing Music Player");
        getContentPane().setForeground(new Color(192, 192, 192));
        getContentPane().setBackground(new Color(59, 59, 59));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);
        mediaComponent=new EmbeddedMediaPlayerComponent();
   	 	mediaPlayer = mediaComponent.mediaPlayer();
   	 	getContentPane().add(mediaComponent);
   	    
        
   	    
   	    BufferedImage playImage = ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\play.jpg"));
        BufferedImage pauseImage = ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\pause24px.png"));
        
        // Previous button with scaled icon
        BufferedImage prevImage = ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\prev512.png"));
       
        
        
   	 	APIhandler sapi=new APIhandler();                  //new APIhandler instantiation
        
   	 	String[] columnNames = {"Title", "Artist", "Duration"};
        model = new DefaultTableModel(columnNames, 0) {
        	@Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells non-editable
            }
        };
        
        searchDisplay = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(searchDisplay);
        scrollPane.setBounds(301, 101, 235, 96);
        getContentPane().add(scrollPane);
         
         searchbtn = new JButton("search");
         searchbtn.setBounds(473, 50, 63, 21);
         getContentPane().add(searchbtn);
         searchbtn.setBackground(new Color(59, 59, 59));
         
          searchbox = new JTextField();
          searchbox.setForeground(new Color(255, 255, 255));
          searchbox.setBounds(301, 44, 155, 34);
          searchbox.setColumns(10);
          getContentPane().add(searchbox);
          searchbox.setBackground(new Color(59, 59, 59));
          // Play/Pause toggle button
          play_pause = new JToggleButton("");
          play_pause.setBounds(391, 478, 64, 64);
          getContentPane().add(play_pause);
          play_pause.addActionListener(new ActionListener() {
          	public void actionPerformed(ActionEvent e) {
          		if(play_pause.isSelected()) {
          			resumeSong();
          		}
          		else {
          			pauseSong();
          		}
          	}
          });
          play_pause.setForeground(new Color(59, 59, 59));
          play_pause.setBackground(new Color(59, 59, 59));
          play_pause.setIcon(new ImageIcon(resizeImage(playImage,64,64)));
          play_pause.setSelectedIcon(new ImageIcon(resizeImage(pauseImage,24,24)));
          
          previous = new JButton("");
          previous.setBounds(301, 504, 57, 33);
          getContentPane().add(previous);
          previous.setBackground(new Color(128, 128, 128));
          previous.setIcon(new ImageIcon(resizeImage(prevImage, 24, 24)));
          previous.addActionListener(e -> previousSong());
          
          next = new JButton("next");
          next.setBounds(480, 504, 51, 33);
          getContentPane().add(next);
          next.addActionListener(new ActionListener() {
          	public void actionPerformed(ActionEvent e) {
          		try {
					nextSong(sapi);
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
          	}
          });
          
          
          // Add a media player event listener to enable slider when media is ready
      	JLayeredPane layeredSeekPane = new JLayeredPane();
        layeredSeekPane.setBounds(301, 404, 235, 64);
        getContentPane().add(layeredSeekPane);
        
        
        
      

        
        
        final JSlider seekSlider = new JSlider(0, 0, 0);
        seekSlider.setBounds(0, 10, 235, 34);
        seekSlider.setEnabled(false);
        layeredSeekPane.add(seekSlider, Integer.valueOf(1));
        
        timeLabel = new JLabel("   00:00                                                   00:00");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setBounds(0, 45, 235, 20);
        layeredSeekPane.add(timeLabel, Integer.valueOf(2));
        
        // Listen for slider adjustments to seek in the media
        seekSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!seekSlider.getValueIsAdjusting()) {
                    // When the user finishes dragging, get the slider value as new time (in ms)
                    int newTime = seekSlider.getValue();
                    mediaPlayer.controls().setTime(newTime);
                }
            }
        });
        
        // Timer to update the seek slider and time label every 500 ms
        updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only update if media is playing and the user is not currently dragging the slider.
                if (!seekSlider.getValueIsAdjusting() && mediaPlayer.status().isPlaying()) {
                    long currentTime = mediaPlayer.status().time();
                    long totalTime = mediaPlayer.status().length();
                    if (totalTime > 0) {
                        seekSlider.setMaximum((int) totalTime);
                        seekSlider.setValue((int) currentTime);

                        timeLabel.setText("   " + formatTime(currentTime) + "                                                   " + formatTime(totalTime));
                        // Enable the slider once the media's duration is known.
                        seekSlider.setEnabled(true);
                    }
                }
            }
        });
        updateTimer.start(); 
          // Next button
          
          
          
          
          
          searchDisplay.addMouseListener(new MouseAdapter() {
        	    @Override
        	    public void mouseClicked(MouseEvent e) {
        	        if (e.getClickCount() == 2) { // Double-click detected
        	        	mediaPlayer.controls().stop();
        	        	int selectedRow = searchDisplay.getSelectedRow();
        	            if (selectedRow != -1 && vidIDs != null && selectedRow < vidIDs.length) {
        	                try {
        	                    URL newURL = new URL(sapi.returnURL(vidIDs[selectedRow]));
//        	                    mediaPlayer.controls().stop();
        	                    isPaused = false;
        	                    play_pause.setSelected(true);
        	                    mediaPlayer.media().play(newURL.toString());
        	                    
        	                } catch (IOException | InterruptedException ex) {
        	                    ex.printStackTrace();
        	                }
        	            }
        	        }
        	    }
        	});
          ActionListener searchAction = e -> performSearch(sapi);
          searchbox.addActionListener(searchAction);
          searchbtn.addActionListener(searchAction);
         
          searchbox.addKeyListener(new KeyAdapter() {
        	    @Override
        	    public void keyPressed(KeyEvent e) {
        	        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        	            performSearch(sapi);
        	        }
        	    }
        	});

          
       
    }

        
    private void performSearch(APIhandler sapi) {
        model.setRowCount(0);
        String userInput = searchbox.getText();
        try {
            APIhandler.Song[] songs = sapi.search(userInput);
            int numSongs = Math.min(3, songs.length);
            vidIDs = new String[numSongs]; // initialize vidIDs based on actual number of songs
            for (int i = 0; i < numSongs; i++) {
                vidIDs[i] = songs[i].getVideoId();
                Object[] row = { songs[i].getTitle(), songs[i].getArtist(), songs[i].getDuration() };
                model.addRow(row);
            }
            searchDisplay.revalidate();
            searchDisplay.repaint();
            // Make results visible if they were hidden:
            // resultsScrollPane.setVisible(true);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void playSong() {
    	isPaused=mediaPlayer.controls().start();
    }


    
        
    
    public void resumeSong() {
        
            mediaPlayer.controls().play();
            isPaused = false;
        
    }

    public void pauseSong() {
            if (!isPaused) {
                mediaPlayer.controls().pause();
                isPaused = true;
            } else {
                mediaPlayer.controls().play();
                isPaused = false;
            }
      
    }


    private void previousSong() {
       //nedd to add reseeting
    }

    public void nextSong(APIhandler newapi) throws MalformedURLException, IOException, InterruptedException {
        
			if (!songQueue.isEmpty()) {
			    Song nextSong = songQueue.queue.poll();
			    URL nextURL=new URL(newapi.returnURL(nextSong.getVideoId()));
			    
			        mediaPlayer.media().play(nextURL.toString());
			        
			        isPaused = false;
			} 
			else {
			    mediaPlayer.controls().stop();
			}
			
			
    }

    public static class songQueue {
    	private static Queue<APIhandler.Song> queue = new LinkedList<>();

        private APIhandler.Song nowPlaying;
        public void addSong(APIhandler.Song song){
        this.queue.add(song);
        }
        public static boolean isEmpty() {
				if(getQueue()==null) return true;
				else return false;
        }	
        public static Queue getQueue() {
				return queue;
		}
        public APIhandler.Song getNowPlaying() {
            return nowPlaying;
        }

        public void setNowPlaying(APIhandler.Song nowPlaying) {
            this.nowPlaying = nowPlaying;
        }

        public void songEnd(){
            this.queue.poll();
            setNowPlaying(this.queue.peek());
        }
    }
    
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        if (originalImage == null) {
            throw new IllegalArgumentException("Original image is null");
        }
        // Use a known type rather than originalImage.getType()
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Set high-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the original image scaled to the new dimensions
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
    
    private String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Inner class to handle API calls
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
//            for (int i = 0; i<3; i++){
//                System.out.println(searchResults[i].getTitle() + " - " + searchResults[i].getArtist());
//            }
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
  
        
        
        // Inner class representing a Song
        public class Song {
            private String title;
            private String videoId;
            private String artist;
            private int duration;

            public Song(String title, String videoId, String songArtist, int songLength) {
                this.title = title;
                this.artist = songArtist;
                this.videoId = videoId;
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
                return videoId;
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

            public void setDuration(int duration) {
                this.duration = duration;
            }
        } // End of Song class
    } // End of APIhandler class
    
      

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MusicPlayerGUI().setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    private class SwingAction extends AbstractAction {
        public SwingAction() {
            putValue(NAME, "SwingAction");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }
        public void actionPerformed(ActionEvent e) {
        }
    }
}