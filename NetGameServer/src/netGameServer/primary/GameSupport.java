package netGameServer.primary;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

import netGameServer.utilities.XMLNode;
import netGameServer.utilities.FileUtils;
import netGameServer.utilities.XMLDocument;

public class GameSupport {
	NetworkActions networkActions;
	private ArrayList<ClientHandler> clients;
	ServerFrame serverFrame;
	String gameStatus;
	int actionNumber;
	String gameID;
	Logger logger;
	
	private final String BAD_REQUEST = "<GSResponse><BadRequest></GSResponse>";
	private final String BAD_GAME_ID = "<GSResponse><BadGameID></GSResponse>";
	private final static String ACTION_NUMBER = "actionNumber=\"(\\d+)\"";
	private final static String REQUEST_ACTION = "<RequestAction "+ ACTION_NUMBER + ">";
	private final static Pattern REQUEST_ACTION_PATTERN = Pattern.compile (REQUEST_ACTION);
	private final static String GAME_ID = "(\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d\\d)";
	private final static Pattern GAME_ID_PATTERN = Pattern.compile (GAME_ID);
	private final static String GS_WITH_GAME_ID = "<GS gameID=\"" + GAME_ID + "\">(.*)</GS>";
	private final static Pattern GS_WITH_GAME_ID_PATTERN = Pattern.compile (GS_WITH_GAME_ID);
	private final static String GS_WITH_NO_GAME_ID = "<GS>(.*)</GS>";
	private final static Pattern GS_WITH_NO_GAME_ID_PATTERN = Pattern.compile (GS_WITH_NO_GAME_ID);
	private final static String GA_WITH_ACTION = "<GA>(<Action.*)</GA>";
	private final static Pattern GA_WITH_NO_GAME_ID_PATTERN = Pattern.compile (GA_WITH_ACTION);
	private final static String REQUEST_HEARTBEAT = "<Heartbeat>";
	private final static Pattern REQUEST_HEARTBEAT_PATTERN = Pattern.compile (REQUEST_HEARTBEAT);
	private final String REQUEST_ACTION_NUMBER = "<ActionNumber requestNew=\"TRUE\">";
	private final static String REQUEST_SAVED_GAMES_FOR = "<RequestSavedGames player=\"([A-Za-z][A-Za-z0-9]+)\"/?>";
	private final static Pattern REQUEST_SAVED_GAMES_FOR_PATTERN = Pattern.compile (REQUEST_SAVED_GAMES_FOR);
	private final String REQUEST_LAST_ACTION = "<ActionNumber requestLast=\"TRUE\">";
	private final String REQUEST_LAST_ACTION_COMPLETE = "<LastAction isComplete=\"TRUE\">";
	private final String REQUEST_LAST_ACTION_PENDING = "<ActionNumber requestPending=\"TRUE\">";
	private final String REQUEST_GAME_ID = "<GameIDRequest>";
	private final static String REQUEST_GAME_LOAD_SETUP = 
				"<LoadGameSetup gameID=\"" + GAME_ID + "\" " + ACTION_NUMBER + " gameName=\"([A-Z0-9]+)\">";
	private final static Pattern REQUEST_WITH_GAME_LOAD_SETUP_PATTERN = Pattern.compile (REQUEST_GAME_LOAD_SETUP);
	private final static String PLAYER_RECONNECT = "<Reconnect name=\"(.*)\">";
	private final static Pattern REQUEST_RECONNECT_WITH_NAME_PATTERN = Pattern.compile (PLAYER_RECONNECT);
	private final String PLAYER_READY = "<Ready>";
	private final String GAME_START = "<Start>";
	private final int NO_ACTION_NUMBER = -1;
	public final String STATUS_PREPARED = "PREPARED";
	private final String STATUS_COMPLETE = "Complete";
	private final String STATUS_PENDING = "Pending";
	private final String STATUS_RECEIVED = "Received";
	private final int MIN_ACTION_NUMBER = 100;
	public final static String NO_GAME_ID = "NOID";
	public static final GameSupport NO_GAME_SUPPORT = null;
	public static String NO_FILE_NAME = "";
	String autoSaveFileName = NO_FILE_NAME;
	File autoSaveFile = null;
	FileUtils fileUtils;
	boolean goodFileWriter = false;
	
	public GameSupport (ServerFrame aServerFrame, String aNewGameID, Logger aLogger) {
		setServerFrame (aServerFrame);
		networkActions = new NetworkActions ();
		setActionNumber (MIN_ACTION_NUMBER);
		setGameID (aNewGameID);
		setLogger (aLogger);
		setupAllAutoSaveFunctionality (aLogger);
		setGameStatus (STATUS_PREPARED);
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
		if (isRequestForStart (tBaseRequest) || isRequestForReady (tBaseRequest)) {
			tWhoGetsResponse = ClientHandler.SEND_TO.AllButRequestor;
		}
		
		return tWhoGetsResponse;
	}
	// -----------------  Auto Save Functions ---------------
	
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
			if (! autoSaveFileName.equals (NO_FILE_NAME)) {
				setAutoSaveFile (new File (autoSaveFileName));	
			}
		}
	}
	
	private String constructAutoSaveFileName (String tDirectoryName, String aGameID) {
		String tAutoSaveFileName = NO_FILE_NAME;
		
		// When running via Eclipse, will save AutoSaves to
		// /Volumes/Public/GIT/NetGameServer/NetGameServer/NetworkAutoSaves/18XX
		// on Drobo
		
		if (! GameSupport.NO_GAME_ID.equals (aGameID) ) {
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
		if (! fileUtils.fileWriterIsSetup ()) {
			goodFileWriter = fileUtils.setupFileWriter ();
		}
		if (goodFileWriter) {
			fileUtils.startXMLFileOutput ();
			fileUtils.outputToFile ("<NetworkSaveGame gameID=\"" + getGameID () + "\" status=\"" + 
					getGameStatus () + "\" lastActionNumber=\"" + getLastActionNumber () +"\">");
			writeClientsInXML ();
			networkActions.writeAllActions (fileUtils);
			fileUtils.outputToFile ("</NetworkSaveGame>");
			fileUtils.closeFile ();
		}
	}
	
	private void writeClientsInXML () {
		String tPlayerName;
		String tPlayerStatus;
		
		fileUtils.outputToFile ("<Players>");
		for (ClientHandler tClientHandler : clients) {
			tPlayerName = tClientHandler.getName ();
			tPlayerStatus = tClientHandler.getPlayerStatus ();
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
				if (tXMLAutoSaveDocument != FileUtils.NO_VALID_XML_DOCUMENT) {
					System.out.println ("Ready to parse Loaded AutoSaved NetGame Network Actions");
					loadXMLNetworkActions (tXMLAutoSaveDocument);
				} else {
					logger.error("Loading of AutoSave File failed to get Valid XML Document");
				}
			}
		} else {
			System.out.println ("Network Actions already loaded");
		}
	}
	
	public void loadXMLNetworkActions (XMLDocument aXMLAutoSaveDocument) {
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
			}
		}
		System.out.println ("Total Actions Found " + networkActions.getCount () + " Last Action Number " + tLastActionNumber);
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
		
		return tGSResponse;
	}
	
	public String getGameIdFromRequest (String aRequest) {
		String tGameID = NO_GAME_ID;
		String tBaseRequest;
		
		if (isRequestWithGameID (aRequest)) {
			tBaseRequest = getBaseRequest (aRequest);
			if (isRequestForReady (tBaseRequest)) {
				tGameID = this.getGameID (aRequest);
			} else if (isRequestForReconnect (tBaseRequest)) {
				tGameID = this.getGameID (aRequest);
			}
		}
		if (isRequestForAnyGame (aRequest)) {
			tBaseRequest = getBaseRequestNoGameID (aRequest);
			if (this.isRequestForGameLoadSetup (tBaseRequest)) {
				tGameID = this.getGameIDFromLoadRequest (tBaseRequest);
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
		Matcher tMatcher = GA_WITH_NO_GAME_ID_PATTERN.matcher (aRequest);

		if (tMatcher.find ()) {
			tAction = tMatcher.group (1);
			updateLastAction (tAction);
			autoSave ();
		} else {

		}
	}

	public void updateLastAction (String aAction) {
		NetworkAction tNetworkAction;
		
		tNetworkAction = getLastNetworkAction ();
		tNetworkAction.setActionXML (aAction);
		tNetworkAction.setStatus (STATUS_COMPLETE);
	}
	
	public NetworkAction getLastNetworkAction () {
		NetworkAction tNetworkAction;
		
		tNetworkAction = networkActions.getLastNetworkAction ();
		
		return tNetworkAction;
	}
	
	public String generateGSResponse (String aRequest, ClientHandler aClientHandler) {
		int tNewActionNumber;
		String tGSResponse = BAD_REQUEST;
		
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
		} else if (isRequestForHeartbeat (aRequest)) {
			tGSResponse = generateGSResponseHearbeat (aClientHandler);
		} else if (isRequestForStart (aRequest)) {
			aClientHandler.handleClientIsStarting ();
			tGSResponse = aClientHandler.getName () + " Starts the Game";
		} else if (isRequestForReady (aRequest)) {
			aClientHandler.handleClientIsReady ();
			tGSResponse = aClientHandler.getName () + " is Ready to play the Game";
		} else if (isRequestForReconnect (aRequest)) {
			updateClientHandlers (aRequest, aClientHandler);
			tGSResponse = generateGSResponseReconnect (aClientHandler);
		} else if (isRequestForGameLoadSetup (aRequest)) {
			tGSResponse = handleGSResponseGameLoadSetup (aRequest, aClientHandler);
		} else if (isRequestForSavedGamesFor (aRequest)) {
			tGSResponse = handleGSResponseRequestSavedGamesFor (aRequest);
		}
		
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
	
	public boolean isRequestForGameID (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (REQUEST_GAME_ID.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;		
	}
	
	public boolean isRequestForStart (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (GAME_START.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForReady (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (PLAYER_READY.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForReconnect (String aRequest) {
		boolean tRequestIsValid = false;
		Matcher tMatcher = REQUEST_RECONNECT_WITH_NAME_PATTERN.matcher (aRequest);
		
		if (tMatcher.matches ()) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForActionNumber (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (REQUEST_ACTION_NUMBER.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForLastAction (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (REQUEST_LAST_ACTION.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForLastActionIsComplete (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (REQUEST_LAST_ACTION_COMPLETE.equals (aRequest)) {
			tRequestIsValid = true;
		}
		
		return tRequestIsValid;
	}
	
	public boolean isRequestForLastActionIsPending (String aRequest) {
		boolean tRequestIsValid = false;
		
		if (REQUEST_LAST_ACTION_PENDING.equals (aRequest)) {
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
			networkActions.addNetworkAction (tNewNetworkAction);
		}
		
		return tNewActionNumber;
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
		
		if (STATUS_COMPLETE.equals (aNewStatus) ||
			STATUS_PENDING.equals (aNewStatus) ||
			STATUS_RECEIVED.equals (aNewStatus)) {
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
			tGSResponse = wrapWithGSResponse ("<ActionNumber newNumber=\"" + 
				aNewActionNumber + "\">");
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
				tGSResponse = wrapWithGSResponse ("<ActionNumber pendingNumber=\"" + 
						aPendingActionNumber + "\">");
			}
		} else {
			tGSResponse = wrapWithGSResponse ("<ActionNotPending>");
		}

		return tGSResponse;
	}
	
	public String generateGSReponseRequestLast () {
		String tGSResponse = "NONE";
		
		tGSResponse = wrapWithGSResponse ("<LastAction actionNumber=\"" + 
							actionNumber + "\" status=\"" +
							networkActions.getLastActionStatus () + "\">");
		
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
		String tNumberMatched, tBadResponse;
		int tActionNumber;
		Matcher tMatcher = REQUEST_ACTION_PATTERN.matcher (aRequest);
		
		if (tMatcher.find ()) {
			tNumberMatched = tMatcher.group (1);
			tActionNumber = Integer.parseInt (tNumberMatched);
			if ((tActionNumber > MIN_ACTION_NUMBER) && (tActionNumber <= actionNumber)) {
				tGSResponse = wrapWithGSResponse (getThisAction (tActionNumber));
			} else {
				tBadResponse = "<ActionOutOfRange find=\"" + tActionNumber + "\" min=\""
						+ MIN_ACTION_NUMBER + "\" max=\"" + actionNumber + "\" />";
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
			logger.info ("Ready to Update Client Handlers with Client Name " + 
						tClientName + " to Game ID " + gameID);
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
			tGameID = tMatcher.group (1);
		}
		
		return tGameID;
	}
	
	public String handleGSResponseGameLoadSetup (String aRequest, ClientHandler aClientHandler) {
		String tGSResponse = BAD_REQUEST;
		Matcher tMatcher = REQUEST_WITH_GAME_LOAD_SETUP_PATTERN.matcher (aRequest);
		String tGameID, tGameName, tActionNumberText;
		int tActionNumber;
		
		if (tMatcher.find ()) {
			tGameID = tMatcher.group (1);
			tActionNumberText = tMatcher.group (2);
			tGameName = tMatcher.group (3);
			setGameID (tGameID);
			tActionNumber = Integer.parseInt (tActionNumberText);
			setActionNumber (tActionNumber);
			setupAutoSaveFile (tGameID);
			loadAutoSave ();
			aClientHandler.addGameNameToList (tGameName);
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

	public int getLastActionNumber() {
		return networkActions.getLastNetworkActionNumber ();
	}
	
	public void setClientHandlers (ArrayList<ClientHandler> aClients) {
		clients = aClients;
	}
}
