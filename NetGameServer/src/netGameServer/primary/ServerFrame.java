package netGameServer.primary;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import org.apache.logging.log4j.Logger;

import netGameServer.utilities.FileUtils;

public class ServerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final int MAX_THREADS = 12;
	private LinkedList<ClientHandler> clients = new LinkedList <> ();
	private ExecutorService pool = Executors.newFixedThreadPool (MAX_THREADS);
	boolean continueThread = true;
	private JLabel frameTitle;
	private JLabel labelPlayers;
	private JLabel labelGames;
	private JLabel portLabel;
	private JLabel connectionsLabel;
	private JLabel maxThreadLabel;
	private JLabel serverIPLabel;
	private JLabel gameActionsLabel;
	private JPanel northJPanel;
	private JPanel westJPanel;
	private JPanel centerJPanel;
	private JPanel eastJPanel;
	
	private JButton quitButton;
	private JList<String> clientList;
	private JList<String> gamesList;
	private JList<String> gameActionList;
	private DefaultListModel<String> clientListModel = new DefaultListModel<String> ();
	private DefaultListModel<String> gameListModel = new DefaultListModel<String> ();
	private DefaultListModel<String> gameActionListModel = new DefaultListModel<String> ();
	private LinkedList<String> gameNames;
	private List<GameSupport> activeGames;
	private String name;
	private int serverPort;
	private ServerThread serverThread;
	private ServerSocket serverSocket;
	private Logger logger;
	private SavedGames savedGames;

	public ServerFrame (String aName, int aServerPort, LinkedList<String> aGameNames, ServerThread aServerThread) 
			throws HeadlessException, IOException {
		super ("");
		
		String tAutoSavesDirectory;
				
		setName (aName);
		setPort (aServerPort);
		setServerThread (aServerThread);
		setLogger (serverThread.getLogger ());
		gameNames = aGameNames;
		setupJPanels ();
		setupJFrameComponents (name + " Server Monitor Frame");
		setupJFrame ();
		setupActions ();
		activeGames = new LinkedList<GameSupport> ();
		setupAutoSaveDirectory ();
		tAutoSavesDirectory = getFullASDirectory ();
		savedGames = new SavedGames (tAutoSavesDirectory);

		serverSocket = null;
		serverThread.setIsRunning (false);
	}
	
	public SavedGames getSavedGames () {
		return savedGames;
	}

	public Logger getLogger () {
		return logger;
	}
	
	public void setLogger (Logger aLogger) {
		logger = aLogger;
	}
	
	public SavedGame getSavedGameFor (String aGameID) {
		SavedGame tSavedGame;
		
		tSavedGame = savedGames.getSavedGameFor (aGameID);
		
		return tSavedGame;
	}
	
	public String getSavedGamesFor (String aPlayerName) {
		String tSavedGamesFor;
		
		tSavedGamesFor = savedGames.getSavedGamesFor (aPlayerName);
		
		return tSavedGamesFor;
	}

	public GameSupport createNewGameSupport (ClientHandler aClientHandler) {
		String tNewGameID;
		GameSupport tNewGameSupport;
		String tFilePath;
		String tPlayerName;
		
		tNewGameID = generateNewGameID ();
		tNewGameSupport = new GameSupport (this, tNewGameID, logger);
		tNewGameSupport.addClientHandler (aClientHandler);
		activeGames.add (tNewGameSupport);
		tFilePath = tNewGameSupport.constructAutoSaveFileName (getFullASDirectory (), tNewGameID);
		tPlayerName = aClientHandler.getName ();
		savedGames.addNewSavedGame (tFilePath, tNewGameID, tPlayerName);
		
		return tNewGameSupport;
	}
	
	public void addPlayerToSavedGame (String aNewGameID, String aPlayerName) {
		savedGames.addPlayerToSavedGame (aNewGameID, aPlayerName);
	}
	
	// -----------------  Auto Save Functions ---------------
	
	public void setupAutoSaveDirectory () {
		String tDirectoryName;
		String tSubDirPath;
		
		tDirectoryName = getBaseASDirectory ();
		FileUtils.createDirectory (tDirectoryName);
		tSubDirPath = getFullASDirectory ();
		FileUtils.createDirectory (tSubDirPath);
	}
	
	public String getFullASDirectory () {
		String tFullPath;
		
		tFullPath = getBaseASDirectory () + File.separator + getName ();
		
		return tFullPath;
	}
	
	public String getBaseASDirectory () {
		String tDirectoryName;
		
		tDirectoryName = "NetworkAutoSaves";
		
		return tDirectoryName;
	}
	
	// -------------------- End Auto Save Functions ----------------

	public String generateNewGameID () {
		String tNewGameID;
		
		LocalDateTime datetime = LocalDateTime.now ();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("yyyy-MM-dd-HHmm");
		tNewGameID = datetime.format (formatter);

		return tNewGameID;
	}
	
	public String generateNewGSocketName () {
		String tNewGSocketName;
		
		LocalDateTime datetime = LocalDateTime.now ();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("HHmm");
		tNewGSocketName = "GSocket Name " + datetime.format (formatter);

		return tNewGSocketName;
	}
	
	public void operateFrame () {
		Socket tClientSocket;
		ClientHandler tClientHandler;
		
		try {
			serverSocket = new ServerSocket (serverPort);
			serverThread.setIsRunning (true);
			while (continueThread) {
				// establishes connection to the Client
				
				logger.info ("Thread Loop Start");
				tClientSocket = acceptServerSocket ("Operate Frame first time");
				tClientHandler = new ClientHandler (this, tClientSocket, clients, 
								clientListModel, gameListModel);
				
				if (clients.size () < MAX_THREADS) {
					clients.add (tClientHandler);
					pool.execute (tClientHandler);
				} else {
					tClientHandler.reportFull ();
				}
				updateLabelConnections (clients.size ());
				logger.info  ("Thread Loop End");
			}
		} catch (SocketException tException) {
			if (continueThread) {
				log ("Server Socket Exception", tException);
			}
		} catch (Exception tException) { 
			log ("Exception thrown from Server Main Class", tException); 
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close ();
				} catch (IOException tException) {
					log ("Exception thrown from Server Main Class", tException); 
				}
			}
		}
		
		pool.shutdown ();
	}

	public Socket acceptServerSocket (String aCaller) {
		Socket tClientSocket = null;
		
		try {
			tClientSocket = serverSocket.accept ();
			logger.info ("New Socket created with Name [" + tClientSocket.toString () + "]");
		} catch (SocketException tException) {
			if (continueThread) {
				log (aCaller + " -- Server Socket Exception - Trying to Accept Socket", tException);
			}
		} catch (Exception tException) { 
			log (aCaller + " -- Exception thrown from Server Main Class", tException); 
		} 

		return tClientSocket;
	}
	
	public void setServerThread (ServerThread aServerThread) {
		serverThread = aServerThread;
	}
	
	public ServerThread getServerThread () {
		return serverThread;
	}
	
	public void setPort (int aServerPort) {
		serverPort = aServerPort;
	}
	
	public int getPort () {
		return serverPort;
	}
	
	@Override
	public void setName (String aName) {
		name = aName;
	}
	
	@Override
	public String getName () {
		return name;
	}
	
	public String getGameName (int aGameIndex) {
		return gameNames.get (aGameIndex);
	}
	
	public void removeClient (ClientHandler aClientHandler) {
		clients.remove (aClientHandler);
		updateLabelConnections (clients.size ());
	}
	
	public void repaintGamesList () {
		gamesList.repaint ();
	}
	
	public JFrame getThisFrame () {
		return this;
	}
	
	private void setupActions () {
		quitButton.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed (ActionEvent e) {
				quitFrame ();
			}
		});
	}

	public void quitFrame () {
		ClientHandler tClientHandler;
		JFrame thisFrame = getThisFrame ();
		
		continueThread = false;
		thisFrame.dispose ();
		if (clients.size () > 0) {
			tClientHandler = clients.get (0);
			tClientHandler.shutdownAll ();
		}
		serverThread.frameQuitting ();
		if (serverSocket!= null) {
			closeServerSocket ("Trying to Quit Server Frame");
		} else {
			System.out.println ("Quitting Server Frame, no Server Socket Set");
		}
	}

	public void closeServerSocket (String aCaller) {
		try {
			serverSocket.close ();
			logger.info ("Server Socket Closed");
		} catch (IOException tException) {
			log (aCaller + " - Exception thrown when closing Socket", tException);
		}
	}
	
	private void setupJPanels () {
		northJPanel = new JPanel ();
		westJPanel = new JPanel ();
		centerJPanel = new JPanel ();
		eastJPanel = new JPanel ();
		
		westJPanel.setBorder (new EmptyBorder (5, 10, 0, 10));
		add (northJPanel, BorderLayout.NORTH);
		add (westJPanel, BorderLayout.WEST);
		westJPanel.setLayout (new BoxLayout (westJPanel, BoxLayout.Y_AXIS));
		add (centerJPanel, BorderLayout.CENTER);
		centerJPanel.setBorder (new EmptyBorder (5, 0, 0, 10));
		centerJPanel.setLayout (new BoxLayout (centerJPanel, BoxLayout.Y_AXIS));
		eastJPanel.setBorder (new EmptyBorder (5, 0, 0, 10));
		add (eastJPanel, BorderLayout.EAST);
		eastJPanel.setLayout (new BoxLayout (eastJPanel, BoxLayout.Y_AXIS));
	}
	
	private void setupJFrameComponents (String aTitle) {
		String tMyIPAddress;
		
		frameTitle = new JLabel (aTitle);
		frameTitle.setHorizontalAlignment (SwingConstants.CENTER);
		northJPanel.add (frameTitle);
		
		labelPlayers = new JLabel ("Clients");
		labelPlayers.setAlignmentX (Component.CENTER_ALIGNMENT);
		clientList = new JList<String> (clientListModel = new DefaultListModel<String> ());
		clientList.setMinimumSize (new Dimension (300, 100));
		clientList.setPreferredSize (new Dimension (300, 200));
		clientList.setMaximumSize (new Dimension (300, 250));
		clientList.setBackground (Color.PINK);
		
		labelPlayers.setLabelFor (clientList);
		
		labelGames = new JLabel ("Games");
		labelGames.setAlignmentX (Component.CENTER_ALIGNMENT);
		gamesList = new JList<String> (gameListModel = new DefaultListModel<String> ());
		gamesList.setMinimumSize (new Dimension (300, 100));
		gamesList.setPreferredSize (new Dimension (300, 200));
		gamesList.setMaximumSize (new Dimension (300, 250));
		gamesList.setBackground (Color.GREEN);
		labelGames.setLabelFor (gamesList);		
		
		tMyIPAddress = whatIsMyIPAddress ();
		serverIPLabel = new JLabel ("Server IP: " + tMyIPAddress);
		serverIPLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		portLabel = new JLabel ("Port: " + serverPort);
		portLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		maxThreadLabel = new JLabel("Max Threads: " + MAX_THREADS);
		maxThreadLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		connectionsLabel = new JLabel ("Connections: 0");
		connectionsLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		gameActionsLabel = new JLabel ("Recent Game Actions");
		gameActionsLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		gameActionList = new JList<String> (gameActionListModel = new DefaultListModel<String> ());
		gameActionList.setMinimumSize (new Dimension (150, 500));
		gameActionList.setPreferredSize (new Dimension (150, 100));
		gameActionList.setMaximumSize (new Dimension (150, 100));
		gameActionList.setBackground (Color.CYAN);
		gameActionList.setAlignmentX (Component.LEFT_ALIGNMENT);

		quitButton = new JButton ("Quit");
		quitButton.setAlignmentX (Component.LEFT_ALIGNMENT);
	}
	
	private void updateLabelConnections (int aCount) {
		connectionsLabel.setText ("Connections: " + aCount);
	}
	
	private void setupJFrame () {
		westJPanel.add (labelPlayers);
		westJPanel.add (clientList);

		centerJPanel.add (labelGames);
		centerJPanel.add (gamesList);

		eastJPanel.add (serverIPLabel);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (portLabel);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (maxThreadLabel);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (connectionsLabel);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (gameActionsLabel);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (gameActionList);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (quitButton);
		
		setSize (800, 330);
		setVisible (true);
	}
	
	private String whatIsMyIPAddress () {
		URL url_name;
		BufferedReader sc;
		String publicIPAddress;
		
		// Find Public IP address
		publicIPAddress = "";
		try
		{
			url_name = new URL ("https://api.ipify.org/");
			sc = new BufferedReader(new InputStreamReader (url_name.openStream()));

			// Reads system Public IPAddress
			publicIPAddress = sc.readLine ().trim ();
		}
		catch (Exception e)
		{
			publicIPAddress = "Cannot Execute Properly";
		}
		
		return publicIPAddress;
	}
	
    public void log (String aMessage, Exception aException) {
    		logger.error (aMessage, aException);
    }

	public boolean isRunning () {
		return true;
	}
	

	public void syncClientHandlersForGame (String aGameID) {
		String tFoundGameName;
		
		tFoundGameName = getGameName (aGameID);
		if (tFoundGameName != ClientHandler.NO_GAME_NAME) {
			setGameNamesForClientHandlers (aGameID, tFoundGameName);
		}
	}

	public void setGameNamesForClientHandlers (String aGameID, String aGameName) {
		for (ClientHandler tClientHandler : clients) {
			if (tClientHandler.getGameID ().equals (aGameID)) {
				tClientHandler.setGameName (aGameName);
			}
		}
	}
	
	public String getGameName (String aGameID) {
		String tGameName;
		String tFoundGameName = ClientHandler.NO_GAME_NAME;
		
		for (ClientHandler tClientHandler : clients) {
			if (tClientHandler.getGameID ().equals (aGameID)) {
				
				tGameName = tClientHandler.getGameName ();
				if (tGameName != ClientHandler.NO_GAME_NAME) {
					tFoundGameName = tGameName;
				}
			}
		}
		
		return tFoundGameName;
	}
}
