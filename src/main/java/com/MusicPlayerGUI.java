package com;

import java.io.File;
import java.util.concurrent.TimeUnit;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.APIhandler.Song;
public class MusicPlayerGUI extends JFrame {

	static {
		// Workaround for WindowBuilder infinite loop
		if (java.awt.GraphicsEnvironment.isHeadless()) {
			System.setProperty("java.awt.headless", "false");
			System.setProperty("https.protocols", "TLSv1.2");
		}
		System.setProperty("wb.disable.background.designer.thread", "true");
	}

	private long totalDuration;
	private EmbeddedMediaPlayerComponent mediaComponent;
	private EmbeddedMediaPlayer mediaPlayer;
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
	private JLabel nowPlayingLabel;
	private JPanel nowPlayingPanel;
	private JLabel thumbnail;
	private JProgressBar loadingProgress;
	private JPanel thumbnailPanel;
	private JPanel queuePanel;
	private JList<String>  queueList;
	private APIhandler apiHandler =new APIhandler();
	private Timer uiTimer;
	private JSlider seekSlider = new JSlider(0, 1000, 0);
	private volatile boolean seekInProgress = false;
	private volatile long seekTargetTime = -1;
	private volatile boolean seekShouldPlay = true;
	private JScrollPane scrollPane;
	private DefaultListModel<Object> listModel;
	private JPanel playlistPanel;
	private List<Playlist> playlists;
	private JList<Object> list;
	private boolean showingPlaylists = true;
	private playlistHandler plHandler;
	private Playlist currentPlaylistView = null;
	private List<Song> searchResults = new ArrayList<>();
	private songQueue songQueue=new songQueue();
	private JToggleButton showPlaylistbtn;
	private JToggleButton queuebutton;
	private JButton addPlaylistButton;
	private JButton backButton;
	private JTextField playlistField;
	private JToggleButton addSongPl;


	public MusicPlayerGUI() throws IOException {
		super("Swing Music Player");
		getContentPane().setForeground(new Color(192, 192, 192));
		getContentPane().setBackground(new Color(0, 0, 0));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setResizable(false);
		setTitle("Orpheus Player");
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

		// loading all the icons images
		ImageIcon playIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/play_arrow_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		ImageIcon pauseIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/pause_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		ImageIcon prevIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/skip_previous_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		ImageIcon nextIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/skip_next_64dp_FFFFFF.png")),64,64),BufferedImage.TYPE_INT_ARGB));
		ImageIcon showPlaylistIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/playlist_play_64dp_FFFFFF.png")),32,32),BufferedImage.TYPE_INT_ARGB));
		ImageIcon queueIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/queue_music_64dp_FFFFFF.png")),32,32),BufferedImage.TYPE_INT_ARGB));
		ImageIcon searchIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/search_64dp_FFFFFF.png")),24,24),BufferedImage.TYPE_INT_ARGB));
		ImageIcon addPlaylistIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/playlist_add_64dp_FFFFFF.png")),16,16),BufferedImage.TYPE_INT_ARGB));
		ImageIcon addIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/add_circle_64dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png")),32,32),BufferedImage.TYPE_INT_ARGB));
		ImageIcon addedIcon=new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/task_alt_64dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png")),32,32),BufferedImage.TYPE_INT_ARGB));


		// Creating Model for Search Display
		String[] columnNames = {"Title", "Artist", "Duration"};
		model = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // all cells non-editable
			}
		};

		searchDisplay = new JTable(model) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				// Zebra striping
				c.setBackground(row % 2 == 0 ? new Color(45, 45, 45) : new Color(50, 50, 50));
				c.setForeground(Color.WHITE);

				// Hover effect
				if (isRowSelected(row)) {
					c.setBackground(new Color(30, 215, 96)); // Spotify green
					c.setForeground(Color.BLACK);
				}

				// Padding
				((JComponent)c).setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
				return c;
			}
		};

		TableColumnModel columnModel = searchDisplay.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(150); // Title
		columnModel.getColumn(1).setPreferredWidth(120); // Artist
		columnModel.getColumn(2).setPreferredWidth(80);  // Duration
		searchDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		searchDisplay.setShowHorizontalLines(false);
		searchDisplay.setShowVerticalLines(false);
		searchDisplay.setIntercellSpacing(new Dimension(0, 5));
		searchDisplay.setRowHeight(25);
		searchDisplay.setSelectionBackground(new Color(30, 215, 96));
		searchDisplay.setSelectionForeground(Color.BLACK);
		searchDisplay.setBackground(new Color(40, 40, 40));
		searchDisplay.setForeground(Color.WHITE);
		searchDisplay.setBorder(null);
		searchDisplay.getTableHeader().setResizingAllowed(false);
		searchDisplay.getTableHeader().setReorderingAllowed(false);
		searchDisplay.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
														   boolean isSelected, boolean hasFocus, int row, int column) {

				Component c = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);

				((JLabel)c).setHorizontalAlignment(SwingConstants.RIGHT);
				((JLabel)c).setForeground(new Color(150, 150, 150));

				if (isSelected) {
					c.setBackground(new Color(30, 215, 96));
					((JLabel)c).setForeground(Color.BLACK);
				}

				return c;
			}
		});


		// Adding ScrollPane in Search Display
		scrollPane = new JScrollPane(searchDisplay);
		scrollPane.setForeground(new Color(69, 69, 69));
		scrollPane.setBackground(new Color(69, 69, 69));
		scrollPane.setBounds(277, 104, 265, 100);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(new Color(40, 40, 40));
		getContentPane().add(scrollPane);
		scrollPane.setVisible(false);

		// Adding a Header in SearchDsiplay for better UI experience
		JTableHeader header = searchDisplay.getTableHeader();
		header.setBackground(new Color(30, 30, 30));
		header.setForeground(new Color(150, 150, 150));
		header.setFont(new Font("Segoe UI", Font.BOLD, 12));
		header.setBorder(BorderFactory.createEmptyBorder());
		header.setPreferredSize(new Dimension(header.getWidth(), 20));
		searchDisplay.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
														   boolean isSelected, boolean hasFocus, int row, int column) {

				Component c = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);

				// Default styling
				c.setBackground(row % 2 == 0 ?
						new Color(45, 45, 45) :
						new Color(50, 50, 50));

				// Hover effect
				if (table.isRowSelected(row)) {
					c.setBackground(new Color(30, 215, 96));
					c.setForeground(Color.BLACK);
				} else if (table.isRowSelected(row)) {
					c.setBackground(new Color(70, 70, 70));
					c.setForeground(Color.WHITE);
				}

				// Text alignment
				((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
				return c;
			}
		});

		// Adding listeners so that search results Highlight when hovered
		searchDisplay.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = searchDisplay.rowAtPoint(e.getPoint());
				searchDisplay.setRowSelectionInterval(row, row);
			}
		});
		searchDisplay.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int row = searchDisplay.getSelectedRow();
				if (row >= 0) {
					Song song = getSongFromSearchRow(row);
					boolean isInPlaylist = isSongInAnyPlaylist(song);
					addSongPl.setSelected(isInPlaylist);
				}
			}
		});

		// SearchButton initiallization
		searchbtn = new JButton(searchIcon);
		searchbtn.setForeground(new Color(69, 69, 69));
		searchbtn.setBackground(new Color(69, 69, 69));
		styleIconButton(searchbtn);
		searchbtn.setBounds(463, 67, 63, 27);
		getContentPane().add(searchbtn);
		searchbtn.setBackground(new Color(69, 69, 69));
		// SearchBox initialization
		searchbox = new JTextField();
		searchbox.setForeground(new Color(255, 255, 255));
		searchbox.setBounds(319, 60, 155, 34);
		searchbox.setColumns(10);
		searchbox.setBorder(BorderFactory.createLineBorder(new Color(30,215,96)));
		searchbox.setBackground(new Color(59, 59, 59));
		getContentPane().add(searchbox);


		// Play_Pause toggle button initialization
		play_pause = new JToggleButton("");
		play_pause.setBounds(375, 489, 64, 64);
		getContentPane().add(play_pause);
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

		// Previous Button initialization
		previous = new JButton(prevIcon);
		styleIconButton(previous);
		previous.setBounds(319, 504, 56, 32);
		getContentPane().add(previous);
		previous.setBackground(new Color(128, 128, 128));
		previous.addActionListener(e -> previousSong());
		//Necxt Button initialization
		next = new JButton(nextIcon);
		next.setBounds(439, 504, 56, 32);
		styleIconButton(next);
		getContentPane().add(next);
		next.addActionListener(e -> new Thread(() -> {
			try {
				mediaPlayer.controls().stop();
				songQueue.removePlayed();  // Remove current track
				loadingProgress.setVisible(true);
				Song nextSong = songQueue.next();

				if (nextSong != null) {
					URL url = new URL(apiHandler.returnURL(nextSong.getVideoId()));
					preloadAndPlay(url, seekSlider);

					SwingUtilities.invokeLater(() -> {
						play_pause.setSelected(true);
						updateQueueDisplay();
					});
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start());

		// Adding loading Progress Bar for whenever a song is being loaded
		loadingProgress = new JProgressBar();
		loadingProgress.setBounds(265, 555, 288, 5);
		loadingProgress.setIndeterminate(true);
		loadingProgress.setVisible(false);
		loadingProgress.setForeground(new Color(30, 215, 96));
		loadingProgress.setBackground(new Color(60, 60, 60));
		getContentPane().add(loadingProgress);

		// Adding thumbnail Panel for showing  thumbnails of Images
		thumbnailPanel = new JPanel(new BorderLayout());
		thumbnailPanel.setBounds(313, 205, 185, 185);
		thumbnailPanel.setBackground(new Color(0, 0, 0));
		getContentPane().add(thumbnailPanel);
		// Adding Jlabel which contains thumbnails of songs
		thumbnail = new JLabel();
		thumbnail.setBackground(new Color(0, 0, 0));
		thumbnailPanel.add(thumbnail, BorderLayout.CENTER);
		thumbnail.setHorizontalAlignment(JLabel.CENTER);
		thumbnail.setVerticalAlignment(JLabel.CENTER);
		// Adding nowPLayingPanel which contains nowPLayingLabel
		nowPlayingPanel = new JPanel();
		nowPlayingPanel.setBounds(313, 392, 185, 32);
		nowPlayingPanel.setBackground(Color.BLACK);
		getContentPane().add(nowPlayingPanel);
		//  shows the title and artist of current song being played
		nowPlayingLabel = new JLabel("");
		nowPlayingLabel.setBounds(310, 392, 185, 23);
		nowPlayingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nowPlayingLabel.setForeground(Color.WHITE);
		nowPlayingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 8));
		nowPlayingPanel.add(nowPlayingLabel, BorderLayout.CENTER);
		// addsongPL button initiallizes the whole Dialog Box which appears when we click on it to add a song to a playlist
		addSongPl = new JToggleButton(addIcon);
		addSongPl.setSelectedIcon(addedIcon);
		addSongPl.setForeground(new Color(255, 255, 255));
		styleIconButton(addSongPl);
		addSongPl.addActionListener(e -> {
			int row = searchDisplay.getSelectedRow();
			if (row == -1) return;

			Song song = getSongFromSearchRow(row);

			// Dialog styling
			JDialog dialog = new JDialog(this, "Add to Playlists", true);
			dialog.setSize(300, 400);
			dialog.setLocationRelativeTo(this);
			dialog.getContentPane().setBackground(new Color(30, 30, 30));

			// Main content panel
			JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			contentPanel.setBackground(new Color(30, 30, 30));

			// Header
			JLabel headerp = new JLabel("Select Playlists:");
			headerp.setFont(new Font("Segoe UI", Font.BOLD, 16));
			headerp.setForeground(new Color(30, 215, 96));  // Spotify green
			headerp.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			contentPanel.add(headerp, BorderLayout.NORTH);

			// Checkbox panel with scroll
			JPanel checkPanel = new JPanel();
			checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
			checkPanel.setBackground(new Color(40, 40, 40));

			JScrollPane scrollPane = new JScrollPane(checkPanel);
			scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
			scrollPane.getViewport().setBackground(new Color(40, 40, 40));
			contentPanel.add(scrollPane, BorderLayout.CENTER);

			// Checkboxes styling
			UIManager.put("CheckBox.background", new Color(40, 40, 40));
			UIManager.put("CheckBox.foreground", Color.WHITE);
			UIManager.put("CheckBox.focus", new Color(30, 215, 96));
			UIManager.put("CheckBox.select", new Color(30, 215, 96));

			ArrayList<Playlist> selectedPlaylists = new ArrayList<>();
			for (Playlist p : playlists) {
				JCheckBox check = new JCheckBox(p.getName());
				check.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				check.setBackground(new Color(40, 40, 40));
				check.setForeground(Color.WHITE);
				check.setOpaque(true);
				check.setIconTextGap(15);
				check.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

				// Custom checkbox icons
				check.setIcon(createCheckBoxIcon(false));
				check.setSelectedIcon(createCheckBoxIcon(true));

				try {
					check.setSelected(plHandler.isSongInPlaylist(p, song));
				} catch (IOException ex) {
					check.setEnabled(false);
				}
				check.addActionListener(ev -> {
					if (check.isSelected()) {
						selectedPlaylists.add(p);
					} else {
						selectedPlaylists.remove(p);
					}
				});
				checkPanel.add(check);
			}

			// Button panel
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
			buttonPanel.setBackground(new Color(30, 30, 30));

			JButton confirm = new JButton("Add to Playlists");
			confirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
			confirm.setBackground(new Color(30, 215, 96));  // Spotify green
			confirm.setForeground(Color.BLACK);
			confirm.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
			confirm.setFocusPainted(false);

			// Hover effect
			confirm.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					confirm.setBackground(new Color(40, 225, 106));
				}
				public void mouseExited(MouseEvent e) {
					confirm.setBackground(new Color(30, 215, 96));
				}
			});

			confirm.addActionListener(ev -> {
				boolean anyAdded = false;
				for (Playlist p : selectedPlaylists) {
					try {
						if (plHandler.addSong(p, song)) {
							anyAdded = true;
							if (currentPlaylistView != null &&
									currentPlaylistView.getName().equals(p.getName())) {
								showSongs(currentPlaylistView);
							}
						}
					} catch (Exception ex) {
						showError("Error adding to " + p.getName() + ": " + ex.getMessage());
					}
				}
				if (anyAdded) {
					showToast("Added to selected playlists!");
				}
				dialog.dispose();
			});

			buttonPanel.add(confirm);
			contentPanel.add(buttonPanel, BorderLayout.SOUTH);

			dialog.setContentPane(contentPanel);
			dialog.setVisible(true);

			SwingUtilities.invokeLater(() -> {
				addSongPl.setSelected(isSongInAnyPlaylist(song));
			});
		});
		addSongPl.setBounds(505, 504, 32, 32);
		getContentPane().add(addSongPl);


		// Adding a layered Pane for Seeking capabilities
		JLayeredPane layeredSeekPane = new JLayeredPane();
		layeredSeekPane.setBounds(265, 430, 288, 64);
		getContentPane().add(layeredSeekPane);

		timeLabel = new JLabel("   00:00                                                            00:00");
		timeLabel.setBackground(new Color(0, 0, 0));
		timeLabel.setForeground(new Color(255, 255, 255));
		timeLabel.setBounds(0, 34, 286, 20);
		layeredSeekPane.add(timeLabel, Integer.valueOf(2));

		seekSlider.setBackground(new Color(0, 0, 0));
		seekSlider.setPaintTicks(true);
		seekSlider.setPaintLabels(true);
		seekSlider.setEnabled(false);
		seekSlider.setBounds(0, 20, 286, 34);
		seekSlider.setEnabled(false);
		layeredSeekPane.add(seekSlider, Integer.valueOf(1));

		//Adding the PLaylist Panel
		playlistPanel = new JPanel(new BorderLayout(10, 10));
		playlistPanel.setBackground(new Color(30, 30, 30));
		playlistPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		playlistPanel.setBounds(10, 111, 227, 400);  // adjust as needed
		playlistPanel.setVisible(false);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(new Color(40, 40, 40));
		playlistPanel.add(headerPanel, BorderLayout.NORTH);

		//Initiallizing the PLaylist Panel
		//Running importPlaylist to get all th playlists stored
		plHandler=new playlistHandler();
		plHandler.importPlaylists();
		playlists= new ArrayList<>();
		playlists=plHandler.playlists;
		// Creating a list Model for songs and Playlists
		listModel = new DefaultListModel<>();
		list= new JList<>(listModel);
		list.setForeground(new Color(255, 255, 255));
		list.setBackground(new Color(69, 69, 69));
		list.setBounds(10, 50, 207, 340);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new CustomCellRenderer());

		//Adding a scrollpane in list
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBorder(UIManager.getBorder("TextField.border"));
		listScrollPane.setBackground(new Color(69, 69, 69));
		listScrollPane.setBounds(10, 50, 207, 340);
		playlistPanel.add(listScrollPane, BorderLayout.CENTER);

		//Running ShowPLaylists method on Swing EDT
		SwingUtilities.invokeLater(() -> {
			showPlaylists();
		});
		getContentPane().add(playlistPanel);
		//Adding addPlaylist Button
		addPlaylistButton = new JButton(addPlaylistIcon);
		styleIconButton(addPlaylistButton);
		addPlaylistButton.setBounds(177, 13, 40, 27);
		//Playlist feild text to enter the playlistname to be created
		playlistField = new JTextField();
		playlistField.setBounds(81, 9, 136, 19);
		playlistPanel.add(playlistField);
		playlistField.setColumns(10);
		playlistField.setVisible(false);
		playlistField.setText("");

		playlistField.addActionListener(e -> {
			createPlaylistFromField();
		});
		playlistField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					playlistField.setText("");
					playlistField.setVisible(false);
					addPlaylistButton.setVisible(true);
				}
			}
		});
		playlistField.setVisible(true);
		playlistField.setText("");
		//Title Label to show Playlist
		JLabel titleLabel = new JLabel("Playlists");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
		titleLabel.setForeground(new Color(30, 215, 96)); // Spotify green
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		playlistField.setVisible(false);
		headerPanel.add(titleLabel, BorderLayout.WEST);
		//Adding backbutton to navigate to playlist list from songs list
		backButton = new JButton("←");
		styleIconButton(backButton);
		backButton.setForeground(new Color(30, 215, 96));
		backButton.setContentAreaFilled(false);
		backButton.setVisible(false);
		backButton.addActionListener(e ->{
			showPlaylists();
		});
		headerPanel.add(backButton, BorderLayout.CENTER);
		headerPanel.add(addPlaylistButton, BorderLayout.EAST);

		addPlaylistButton.addActionListener(e -> {
			JDialog createDialog = new JDialog(this, "New Playlist", true);
			createDialog.getContentPane().setBackground(new Color(30, 30, 30));

			// Main content panel
			JPanel content = new JPanel(new BorderLayout(15, 15));
			content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			content.setBackground(new Color(30, 30, 30));

			// Header
			JLabel headert = new JLabel("Playlist Name:");
			headert.setFont(new Font("Segoe UI", Font.BOLD, 16));
			headert.setForeground(new Color(30, 215, 96)); // Spotify green
			headert.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			content.add(headert, BorderLayout.NORTH);

			// Input field
			JTextField nameField = new JTextField();
			nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
			nameField.setForeground(Color.WHITE);
			nameField.setBackground(new Color(40, 40, 40));
			nameField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(60, 60, 60)),
					BorderFactory.createEmptyBorder(8, 12, 8, 12)
			));
			content.add(nameField, BorderLayout.CENTER);

			// Create button
			JButton createButton = new JButton("Create Playlist");
			createButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
			createButton.setBackground(new Color(30, 215, 96)); // Spotify green
			createButton.setForeground(Color.BLACK);
			createButton.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(20, 195, 86)),
					BorderFactory.createEmptyBorder(8, 25, 8, 25)
			));
			createButton.setFocusPainted(false);

			// Hover effects
			createButton.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					createButton.setBackground(new Color(40, 225, 106));
					createButton.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createLineBorder(new Color(30, 205, 96)),
							BorderFactory.createEmptyBorder(8, 25, 8, 25)
					));
				}
				public void mouseExited(MouseEvent e) {
					createButton.setBackground(new Color(30, 215, 96));
					createButton.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createLineBorder(new Color(20, 195, 86)),
							BorderFactory.createEmptyBorder(8, 25, 8, 25)
					));
				}
			});

			createButton.addActionListener(ev -> {
				String name = nameField.getText().trim();
				if (!name.isEmpty()) {
					Playlist newPl = plHandler.createPlaylist(name);
					if (newPl != null) {
						playlists.add(newPl);
						listModel.addElement(newPl);
						createDialog.dispose();
						showToast("Playlist created!");
					}
				}
			});

			// Button panel
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.setBackground(new Color(30, 30, 30));
			buttonPanel.add(createButton);
			content.add(buttonPanel, BorderLayout.SOUTH);

			// Dialog setup
			createDialog.setContentPane(content);
			createDialog.getRootPane().setDefaultButton(createButton);
			createDialog.pack();
			createDialog.setLocationRelativeTo(this);
			createDialog.setVisible(true);
		});
		// When a playlist is selected from the JList  updating main view and hiding panel.
		list.setBackground(new Color(50, 50, 50));
		list.setForeground(Color.WHITE);
		list.setSelectionBackground(new Color(30, 215, 96));
		list.setSelectionForeground(Color.BLACK);
		list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {  // Changed to mousePressed for single-click
				int index = list.locationToIndex(e.getPoint());
				if (index == -1) return;

				Rectangle cellBounds = list.getCellBounds(index, index);
				Point relativePoint = new Point(e.getX() - cellBounds.x, e.getY() - cellBounds.y);

				// Handle menu button click (single-click)
				CustomCellRenderer renderer = (CustomCellRenderer) list.getCellRenderer();
				if (renderer.getMenuButton().getBounds().contains(relativePoint)) {
					showSongContextMenu(list, index, e.getX(), e.getY());
					return;  // Exit after handling menu button
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// Handling double-click separately
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					if (index == -1) return;

					if (showingPlaylists) {
						showSongs(playlists.get(index));
					} else {
						// Handle song double-click
						Object selectedValue = list.getModel().getElementAt(index);
						if (selectedValue instanceof APIhandler.Song) {
							APIhandler.Song song = (APIhandler.Song) selectedValue;
							new Thread(() -> playSelectedSong(song)).start();
						}
					}
				}
			}
			//Method to play the selected song
			private void playSelectedSong(APIhandler.Song song) {
				SwingUtilities.invokeLater(() ->{
					loadingProgress.setVisible(true);
					loadingProgress.setIndeterminate(true);
				});
				try {
					mediaPlayer.controls().stop();
					URL songUrl = new URL(apiHandler.returnURL(song.getVideoId()));

					SwingUtilities.invokeLater(() -> {
						if (!songQueue.isEmpty()) songQueue.remove(0);
						songQueue.addFirst(song);
						updateThumbnail(song.getThumbnailUrl());
						updateQueueDisplay();
						nowPlayingLabel.setText("<html><center>" + song.getTitle() + "<br>-- " + song.getArtist() + "</center></html>");
						play_pause.setSelected(true);
						timeLabel.setText("   00:00                                                            " +
								formatTime(song.getDuration() * 1000L));
					});

					preloadAndPlay(songUrl, seekSlider);
					System.currentTimeMillis();

				} catch (IOException | InterruptedException ex) {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(MusicPlayerGUI.this,
								"Error playing song: " + ex.getMessage(),
								"Playback Error",
								JOptionPane.ERROR_MESSAGE);
					});
				}
			}
		});
		//listener to show playlist context menu
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int index = list.locationToIndex(e.getPoint());
					if (index != -1) {
						showPlaylistContextMenu(index, e.getX(), e.getY());
					}
				}
			}
		});
		list.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				list.setSelectedIndex(index);
			}
		});
		//Creating the queuePanel
		queuePanel = new JPanel(new BorderLayout());
		queuePanel.setBackground(new Color(30, 30, 30));
		queuePanel.setBounds(576, 111, 200, 400);
		queuePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 60, 60)),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)
		));
		queuePanel.setVisible(false);
		// Header with clear button
		JPanel headerq = new JPanel(new BorderLayout());
		headerq.setBackground(new Color(45, 45, 45));
		headerq.setForeground(new Color(30, 30, 30));
		JLabel title = new JLabel("Up Next");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(new Font("Segoe UI", Font.BOLD, 10));
		title.setForeground(new Color(30, 215, 96));
		title.setBackground(new Color(45, 45, 45));

		JButton clearButton = new JButton("Clear");
		styleIconButton(clearButton);
		clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		clearButton.setBackground(new Color(255, 255, 255));
		clearButton.setForeground(new Color(0, 0, 0));
		clearButton.addActionListener(e -> {
			songQueue.clear();
			updateQueueDisplay();
		});

		headerq.add(title, BorderLayout.CENTER);
		headerq.add(clearButton, BorderLayout.EAST);

		// Queue list
		queueList = new JList<>();
		queueList.setModel(new DefaultListModel<>());
		queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queueList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
														  int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (index == songQueue.getCurrentIndex()) {
					c.setBackground(new Color(30, 215, 96));
					c.setForeground(Color.BLACK);
				} else {
					c.setBackground(new Color(60, 60, 60));
					c.setForeground(Color.WHITE);
				}
				return c;
			}
		});
		queueList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		queueList.setBackground(new Color(45, 45, 45));
		queueList.setForeground(Color.WHITE);
		queueList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int index = queueList.locationToIndex(e.getPoint());
					if (index != -1) {
						showQueueContextMenu(index, e.getX(), e.getY());
					}
				}
			}
		});
		JScrollPane scroll = new JScrollPane(queueList);
		scroll.setBorder(null);

		queuePanel.add(headerq, BorderLayout.NORTH);
		queuePanel.add(scroll, BorderLayout.CENTER);
		getContentPane().add(queuePanel);

		//Toggle Button to show the playlist panel
		showPlaylistbtn = new JToggleButton(showPlaylistIcon);
		showPlaylistbtn.setBackground(new Color(69, 69, 69));
		styleIconButton(showPlaylistbtn);
		showPlaylistbtn.setBounds(33, 521, 32, 32);
		getContentPane().add(showPlaylistbtn);
		showPlaylistbtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(showPlaylistbtn.isSelected())
					playlistPanel.setVisible(true);
				else playlistPanel.setVisible(false);
			}
		});
		//Toggle Button to show queuePanel
		queuebutton = new JToggleButton(queueIcon);

		queuebutton.addActionListener(e ->
				queuePanel.setVisible(queuebutton.isSelected())
		);
		styleIconButton(queuebutton);
		queuebutton.setBounds(737, 521, 32, 32);
		getContentPane().add(queuebutton);

		// Listen for slider adjustments to seek in the media
		seekSlider.addMouseListener(new MouseAdapter() {
			private boolean wasPlaying;

			@Override
			public void mousePressed(MouseEvent e) {
				wasPlaying = mediaPlayer.status().isPlaying();
				if (wasPlaying) {
					mediaPlayer.controls().pause();
					mediaPlayer.audio().setMute(true);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!seekSlider.getValueIsAdjusting()) {
					float position = seekSlider.getValue() / 1000f;
					long targetTime = (long) (position * totalDuration);
					mediaPlayer.audio().setMute(false);
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
							"                                                            "+
							formatTime(duration));

				});
			}
		});
		uiTimer.start();

		//Adding searchDisplay listenr to play song when selected
		searchDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int selectedRow = searchDisplay.getSelectedRow();
					if (selectedRow == -1 || vidIDs == null || selectedRow >= vidIDs.length) return;
					Song song=searchResults.get(selectedRow);

					new Thread(() ->
							playSong(song, false)).start();
				}
			}
		});
		ActionListener searchAction = e -> performSearch(apiHandler);
		//SearchBox and SearchButton listenr
		searchbox.addActionListener(searchAction);
		searchbtn.addActionListener(searchAction);

		//the mediaPLayer events listener for various operations
		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {


			@Override
			public void opening(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					loadingProgress.setVisible(true);
				});
			}


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
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					loadingProgress.setVisible(false);
					play_pause.setSelected(true);
					seekSlider.setEnabled(true);
					Song currentSong = songQueue.getCurrent();
					if (currentSong != null) {
						updateThumbnail(currentSong.getThumbnailUrl());
					}


				});
			}
			@Override
			public void paused(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					play_pause.setSelected(false);

				});
			}
			@Override
			public void stopped(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					play_pause.setSelected(false);
					mediaPlayer.audio().setMute(true);
				});
			}
			@Override
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
			@Override
			public void error(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(MusicPlayerGUI.this,
							"WebM Playback Error\nCheck codec support in your VLC installation"
									+ " ",
							"WebM Error",
							JOptionPane.ERROR_MESSAGE);
					loadingProgress.setVisible(false);
					thumbnail.setIcon(createFallbackIcon());
				});
			}
			@Override
			public void finished(MediaPlayer mediaPlayer) {
				autoPlayNext();
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
							"                                                            "+
							formatTime(totalDuration));
				});

			}
		});


	}



	//Method to format time
	private String formatTime(long millis) {
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		return String.format("%02d:%02d", minutes, seconds);
	}
	//Method preloadAndPlay : to load a song from url
	private void preloadAndPlay(URL newurl, JSlider seekSlider) {
		new Thread(() -> {
			try {
				mediaPlayer.controls().stop();
				// Adding buffer clearing delay
				Thread.sleep(300);
				//mediaOptions to prepare it for particularly WEBM playing
				String[] mediaOptions = {
						":no-video",
						":network-caching=3000",
						":demuxer=matroska",
						":audio-codec=opus",
						":file-caching=3000",
						":avcodec-hw=any",
						":input-repeat=0",
						":clock-synchro=0"
				};


				Song song=songQueue.getCurrent();
				mediaPlayer.media().startPaused(newurl.toString(), mediaOptions);
				while (!mediaPlayer.status().isPlayable()) {
					Thread.sleep(100);
				}
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

	//method to get current song from URL
	private APIhandler.Song getCurrentSongFromURL(URL url) {
		String videoId = url.toString().split("v=")[1];
		return songQueue.getQueue().stream()
				.filter(song -> song.getVideoId().equals(videoId))
				.findFirst()
				.orElse(null);
	}

	//method to load the thumbnail of a song
	private void updateThumbnail(String thumbnailUrl) {
		new SwingWorker<ImageIcon, Void>() {
			@Override
			protected ImageIcon doInBackground() {
				// Use 720x720 crop scaled down to 180x180 (adjust targetSize as needed)
				return getProcessedThumbnail(thumbnailUrl, 180);

			}

			@Override
			protected void done() {
				try {
					thumbnail.setIcon(get());
				} catch (Exception e) {
					thumbnail.setIcon(new ImageIcon("placeholder.jpg"));
				}
			}
		}.execute();
	}
	private ImageIcon createFallbackIcon() {
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();

		// Drawing music note icon
		g2d.setColor(new Color(80, 80, 80));
		g2d.fillRoundRect(0, 0, 200, 200, 20, 20);
		g2d.setColor(new Color(30, 215, 96));
		g2d.setFont(new Font("Arial", Font.BOLD, 48));
		g2d.drawString("♫", 76, 140);

		g2d.dispose();
		return new ImageIcon(img);
	}

	private void updateSearchResults(Song[] songs) {
		SwingUtilities.invokeLater(() -> {
			model.setRowCount(0);
			vidIDs=new String[songs.length];
			for (int i=0;i<songs.length;i++) {
				vidIDs[i]=songs[i].getVideoId();
				model.addRow(new Object[]{
						songs[i].getTitle(),
						songs[i].getArtist(),
						formatTime(songs[i].getDuration()* 1000L)
				});
			}
		});
	}

	//Method to show error of a particular meassage
	private void showError(String message) {
		JOptionPane.showMessageDialog(this,
				message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	//Method to show toast(a banner of sort)
	private void showToast(String message) {
		JDialog toast = new JDialog();
		toast.setUndecorated(true);
		JLabel label = new JLabel(message);
		label.setForeground(Color.WHITE);
		label.setBackground(new Color(60, 60, 60));
		label.setOpaque(true);
		toast.getContentPane().add(label);
		toast.pack();
		toast.setLocationRelativeTo(this);

		Timer timer = new Timer(2000, e -> toast.dispose());
		timer.setRepeats(false);
		timer.start();
		toast.setVisible(true);
	}
	//Method to show timed toast of particular timing
	private void showTimedToast(String message, int duration) {
		JWindow toast = new JWindow();
		toast.getContentPane().setLayout(new BorderLayout());

		JLabel label = new JLabel(message);
		label.setForeground(Color.WHITE);
		label.setBackground(new Color(60, 60, 60));
		label.setOpaque(true);
		label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		toast.getContentPane().add(label);
		toast.pack();
		toast.setLocationRelativeTo(this);
		toast.setOpacity(0.9f);

		Timer timer = new Timer(duration, e -> {
			toast.dispose();
		});
		timer.setRepeats(false);
		timer.start();
		toast.setVisible(true);
	}
	//Method to get song from search row of Search Display
	private APIhandler.Song getSongFromSearchRow(int row) {
		if (row >= 0 && row < searchResults.size()) {
			return searchResults.get(row);
		}
		return null;
	}

	//Method to check if song is in any playlist
	private boolean isSongInAnyPlaylist(Song song) {
		try {
			for (Playlist p : playlists) {
				if (plHandler.isSongInPlaylist(p, song)) {
					return true;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	//Method to return duration in int format convertewd from String
	public int parseDuration(String duration) {
		try {
			String[] parts = duration.split(":");
			int minutes = Integer.parseInt(parts[0]);
			int seconds = Integer.parseInt(parts[1]);
			return (minutes * 60) + seconds;
		} catch (Exception e) {
			return 0; // Fallback value
		}
	}

	//Methid ti create a playlist from textfeild
	private void createPlaylistFromField() {
		String playlistName = playlistField.getText().trim(); // Get current text
		if (playlistName.equalsIgnoreCase("Liked Songs")) {
			JOptionPane.showMessageDialog(null,
					"Liked Songs is a reserved name!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!playlistName.isEmpty()) {
			Playlist newPl = plHandler.createPlaylist(playlistName);
			if (newPl != null) {
				playlists.add(newPl);
				listModel.addElement(newPl);
			}
		}
		playlistField.setText("");
		playlistField.setVisible(false);
		addPlaylistButton.setVisible(true);
	}
	//Method to remove a song from queue
	private void removeFromQueue(int index) {
		if (index < 0 || index >= songQueue.getQueue().size()) return;

		// Adjust current index if needed
		if (index < songQueue.getCurrentIndex()) {
			songQueue.setCurrentIndex(songQueue.getCurrentIndex() - 1);
		}
		else if (index == songQueue.getCurrentIndex()) {
			// If removing currently playing song
			mediaPlayer.controls().stop();
			songQueue.setCurrentIndex(-1);
			play_pause.setSelected(false);
			updateQueueDisplay();
		}

		// Remove from actual queue
		songQueue.remove(index);

		// Updating UI
		updateQueueDisplay();

		// Show confirmation
		showTimedToast("Removed from queue", 1000);
	}
	//method of creating the playlist context menu
	private void showPlaylistContextMenu(int index, int x, int y) {
		if (index < 0 || index >= playlists.size()) return;

		JPopupMenu menu = new JPopupMenu();
		styleContextMenu(menu);

		// Delete Playlist
		JMenuItem deleteItem = new JMenuItem("Delete Playlist");
		deleteItem.addActionListener(e -> deletePlaylist(index));
		styleMenuItem(deleteItem);

		// Rename Playlist
		JMenuItem renameItem = new JMenuItem("Rename");
		renameItem.addActionListener(e -> renamePlaylist(index));
		styleMenuItem(renameItem);

		menu.add(deleteItem);
		menu.add(renameItem);
		menu.show(list, x, y);
	}
	//method to delete a playlist(UI enabled)
	private void deletePlaylist(int index) {
		Playlist playlist = playlists.get(index);
		int confirm = JOptionPane.showConfirmDialog(this,
				"Delete playlist '" + playlist.getName() + "'?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				// Delete from filesystem
				if (playlist.getPlaylist().delete()) {
					// Update UI and model
					playlists.remove(index);
					listModel.remove(index);
					plHandler.importPlaylists(); // Refresh playlist data
					showPlaylists();
					showToast("Playlist deleted");
				}
			} catch (Exception ex) {
				showError("Failed to delete playlist: " + ex.getMessage());
			}
		}
	}
	//Method to rename a playlist
	private void renamePlaylist(int index) {
		if (index < 0 || index >= playlists.size()) return;

		Playlist playlist = playlists.get(index);
		String oldName = playlist.getName();

		// Show input dialog
		String newName = JOptionPane.showInputDialog(
				this,
				"Enter new playlist name:",
				"Rename Playlist",
				JOptionPane.PLAIN_MESSAGE
		);

		if (newName == null || newName.trim().isEmpty() || newName.equals(oldName)) {
			return; // Cancel or invalid input
		}

		newName = newName.trim();

		try {
			boolean success = plHandler.renamePlaylist(playlist, newName);

			if (success) {
				// Update UI
				playlists.set(index, playlist);
				listModel.setElementAt(playlist, index);
				showToast("Playlist renamed to: " + newName);

				// Refresh if viewing this playlist
				if (currentPlaylistView != null &&
						currentPlaylistView.getName().equals(oldName)) {
					currentPlaylistView.setName(newName);
				}
			}
		} catch (Exception ex) {
			showError("Failed to rename playlist: " + ex.getMessage());
		}
	}
	//method to crate Queue context menu
	private void showQueueContextMenu(int index, int x, int y) {
		JPopupMenu menu = new JPopupMenu();
		styleContextMenu(menu);  // Apply styling

		JMenuItem removeItem = new JMenuItem("Remove from Queue");
		removeItem.addActionListener(e -> removeFromQueue(index));
		menu.add(removeItem);

		// Add more items if needed
		JMenuItem playNextItem = new JMenuItem("Play Next");
		playNextItem.addActionListener(e -> moveToTop(index));
		menu.add(playNextItem);

		menu.show(queueList, x, y);
	}
	//styling context menu(Utility method)
	private void styleContextMenu(JPopupMenu menu) {
		menu.setBackground(new Color(50, 50, 50));
		menu.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));
		UIManager.put("MenuItem.selectionBackground", new Color(30, 215, 96));
		UIManager.put("MenuItem.selectionForeground", Color.BLACK);
		SwingUtilities.updateComponentTreeUI(menu);
	}
	//styling menu item(Utility method)
	private void styleMenuItem(JMenuItem item) {
		item.setBackground(new Color(60, 60, 60));
		item.setForeground(Color.WHITE);
		item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		item.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
	}
	//method to check if song is in current playlist
	private boolean isSongInCurrentPlaylist(Song song) {
		try {
			return currentPlaylistView != null &&
					plHandler.isSongInPlaylist(currentPlaylistView, song);
		} catch (IOException e) {
			return false;
		}
	}
	//Method to showPlaylists(UI enabled)
	private void showPlaylists() {
		SwingUtilities.invokeLater(() -> {
			backButton.setVisible(false);
			showingPlaylists = true;
			listModel.clear();
			plHandler.importPlaylists(); // Force refresh from disk

			for (Playlist p : plHandler.playlists) {
				listModel.addElement(p);
			}

			list.revalidate();
			list.repaint();
		});
	}
	//Method to show songs(UI enabled)
	private void showSongs(Playlist playlist) {
		SwingUtilities.invokeLater(() -> {
			try {
				backButton.setVisible(true);
				currentPlaylistView = playlist;
				showingPlaylists = false;
				listModel.clear();

				Song[] songs = plHandler.initialisePlaylist(playlist);
				if (songs.length == 0) {
					listModel.addElement("Empty playlist --Add some SONGS!!!");
				} else {
					for (Song song : songs) {
						if (song != null) {
							listModel.addElement(song);
						}
					}
				}

				list.revalidate();
				list.repaint();
			} catch (Exception e) {
				showError("Error loading playlist: " + e.getMessage());
			}
		});
	}
	//Method to to remove a song fromplaylist(UI enabled)
	private void removeFromPlaylist(APIhandler.Song song) {
	    int confirm = JOptionPane.showConfirmDialog(MusicPlayerGUI.this,
	            "Remove '" + song.getTitle() + "' from playlist?",
	            "Confirm Removal",
	            JOptionPane.YES_NO_OPTION);

	    if (confirm == JOptionPane.YES_OPTION) {
	        try {
	            Playlist currentPlaylist = currentPlaylistView;
	            plHandler.deleteSongFromPlaylist(currentPlaylist, song);
	            showSongs(currentPlaylist);
	            showToast("Song removed from playlist");
	        } catch (Exception ex) {
	            showError("Error removing song: " + ex.getMessage());
	        }
	    }
	}
	//method to add a song to queue
	private void addToQueue(APIhandler.Song song) {
		if (songQueue == null) {
			songQueue = new songQueue();
		}

		if (song != null) {
			songQueue.add(song);
			SwingUtilities.invokeLater(() -> {
				updateQueueDisplay();
				showTimedToast("Added to queue: " + song.getTitle(), 1500);
			});
		} else {
			showError("Invalid song selection");
		}
	}

	//Method to update queue display
	private void updateQueueDisplay() {
		SwingUtilities.invokeLater(() -> {
			DefaultListModel<String> model = new DefaultListModel<>();
			if (songQueue != null && !songQueue.isEmpty()) {
				// Add current song
				Song current = songQueue.getCurrent();
				if (current != null) {
					model.addElement("<html><b>Now Playing:</b> " + current.getTitle() + "</html>");
				}

				// Add upcoming songs with numbering
				int counter = 1;
				for (int i = songQueue.getCurrentIndex() + 1; i < songQueue.getQueue().size(); i++) {
					Song s = songQueue.getQueue().get(i);
					if (s != null) {
						model.addElement("<html><font color='#666666'>" + counter++ + ".</font> " + s.getTitle() + "</html>");
					}
				}
			} else {
				model.addElement("<html><i>Queue is empty</i></html>");
			}

			queueList.setModel(model);
			queueList.setFixedCellHeight(40);
			queueList.revalidate();
			queueList.repaint();
			if (songQueue.getCurrentIndex() >= 0) {
				queueList.ensureIndexIsVisible(songQueue.getCurrentIndex());
			}
		});
	}

	//Method to create song context menu
	private void showSongContextMenu(JList<?> list, int index, int x, int y) {
		Object value = list.getModel().getElementAt(index);
		if (!(value instanceof APIhandler.Song)) return;

		APIhandler.Song song = (APIhandler.Song) value;
		JPopupMenu popupMenu = new JPopupMenu();
		styleContextMenu(popupMenu);
		// Add to Queue
		JMenuItem queueItem = new JMenuItem("Add to Queue");
		queueItem.setBackground(new Color(69,69,69));
		queueItem.setForeground(Color.WHITE);
		queueItem.addActionListener(evt -> addToQueue(song));
		popupMenu.add(queueItem);

		JMenu addToPlaylistMenu = new JMenu("Add to Playlist");
		addToPlaylistMenu.setForeground(Color.WHITE);
		addToPlaylistMenu.setBackground(new Color(69, 69,69));
		addToPlaylistMenu.setForeground(Color.WHITE);
		addToPlaylistMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		PlaylistMenu(addToPlaylistMenu, song);
		// Remove from Playlist
		JMenuItem removeItem = new JMenuItem("Remove from Playlist");
		removeItem.setBackground(new Color(69,69,69));
		removeItem.setForeground(Color.WHITE);
		removeItem.addActionListener(evt -> removeFromPlaylist(song));
		popupMenu.add(removeItem);
		popupMenu.add(queueItem);
		popupMenu.add(addToPlaylistMenu);
		popupMenu.addSeparator();
		popupMenu.add(removeItem);

		popupMenu.show(list, x, y);
	}
	//method to create playlistmenu to add a song to playlist through contextmenu
	private void PlaylistMenu(JMenu menu, Song song) {
		for (Playlist p : playlists) {
			if (p.equals(currentPlaylistView)) continue;

			JMenuItem item = new JMenuItem(p.getName());
			item.addActionListener(e -> {
				if (plHandler.addSong(p, song)) {
					showTimedToast("Added to " + p.getName(), 1500);
				}
			});
			styleMenuItem(item);
			menu.add(item);
		}
	}
	//method to move a song to top of queue
	private void moveToTop(int index) {
		if (index < 0 || index >= songQueue.getQueue().size()) return;

		songQueue.moveToTop(index);
		updateQueueDisplay();
		showTimedToast("Moved to play next", 1000);
	}

	//method to customly render cells of list (PlaylistPanel)
	private class CustomCellRenderer extends JPanel implements ListCellRenderer<Object> {
		private JLabel textLabel1;
		private JLabel textLabel2;
		private JToggleButton playlistToggle;
		private JButton menuButton;
		public CustomCellRenderer() {
			setLayout(new BorderLayout(5, 5));
			setOpaque(true);

			textLabel1 = new JLabel();
			playlistToggle = new JToggleButton("♥");
			playlistToggle.setPreferredSize(new Dimension(30, 20));
			styleIconButton(playlistToggle);

			add(textLabel1, BorderLayout.CENTER);
			add(playlistToggle, BorderLayout.EAST);

			textLabel2 = new JLabel();
			menuButton = new JButton("::");
			styleIconButton(menuButton);
			menuButton.setMargin(new Insets(2,2,2,2));

			menuButton.setFocusable(false);
			menuButton.setBorderPainted(false);
			menuButton.setContentAreaFilled(false);
			add(textLabel2, BorderLayout.CENTER);
			add(menuButton, BorderLayout.EAST);
		}
		public JButton getMenuButton() {
			return menuButton;
		}
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {


			if (value instanceof String && value.equals("Back to Playlists")) {
				textLabel2.setText((String) value);
				menuButton.setVisible(false);
			} else {
				textLabel2.setText(value.toString());
				// For playlists, you may not want a menu button.
				boolean isSong = value instanceof Song;
				boolean isBack = value instanceof String && ((String) value).equals("Back to Playlists");
				menuButton.setVisible(isSong && !isBack);
			}

			// Highlight selection
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				textLabel2.setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				textLabel2.setForeground(list.getForeground());
			}
			if (value instanceof Playlist) {
				textLabel1.setText(value.toString());
				playlistToggle.setVisible(false);
			} else if (value instanceof Song) {
				Song song = (Song) value;
				textLabel1.setText(song.getTitle() + " - " + song.getArtist());
				playlistToggle.setVisible(true);
				playlistToggle.setSelected(isSongInCurrentPlaylist(song));
			}
			setOpaque(true);
			setBackground(isSelected ? new Color(69, 69, 69) : new Color(51, 51, 51));
			textLabel1.setForeground(isSelected ? Color.WHITE : new Color(200, 200, 200));
			return this;

		}
	}
	//Method to style buttons
	private void styleIconButton(AbstractButton button) {
		button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setBackground(new Color(30, 215, 96));
		button.setForeground(Color.BLACK);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
		button.setFont(new Font("Segoe UI", Font.BOLD, 12));

		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setBackground(new Color(40, 225, 106));
			}
			public void mouseExited(MouseEvent e) {
				button.setBackground(new Color(30, 215, 96));
			}
		});
	}

	//Method to perform search through searchbox,searchbutton
	private void performSearch(APIhandler sapi) {
		new Thread(() -> {
			searchDisplay.setVisible(true);
			model.setRowCount(0); // Clear existing data
			String userInput = searchbox.getText().trim();

			if (userInput.isEmpty()) {
				SwingUtilities.invokeLater(() -> scrollPane.setVisible(false));
				return;
			}

			try {
				APIhandler.Song[] songs = sapi.search(userInput);
				searchResults.clear();
				searchResults.addAll(Arrays.asList(songs));



				SwingUtilities.invokeLater(() -> {
					if (songs.length > 0) {
						updateSearchResults(songs);
						scrollPane.setVisible(true);
					} else {
						scrollPane.setVisible(false);
					}
					searchDisplay.revalidate();
					searchDisplay.repaint();
				});

			} catch (IOException | InterruptedException ex) {
				SwingUtilities.invokeLater(() -> showError("Search failed: " + ex.getMessage()));
				ex.printStackTrace();
			}
		}).start();
	}
	//method to playsong which passes song and and a boolean(of if song is currently in Queue)
	private void playSong(Song song, boolean fromQueue) {
		SwingUtilities.invokeLater(() -> {
			loadingProgress.setVisible(true);
			loadingProgress.setIndeterminate(true);
		});

		try {
			if(!fromQueue) {
//	                songQueue.clear();
				if(!songQueue.isEmpty()) songQueue.remove(0);
				songQueue.addFirst(song);
			}

			mediaPlayer.controls().stop();
			APIhandler api=new APIhandler();
			URL url = new URL(api.returnURL(song.getVideoId()));

			SwingUtilities.invokeLater(() -> {
				play_pause.setSelected(true);
				updateThumbnail(song.getThumbnailUrl());
				nowPlayingLabel.setText("<html><center>" + song.getTitle() + "<br>--" + song.getArtist() + "</center></html>");
				loadingProgress.setVisible(false);
				timeLabel.setText("   "+formatTime(0) +
						"                                                            "
						+ formatTime(song.getDuration() * 1000L));
				updateQueueDisplay();
			});

			preloadAndPlay(url, seekSlider);

		} catch (Exception ex) {
			SwingUtilities.invokeLater(() -> {
				loadingProgress.setVisible(false);
				thumbnail.setIcon(createFallbackIcon());
				JOptionPane.showMessageDialog(MusicPlayerGUI.this,
						"Error playing song: " + ex.getMessage(),
						"Playback Error",
						JOptionPane.ERROR_MESSAGE);
			});
		}
	}
	//Method autoplaynext enables next button action
	private void autoPlayNext() {
		APIhandler api=new APIhandler();
		Song nextSong = songQueue.next();
		if (nextSong != null) {
			try {
				URL url = new URL(api.returnURL(nextSong.getVideoId()));
				preloadAndPlay(url, seekSlider);
				play_pause.setSelected(true);
				updateQueueDisplay();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			SwingUtilities.invokeLater(() -> {
				play_pause.setSelected(false);

				updateQueueDisplay();
			});
		}
	}
	//method to resume playback
	public void resumeSong() {

		mediaPlayer.controls().play();
		uiTimer.start();
		isPaused = false;

	}
	//method to pause playback
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
	//method to enable previous button
	private void previousSong() {
		mediaPlayer.controls().setTime(0);
		// Optional: Update UI components
		SwingUtilities.invokeLater(() -> {
			seekSlider.setValue(0);
			timeLabel.setText("   00:00                                                            "
					+ formatTime(totalDuration));
		});
		if (!mediaPlayer.status().isPlaying()) {
			mediaPlayer.controls().play();
		}
	}

	public void nextSong(APIhandler newapi) throws MalformedURLException, IOException, InterruptedException {

		try {
			if (!songQueue.isEmpty()) {
				Song nextSong = songQueue.queue.poll();
				URL nextURL=new URL(newapi.returnURL(nextSong.getVideoId()));

				mediaPlayer.media().play(nextURL.toString());
				isPaused = false;
			}
			else {
				mediaPlayer.controls().stop();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}


	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		Graphics2D g2d = (Graphics2D) g;
		Color color1 = new Color(40, 40, 40);
		Color color2 = new Color(25, 25, 25);
		g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}
	//method to create multiresolution icon
	public static ImageIcon createMultiResolutionIcon(String basePath) throws IOException {
		BufferedImage normalResImage = ImageIO.read(new File(basePath));
		if (normalResImage == null) {
			throw new IOException("Failed to load image: " + basePath);
		}
		return new ImageIcon(normalResImage);
	}

	//method to convert buffered image to image
	public static BufferedImage convert(BufferedImage source, int target) {
		BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), target);
		Graphics2D g2d = image.createGraphics();
		g2d.drawImage(source, 0, 0, null);  // Draw the source image onto the new image
		g2d.dispose();
		return image;
	}


	//method to resize a image
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
	private Icon createCheckBoxIcon(boolean selected) {
		int size = 18;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Border
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(1, 1, size-3, size-3, 4, 4);

		if (selected) {
			// Fill
			g2.setColor(new Color(30, 215, 96));
			g2.fillRoundRect(2, 2, size-4, size-4, 4, 4);

			// Checkmark
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(2));
			g2.drawLine(4, 9, 8, 13);
			g2.drawLine(8, 13, 14, 5);
		}

		g2.dispose();
		return new ImageIcon(img);
	}
	//method to process thumbnail
	private ImageIcon getProcessedThumbnail(String imageUrl, int targetSize) {
		try {
			// 1. Load original image from URL
			URL url = new URL(imageUrl);
			BufferedImage originalImage = ImageIO.read(url);
			// 2. Crop to square (center 720x720 of 1280x720)
			int cropX = (originalImage.getWidth() - 720) / 2;
			int cropY = 0;
			BufferedImage croppedImage = originalImage.getSubimage(
					cropX,
					cropY,
					720,
					720
			);
			// 3. Scale to desired size (e.g., 180*180) with smooth interpolation
			Image scaledImage = croppedImage.getScaledInstance(
					targetSize,
					targetSize,
					Image.SCALE_SMOOTH
			);
			// 4. Convert to BufferedImage for better quality
			BufferedImage optimizedImage = new BufferedImage(
					targetSize,
					targetSize,
					BufferedImage.TYPE_INT_ARGB
			);
			Graphics2D g2d = optimizedImage.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.drawImage(scaledImage, 0, 0, null);
			g2d.dispose();
			return new ImageIcon(optimizedImage);

		} catch (IOException e) {
			// Fallback to placeholder if image loading fails
			return new ImageIcon("placeholder.jpg");
		}
	}
	//main
	public static void main(String[] args) throws IOException {

		JWindow splash = new JWindow();

		splash.setSize(800, 600); // Set window size
		splash.setLocationRelativeTo(null);

		ImageIcon logo=null;
		try {
			logo = new ImageIcon(convert(highQualityResize(ImageIO.read(MusicPlayerGUI.class.getResourceAsStream("/icons/orpheus.png")), 800,600),BufferedImage.TYPE_INT_ARGB));

		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel logoLabel = new JLabel(logo);
		logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		splash.getContentPane().add(logoLabel);
		splash.setVisible(true);
		Timer timer=new Timer(3000, e -> {
			splash.dispose();

			SwingUtilities.invokeLater(() -> {
				try {
					new MusicPlayerGUI().setVisible(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		});
		timer.setRepeats(false);
		timer.start();// Open main UI
	}
}
