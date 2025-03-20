	package org.sample.sampleGUI;
	
	import java.io.File;
	import java.io.FileNotFoundException;
	import java.util.concurrent.TimeUnit;
	import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
	import uk.co.caprica.vlcj.player.base.MediaPlayer;
	import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
	import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
	import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;	
	
	import javax.imageio.ImageIO;
	import javax.swing.*;
	import javax.swing.table.DefaultTableModel;
	
	import org.sample.sampleGUI.MusicPlayerGUI.Playlist;
	import org.sample.sampleGUI.MusicPlayerGUI.APIhandler.Song;
	
	
	import java.awt.*;
	import java.awt.event.*;
	import java.util.Queue;
	import java.net.URL;
	import java.net.URI;
	import java.awt.image.BaseMultiResolutionImage;
	import java.awt.image.BufferedImage;
	import java.awt.image.MultiResolutionImage;
	import java.io.IOException;
	import java.io.Reader;
	import java.io.Writer;
	import java.net.MalformedURLException;
	import java.net.http.HttpClient;
	import java.net.http.HttpRequest;
	import java.net.http.HttpResponse;
	import java.nio.file.Files;
	import java.nio.file.Path;
	import java.util.ArrayList;
	import java.util.Arrays;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Map;
	
	import com.google.gson.Gson;
	import com.google.gson.JsonArray;
	import com.google.gson.JsonElement;
	import com.google.gson.JsonObject;
	import com.google.gson.JsonParser;
	//@wbp.parser.entryPoint
	public class MusicPlayerGUI extends JFrame {
	
		static {
		    // Workaround for WindowBuilder infinite loop
		    if (java.awt.GraphicsEnvironment.isHeadless()) {
		        System.setProperty("java.awt.headless", "false");
		    }
		    System.setProperty("wb.disable.background.designer.thread", "true");
		}
		
		
	
	    private int currentIndex;
	    private long totalDuration;
	    private EmbeddedMediaPlayerComponent mediaComponent;
	    private EmbeddedMediaPlayer mediaPlayer;
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
	    private JPanel queuePane;
	    private JList<String> playlistJList;
	    private JList<String>  queueList;
	    private List<String> playlistNames;
	    private volatile boolean bufferingComplete = false;
	    private long playbackStartTime;
	    private Timer uiTimer;  
	    private volatile boolean seekInProgress = false;
	    private volatile long seekTargetTime = -1;
	    private volatile boolean seekShouldPlay = true;
	    private JScrollPane scrollPane_1;
	    private DefaultListModel<Object> listModel;
	    private JPanel playlistPanel;
	    private List<Playlist> playlists;
	    private List<Song> songs;
	    private JList<Object> list;
	    private boolean showingPlaylists = true;
	    private JTable songTable;
	    private DefaultListModel<String> playlistModel;
	    private Playlist playlist;
	    private playlistHandler plHandler;
	//    private SongTableModel songModel;
	    private Map<String, List<Song>> playlistData;
	    private JToggleButton showPlaylistbtn;
	    private JButton addPlaylistButton;
	    private JButton backButton=new JButton();
	    private JTextField playlistFeild;
	  
	    
	    public MusicPlayerGUI() throws IOException {
	    	
	    	
	    	
	
	    	super("Swing Music Player");
	        getContentPane().setForeground(new Color(192, 192, 192));
	        getContentPane().setBackground(new Color(51, 51, 51));
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setSize(800, 600);
	        setLocationRelativeTo(null);
	        getContentPane().setLayout(null);
	        mediaComponent=new EmbeddedMediaPlayerComponent();
	   	    mediaPlayer = mediaComponent.mediaPlayer();
	   	// Add to your media player initialization in constructor:
	   	    mediaPlayer.submit(new Runnable() {
	   	    	
	   	      @Override
	   	      public void run() {
	   	         // This ensures media player runs in its own thread
	   	      }
	   	   
	   	    });
	   	// In constructor, update media preparation options:
	   	 mediaPlayer.media().prepare(
	   	     ":avcodec-hw=any",
	   	     ":drop-late-frames",
	   	     ":skip-frames",
	   	     ":audio-desync=50",
	   	     ":http-continuous",
	   	     ":http-reconnect",
	   	     ":audio-resampler=soxr",
	   	     ":audio-filter=scaletempo",
	   	     ":audio-time-stretch",
	   	     ":audio-replay-gain-mode=track",
	   	     ":demuxer-seekable-cache=1"  // Add this for better seeking
	   	    );
	   	 	getContentPane().add(mediaComponent);
	   	 	
	   	 
	   	 	
	   	 	
			
		   	 ImageIcon playIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\play_arrow_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		   	 ImageIcon pauseIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\pause_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		   	 ImageIcon prevIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\skip_previous_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		   	 ImageIcon nextIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\skip_next_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		   	 ImageIcon showPlaylistIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\\\codes\\\\eclipse\\\\sampleGUI\\\\icons\\\\playlist_play_64dp_FFFFFF.png")),32,32),BufferedImage.TYPE_INT_ARGB));
		   	 ImageIcon queueIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\queue_music_64dp_FFFFFF.png")),32,32),BufferedImage.TYPE_INT_ARGB));	
		   	 ImageIcon searchIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\search_64dp_FFFFFF.png")),24,24),BufferedImage.TYPE_INT_ARGB));	
		   	 ImageIcon addPlaylistIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\\\codes\\\\eclipse\\\\sampleGUI\\\\icons\\\\playlist_add_64dp_FFFFFF.png")),16,16),BufferedImage.TYPE_INT_ARGB));	
		   	 ImageIcon closeIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(new File("D:\\codes\\eclipse\\sampleGUI\\icons\\close_64dp_FFFFFF.png")),16,16),BufferedImage.TYPE_INT_ARGB));	
		   	
	        
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
	        scrollPane.setForeground(new Color(69, 69, 69));
	        scrollPane.setBackground(new Color(69, 69, 69));
	        scrollPane.setBounds(291, 104, 247, 73);
	        getContentPane().add(scrollPane);
	         
	         searchbtn = new JButton(searchIcon);
	         searchbtn.setForeground(new Color(69, 69, 69));
	         styleIconButton(searchbtn);
	         searchbtn.setBounds(463, 67, 63, 27);
	         getContentPane().add(searchbtn);
	         searchbtn.setBackground(new Color(69, 69, 69));
	         
	          searchbox = new JTextField();
	          searchbox.setForeground(new Color(255, 255, 255));
	          searchbox.setBounds(298, 60, 155, 34);
	          searchbox.setColumns(10);
	          getContentPane().add(searchbox);
	          searchbox.setBackground(new Color(59, 59, 59));
	          // Play/Pause toggle button
	          play_pause = new JToggleButton("");
	          play_pause.setBounds(375, 489, 64, 64);
	          getContentPane().add(play_pause);
	       // Replace play_pause action listener with:
	          play_pause.addActionListener(e -> {
	              new Thread(() -> {
	                  if (mediaPlayer.status().isPlaying()) {
	                      mediaPlayer.controls().pause();
	                      uiTimer.stop();
	                  } else {
	                      if (mediaPlayer.status().time() >= mediaPlayer.status().length() - 1000) {
	                          mediaPlayer.controls().setTime(0);
	                      }
	                      mediaPlayer.controls().play();
	                      uiTimer.start();
	                  }
	                  SwingUtilities.invokeLater(() -> {
	                  });
	              }).start();
	          });
	          play_pause.setForeground(new Color(69, 69, 69));
	          play_pause.setBackground(new Color(69, 69, 69));
	          play_pause.setIcon(playIcon);
	          play_pause.setSelectedIcon(pauseIcon);
	          styleIconButton(play_pause);
	          
	
	          
	          previous = new JButton(prevIcon);
	          styleIconButton(previous);
	          previous.setBounds(291, 504, 57, 33);
	          getContentPane().add(previous);
	          previous.setBackground(new Color(128, 128, 128));
	
	          previous.addActionListener(e -> previousSong());
	          
	          next = new JButton(nextIcon);
	          next.setBounds(468, 504, 51, 33);
	          styleIconButton(next);
	          getContentPane().add(next);
	          next.addActionListener(e -> {
	              // [Threading Change] Run next song operation in a separate thread.
	              new Thread(() -> {
	                  try {
	                      nextSong(sapi);
	                  } catch (IOException | InterruptedException ex) {
	                      ex.printStackTrace();
	                  }
	              }).start();
	          });
	          
	          
	          // Add a media player event listener to enable slider when media is ready
	      	JLayeredPane layeredSeekPane = new JLayeredPane();
	        layeredSeekPane.setBounds(265, 425, 288, 64);
	        getContentPane().add(layeredSeekPane);
	        
	        timeLabel = new JLabel("   00:00                                                       00:00");
	        timeLabel.setBackground(new Color(0, 0, 0));
	        timeLabel.setForeground(new Color(0, 0, 0));
	        timeLabel.setBounds(0, 24, 286, 20);
	        layeredSeekPane.add(timeLabel, Integer.valueOf(2));
	        
	        
	        
	      
	
	        
	        
	        JSlider seekSlider = new JSlider(0, 1000, 0);
	        seekSlider.setBackground(new Color(69, 69, 69));
	        seekSlider.setPaintTicks(true);
	        seekSlider.setPaintLabels(true);
	        seekSlider.setEnabled(false);
	        seekSlider.setBounds(0, 10, 286, 34);
	        seekSlider.setEnabled(false);
	        layeredSeekPane.add(seekSlider, Integer.valueOf(1));
	        
	         playlistPanel = new JPanel();
	         playlistPanel.setLayout(null);
	         playlistPanel.setBackground(new Color(69, 69, 69)); // semi-transparent
	         playlistPanel.setBounds(10, 111, 227, 400);  // adjust as needed
	         playlistPanel.setVisible(false);
	//    
	         
	         
	         
	         
	        JLabel playlistLabel = new JLabel("Playlists");
	        playlistLabel.setBackground(new Color(69, 69, 69));
	        playlistLabel.setForeground(new Color(255, 255, 255));
	        playlistLabel.setBounds(10, 10, 103, 30);
	        playlistPanel.add(playlistLabel);
	//        
	      	
	//        
	       
	        
	        plHandler=new playlistHandler();
	   	 	plHandler.importPlaylists();
	   	    playlists= new ArrayList<>();
	        playlists=plHandler.playlists;
	        
	        listModel = new DefaultListModel<>();
	        list= new JList<>(listModel);
	        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        list.setCellRenderer(new CustomCellRenderer());
	        
	        SwingUtilities.invokeLater(() -> {
	            showPlaylists();
	        });
	//        
	        getContentPane().add(playlistPanel);
	        
	        
	        JScrollPane listScrollPane = new JScrollPane(list);
	        listScrollPane.setBorder(UIManager.getBorder("TextField.border"));
	        listScrollPane.setBackground(new Color(69, 69, 69));
	        listScrollPane.setBounds(10, 50, 207, 340);
	        playlistPanel.add(listScrollPane);
	//        
	        addPlaylistButton = new JButton(addPlaylistIcon);
	     
	        styleIconButton(addPlaylistButton);
	        addPlaylistButton.setBounds(177, 10, 40, 27);
	        playlistPanel.add(addPlaylistButton);
	        
	        list.setBackground(new Color(69, 69, 69));
	        list.setBounds(10, 50, 207, 340);
	         
	         
	         backButton = new JButton("<-");
	         backButton.setBounds(20, 36, 59, 15);
	         playlistPanel.add(backButton);
	         
	         
	         
	          playlistFeild = new JTextField();
	          playlistFeild.setBounds(81, 16, 136, 19);
	          playlistPanel.add(playlistFeild);
	          playlistFeild.setColumns(10);
	          playlistFeild.setVisible(false);
	          playlistFeild.setText("");
	          
	          
	          playlistFeild.addActionListener(e -> {
	              createPlaylistFromField();
	          });
	          playlistFeild.addKeyListener(new KeyAdapter() {
	        	    @Override
	        	    public void keyPressed(KeyEvent e) {
	        	        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
	        	            playlistFeild.setText("");
	        	            playlistFeild.setVisible(false);
	        	            addPlaylistButton.setVisible(true);
	        	        }
	        	    }
	        	});
	          playlistFeild.setVisible(true);
	          String playlistName = playlistFeild.getText().trim();
	          playlistFeild.setText("");
	          playlistFeild.setVisible(false);
	         backButton.addActionListener(e -> showPlaylists());
	         addPlaylistButton.addActionListener(e -> {
	        	 if (!playlistFeild.isVisible()) {
	        	        // Show text field when add button is clicked
	        	        playlistFeild.setVisible(true);
	        	        playlistFeild.requestFocus();
	        	        addPlaylistButton.setVisible(false);
	        	    }
	        	 if (!playlistName.isEmpty()) {
	 	            Playlist newPl = plHandler.createPlaylist(playlistName);
	 	            if (newPl != null) {
	 	                playlists.add(newPl);
	 	                listModel.addElement(newPl);
	 	            }
	 	        }
		        });
	//        
	        // When a playlist is selected from the JList (double-click), update main view and hide panel.
	        list.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                if (e.getClickCount() == 2) {
	                    int index = list.getSelectedIndex();
	                    if (showingPlaylists && index != -1) {
	                        try {
	                            showSongs(playlists.get(index));
	                        } catch (FileNotFoundException ex) {
	                            ex.printStackTrace();
	                        }
	                    } else if (!showingPlaylists) {
	                        // Handle song double-click
	                    }
	                }
	            }
	        });
	    
	        
	
	//    
	        queuePane = new JPanel();
	        queuePane.setBackground(new Color(69, 69, 69));
	        queuePane.setLayout(null);
	        queuePane.setBounds(582, 111, 194, 400);
	        getContentPane().add(queuePane);
	        queuePane.setVisible(false);
	//        
	        DefaultListModel<String> qList= new DefaultListModel<>();
	        queueList = new JList<>(qList);
	        queueList.setBackground(new Color(69, 69, 69));
	        queueList.setBounds(10, 47, 174, 343);
	        queuePane.add(queueList);
	        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        
	        scrollPane_1 = new JScrollPane();
	        scrollPane_1.setBackground(new Color(69, 69, 69));
	        scrollPane_1.setBounds(10, 47, 174, 343);
	        queuePane.add(scrollPane_1);
	        
	        showPlaylistbtn = new JToggleButton(showPlaylistIcon);
	        showPlaylistbtn.setBackground(new Color(69, 69, 69));
	        styleIconButton(showPlaylistbtn);
	        showPlaylistbtn.setBounds(20, 521, 115, 32);
	        getContentPane().add(showPlaylistbtn);
	        showPlaylistbtn.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e) {
	        		if(showPlaylistbtn.isSelected())
	        		playlistPanel.setVisible(true);
	        		else playlistPanel.setVisible(false);
	        	}
	        });
	//        
	
	        
	        JToggleButton queuebutton = new JToggleButton(queueIcon);
	        
	        queuebutton.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent e) {
	        	if(queuebutton.isSelected())	queuePane.setVisible(true);        	
	        	else queuePane.setVisible(false);
	        	}
	        });
	        
	        styleIconButton(queuebutton);
	        queuebutton.setBounds(691, 521, 85, 21);
	        getContentPane().add(queuebutton);
	        
	        
	        
	        // Listen for slider adjustments to seek in the media
	        
	
	        seekSlider.addMouseListener(new MouseAdapter() {
	            private boolean wasPlaying;
	
	            @Override
	            public void mousePressed(MouseEvent e) {
	                wasPlaying = mediaPlayer.status().isPlaying();
	                if (wasPlaying) {
	                    mediaPlayer.controls().pause();
	                }
	            }
	
	            @Override
	            public void mouseReleased(MouseEvent e) {
	                if (!seekSlider.getValueIsAdjusting()) {
	                    float position = seekSlider.getValue() / 1000f;
	                    long targetTime = (long) (position * totalDuration);
	                    
	                    seekTargetTime = targetTime;
	                    seekInProgress = true;
	                    seekShouldPlay = wasPlaying;
	
	                    new Thread(() -> {
	                        mediaPlayer.controls().setTime(targetTime);
	                    }).start();
	                }
	            }
	        });
	        // Timer to update the seek slider and time label every 1000 ms
	        
	        
	        
	     // Replace UI timer with:
	        uiTimer = new Timer(100, e -> {
	            if (mediaPlayer.status().isPlaying() && totalDuration > 0) {
	                final long currentTime = mediaPlayer.status().time();
	                final long duration = totalDuration;
	                
	                SwingUtilities.invokeLater(() -> {
	                    if (!seekSlider.getValueIsAdjusting()) {
	                        float progress = (float) currentTime / duration;
	                        seekSlider.setValue((int) (progress * 1000));
	                    }
	                    timeLabel.setText("   "+formatTime(currentTime)+
	                    		"                                                       "+
	                    		formatTime(duration));
	                    
	                   
	                });
	            }
	        });
	        uiTimer.start();
	          // Next button
	
	          searchDisplay.addMouseListener(new MouseAdapter() {
	        	    @Override
	        	    public void mouseClicked(MouseEvent e) {
	        	        if (e.getClickCount() == 2) { // Double-click detected
	        	        	mediaPlayer.controls().stop();
	        	        	totalDuration = 0;
	        	        	int selectedRow = searchDisplay.getSelectedRow();
	        	            if (selectedRow != -1 && vidIDs != null && selectedRow < vidIDs.length) {
	        	                new Thread(()->{
	        	                try {
	        	                    URL newURL = new URL(sapi.returnURL(vidIDs[selectedRow]));
	//        	                    mediaPlayer.controls().stop();
	        	                    isPaused = false;
	        	                    play_pause.setSelected(true);
	        	                    
	        	                    preloadAndPlay(newURL, seekSlider);
	        	                    playbackStartTime=System.currentTimeMillis();
	        	                    
	        	                } catch (IOException | InterruptedException ex) {
	        	                    ex.printStackTrace();
	        	                }
	        	                }).start();
	        	            }
	        	        }
	        	    }
	        	});
	          ActionListener searchAction = e -> performSearch(sapi);
	          searchbox.addActionListener(searchAction);
	          searchbtn.addActionListener(searchAction);
	          
	          
	          mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
	        	    @Override
	        	    public void buffering(MediaPlayer mediaPlayer, float newCache) {
	        	        if (newCache >= 80f) {  // Lower threshold for WebM
	        	            SwingUtilities.invokeLater(() -> {
	        	                if (!mediaPlayer.status().isPlaying()) {
	        	                    mediaPlayer.controls().play();
	        	                }
	        	            });
	        	        }
	        	    }
	        	        public void playing(MediaPlayer mediaPlayer) {
	        	            SwingUtilities.invokeLater(() -> seekSlider.setEnabled(true));
	        	        }
	        	        public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
	        	            totalDuration = newLength;
	        	        }
	        	        
	        	        @Override
	        	        
	        	        public void seekableChanged(MediaPlayer mediaPlayer, int seekable) {
	        	            SwingUtilities.invokeLater(() -> {
	        	                seekSlider.setEnabled(seekable == 1);
	        	            });
	        	        }
	
	        	        @Override
	        	        public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
	        	            SwingUtilities.invokeLater(() -> {
	        	                if (!seekSlider.getValueIsAdjusting()) {
	        	                    seekSlider.setValue((int) (newPosition * 1000));
	        	                }
	        	            });
	        	        }
	        	        public void error(MediaPlayer mediaPlayer) {
	        	            SwingUtilities.invokeLater(() -> {
	        	                JOptionPane.showMessageDialog(MusicPlayerGUI.this,
	        	                    "WebM Playback Error\nCheck codec support in your VLC installation",
	        	                    "WebM Error",
	        	                    JOptionPane.ERROR_MESSAGE);
	        	            });
	        	        }
	        	        @Override
	        	        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
	        	            SwingUtilities.invokeLater(() -> {
	        	                if (seekInProgress && Math.abs(newTime - seekTargetTime) < 1000) {
	        	                    seekInProgress = false;
	        	                    if (!seekShouldPlay) {
	        	                        mediaPlayer.controls().pause();
	        	                    }
	        	                }
	        	                
	        	                // Original time label update code
	        	                timeLabel.setText("   "+formatTime(newTime)+
	                            		"                                                   "+
	                            		formatTime(totalDuration));
	        	            });
	        	        }
	        	});
	          
	
	    }
	    
	    public class Playlist {
	        private File playlist;
	        private String name;
	        @Override
	        public String toString() {
	            return name;
	        }
	        public void setName(String name) {
	            this.name = name;
	        }
	        public File getPlaylist() {
	            return playlist;
	        }
	        public String getName() {
	            return name;
	        }
	        public void setPlaylist(File playlist) {
	            this.playlist = playlist;
	        }
	
	
	    }
	    private void createPlaylistFromField() {
	    	playlistFeild.setVisible(true);
	    	String playlistName = playlistFeild.getText().trim();
	        if (!playlistName.isEmpty()) {
	            Playlist newPl = plHandler.createPlaylist(playlistName);
	            if (newPl != null) {
	                playlists.add(newPl);
	                listModel.addElement(newPl);
	                playlistFeild.setText("");
	            }
	        }
	        
	        playlistFeild.setText("");
	        playlistFeild.setVisible(false);
	        addPlaylistButton.setVisible(true);
	    }
	    
	    private void loadPlaylistsAsync() {
	        SwingWorker<Void, Playlist> worker = new SwingWorker<>() {
	            protected Void doInBackground() {
	                plHandler.importPlaylists();
	                for (Playlist p : plHandler.playlists) {
	                    publish(p);
	                }
	                return null;
	            }
	            protected void process(List<Playlist> chunks) {
	                chunks.forEach(listModel::addElement);
	            }
	        };
	        worker.execute();
	    }
	    
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
	            jsonFile.add(newSong);
	            try (Writer writer = Files.newBufferedWriter(filePath)) {
	                gson.toJson(jsonFile, writer);
	            } catch (IOException e) {
	                System.err.println("Error writing the JSON file: " + e.getMessage());
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
	
	    }
	    private void showPlaylists() {
	    	list.clearSelection();
	    	listModel.clear();
	        showingPlaylists = true;
	        backButton.setVisible(false);
	        
	        for (Playlist p : playlists) {
	            listModel.addElement(p);
	        }
	        
	        list.revalidate();
	        list.repaint();
	    }
	
	    private void showSongs(Playlist playlist) throws FileNotFoundException {
	        listModel.clear();
	        showingPlaylists = false;
	        backButton.setVisible(true);
	        for (Song song : plHandler.initialisePlaylist(playlist)) {
	        listModel.addElement(song);
	        }
	    }
	    
	    private boolean isShowingPlaylists() {
	        // In this simple example, if the first element is a Playlist, we assume we're in the playlists view.
	        return !listModel.isEmpty() && listModel.getElementAt(0) instanceof Playlist;
	    }
	    
	    private class CustomCellRenderer extends JPanel implements ListCellRenderer<Object> {
	        private JLabel textLabel;
	        private JButton menuButton;
	        
	        public CustomCellRenderer() {
	            setLayout(new BorderLayout(5, 5));
	            textLabel = new JLabel();
	            menuButton = new JButton("⋮");
	            menuButton.setMargin(new Insets(2,2,2,2));
	            // Optionally, style menuButton as non-opaque and no border
	            menuButton.setFocusable(false);
	            menuButton.setBorderPainted(false);
	            menuButton.setContentAreaFilled(false);
	            add(textLabel, BorderLayout.CENTER);
	            add(menuButton, BorderLayout.EAST);
	        }
	        
	        @Override
	        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
	                boolean isSelected, boolean cellHasFocus) {
	            // If value is the "Back" option, just show it without a menu button.
	            if (value instanceof String && value.equals("Back to Playlists")) {
	                textLabel.setText((String) value);
	                menuButton.setVisible(false);
	            } else {
	                textLabel.setText(value.toString());
	                // For playlists, you may not want a menu button.
	                boolean isSong = value instanceof Song;
	                boolean isBack = value instanceof String && ((String) value).equals("Back to Playlists");
	                menuButton.setVisible(isSong && !isBack);
	            }
	            
	            // Highlight selection
	            if (isSelected) {
	                setBackground(list.getSelectionBackground());
	                setForeground(list.getSelectionForeground());
	                textLabel.setForeground(list.getSelectionForeground());
	            } else {
	                setBackground(list.getBackground());
	                setForeground(list.getForeground());
	                textLabel.setForeground(list.getForeground());
	            }
	            setOpaque(true);
	            return this;
	        }
	    }
	    
	    private void initNative() {
	        NativeDiscovery discovery = new NativeDiscovery();
	        if (!discovery.discover()) {
	            System.setProperty("jna.library.path", "C:/vlc-3.0.20");
	        }
	    }
	    private static void styleIconButton(AbstractButton button) {
	    	button.setBorderPainted(false);
	        button.setContentAreaFilled(false);
	        button.setFocusPainted(false);
	        button.setOpaque(false);
	        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }
	    
	       
	    private void performSearch(APIhandler sapi) {
	    	
	    	new Thread(()->{
	    		model.setRowCount(0);
	    		String userInput = searchbox.getText();
	            try {
	                APIhandler.Song[] songs = sapi.search(userInput);
	                int numSongs = Math.min(3, songs.length);
	                vidIDs = new String[numSongs]; // persist at class-level
	                for (int i = 0; i < numSongs; i++) {
	                    vidIDs[i] = songs[i].getVideoId();
	                    Object[] row = {songs[i].getTitle(), songs[i].getArtist(), songs[i].getDuration()};
	                    // [Threading Change] Use SwingUtilities.invokeLater to update the model safely
	                  SwingUtilities.invokeLater(()-> model.addRow(row));
	                }
	               SwingUtilities.invokeLater(()->{
	                    searchDisplay.revalidate();
	                    searchDisplay.repaint();
	               });
	            } catch (IOException | InterruptedException ex) {
	                ex.printStackTrace();
	            }
	    	}).start();
	        
	    }
	    
	    
	
	
	    public void playSong() {
	    	isPaused=mediaPlayer.controls().start();
	    }
	
	
	    
	        
	    
	    public void resumeSong() {
	        
	            mediaPlayer.controls().play();
	            uiTimer.start();
	            isPaused = false;
	        
	    }
	
	    public void pauseSong() {
	            if (!isPaused) {
	            	uiTimer.stop();
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
	    
	
	    public static ImageIcon createMultiResolutionIcon(String basePath) throws IOException {
	        BufferedImage normalResImage = ImageIO.read(new File(basePath));
	        if (normalResImage == null) {
	            throw new IOException("Failed to load image: " + basePath);
	        }
	        return new ImageIcon(normalResImage);
	    }
	
	    private static BufferedImage loadFallbackImage(String path) {
	        try {
	            return ImageIO.read(new File(path));
	        } catch (IOException e) {
	            // Generate simple fallback
	            BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
	            Graphics2D g = img.createGraphics();
	            g.setColor(Color.WHITE);
	            g.fillRect(0, 0, 64, 64);
	            g.dispose();
	            return img;
	        }
	    }
	
	    private static void configureButtonAppearance(AbstractButton button) {
	        button.setBorderPainted(false);
	        button.setContentAreaFilled(false);
	        button.setFocusPainted(false);
	        button.setOpaque(false);
	    }
	    
	    public static BufferedImage convert(BufferedImage source, int target) {
	        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), target);
	        Graphics2D g2d = image.createGraphics();
	        g2d.drawImage(source, 0, 0, null);  // Draw the source image onto the new image
	        g2d.dispose();
	        return image;
	    }
	
	
	    
	    public static BufferedImage highQualityResize(BufferedImage original, int targetWidth, int targetHeight) {
	        if (original == null) throw new IllegalArgumentException("Original image is null");
	        
	        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g2d = resized.createGraphics();
	
	        // Ultimate quality settings
	        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
	
	        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
	        g2d.dispose();
	        
	        return resized;
	    }
	    
	    
	    private String formatTime(long millis) {
	        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
	        long minutes = seconds / 60;
	        seconds %= 60;
	        return String.format("%02d:%02d", minutes, seconds);
	    }
	 // Modify preloadAndPlay method:
	    public void preloadAndPlay(URL newurl, JSlider seekSlider) {
	        new Thread(() -> {
	            try {
	                mediaPlayer.controls().stop();
	                
	                // Add buffer clearing delay
	                Thread.sleep(300);
	
	                String[] mediaOptions = {
	                    ":no-video",
	                    ":network-caching=3000",
	                    ":demuxer=matroska",
	                    ":audio-codec=opus",
	                    ":file-caching=3000",
	                    ":avcodec-hw=any",
	                    ":input-repeat=0",
	                    ":clock-synchro=0"  // Add this for WebM seeking
	                };
	
	                mediaPlayer.media().startPaused(newurl.toString(), mediaOptions);
	                
	                // Wait for valid media state
	                while (!mediaPlayer.status().isPlayable()) {
	                    Thread.sleep(100);
	                }
	                
	                // Get actual duration
	                totalDuration = mediaPlayer.status().length();
	                
	                SwingUtilities.invokeLater(() -> {
	                    seekSlider.setValue(0);
	                    uiTimer.start();
	                });
	
	                mediaPlayer.controls().play();
	
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }).start();
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
	        public  class Song {
	            private String title;
	            private String videoId;
	            private String artist;
	            private int duration;
	            @Override
	            public String toString() {
	                return title + " - " + artist;
	            }
	            
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
	    
	      
	
	//    public static void main(String[] args) {
	//    	
	//    	JWindow splash = new JWindow();
	//    	
	//        splash.setSize(400, 300); // Set window size
	//        splash.setLocationRelativeTo(null);
	//        
	//        ImageIcon logo = new ImageIcon("D:\\codes\\eclipse\\sampleGUI\\icons\\app_icon.jpg"); // Replace with your logo path
	//        JLabel logoLabel = new JLabel(logo);
	//        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
	//        splash.getContentPane().add(logoLabel);
	//        splash.setVisible(true);
	//        Timer timer=new Timer(3000, e -> {
	//            splash.dispose();
	//        
	//            SwingUtilities.invokeLater(() -> {
	//				try {
	//					new MusicPlayerGUI().setVisible(true);
	//				} catch (IOException e1) {
	//					// TODO Auto-generated catch block
	//					e1.printStackTrace();
	//				}
	//			});
	//        });
	//        timer.setRepeats(false);
	//        timer.start();// Open main UI
	//        
	//        
	//    }
	    public static void main(String[] args) {
	    	SwingUtilities.invokeLater(()->{
	    		try {
					new MusicPlayerGUI().setVisible(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	);
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
