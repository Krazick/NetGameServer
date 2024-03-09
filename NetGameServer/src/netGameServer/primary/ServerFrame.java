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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import org.apache.logging.log4j.Logger;

import geUtilities.FileUtils;
import swingDelays.KButton;

public class ServerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public static final ServerFrame NO_SERVER_FRAME = null;
	private final int MAX_THREADS = 12;
	private LinkedList<ClientHandler> clients = new LinkedList <> ();
	private ExecutorService pool = Executors.newFixedThreadPool (MAX_THREADS);
	private String NO_SELECTED_GAME = "NO SELECTED GAME";
	boolean continueThread;
	private JLabel frameTitle;
	private JLabel playersLabel;
	private JLabel gamesLabel;
	private JLabel portLabel;
	private JLabel connectionsLabel;
	private JLabel maxThreadLabel;
	private JLabel serverIPLabel;
	private JLabel gameActionsLabel;
	private JPanel northJPanel;
	private JPanel westJPanel;
	private JPanel centerJPanel;
	private JPanel eastJPanel;
	
	private KButton quitButton;
	private JList<String> clientList;
	private JList<String> gamesList;
	private ListSelectionModel gamesListSelectionModel;
	private SharedListSelectionHandler gameListSelectionHandler;
	private JList<String> gameActionList;
	private JScrollPane clientListPane;
	private JScrollPane gamesListPane;
	private JScrollPane gameActionListPane;
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
	private GameSupport selectedGameSupport;

	public ServerFrame (String aName, int aServerPort, LinkedList<String> aGameNames, ServerThread aServerThread) 
			throws HeadlessException, IOException {
		super ("");
		
		String tAutoSavesDirectory;
		
		continueThread = true;
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
		selectedGameSupport = GameSupport.NO_GAME_SUPPORT;
		
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
		addActiveGameSupport (tNewGameSupport);
		tFilePath = tNewGameSupport.constructAutoSaveFileName (getFullASDirectory (), tNewGameID);
		tPlayerName = aClientHandler.getName ();
		savedGames.addNewSavedGame (tFilePath, tNewGameID, tPlayerName);
		
		return tNewGameSupport;
	}
	
	public void addActiveGameSupport (GameSupport aGameSupport) {
		if (! activeGames.contains (aGameSupport)) {
			activeGames.add (aGameSupport);
		}
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
		
		westJPanel.setLayout (new BoxLayout (westJPanel, BoxLayout.Y_AXIS));
		westJPanel.setBorder (new EmptyBorder (5, 10, 0, 10));
		centerJPanel.setLayout (new BoxLayout (centerJPanel, BoxLayout.Y_AXIS));
		centerJPanel.setBorder (new EmptyBorder (5, 0, 0, 10));
		eastJPanel.setLayout (new BoxLayout (eastJPanel, BoxLayout.Y_AXIS));
		eastJPanel.setBorder (new EmptyBorder (5, 0, 0, 10));
		add (northJPanel, BorderLayout.NORTH);
		add (westJPanel, BorderLayout.WEST);
		add (centerJPanel, BorderLayout.CENTER);
		add (eastJPanel, BorderLayout.EAST);
	}
	
	private void setupJFrameComponents (String aTitle) {
		String tMyIPAddress;
		
		frameTitle = new JLabel (aTitle);
		frameTitle.setHorizontalAlignment (SwingConstants.CENTER);
		northJPanel.add (frameTitle);
		
		playersLabel = new JLabel ("Clients");
		playersLabel.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		setupClientList ();
		setupGamesList ();		
		setupGameActionList ();
		
		tMyIPAddress = whatIsMyIPAddress ();
		serverIPLabel = new JLabel ("Server IP: " + tMyIPAddress);
		serverIPLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		portLabel = new JLabel ("Port: " + serverPort);
		portLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		maxThreadLabel = new JLabel("Max Threads: " + MAX_THREADS);
		maxThreadLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		connectionsLabel = new JLabel ("Connections: 0");
		connectionsLabel.setAlignmentX (Component.LEFT_ALIGNMENT);

		quitButton = new KButton ("Quit");
		quitButton.setAlignmentX (Component.LEFT_ALIGNMENT);
	}

	public void setupClientList () {
		clientList = new JList<String> (clientListModel);
		clientListPane = new JScrollPane (clientList);
		clientListPane.setMinimumSize (new Dimension (300, 100));
		clientListPane.setPreferredSize (new Dimension (300, 200));
		clientListPane.setMaximumSize (new Dimension (300, 250));
		playersLabel.setLabelFor (clientList);
	}

	public void setupGameActionList () {
		gameActionsLabel = new JLabel ("Game Actions");
		gameActionsLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
		
		gameActionList = new JList<String> (gameActionListModel);
		gameActionListPane = new JScrollPane (gameActionList);
		gameActionListPane.setMinimumSize (new Dimension (260, 500));
		gameActionListPane.setPreferredSize (new Dimension (260, 100));
		gameActionListPane.setMaximumSize (new Dimension (260, 100));
		gameActionListPane.setAlignmentX (Component.LEFT_ALIGNMENT);
		addGameAction (NO_SELECTED_GAME);
	}
	
	private void setupGamesList () {
		gameListSelectionHandler = new SharedListSelectionHandler (this);
		gamesLabel = new JLabel ("Active Games");
		gamesLabel.setAlignmentX (Component.CENTER_ALIGNMENT);
		gamesList = new JList<String> (gameListModel);
		gamesListPane = new JScrollPane (gamesList);
		gamesListPane.setMinimumSize (new Dimension (180, 100));
		gamesListPane.setPreferredSize (new Dimension (180, 200));
		gamesListPane.setMaximumSize (new Dimension (180, 250));
		gamesListSelectionModel = gamesList.getSelectionModel ();
		gamesListSelectionModel.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		gamesListSelectionModel.addListSelectionListener (gameListSelectionHandler);
		gamesLabel.setLabelFor (gamesList);
	}
	
	public void clearGameActionList () {
		gameActionListModel.clear ();
	}

	private void setGameActionsLabel () {
		int tActionCount;
		String tGameID;
		
		if (selectedGameSupport == GameSupport.NO_GAME_SUPPORT) {
			gameActionsLabel = new JLabel ("Game Actions");
		} else {
			tGameID = selectedGameSupport.getGameID ();
			tActionCount = selectedGameSupport.getActionCount ();
			gameActionsLabel.setText ("(" + tGameID + ") Game Actions [" + tActionCount + "]");
		}
	}

	public void addGameAction (String aGameAction) {
		gameActionListModel.add (0, aGameAction);
		setGameActionsLabel ();
	}
	
	public void removeLastGameAction () {
		gameActionListModel.remove (0);
		setGameActionsLabel ();
	}
	
	public void handleGameSelection () {
		int tServerFrameIndex;
		
		tServerFrameIndex = 0;		// Need to get the Index for this Server Frame in list of Games to pass in.
		handleGameSelection (tServerFrameIndex);
	}
	
	public void handleGameSelection (int aSelectedGameIndex) {
		GameSupport tGameSupport;
		int tCountActiveGames;
		
		clearGameActionList ();
		tCountActiveGames = activeGames.size ();
		if (tCountActiveGames > 0) {
			if (aSelectedGameIndex < tCountActiveGames) {
				tGameSupport = activeGames.get (aSelectedGameIndex);
				setSelectedGameSupport (tGameSupport);
				selectedGameSupport.addGameActionsToFrame ();
				setGameActionsLabel ();
			}
		}
	}
	
	public void setSelectedGameSupport (GameSupport aGameSupport) {
		selectedGameSupport = aGameSupport;
	}
	
	class SharedListSelectionHandler implements ListSelectionListener {
		ServerFrame serverFrame;
		
		SharedListSelectionHandler (ServerFrame aServerFrame) {
			serverFrame = aServerFrame;
		}
		
		@Override
		public void valueChanged (ListSelectionEvent aLSEvent) { 
            ListSelectionModel tLSM;
            int tMinIndex;
            int tMaxIndex;
            int tSelectedID;

            tLSM = (ListSelectionModel) aLSEvent.getSource ();
            tSelectedID = -1;
            if (! tLSM.isSelectionEmpty ()) {
            		// Find out which indexes are selected.
            		tMinIndex = tLSM.getMinSelectionIndex ();
                tMaxIndex = tLSM.getMaxSelectionIndex ();
                for (int i = tMinIndex; i <= tMaxIndex; i++) {
                    if (tLSM.isSelectedIndex (i)) {
                    		tSelectedID = i;
                    }
                }
            }
           serverFrame.handleGameSelection (tSelectedID);
         }
    }
	
	private void updateLabelConnections (int aCount) {
		connectionsLabel.setText ("Connections: " + aCount);
	}
	
	private void setupJFrame () {
		westJPanel.add (playersLabel);		
		westJPanel.add (Box.createVerticalStrut (5));
		westJPanel.add (clientListPane);

		centerJPanel.add (gamesLabel);
		centerJPanel.add (Box.createVerticalStrut (5));
		centerJPanel.add (gamesListPane);

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
		eastJPanel.add (gameActionListPane);
		eastJPanel.add (Box.createVerticalStrut (10));
		eastJPanel.add (quitButton);
		
		setSize (800, 340);
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
