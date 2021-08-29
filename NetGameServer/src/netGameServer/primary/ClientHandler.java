package netGameServer.primary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.apache.logging.log4j.Logger;

public class ClientHandler implements Runnable {
	public final static String GAME_ACTIVITY_PREFIX = "Game Activity <GA>";
	public final static String GAME_ACTIVITY_SUFFFIX = "</GA>";
	public final static String GAME_SUPPORT_PREFIX = "Game Support ";
	public final static String GAME_SUPPORT_SUFFFIX = "</GS>";
	public static final String GAME_INDEX = "gameIndex";
	public static final String GAME_SELECTION = "GameSelection";
	public static final String PLAYER_ORDER = "PlayerOrder";
	public static final int NO_GAME_INDEX = -1;
	public static final String NO_CLIENT_NAME = null;
	public static final String NO_GAME_NAME = null;
	public static final ClientHandler NO_CLIENT_HANDLER = null;
	public static final ArrayList<ClientHandler> NO_CLIENT_HANDLERS = null;
	public static enum SEND_TO { Requestor, AllClients, AllButRequestor };
	private static ArrayList<ClientHandler> clients;
	private DefaultListModel<String> clientListModel;
	private DefaultListModel<String> gameListModel;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String name;	// Player Name
	private String geVersion;
	private String gameName;
	private ServerFrame serverFrame;
	private boolean afk;
	private boolean ready;
	private boolean inBufferGood = false;
	private boolean outBufferGood = false;
	GameSupport gameSupport;
	Logger logger;

	public ClientHandler (ServerFrame aServerFrame, Socket aClientSocket, 
			ArrayList<ClientHandler> aClients, 
			DefaultListModel<String> aClientListModel,
			DefaultListModel<String> aGameListModel) {
		
		this (aServerFrame, aClientSocket, aClients, aClientListModel, aGameListModel, true);
	}
	
	public ClientHandler (ServerFrame aServerFrame, Socket aClientSocket, 
			ArrayList<ClientHandler> aClients, 
			DefaultListModel<String> aClientListModel,
			DefaultListModel<String> aGameListModel, boolean aSetupInOut) {
		serverFrame = aServerFrame;
		try {
			setSocket (aClientSocket);
		} catch (IOException tException) {
			logger.error ("Creating Client Handler, Setting Socket throwing Exception", tException);
		}
		setClientHandlers (aClients);
		setClientList (aClientListModel);
		setGameList (aGameListModel);
		setLogger (serverFrame.getLogger ());
		setGEVersion ("");
		if (aSetupInOut) {
			SetupSocketInOut ();
		}
	}

	public void setClientHandlers (ArrayList<ClientHandler> aClients) {
		clients = aClients;
	}
	
	private void setSocket (Socket aSocket) throws IOException {
		if (socket != null) {
			socket.close ();
		}
		socket = aSocket;
	}
	
	private Socket getSocket () {
		return socket;
	}
	
	private void setLogger (Logger aLogger) {
		logger = aLogger;
	}
	
	private void SetupSocketInOut() {
		inBufferGood = setupInBufferedReader ();
		if (inBufferGood) {
			outBufferGood = setupOutPrintWriter ();
		}
	}

	private boolean setupOutPrintWriter () {
		boolean tOutBufferGood = false;
		PrintWriter tPrintWriter;
		OutputStream tOutputStream;
		
		try {
			logger.info ("Getting OutputStream from socket");
			tOutputStream = socket.getOutputStream ();
			logger.info ("Creating PrintWriter from OutputStream");
			tPrintWriter = new PrintWriter (tOutputStream, true);
			setOutputWriter (tPrintWriter);
			tOutBufferGood = true;
		} catch (IOException tException) {
			log ("Exception thrown when Setting up PrinterWriter for Client", tException);
		} catch (Exception tException1) {
			log ("Exception thrown when Setting up PrinterWriter for Client", tException1);
		}
		
		return tOutBufferGood;
	}

	public void setOutputWriter (PrintWriter aOutputWriter) {
		logger.info ("Setting Output Stream");
		out = aOutputWriter;
	}
	
	public void setInputReader (BufferedReader aInputReader) throws IOException {
		logger.info ("Setting Input Stream");
		in = aInputReader;
	}
	
	public BufferedReader getInputReader () {
		return in;
	}
	
	public PrintWriter getOutputWriter () {
		return out;
	}
	
	private boolean setupInBufferedReader() {
		boolean tInBufferGood = false;
		BufferedReader tInputReader;
		InputStream tInputStream;
		InputStreamReader tInputStreamReader;
		
		try {
			if (socket != null) {
				tInputStream = socket.getInputStream ();
				logger.info ("Retrieved Input Stream from Socket");
				tInputStreamReader = new InputStreamReader (tInputStream);
				logger.info ("Created InputStream Reader from InputStream");
				tInputReader = new BufferedReader (tInputStreamReader);
				logger.info ("Created BufferedReader from InputStreamReader");
				setInputReader (tInputReader);
				tInBufferGood = true;
			}
		} catch (IOException tException) {
			log ("Exception thrown when Setting up BufferedReader for Client", tException);
		} catch (Exception tException1) {
			log ("Exception thrown when Setting up BufferedReader for Client", tException1);
		}
		
		return tInBufferGood;
	}

	public boolean buffersAreGood () {
		return inBufferGood && outBufferGood;
	}
	
	@Override
	public void run () {
		boolean tContinue = true;
		String tMessage;
		String tMessageClean;
		int tNullReadCount = 0;
		int tMaxNullReadCount = 10;
		Thread tThread = Thread.currentThread ();
		
		while (tContinue) {
			if (inBufferGood) {
				try {
					logger.info ("Reading for " + name + " from SocketPort " + socket.getPort () + " Thread ID " + tThread.getId ());
					tMessage = in.readLine ();
					logger.info ("RAW Input from " + name + " [" + tMessage + "]");
					if (tMessage == null) {
						tNullReadCount++;
						Thread.sleep (3000);
					} else {
						tMessageClean = tMessage.replaceAll (">\s+<","><");
						tContinue = handleMessage (tContinue, tMessageClean);
					}
				} catch (SocketException tException) {
					serverMessage ("Your socket needs to be reset");
					log ("Socket Exception Thrown", tException);
					tContinue = false;
				} catch (IOException tException) {
					log ("Exception thrown when Reading from Client", tException);
					tContinue = false;
				} catch (InterruptedException tException) {
					log ("Interuption Exception thrown when Sleeping after Reading NULL from Client", tException);
				}
			} else {
				tContinue = false;
			}
			if (tNullReadCount > tMaxNullReadCount) {
				logger.error ("Null Read Count has exceeded the Max Null Read Count of " + tMaxNullReadCount);
				tContinue = false;
				inBufferGood = false;
			}
		}
		shutdown ();
	}

	public boolean startsAndEndsWith (String aText, String aStartText, String aEndText) {
		boolean tStartsAndEndsWith = false;
		
		tStartsAndEndsWith = aText.startsWith (aStartText) && aText.endsWith (aEndText);
		
		return tStartsAndEndsWith;
	}

	public boolean handleMessage (boolean aContinue, String aMessage) {
		if (aMessage == null) {
			aContinue = false;
			serverBroadcast (name + " has aborted", SEND_TO.AllButRequestor);
		} else if (aMessage.startsWith ("name")) {
			aContinue = handleNewPlayer (aContinue, aMessage);
		} else if (aMessage.startsWith ("GEVersion")) {
			aContinue = handleGEVersion (aContinue, aMessage);
		} else if (aMessage.startsWith ("say")) {
			aContinue = playerBroadcast (aMessage);
		} else if (startsAndEndsWith (aMessage, GAME_ACTIVITY_PREFIX, GAME_ACTIVITY_SUFFFIX)) {
			handleGameActivity (aMessage);
		} else if (startsAndEndsWith (aMessage, GAME_SUPPORT_PREFIX, GAME_SUPPORT_SUFFFIX)) {
			handleGameSupport (aMessage);
		} else if (aMessage.equals ("who")) {
			reportWho ();
		} else if (aMessage.equals ("stop")) {
			aContinue = handlePlayerStop ();
		} else if (aMessage.equals ("AFK")) {
			serverBroadcast (name + " is AFK", SEND_TO.AllClients);
			setClientIsAFK (true);
		} else if (aMessage.equals ("Not AFK")) {
			serverBroadcast (name + " is Not AFK", SEND_TO.AllClients);
			setClientIsAFK (false);
		} else if (aMessage.equals ("Ready")) {
			handleClientIsReady ();
		} else if (aMessage.equals ("Not Ready")) {
			setClientIsReady (false);
			serverBroadcast (name + " is Not Ready to play the Game", SEND_TO.AllButRequestor);
		} else if (aMessage.equals ("Start")) {
//			handleClientIsStarting ();
		} else {
			handleUnrecognizedDataReceived (aMessage);
		}
		
		return aContinue;
	}

	public boolean handlePlayerStop () {
		boolean tContinue;
		
		tContinue = false;
		out.println ("stopping");
		serverBroadcast (name + " has left", SEND_TO.AllButRequestor);
		
		return tContinue;
	}

	public void handleGameActivity (String aMessage) {
		boolean tHandled;
		
		tHandled = handleGameSelection (aMessage);
		if (! tHandled) {
			tHandled = handlePlayerOrder (aMessage);
		}
		if (! tHandled) {
			gameSupport.handleGameActivityRequest (aMessage);
		}
		broadcastGameActivity (aMessage);
	}

	private void handleUnrecognizedDataReceived (String aMessage) {
		logger.error ("+++++ UNRECOGNIZED DATA RECEUVED [" + aMessage + "]");
	}

	public void handleClientIsStarting () {
		setClientIsReady (true);
	}

	public void handleClientIsReady () {
		setClientIsReady (true);
	}

	public String getGameID () {
		String tGameID = GameSupport.NO_GAME_ID;
		
		if (gameSupport != null) {
			tGameID = gameSupport.getGameID ();
		}
		
		return tGameID;
	}
	
	public GameSupport getGameSupport () {
		return gameSupport;
	}
	
	public void setGameSupport (GameSupport aGameSupport) {
		gameSupport = aGameSupport;
	}
	
	public void setGSClientHandlers (ArrayList<ClientHandler> aClientHandlers) {
		gameSupport.setClientHandlers (aClientHandlers);
	}
	
	public void setNewGameSupport (Logger aLogger) {
		GameSupport tNewGameSupport;
		ArrayList<ClientHandler> tClientHandlers;
		
		tClientHandlers = clients;
		tNewGameSupport = new GameSupport (serverFrame, GameSupport.NO_GAME_ID, aLogger);
		setGameSupport (tNewGameSupport);
		setGSClientHandlers (tClientHandlers);
	}
	
	public GameSupport getMatchingGameSupport (String aGameID) {
		GameSupport tFoundGameSupport = GameSupport.NO_GAME_SUPPORT;
		String tFoundGameID;
		
		for (ClientHandler tClientHandler : clients) {
			tFoundGameID = tClientHandler.getGameID ();
			if (tFoundGameID != GameSupport.NO_GAME_ID) {
				if (aGameID.equals (tFoundGameID)) {
					tFoundGameSupport = tClientHandler.getGameSupport ();
				}
			}
		}
		
		return tFoundGameSupport;
	}
	

	private void syncGameSupport (String aGameSupportText) {
		String tGameID;
		
		if (gameSupport == null) {
			setNewGameSupport (logger);
			tGameID = gameSupport.getGameIdFromRequest (aGameSupportText);
			logger.info ("Game ID Found " + tGameID);
			updateGameSupport (tGameID);
		} else {
			tGameID = gameSupport.getGameIdFromRequest (aGameSupportText);
			if ( ! tGameID.equals (GameSupport.NO_GAME_ID)) {
				if (gameSupport.getGameID ().equals (GameSupport.NO_GAME_ID)) {
					updateGameSupport (tGameID);					
				}
			}
		}
	}

	public void updateGameSupport (String aGameID) {
		GameSupport tFoundGameSupport;
		
		tFoundGameSupport = getMatchingGameSupport (aGameID);
		if (tFoundGameSupport == GameSupport.NO_GAME_SUPPORT) {
			logger.info ("Did not Find a loaded Game Support");
		} else {
			logger.info ("Found Game Support replacing the Place Holder");
			serverFrame.syncClientHandlersForGame (aGameID);
			setGameSupport (tFoundGameSupport);
		}
	}
	
	public boolean handleGameSupport (String aGameSupportText) {
		boolean tHandledGameSupport = true;
		String tGSResponse;
		SEND_TO tSendTo;
		String tGameID;
		int tLastActionNumber;
		
		if (gameSupport == null) {
			setNewGameSupport (logger);
			tGameID = gameSupport.getGameIdFromRequest (aGameSupportText);
			logger.info ("Game ID Found " + tGameID);
			updateGameSupport (tGameID);
		} else {
			syncGameSupport (aGameSupportText);
		}
		
		tGSResponse = gameSupport.handleGameSupportRequest (aGameSupportText, this);
		tSendTo = gameSupport.whoGetsResponse (aGameSupportText);
		tLastActionNumber = gameSupport.getLastActionNumber ();
		if (gameSupport.isRequestForHeartbeat (aGameSupportText)) {
			// Setup Customer Logger for Heartbeat Logging to separate file.
		} else {
			logger.info ("----- Client " + name + " Last Action Number " + tLastActionNumber);
			logger.info ("Generated Response is [" + tGSResponse + "]");
		}
		serverBroadcast (tGSResponse, tSendTo);
		
		return tHandledGameSupport;
	}

	private boolean handleGEVersion (boolean aContinue, String aMessage) {
		if (! setGEVersionFromMessage (aMessage) ) {
			aContinue = false;		
		}
		
		return aContinue;
	}
	
	private boolean handleNewPlayer (boolean aContinue, String aMessage) {
		String tResponse;
		
		if (! setNameFromMessage (aMessage) ) {
			// If the name provided is already in the list, Reject this Client, Send 'stopping'
			// and do not continue the processing;
			aContinue = false;
			if (aMessage.indexOf (" ") < 0) {
				tResponse = "No Name provided";
			} else {
				tResponse = "Name already in use";
			}
			serverMessage ("rejected - " + tResponse);
			logger.error ("Rejected - " + tResponse + " [" + aMessage + "]");
		}
		
		return aContinue;
	}
	
    private void log (String aMessage, Exception aException) {
    	logger.error (aMessage + " [" + name + "]", aException);
    }

    public void setGEVersion (String aGEVersion) {
    	geVersion = aGEVersion;
    }
    
    public String getGEVersion () {
    	return geVersion;
    }
    
	public String getName () {
		return name;
	}
	
	public String getPlayerStatus () {
		String tPlayerStatus;
		
		tPlayerStatus = "NotConnected";
		if (isClientReady ()) {
			tPlayerStatus = "READY";
		} else if (isClientAFK ()) {
			tPlayerStatus = "AFK";
		}
		
		return tPlayerStatus;
	}

	public String getAFKName (String aName) {
		return aName + " [AFK]";
	}
	
	public String getReadyName (String aName) {
		return aName + " [READY]";
	}
	
	public String getActiveName (String aName) {
		return aName + " [ACTIVE]";
	}
	
	public String getAFKName () {
		return getAFKName (name); 
	}
	
	public String getFullName () {
		String tFullName;
		
		tFullName = name;
		if (isClientAFK ()) {
			tFullName = getAFKName (tFullName);
		}
		if (isClientReady ()) {
			tFullName = getReadyName (tFullName);
		}
		
		tFullName += " " + geVersion;
		
		return tFullName;
	}
	
	public void setClientIsReady (boolean aIsReady) {
		String tCurrentName, tNewName;
		
		tCurrentName = getFullName ();
		ready = aIsReady;
		tNewName = getFullName ();
		
		swapUser (tCurrentName, tNewName);
	}

	private void swapUser (String aCurrentName, String aNewName) {
		removeUser (aCurrentName);
		addNewUser (aNewName);
	}
	
	private void removeUser (String aCurrentName) {
		clientListModel.removeElement (aCurrentName);
	}
	
	public void setClientIsAFK (boolean aIsAFK) {
		String tCurrentName, tNewName;
		
		tCurrentName = getFullName ();
		afk = aIsAFK;
		tNewName = getFullName ();
		swapUser (tCurrentName, tNewName);
	}
	
	public boolean getClientIsReady () {
		return ready;
	}
	
	public boolean isClientReady () {
		return ready;
	}
	
	public boolean getClientIsAFK () {
		return afk;
	}
	
	public boolean isClientAFK () {
		return afk;
	}

	public void setClientList (DefaultListModel<String> aClientListModel) {
		clientListModel = aClientListModel;
	}

	public void setGameList (DefaultListModel<String> aGameListModel) {
		gameListModel = aGameListModel;
	}
	
	private boolean clientListContains (String aName) {
		String tClientName;
		int tIndex;
		boolean tContains = false;
		
		for (tIndex = 0; tIndex < clientListModel.size (); tIndex++) {
			tClientName = clientListModel.get (tIndex);
			if (tClientName.startsWith (aName)) {
				tContains = true;
			}	
		}
		
		return tContains;
	}
	
	private void addNewUser (String aName) {
		if (! clientListContains (aName)) {
			clientListModel.addElement (aName);
		}
	}
	
	private boolean setGEVersionFromMessage (String aMessage) {
		boolean tAccepted = false;
		String tGEVersion;
		int tSpaceIndex = aMessage.indexOf (" ");
		String tCurrentName, tNewName;
		
		if (tSpaceIndex > 0) {
			tGEVersion = aMessage.substring (tSpaceIndex + 1);
			tCurrentName = getFullName ();
			setGEVersion (tGEVersion);
			tNewName = getFullName ();
			swapUser (tCurrentName, tNewName);
			tAccepted = true;
		} else {
			logger.error (">> No Space in GEVersion Command [" + aMessage + "] <<");
		}
		
		return tAccepted;
	}
	
	private boolean setNameFromMessage (String aMessage) {
		boolean tAccepted = false;
		String tName, tFullName;
		
		int tSpaceIndex = aMessage.indexOf (" ");
		if (tSpaceIndex > 0) {
			tName = aMessage.substring (tSpaceIndex + 1);
			if (! clientListContains (tName)) {
				setName (tName);
				tFullName = getFullName ();
				addNewUser (tFullName);
				tAccepted = true;
				serverBroadcast (name + " has joined", SEND_TO.AllButRequestor);
			}
		} else {
			logger.error (">> No Space in Name Command [" + aMessage + "] <<");
		}
		
		return tAccepted;
	}

	public void setName (String aName) {
		name = aName;
	}
	
	// If the "aToAll" flag is false, don't send it the the Client that matches this
	// Reason -- they are likely broken, timed out, exception, etc. and it won't get 
	// through. 
	// If the "aToAll" flag is true, they are up, and want send a "AFK Message", or 
	// Something else we want them to get back anyway.
	
	public void serverBroadcast (String aMessage, SEND_TO aSendTo) {
		for (ClientHandler tClientHandler : clients) {
			if (name != null) {
				if (SEND_TO.AllClients == aSendTo) {
					tClientHandler.serverMessage (aMessage);
				} else if (SEND_TO.Requestor == aSendTo) {
					if (name.equals (tClientHandler.getName ())) {
						tClientHandler.serverMessage (aMessage);
					}
				} else if (SEND_TO.AllButRequestor == aSendTo) {
					if (! name.equals (tClientHandler.getName ())) {
						tClientHandler.serverMessage (aMessage);
					}
				}
			}
		}
	}
	
	public int getGameIndex (String aGameActivity) {
		int tGameIndex = NO_GAME_INDEX;
		String tPrefix = getGameSelectPrefix ();
		String tShort, tShortInt;
		int tGameIndexLoc;
		
		if (aGameActivity.startsWith (tPrefix)) {
			tGameIndexLoc = aGameActivity.indexOf (GAME_INDEX);
			
			if (tGameIndexLoc > 0) {
				tShort = aGameActivity.substring (tGameIndexLoc);
				tShort = tShort.substring (GAME_INDEX.length () + 2);
				tShortInt = tShort.substring (0, tShort.indexOf ("\""));
				tGameIndex = Integer.parseInt (tShortInt);
			}
		}
		
		return tGameIndex;
	}
	
	public String getPlayerOrderPrefix () {
		return GAME_ACTIVITY_PREFIX + "<" + PLAYER_ORDER + " ";
	}
	
	public String getGameSelectPrefix () {
		return GAME_ACTIVITY_PREFIX + "<" + GAME_SELECTION + " ";
	}

	public void addGameName (String aGameActivity) {
		int tGameIndex;
		
		tGameIndex = getGameIndex (aGameActivity);
		if (tGameIndex != NO_GAME_INDEX) {
			gameSupport = serverFrame.createNewGameSupport (this);
			addGameNameToList (tGameIndex);
		}
	}

	public void addGameNameToList (int aGameIndex) {
		String tGameName;
		
		tGameName = serverFrame.getGameName (aGameIndex);
		addGameNameToList (tGameName);
	}
	
	public void addGameNameToList (String aGameName) {
		setGameName (aGameName + " " + gameSupport.getGameID ());
		if (! gameListModel.contains (gameName)) {
			gameListModel.addElement (gameName);
		}
	}

	public void setGameName (String aGameName) {
		gameName = aGameName;
	}
	
	public int getGameListCount () {
		return gameListModel.getSize ();
	}
	
	public String getGameName () {
		return gameName;
	}
	
	public String getGameName (int aGameIndex) {
		String tFoundName = NO_GAME_NAME;
		
		if ((aGameIndex >= 0) && (aGameIndex < getGameListCount ())) {
			tFoundName = gameListModel.get (aGameIndex);
		}
		
		return tFoundName;
	}
	
	private void broadcastGameActivity (String aGameActivity) {
		for (ClientHandler tClientHandler : clients) {
			if (name != null) {
				if (! name.equals (tClientHandler.getName ())) {
					tClientHandler.out.println (aGameActivity);
				}
			}
		}
	}

	public boolean handlePlayerOrder (String aGameActivity) {
		String tPlayerOrderPrefix;
		boolean tHandled = false;
		
		tPlayerOrderPrefix = getPlayerOrderPrefix ();
		if (aGameActivity.startsWith (tPlayerOrderPrefix)) {
			tHandled = true;
		}
		
		return tHandled;
	}

	public boolean handleGameSelection (String aGameActivity) {
		String tGameSelectPrefix;
		boolean tHandled = false;
		
		tGameSelectPrefix = getGameSelectPrefix ();
		if (aGameActivity.startsWith (tGameSelectPrefix)) {
			setClientIsReady (true);
			addGameName (aGameActivity);
			tHandled = true;
		}
		
		return tHandled;
	}

	private boolean playerBroadcast (String aMessage) {
		String tMessage;
		boolean tSuccess = false;
		
		int tSpaceIndex = aMessage.indexOf (" ");
		if (tSpaceIndex > 0) {
			tMessage = aMessage.substring (tSpaceIndex + 1);
			for (ClientHandler tClientHandler : clients) {
				if (name != null) {
					if (! name.equals (tClientHandler.getName ())) {
						tClientHandler.out.println (name + ": " + tMessage);
						tSuccess = tSuccess || true;
					}
				}
			}
		} else {
			logger.error (">> No Space in Say Command [" + aMessage + "] <<");
			tSuccess = false;
		}
		
		return tSuccess;
	}

	private void serverMessage (String aMessage) {
		logger.info ("Sending to " + name + " over Port " + socket.getPort () + " the following [" + aMessage + "]");
		out.println ("[Server: " + aMessage + "]");
	}

	public void reportFull () {
		serverMessage ("Sorry we are full at " + clients.size ());
		shutdown ();
	}

	private void reportWho () {
		String tMessage;
		
		for (ClientHandler tClientHandler : clients) {
			tMessage = tClientHandler.getFullName ()  + " has joined";
			serverMessage (tMessage);
		}
	}
	
	private void shutdown () {
		removeUser (getFullName ());
		if (inBufferGood) {
			try {
				in.close ();
				inBufferGood = false;
			} catch (IOException tException) {
				log ("Exception thrown when Closing the Client InputStream", tException);
			}
		}
		if (outBufferGood) {
			try {
				out.close ();
				outBufferGood = false;
			} catch (Exception tException) {
				log ("Exception thrown when Closing the Client OutputStream", tException);

			}
		}
		serverFrame.removeClient (this);
		if (clients.size () == 1) { // Last Client being Removed, so perform autosave
			if (gameSupport != GameSupport.NO_GAME_SUPPORT) {
				if (gameSupport.getGameStatus ().equals (SavedGame.STATUS_ACTIVE)) {
					gameSupport.setGameStatus (SavedGame.STATUS_INACTIVE);
				}
				gameSupport.autoSave ();
			}
		}
		clients.remove (this);
	}

	public void shutdownAll () {
		if (clients != NO_CLIENT_HANDLERS) {
			if (clients.size () > 0) {
				for (ClientHandler tClientHandler : clients) {
					tClientHandler.shutdown ();
				}
			}
		}
	}

	public String generateGameID () {
		String tNewGameID;
		
		tNewGameID = serverFrame.generateNewGameID ();
		
		return tNewGameID;
	}

	public void printAllClientHandlerNames () {
		String tClientName;
		Socket tSocket;
		String tGameName;
		
		logger.info ("There are " + clients.size () + " Clients in the list");
		for (ClientHandler tClientHandler : clients) {
			tClientName = tClientHandler.getName ();
			tSocket = tClientHandler.getSocket ();
			tGameName = tClientHandler.getGameID ();
			if (tClientName == null) {
				logger.info ("Client Name is NULL - Replacing this one");
			} else {
				logger.info ("Client Name is " + tClientName + " Socket on Port " + 
						tSocket.getPort () + " Game Name " + tGameName);
			}
		}
	}
	
	public boolean updateClientHandlers (String aClientName, String aGameID, GameSupport aGameSupport) {
		String tThisClientName;
		boolean tSuccessfulUpdate = false;;
		GameSupport tFoundGameSupport;
		Socket tFoundSocket;
		int tClientIndex, tClientCount;
		ClientHandler tClientHandler;
		
		logger.info ("Before Updating Client Handlers:");
		printAllClientHandlerNames ();
//		aGameSupport.printInfo();
		tClientCount = clients.size ();
		for (tClientIndex = 0; tClientIndex < tClientCount; tClientIndex++) {
			tClientHandler = clients.get (tClientIndex);
			tThisClientName = tClientHandler.getName ();
			tFoundSocket = tClientHandler.getSocket ();
			if (! tSuccessfulUpdate) {
				if (tThisClientName != null) {
					if (tThisClientName.equals (aClientName)) {
						logger.info ("Found Client Handler with Client Name of " + aClientName +
								" OLD Socket on Port " + tFoundSocket.getPort ());
						
						try {
							setName (tThisClientName);
							setGameSupport (aGameSupport);
							logger.info ("Ready to remove Client at " + tClientIndex);
							clients.remove (tClientIndex);
							tClientCount--;
							tSuccessfulUpdate = true;
						} catch (Exception tException) {
							logger.error ("Replacing Client Handler - Exception Thrown", tException);
						}
					}
				}
			}
		}
		if (! tSuccessfulUpdate) {
			setName (aClientName);
			addNewUser (name);
			serverBroadcast (name + " has reconnected", SEND_TO.AllButRequestor);
			// Need to attach GameSupport Object to this Client Handler.
			
			tFoundGameSupport = getMatchingGameSupport (aGameID);
			if (tFoundGameSupport != null) {
				gameSupport = tFoundGameSupport;
			} else {
				logger.warn ("Did not find Game Support with Game ID " + aGameID);
			}
		}
		logger.info ("After Updating Client Handlers:");
		printAllClientHandlerNames ();
//		aGameSupport.printInfo();
		
		return tSuccessfulUpdate;
		// TODO --
		// (2) Test if this Client Handler Name matches the Client. 
		//		(a) If Yes, replace the Socket, In and Out
		//		(b) If No, 
		// (2) Update the new Client Handler with the Game Support matching the GameID
		// (3) For each of the Clients, 
		//		(a) Remove this Client Handler that matches this name
		//		(b) Flush the Socket associated with the Client Handler being removed
		//		(c) Add this Client Handler to the client;
		// (4) Broadcast to the Client Player X has reconnected
		
	}
}
