package netGameServer.primary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SavedGame {
	String gameID;
	String gameStatus;
	int lastActionNumber;
	ArrayList<String> players;
	private final static String GAME_ID = "(\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d\\d)";
	private final static String NSG_WITH_GAME_ID = "<NetworkSaveGame gameID=\"" + GAME_ID + "\" status=\"(.*)\" lastActionNumber=\"(\\d+)\"/?>";
	private final static Pattern NSG_WITH_GAME_ID_PATTERN = Pattern.compile (NSG_WITH_GAME_ID);
	private final static String PLAYER_WITH_NAME = "<Player name=\"(.*)\" status=\"(.*)\"/?>"; 
	private final static Pattern PLAYER_WITH_NAME_PATTERN = Pattern.compile (PLAYER_WITH_NAME);
	public final static SavedGame NO_GAME = null;
	public final static ArrayList<String> NO_PLAYERS = null;
	public final static String STATUS_PREPARED = "PREPARED";
	public final static String STATUS_ACTIVE = "ACTIVE";
	public final static String STATUS_INACTIVE = "INACTIVE";
	public final static String STATUS_COMPLETED = "COMPLETED";
	public final static String NO_STATUS = "NO_STATUS";
	public final static String NO_GAME_ID = "NOID";
	public final static String NO_NAME = "NO_NAME";
	public final static String TEST_FILE = "JunitTestFile";
	public final static int BAD_ACTION_NUMBER = -1;
	
	public SavedGame (String aFileName) throws FileNotFoundException {
		setupPlayers();
		if (aFileName != null) {
			readFile (aFileName);
		} else {
			throw (new FileNotFoundException ("Null File Name"));
		}
	}

	public SavedGame (String aFilePath, String aGameID, String aPlayerName) {
		setupPlayers();
		setGameID (aGameID);
		addPlayer (aPlayerName);
		setGameStatus (NO_STATUS);
		setLastActionNumber (0);
	}
	
	private void setupPlayers() {
		players = new ArrayList<String> ();
	}

	private void readFile (String aFileName) {
		FileReader tFile;
		BufferedReader tReader;
		String tLine, tGameID, tPlayerName, tGameStatus;
		int tLastActionNumber;
		
		if (! TEST_FILE.equals (aFileName)) {
			try {
				tFile = new FileReader (aFileName);
				tReader = new BufferedReader (tFile);
				while ((tLine = tReader.readLine()) != null) {
					tGameID = getGameIDFromLine (tLine);
					if (tGameID.equals (NO_GAME_ID)) {
						tPlayerName = getPlayerNameFromLine (tLine);
						if (! tPlayerName.equals (NO_NAME)) {
							addPlayer (tPlayerName);
						}
					} else {
						setGameID (tGameID);
						tGameStatus = getGameStatusFromLine (tLine);
						setGameStatus (tGameStatus);
						tLastActionNumber = getLastActionNumberFromLine (tLine);
						setLastActionNumber (tLastActionNumber);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			setGameID ("");
			setLastActionNumber (0);
			setGameStatus ("");
		}
	}
	
	public void addPlayer (String aPlayerName) {
		if (! hasPlayer (aPlayerName)) {
			players.add (aPlayerName);
		}
	}
	
	public void setGameID (String aGameID) {
		gameID = aGameID;
	}
	
	public String getGameID () {
		return gameID;
	}
	
	public void setGameStatus (String aGameStatus) {
		gameStatus = aGameStatus;
	}
	
	public String getGameStatus () {
		return gameStatus;
	}
	
	public void setLastActionNumber (int aLastActionNumber) {
		lastActionNumber = aLastActionNumber;
	}
	
	public int getLastActionNumber () {
		return lastActionNumber;
	}
	
	public int getPlayerCount () {
		return players.size ();
	}
	
	public int getLastActionNumberFromLine (String aRequest) {
		Matcher tMatcher = NSG_WITH_GAME_ID_PATTERN.matcher (aRequest);
		int tLastActionNumber = BAD_ACTION_NUMBER;
		String tLANText = "";
		
		if (tMatcher.find ()) {
			tLANText = tMatcher.group (3);
			tLastActionNumber = Integer.parseInt (tLANText);
		}
		
		return tLastActionNumber;
	}
	
	public String getGameIDFromLine (String aRequest) {
		Matcher tMatcher = NSG_WITH_GAME_ID_PATTERN.matcher (aRequest);
		String tGameID = NO_GAME_ID;
		
		if (tMatcher.find ()) {
			tGameID = tMatcher.group (1);
		}
		
		return tGameID;
	}
	
	public String getGameStatusFromLine (String aRequest) {
		Matcher tMatcher = NSG_WITH_GAME_ID_PATTERN.matcher (aRequest);
		String tGameStatus = NO_STATUS;
		
		if (tMatcher.find ()) {
			tGameStatus = tMatcher.group (2);
		}
		
		return tGameStatus;
	}
	
	public String getPlayerNameFromLine (String aRequest) {
		Matcher tMatcher = PLAYER_WITH_NAME_PATTERN.matcher (aRequest);
		String tPlayerName = NO_NAME;
		
		if (tMatcher.find ()) {
			tPlayerName = tMatcher.group (1);
		}
		
		return tPlayerName;
	}
	
	public String getPlayers () {
		String tPlayers;
		
		tPlayers = "";
		if (players.size () > 0) {
			for (String tPlayer : players) {
				if (tPlayers.length () > 0) {
					tPlayers += ", ";
				}
				tPlayers += tPlayer;
			}
		}
		
		return tPlayers;
	}
	
	public boolean hasPlayer (String aPlayerName) {
		boolean tHasPlayer = false;
		
		if (players.size () > 0) {
			for (String tPlayer : players) {
				if (tPlayer.equals(aPlayerName)) {
					tHasPlayer = true;
				}
			}
		}
		
		return tHasPlayer;
	}

	public void removeFirstPlayer () {
		players.remove (0);
	}
	
	public String getSavedGameXML () {
		String tSavedGameXML = "";
		String tPlayers;
		
		tPlayers = getPlayers ();
		tSavedGameXML = "<Game gameID=\"" + gameID + "\" lastActionNumber=\"" + lastActionNumber + 
				"\" players=\"" + tPlayers + "\" status=\"" + gameStatus + "\">";
		
		return tSavedGameXML;
	}
}
