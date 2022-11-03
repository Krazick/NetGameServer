package netGameServer.primary;

import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

import netGameServer.utilities.XMLNode;
import netGameServer.utilities.ElementName;
import netGameServer.utilities.AttributeName;
import netGameServer.utilities.FileUtils;
import netGameServer.utilities.XMLDocument;

public class GameSupport {
	public static final ElementName EN_PLAYERS = new ElementName ("Players");
	public static final ElementName EN_PLAYER = new ElementName ("Player");
	public static final AttributeName AN_NAME = new AttributeName ("name");
	public final String NOT_CONNECTED = "Not Connected";
	private final String BAD_REQUEST = "<GSResponse><BadRequest/></GSResponse>";
	private final String BAD_GAME_ID = "<GSResponse><BadGameID/></GSResponse>";
	private final static String ACTION_NUMBER = "actionNumber=\"(\\d+)\"";
	private final static String REQUEST_ACTION = "<RequestAction " + ACTION_NUMBER + ">";
	private final static Pattern REQUEST_ACTION_PATTERN = Pattern.compile (REQUEST_ACTION);
	private final static String GAME_ID = "(\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d\\d)";
	private final static Pattern GAME_ID_PATTERN = Pattern.compile (GAME_ID);
	private final static String GS_WITH_GAME_ID = "<GS gameID=\"" + GAME_ID + "\">(.*)</GS>";
	private final static Pattern GS_WITH_GAME_ID_PATTERN = Pattern.compile (GS_WITH_GAME_ID);
	private final static String GS_WITH_NO_GAME_ID = "<GS>(.*)</GS>";
	private final static Pattern GS_WITH_NO_GAME_ID_PATTERN = Pattern.compile (GS_WITH_NO_GAME_ID);
	
	private final static String GA_WITH_ACTION = "<GA>(<Action.*)</GA>";
	private final static Pattern GA_WITH_NO_GAME_ID_PATTERN = Pattern.compile (GA_WITH_ACTION);
	private final static String GA_REMOVE_ACTION = "<GA>(<RemoveAction number=\"(\\d+)\").*></GA>";
	private final static Pattern GA_REMOVE_NO_GAME_ID_PATTERN = Pattern.compile (GA_REMOVE_ACTION);
	
	private final static String REQUEST_HEARTBEAT = "<Heartbeat>";
	private final static Pattern REQUEST_HEARTBEAT_PATTERN = Pattern.compile (REQUEST_HEARTBEAT);
	private final String REQUEST_ACTION_NUMBER = "<ActionNumber requestNew=\"TRUE\">";
	private final static String REQUEST_SAVED_GAMES_FOR = "<RequestSavedGames player=\"([A-Za-z][A-Za-z0-9]+)\"/?>";
	private final static Pattern REQUEST_SAVED_GAMES_FOR_PATTERN = Pattern.compile (REQUEST_SAVED_GAMES_FOR);
	private final String REQUEST_LAST_ACTION = "<ActionNumber requestLast=\"TRUE\">";
	private final String REQUEST_LAST_ACTION_COMPLETE = "<LastAction isComplete=\"TRUE\">";
	private final String REQUEST_LAST_ACTION_PENDING = "<ActionNumber requestPending=\"TRUE\">";
	private final String REQUEST_GAME_ID = "<GameIDRequest>";
	private final static String REQUEST_GAME_LOAD_SETUP = "<LoadGameSetup " + ACTION_NUMBER + " gameID=\"" + GAME_ID + "\" gameName=\"([A-Z0-9\\+]+)\"/>";
	private final static Pattern REQUEST_WITH_GAME_LOAD_SETUP_PATTERN = Pattern.compile (REQUEST_GAME_LOAD_SETUP);
	private final static String PLAYER_RECONNECT = "<Reconnect name=\"(.*)\">";
	private final static Pattern REQUEST_RECONNECT_WITH_NAME_PATTERN = Pattern.compile (PLAYER_RECONNECT);
	private final String PLAYER_READY = "<Ready>";
	private final String GAME_START = "<Start>";
	private final String PLAYER_ACTIVE = "<Active>";
	private final int NO_ACTION_NUMBER = -1;
	private final String STATUS_COMPLETE = "Complete";
	private final String STATUS_PENDING = "Pending";
	private final String STATUS_RECEIVED = "Received";
	private final int MIN_ACTION_NUMBER = 100;
	public final static String NO_GAME_ID = "NOID";
	public static final GameSupport NO_GAME_SUPPORT = null;
	public static String NO_FILE_NAME = "";

	NetworkActions networkActions;
	private LinkedList<ClientHandler> clients;
	private LinkedList<String> clientNames;
	ServerFrame serverFrame;
	String gameStatus;
	int actionNumber;
	String gameID;
	Logger logger;
	String autoSaveFileName = NO_FILE_NAME;
	File autoSaveFile = FileUtils.NO_FILE;
	FileUtils fileUtils = FileUtils.NO_FILE_UTILS;
	boolean goodFileWriter = false;
	boolean doAutoSave = true;

	public void setDoAutoSave (boolean aDoAutoSave) {
		doAutoSave = aDoAutoSave;
	}

	public void printInfo () {
		System.out.println ("Game Support Info");
		System.out.println ("Game ID " + gameID + " Status " + gameStatus + " Last Action Number " + actionNumber);
		if (clients != null) {
			System.out.println ("Client Handler Count is " + clients.size ());
		} else {
			System.out.println ("Client Handler Count is ZERO");
		}
		if (clientNames != null) {
			System.out.println ("Client Names count is " + clientNames.size ());

		} else {
			System.out.println ("Client Names count is ZERO");
		}
		System.out.println ("Network Action Count " + networkActions.getCount ());
		if (autoSaveFileName == NO_FILE_NAME) {
			System.out.println ("Auto Save File Name NOT SET");
		} else {
			System.out.println ("Auto Save File Name [" + autoSaveFileName + "]");
		}
		System.out.println ("Good File Writer " + goodFileWriter);
		if (fileUtils == FileUtils.NO_FILE_UTILS) {
			System.out.println ("File Utils NOT Set");
		} else {
			fileUtils.printInfo ();
		}
//		networkActions.printInfo ();
	}

	public GameSupport (ServerFrame aServerFrame, String aNewGameID, Logger aLogger) {
		setServerFrame (aServerFrame);
		networkActions = new NetworkActions ();
		clientNames = new LinkedList<String> ();
		clients = new LinkedList<ClientHandler> ();
		setActionNumber (MIN_ACTION_NUMBER);
		setGameID (aNewGameID);
		setLogger (aLogger);
		setupAllAutoSaveFunctionality (aLogger);
		setGameStatus (SavedGame.STATUS_PREPARED);
	}

	public void setServerFrame (ServerFrame aServerFrame) {
		serverFrame = aServerFrame;
	}

	public ServerFrame getServerFrame () {
		return serverFrame;
	}

	public void setGameStatus (String aGameStatus) {
		gameStatus = aGameStatus;
	}

	public String getGameStatus () {
		return gameStatus;
	}

	public void setLogger (Logger aLogger) {
		logger = aLogger;
	}

	public ClientHandler.SEND_TO whoGetsResponse (String aRequest) {
		ClientHandler.SEND_TO tWhoGetsResponse = ClientHandler.SEND_TO.Requestor;
		String tBaseRequest;

		tBaseRequest = getBaseRequest (aRequest);
		if (isRequestForStart (tBaseRequest) || isRequestForReady (tBaseRequest) || isRequestForActive (tBaseRequest)) {
			tWhoGetsResponse = ClientHandler.SEND_TO.AllButRequestor;
		}

		return tWhoGetsResponse;
	}
	// ----------------- Auto Save Functions ---------------

	public void setupAllAutoSaveFunctionality (Logger aLogger) {
		FileUtils tFileUtils;
		String tGameID;

		tFileUtils = new FileUtils (logger);
		setFileUtils (tFileUtils);
		tGameID = getGameID ();
		setupAutoSaveFile (tGameID);
	}

	public void setFileUtils (FileUtils aFileUtils) {
		fileUtils = aFileUtils;
	}

	public void setupAutoSaveFile (String aGameID) {
		String tDirectoryName;

		if (autoSaveFile == null) {
			tDirectoryName = serverFrame.getFullASDirectory ();
			autoSaveFileName = constructAutoSaveFileName (tDirectoryName, aGameID);
			if (!autoSaveFileName.equals (NO_FILE_NAME)) {
				setAutoSaveFile (new File (autoSaveFileName));
			}
		}
	}

	public String constructAutoSaveFileName (String tDirectoryName, String aGameID) {
		String tAutoSaveFileName = NO_FILE_NAME;

		// When running via Eclipse, will save AutoSaves to
		// /Volumes/Public/GIT/NetGameServer/NetGameServer/NetworkAutoSaves/18XX
		// on Drobo
		// /Users/marksmith/git/NetGameServer/NetGameServer/NetworkAutoSaves/18XX
		// on Local Mac

		if (!GameSupport.NO_GAME_ID.equals (aGameID)) {
			tAutoSaveFileName = tDirectoryName + File.separator + aGameID + ".autoSave";
		}

		return tAutoSaveFileName;
	}

	public void setAutoSaveFile (File aFile) {
		autoSaveFile = aFile;
		fileUtils.setFile (aFile);
	}

	// -------------------- End Auto Save Functions ----------------

	public void autoSave () {
		SavedGame tSavedGame;

		if (doAutoSave) {
			if (!fileUtils.fileWriterIsSetup () || !fileUtils.fileIsSetup ()) {
				setupAutoSaveFile (gameID);
				goodFileWriter = fileUtils.setupFileWriter ();
			}
			if (goodFileWriter) {
				fileUtils.startXMLFileOutput ();
				fileUtils.outputToFile ("<NetworkSaveGame gameID=\"" + getGameID () + "\" status=\"" + getGameStatus ()
						+ "\" lastActionNumber=\"" + getLastActionNumber () + "\">");
				writeClientsInXML ();
				networkActions.writeAllActions (fileUtils);
				fileUtils.outputToFile ("</NetworkSaveGame>");
				fileUtils.closeFile ();
				tSavedGame = serverFrame.getSavedGameFor (gameID);
				tSavedGame.setLastActionNumber (actionNumber);
			}
		}
	}

	private void writeClientsInXML () {
		String tPlayerStatus;

		fileUtils.outputToFile ("<Players>");
		for (String tPlayerName : clientNames) {
			tPlayerStatus = getPlayerStatus (tPlayerName);
			if (tPlayerName.length () > 0) {
				tPlayerName = "<Player name=\"" + tPlayerName + "\" status=\"" + tPlayerStatus + "\"/>";
				fileUtils.outputToFile (tPlayerName);
			}
		}

		fileUtils.outputToFile ("</Players>");
	}

	public void loadAutoSave () {
		XMLDocument tXMLAutoSaveDocument;

		if (networkActions.getCount () == 0) {
			if (autoSaveFile == null) {
				logger.error ("AutoSave File not set yet");
			} else {
				tXMLAutoSaveDocument = fileUtils.loadXMLFile (autoSaveFile);
				if (tXMLAutoSaveDocument != XMLDocument.NO_XML_DOCUMENT) {
					loadXMLAutoSave (tXMLAutoSaveDocument);
				} else {
					logger.error ("Loading of AutoSave File failed to get Valid XML Document");
				}
			}
		} else {
			System.out.println ("Network Actions already loaded");
		}
	}

	public void loadXMLAutoSave (XMLDocument aXMLAutoSaveDocument) {
		XMLNode tXMLSaveGame, tChildNode;
		NodeList tChildren;
		int tChildrenCount, tIndex, tLastActionNumber;
		String tChildName;

		tXMLSaveGame = aXMLAutoSaveDocument.getDocumentElement ();
		tChildren = tXMLSaveGame.getChildNodes ();
		tChildrenCount = tChildren.getLength ();
		tLastActionNumber = 0;
		for (tIndex = 0; tIndex < tChildrenCount; tIndex++) {
			tChildNode = new XMLNode (tChildren.item (tIndex));
			tChildName = tChildNode.getNodeName ();
			if (NetworkActions.EN_NETWORK_ACTIONS.equals (tChildName)) {
				networkActions.loadSavedActions (tChildNode);
				tLastActionNumber = networkActions.getLastNetworkActionNumber ();
				setActionNumber (tLastActionNumber);
			} else if (EN_PLAYERS.equals (tChildName)) {
				loadClientNames (tChildNode);
			}
		}
		System.out.println (
				"Total Actions Found " + networkActions.getCount () + " Last Action Number " + tLastActionNumber);
	}

	public String handleGameSupportRequest (String aRequest, ClientHandler aClientHandler) {
		String tBaseRequest;
		String tGSResponse = BAD_REQUEST;

		if (NO_GAME_ID.equals (gameID)) {
			updateGameID (aRequest);
		}
		if (isRequestForThisGame (aRequest)) {
			tBaseRequest = getBaseRequest (aRequest);
			tGSResponse = generateGSResponse (tBaseRequest, aClientHandler);
		} else if (isRequestForAnyGame (aRequest)) {
			tBaseRequest = getBaseRequestNoGameID (aRequest);
			tGSResponse = generateGSResponse (tBaseRequest, aClientHandler);
		} else {
			tGSResponse = BAD_GAME_ID;
		}
//		printInfo ();
		
		return tGSResponse;
	}

	public String getGameIdFromRequest (String aRequest) {
		String tGameID = NO_GAME_ID;
		String tBaseRequest;

		if (isRequestWithGameID (aRequest)) {
			tBaseRequest = getBaseRequest (aRequest);
			if (isRequestForReady (tBaseRequest)) {
				tGameID = getGameID (aRequest);
			} else if (isRequestForReconnect (tBaseRequest)) {
				tGameID = getGameID (aRequest);
			}
		}
		if (isRequestForAnyGame (aRequest)) {
			tBaseRequest = getBaseRequestNoGameID (aRequest);
			if (isRequestForGameLoadSetup (tBaseRequest)) {
				tGameID = getGameIDFromLoadRequest (tBaseRequest);
			}
		}

		return tGameID;
	}

	public void updateGameID (String aRequest) {
		String tBaseRequest;
		String tGameID;

		tBaseRequest = getBaseRequest (aRequest);
		if (isRequestForReady (tBaseRequest)) {
			tGameID = getGameID (aRequest);
			resetGameID (tGameID);
		} else if (isRequestForReconnect (tBaseRequest)) {
			tGameID = getGameID (aRequest);
			resetGameID (tGameID);
		}
	}

	public void handleGameActivityRequest (String aRequest) {
		String tAction;
		String tNumberMatched;
		int tActionNumber;
		Matcher tMatcher = GA_WITH_NO_GAME_ID_PATTERN.matcher (aRequest);
		Matcher tMatcherRemoval = GA_REMOVE_NO_GAME_ID_PATTERN.matcher (aRequest);

		if (tMatcherRemoval.find ()) {
			tNumberMatched = tMatcherRemoval.group (2);
			tActionNumber = Integer.parseInt (tNumberMatched);
			removeAction (tActionNumber);
		} else if (tMatcher.find ()) {
			tAction = tMatcher.group (1);
			updateLastAction (tAction);
			setGameStatus (SavedGame.STATUS_ACTIVE);
			autoSave ();
		} else {
			System.err.println ("Handle Action not matched [" + aRequest + "]");
		}
	}

	public void removeAction (int aActionNumber) {
		int tLastActionNumber;

		System.out.println ("Ready to Remove Action " + aActionNumber + " Count Before " + networkActions.getCount ());
		networkActions.remove (aActionNumber);
		tLastActionNumber = networkActions.getLastNetworkActionNumber ();
		setActionNumber (tLastActionNumber);
		serverFrame.removeGameAction ();
		System.out.println ("Action Removed " + aActionNumber + " Count After " + networkActions.getCount ()
				+ " Reset Action Number to " + tLastActionNumber);
	}

	public void updateLastAction (String aAction) {
		NetworkAction tNetworkAction;
		String tGameAction;
		
		tNetworkAction = getLastNetworkAction ();
		if (tNetworkAction != NetworkAction.NO_ACTION) {
			tNetworkAction.setActionXML (aAction);
			tNetworkAction.setStatus (STATUS_COMPLETE);
			setActionNumber (tNetworkAction.getNumber ());
			tGameAction = tNetworkAction.getCompactAction ();
			serverFrame.addGameAction (tGameAction);
		} else {
			System.err.println ("Found No Last Action");
		}
	}

	public NetworkAction getLastNetworkAction () {
		NetworkAction tNetworkAction;

		tNetworkAction = networkActions.getLastNetworkAction ();

		return tNetworkAction;
	}

	public String generateGSResponse (String aRequest, ClientHandler aClientHandler) {
		int tNewActionNumber;
		String tGSResponse = BAD_REQUEST;

		if (isRequestForHeartbeat (aRequest)) {
			tGSResponse = generateGSResponseHearbeat (aClientHandler);
		} else {
			if (isRequestForActionNumber (aRequest)) {
				tNewActionNumber = getNewActionNumber ();
				tGSResponse = generateGSReponseNewAN (tNewActionNumber);
			} else if (isRequestForLastAction (aRequest) || isRequestForLastActionIsComplete (aRequest)) {
				tGSResponse = generateGSReponseRequestLast ();
			} else if (isRequestForLastActionIsPending (aRequest)) {
				tGSResponse = generateGSReponsePending (actionNumber);
			} else if (isRequestForAction (aRequest)) {
				tGSResponse = generateGSResponseRequestAction (aRequest);
			} else if (isRequestForGameID (aRequest)) {
				tGSResponse = generateGSResponseGameID (aClientHandler);
			} else if (isRequestForStart (aRequest)) {
				aClientHandler.startAllClientsInGame ();
				tGSResponse = aClientHandler.getName () + " Starts the Game";
			} else if (isRequestForReady (aRequest)) {
				tGSResponse = handleClientIsReady (aClientHandler);
			} else if (isRequestForActive (aRequest)) {
				tGSResponse = handleClientIsActive (aClientHandler);
			} else if (isRequestForReconnect (aRequest)) {
				updateClientHandlers (aRequest, aClientHandler);
				tGSResponse = generateGSResponseReconnect (aClientHandler);
			} else if (isRequestForGameLoadSetup (aRequest)) {
				tGSResponse = handleGSResponseGameLoadSetup (aRequest, aClientHandler);
			} else if (isRequestForSavedGamesFor (aRequest)) {
				tGSResponse = handleGSResponseRequestSavedGamesFor (aRequest);
			}
		}

		return tGSResponse;
	}

	private String handleClientIsReady (ClientHandler aClientHandler) {
		String tGameName;
		String tGSResponse;
		ClientHandler tFirstClientHandler;
		String tPlayerName;

		tFirstClientHandler = clients.get (0);
		tGameName = tFirstClientHandler.getGameName ();
		aClientHandler.setGameName (tGameName);
		aClientHandler.setGameSupport (this);
		aClientHandler.handleClientIsReady ();
		addClientHandler (aClientHandler);
		tPlayerName = aClientHandler.getName ();
		serverFrame.addPlayerToSavedGame (gameID, tPlayerName);
		tGSResponse = aClientHandler.getName () + " is Ready to play the Game";

		return tGSResponse;
	}

	public String handleClientIsActive (ClientHandler aClientHandler) {
		String tGameName;
		String tGSResponse;
		ClientHandler tFirstClientHandler;
		String tPlayerName;

		tFirstClientHandler = clients.get (0);
		tGameName = tFirstClientHandler.getGameName ();
		aClientHandler.setGameName (tGameName);
		aClientHandler.setGameSupport (this);
		aClientHandler.startClient ();
		addClientHandler (aClientHandler);
		tPlayerName = aClientHandler.getName ();
		serverFrame.addPlayerToSavedGame (gameID, tPlayerName);
		tGSResponse = aClientHandler.getName () + " is Active in the Game";

		return tGSResponse;

	}

	public void clearAllNetworkActions () {
		networkActions.clearAll ();
		actionNumber = MIN_ACTION_NUMBER;
	}

	public boolean resetGameID (String aGameID) {
		Matcher tMatcher = GAME_ID_PATTERN.matcher (aGameID);
		boolean tGameIDReset = false;

		if (tMatcher.matches ()) {
			setGameID (aGameID);
			tGameIDReset = true;
		}

		return tGameIDReset;
	}

	private void setGameID (String aGameID) {
		gameID = aGameID;
	}

	public String getGameID () {
		return gameID;
	}

	public String getGameID (String aRequest) {
		Matcher tMatcher = GS_WITH_GAME_ID_PATTERN.matcher (aRequest);
		String tFoundGameID = NO_GAME_ID;

		if (tMatcher.find ()) {
			tFoundGameID = tMatcher.group (1);
		}

		return tFoundGameID;
	}

	// Various routines to test if a Request matches what is expected

	public boolean isRequestForAnyGame (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher1 = GS_WITH_NO_GAME_ID_PATTERN.matcher (aRequest);

		if (tMatcher1.find ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestWithGameID (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher1 = GS_WITH_GAME_ID_PATTERN.matcher (aRequest);

		if (tMatcher1.find ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestForThisGame (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = GS_WITH_GAME_ID_PATTERN.matcher (aRequest);
		String tFoundGameID;

		if (tMatcher.find ()) {
			tFoundGameID = tMatcher.group (1);
			tRequestIsValid = gameID.equals (tFoundGameID);
		}

		return tRequestIsValid;
	}

	public String getBaseRequest (String aRequest) {
		Matcher tMatcher = GS_WITH_GAME_ID_PATTERN.matcher (aRequest);
		String tBaseRequest = BAD_REQUEST;

		if (tMatcher.find ()) {
			tBaseRequest = tMatcher.group (2);
		}

		return tBaseRequest;
	}

	public String getBaseRequestNoGameID (String aRequest) {
		Matcher tMatcher = GS_WITH_NO_GAME_ID_PATTERN.matcher (aRequest);
		String tBaseRequest = BAD_REQUEST;

		if (tMatcher.find ()) {
			tBaseRequest = tMatcher.group (1);
		}

		return tBaseRequest;
	}

	public boolean isRequestForGameActivity (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = GA_WITH_NO_GAME_ID_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	private boolean isValidRequestFor (String aRequest, String aMatch) {
		boolean tRequestIsValid = false;

		if (aMatch.equals (aRequest)) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestForGameID (String aRequest) {
		return isValidRequestFor (aRequest, REQUEST_GAME_ID);
	}

	public boolean isRequestForStart (String aRequest) {
		return isValidRequestFor (aRequest, GAME_START);
	}

	public boolean isRequestForReady (String aRequest) {
		return isValidRequestFor (aRequest, PLAYER_READY);
	}

	public boolean isRequestForActive (String aRequest) {
		return isValidRequestFor (aRequest, PLAYER_ACTIVE);
	}

	public boolean isRequestForActionNumber (String aRequest) {
		return isValidRequestFor (aRequest, REQUEST_ACTION_NUMBER);
	}

	public boolean isRequestForLastAction (String aRequest) {
		return isValidRequestFor (aRequest, REQUEST_LAST_ACTION);
	}

	public boolean isRequestForLastActionIsComplete (String aRequest) {
		return isValidRequestFor (aRequest, REQUEST_LAST_ACTION_COMPLETE);
	}

	public boolean isRequestForLastActionIsPending (String aRequest) {
		return isValidRequestFor (aRequest, REQUEST_LAST_ACTION_PENDING);
	}

	public boolean isRequestForReconnect (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_RECONNECT_WITH_NAME_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestForAction (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_ACTION_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestForGameLoadSetup (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_WITH_GAME_LOAD_SETUP_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}

	public boolean isRequestForSavedGamesFor (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_SAVED_GAMES_FOR_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public boolean isRequestForHeartbeat (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_HEARTBEAT_PATTERN.matcher (aRequest);

		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}

		return tRequestIsValid;
	}

	public int getNewActionNumber () {
		int tNewActionNumber;

		tNewActionNumber = generateNewActionNumber ();

		if (tNewActionNumber == actionNumber) {
			tNewActionNumber = NO_ACTION_NUMBER;
		} else {
			setActionNumber (tNewActionNumber);
		}

		return tNewActionNumber;
	}

	public void setActionNumber (int aActionNumber) {
		actionNumber = aActionNumber;
	}

	public int generateNewActionNumber () {
		int tNewActionNumber;
		NetworkAction tNewNetworkAction;

		tNewActionNumber = networkActions.getLastNetworkActionNumber ();
		if (isLastActionComplete ()) {
			tNewActionNumber++;
			tNewNetworkAction = new NetworkAction (tNewActionNumber, NetworkAction.ACTION_PENDING);
			addNewNetworkAction (tNewNetworkAction);
		}

		return tNewActionNumber;
	}

	public void addNewNetworkAction (NetworkAction aNewNetworkAction) {
		networkActions.addNetworkAction (aNewNetworkAction);
	}

	public boolean isLastActionComplete () {
		boolean tIsLastActionComplete;
		String tLastActionStatus;

		tLastActionStatus = networkActions.getLastActionStatus ();
		tIsLastActionComplete = NetworkAction.ACTION_COMPLETE.equals (tLastActionStatus);

		return tIsLastActionComplete;
	}

	public boolean setStatus (String aNewStatus) {
		boolean tStatusUpdated = false;
		NetworkAction tNetworkAction;

		if (STATUS_COMPLETE.equals (aNewStatus) || STATUS_PENDING.equals (aNewStatus)
				|| STATUS_RECEIVED.equals (aNewStatus)) {
			if (networkActions.getCount () > 0) {
				tNetworkAction = networkActions.getLastNetworkAction ();
				tNetworkAction.setStatus (aNewStatus);
				tStatusUpdated = true;
			}
		}

		return tStatusUpdated;
	}

	public String getStatus () {
		return networkActions.getLastActionStatus ();
	}

	public String generateGSResponseGameID (ClientHandler aClientHandler) {
		String tGSResponse;

		if (gameID.equals (NO_GAME_ID)) {
			gameID = aClientHandler.generateGameID ();
		}
		tGSResponse = "<GSResponse gameID=\"" + gameID + "\">";

		return tGSResponse;
	}

	public String generateGSResponseReconnect (ClientHandler aClientHandler) {
		String tGSResponse;

		tGSResponse = wrapWithGSResponse ("<Reconnected>");

		return tGSResponse;
	}

	public String generateGSResponseHearbeat (ClientHandler aClientHandler) {
		String tGSResponse;

		tGSResponse = wrapWithGSResponse ("<Heartbeat>");

		return tGSResponse;
	}

	public String generateGSReponseNewAN (int aNewActionNumber) {
		String tGSResponse;

		if (aNewActionNumber != NO_ACTION_NUMBER) {
			tGSResponse = wrapWithGSResponse ("<ActionNumber newNumber=\"" + aNewActionNumber + "\">");
		} else {
			tGSResponse = wrapWithGSResponse ("<ActionNotComplete>");
		}

		return tGSResponse;
	}

	public String generateGSReponsePending (int aPendingActionNumber) {
		String tGSResponse;

		if (aPendingActionNumber != NO_ACTION_NUMBER) {
			if (this.isLastActionComplete ()) {
				tGSResponse = wrapWithGSResponse ("<ActionNotPending>");

			} else {
				tGSResponse = wrapWithGSResponse ("<ActionNumber pendingNumber=\"" + aPendingActionNumber + "\">");
			}
		} else {
			tGSResponse = wrapWithGSResponse ("<ActionNotPending>");
		}

		return tGSResponse;
	}

	public String generateGSReponseRequestLast () {
		String tGSResponse = "NONE";

		tGSResponse = wrapWithGSResponse ("<LastAction actionNumber=\"" + actionNumber + "\" status=\""
				+ networkActions.getLastActionStatus () + "\">");

		return tGSResponse;
	}

	private String wrapWithGSResponse (String tBody) {
		String tGSResponse;

		tGSResponse = "<GSResponse>" + tBody + "</GSResponse>";

		return tGSResponse;
	}

	private String getThisAction (int aActionNumber) {
		String tFoundAction;

		tFoundAction = networkActions.getActionXMLFor (aActionNumber);

		return tFoundAction;
	}

	public String generateGSResponseRequestAction (String aRequest) {
		String tGSResponse = BAD_REQUEST;
		String tNumberMatched, tBadResponse, tActionFound;
		int tActionNumber;
		Matcher tMatcher = REQUEST_ACTION_PATTERN.matcher (aRequest);

		if (tMatcher.find ()) {
			tNumberMatched = tMatcher.group (1);
			tActionNumber = Integer.parseInt (tNumberMatched);
			System.out.println (
					"Handling Request Action Number " + tActionNumber + " current Last Action Number " + actionNumber);
			if ((tActionNumber > MIN_ACTION_NUMBER) && (tActionNumber <= actionNumber)) {
				tActionFound = getThisAction (tActionNumber);
				tGSResponse = wrapWithGSResponse (tActionFound);
			} else {
				tBadResponse = "<ActionOutOfRange find=\"" + tActionNumber + "\" min=\"" + MIN_ACTION_NUMBER
						+ "\" max=\"" + actionNumber + "\" />";
				tGSResponse = wrapWithGSResponse (tBadResponse);
			}
		}

		return tGSResponse;
	}

	public void updateClientHandlers (String aRequest, ClientHandler aClientHandler) {
		Matcher tMatcher = REQUEST_RECONNECT_WITH_NAME_PATTERN.matcher (aRequest);
		String tClientName;
		boolean tSuccessfulUpdate;

		if (tMatcher.find ()) {
			tClientName = tMatcher.group (1);
			logger.info ("Ready to Update Client Handlers with Client Name " + tClientName + " to Game ID " + gameID);
			tSuccessfulUpdate = aClientHandler.updateClientHandlers (tClientName, gameID, this);
			if (tSuccessfulUpdate) {
				logger.info ("Successfully Updated to new Socket");
			}
		}
	}

	public String getGameIDFromLoadRequest (String aRequest) {
		Matcher tMatcher = REQUEST_WITH_GAME_LOAD_SETUP_PATTERN.matcher (aRequest);
		String tGameID = NO_GAME_ID;

		if (tMatcher.find ()) {
			tGameID = tMatcher.group (2);
		}

		return tGameID;
	}

	public String handleGSResponseGameLoadSetup (String aRequest, ClientHandler aClientHandler) {
		String tGSResponse = BAD_REQUEST;
		Matcher tMatcher = REQUEST_WITH_GAME_LOAD_SETUP_PATTERN.matcher (aRequest);
		String tGameID, tGameName, tActionNumberText;
		int tActionNumber;

		if (tMatcher.find ()) {
			tGameID = tMatcher.group (2);
			// If the GameID found in the request does not match the Game ID in the Client
			// Handler
			if (!tGameID.equals (aClientHandler.getGameID ())) {
				// Then update the Game Support for this GameID - Expected for first person to
				// join a saved Game
				if (!aClientHandler.updateGameSupport (tGameID)) {
					logger.info ("The Client Handler updated to Game ID " + tGameID);
					tActionNumberText = tMatcher.group (1);
					tGameName = tMatcher.group (3);
					setGameID (tGameID);
					tActionNumber = Integer.parseInt (tActionNumberText);
					setActionNumber (tActionNumber);
					setupAutoSaveFile (tGameID);
					loadAutoSave ();
					aClientHandler.addGameNameToList (tGameName);
					aClientHandler.addActiveGameSupport (this);
				}
			}
			tGSResponse = wrapWithGSResponse ("<GOOD>");
		}

		return tGSResponse;
	}

	public String handleGSResponseRequestSavedGamesFor (String aRequest) {
		String tGSResponse = BAD_REQUEST;
		Matcher tMatcher = REQUEST_SAVED_GAMES_FOR_PATTERN.matcher (aRequest);
		String tPlayerName;
		String tAllSavedGamesFor;
		String tSavedGamesFor;

		if (tMatcher.find ()) {
			tPlayerName = tMatcher.group (1);
			tSavedGamesFor = serverFrame.getSavedGamesFor (tPlayerName);
			if (tSavedGamesFor == null) {
				tSavedGamesFor = "";
			}
			tAllSavedGamesFor = "<SavedGames name=\"" + tPlayerName + "\">" + tSavedGamesFor + "</SavedGames>";
			tGSResponse = wrapWithGSResponse (tAllSavedGamesFor);
		}

		return tGSResponse;
	}

	public void addGameActionsToFrame () {
		networkActions.addGameActionsToFrame (serverFrame);
	}
	
	public int getLastActionNumber () {
		return networkActions.getLastNetworkActionNumber ();
	}

	public int getPlayerCount () {
		return clientNames.size ();
	}

	public String getPlayerIndex (int aIndex) {
		return clientNames.get (aIndex);
	}

	public String getPlayerStatus (String aPlayerName) {
		ClientHandler tFoundClientHandler;
		String tPlayerStatus = NOT_CONNECTED;

		tFoundClientHandler = getClientHandlerFor (aPlayerName);
		if (tFoundClientHandler != ClientHandler.NO_CLIENT_HANDLER) {
			tPlayerStatus = tFoundClientHandler.getPlayerStatus ();
		}

		return tPlayerStatus;
	}

	public ClientHandler getClientHandlerFor (String aClientName) {
		ClientHandler tFoundClientHandler;
		String tClientName;

		tFoundClientHandler = ClientHandler.NO_CLIENT_HANDLER;
		if (clients != ClientHandler.NO_CLIENT_HANDLERS) {
			for (ClientHandler tClientHandler : clients) {
				tClientName = tClientHandler.getName ();
				if (tClientName.equals (aClientName)) {
					tFoundClientHandler = tClientHandler;
				}
			}
		}

		return tFoundClientHandler;
	}

	public LinkedList<ClientHandler> getClientHandlers () {
		return clients;
	}

	public int getCountClientHandlers () {
		return clients.size ();
	}
	
	public void addClientHandler (ClientHandler aClientHandler) {
		String tClientName;
		String tNewClientName;
		boolean tAddClientHandler;
		
		if (aClientHandler != ClientHandler.NO_CLIENT_HANDLER) {
			tNewClientName = aClientHandler.getName ();
			tAddClientHandler = true;
			for (ClientHandler tClientHandler : clients) {
				tClientName = tClientHandler.getName ();
				// If Client is in the List, DO NOT Add --- don't want duplicates
				if (tNewClientName.equals (tClientName)) {
					tAddClientHandler = false;
				}
			}
			// If the Client was not found, add it
			if (tAddClientHandler) {
				clients.add (aClientHandler);
				addClientName (tNewClientName);
			}
		}
	}

	public void setClientHandlers (LinkedList<ClientHandler> aClients) {
		String tClientName;

		clients = aClients;
		if (clients != ClientHandler.NO_CLIENT_HANDLERS) {
			for (ClientHandler tClientHandler : clients) {
				tClientName = tClientHandler.getName ();
				addClientName (tClientName);
			}
		}
	}

	public void addClientName (String aClientName) {
		if (aClientName != ClientHandler.NO_CLIENT_NAME) {
			if (!clientNames.contains (aClientName)) {
				clientNames.add (aClientName);
			}
		}
	}

	public void loadClientNames (XMLNode aClientNames) {
		XMLNode tChildNode;
		NodeList tChildren;
		int tChildrenCount, tIndex;
		String tChildName;
		String tPlayerName;

		tChildren = aClientNames.getChildNodes ();
		tChildrenCount = tChildren.getLength ();
		for (tIndex = 0; tIndex < tChildrenCount; tIndex++) {
			tChildNode = new XMLNode (tChildren.item (tIndex));
			tChildName = tChildNode.getNodeName ();
			if (EN_PLAYER.equals (tChildName)) {
				tPlayerName = tChildNode.getThisAttribute (AN_NAME);
				addClientName (tPlayerName);
			}
		}

	}
}
