package netGameServer.primary;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.logging.log4j.Logger;

import netGameServer.utilities.FileUtils;

public class ServerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final int MAX_THREADS = 12;
	private LinkedList<ClientHandler> clients = new LinkedList <> ();
	private ExecutorService pool = Executors.newFixedThreadPool (MAX_THREADS);
	boolean continueThread = true;
	private JButton btnQuit;
	private JList<String> clientList;
	private JList<String> gamesList;
	private DefaultListModel<String> clientListModel = new DefaultListModel<String> ();
	private DefaultListModel<String> gameListModel = new DefaultListModel<String> ();
	private JTextField connectionCount;
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
		setupJFrame (name + " Server Monitor Frame");
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
				connectionCount.setText ("" + clients.size ());
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
		connectionCount.setText ("" + clients.size ());
	}
	
	public void repaintGamesList () {
		gamesList.repaint ();
	}
	
	public JFrame getThisFrame () {
		return this;
	}
	
	private void setupActions () {
		btnQuit.addActionListener (new ActionListener () {
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
		
		closeServerSocket ("Trying to Quit Server Frame");
	}

	public void closeServerSocket (String aCaller) {
		try {
			serverSocket.close ();
			logger.info ("Server Socket Closed");
		} catch (IOException tException) {
			log (aCaller + " - Exception thrown when closing Socket", tException);
		}
	}
	
	private void setupJFrame (String aTitle) {

		JLabel lblTitle = new JLabel (aTitle);
		lblTitle.setHorizontalAlignment (SwingConstants.CENTER);
		
		btnQuit = new JButton ("Quit");
		btnQuit.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		JLabel lblPlayers = new JLabel ("Clients");
		clientList = new JList<String> (clientListModel = new DefaultListModel<String> ());
		lblPlayers.setLabelFor (clientList);

		
		JLabel lblGames = new JLabel ("Games");
		gamesList = new JList<String> (gameListModel = new DefaultListModel<String> ());
		lblGames.setLabelFor (gamesList);
		
		JLabel lblPort = new JLabel ("Port: " + serverPort);
		
		connectionCount = new JTextField ();
		connectionCount.setAlignmentX (Component.RIGHT_ALIGNMENT);
		connectionCount.setText ("0");
		connectionCount.setColumns (10);
		
		JLabel lblConnections = new JLabel ("Connections");
		lblConnections.setLabelFor (connectionCount);
		
		JLabel lblMaxThreads = new JLabel("Max Threads: " + MAX_THREADS);
		
		GroupLayout groupLayout = new GroupLayout (getContentPane ());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(63)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblPlayers)
								.addComponent(clientList, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(gamesList, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
									.addGap(33))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblGames, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
									.addGap(250)))
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblConnections)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(connectionCount, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE))
								.addComponent(btnQuit, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
									.addComponent(lblPort)
									.addGap(114))
								.addComponent(lblMaxThreads)))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 551, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(16)
					.addComponent(lblTitle)
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPlayers)
						.addComponent(lblPort)
						.addComponent(lblGames))
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(clientList, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
								.addComponent(gamesList, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE))
							.addContainerGap(26, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(8)
							.addComponent(lblMaxThreads)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblConnections)
								.addComponent(connectionCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(70)
							.addComponent(btnQuit)
							.addGap(40))))
		);
		getContentPane ().setLayout (groupLayout);

		setSize (800, 300);
		setVisible (true);
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
