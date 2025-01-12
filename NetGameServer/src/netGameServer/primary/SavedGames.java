package netGameServer.primary;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class SavedGames {
	private ArrayList<SavedGame> games;
	
	public SavedGames (String aDirectoryName) {
		File tDirectory;
		String [] tFiles;
		String tFilePath;
		
		tDirectory = new File (aDirectoryName);
		games = new ArrayList<SavedGame> ();
		tFiles = tDirectory.list ();
		for (String tFileName : tFiles) {
			if (tFileName.endsWith (".autoSave")) {
				tFilePath = aDirectoryName + File.separator + tFileName;
				addSavedGame (tFilePath);
			}
		}
	}

	public void addNewSavedGame (String aFilePath, String aGameID, String aPlayerName) {
		SavedGame tGame;

		tGame = new SavedGame (aFilePath, aGameID, aPlayerName);
		games.add (tGame);
	}
	
	public void addSavedGame (String aFilePath) {
		SavedGame tGame;
		try {
			tGame = new SavedGame (aFilePath);
			games.add (tGame);
		} catch (FileNotFoundException tException) {
			tException.printStackTrace ();
		}
	}
	
	public void addPlayerToSavedGame (String aGameID, String aPlayerName) {
		SavedGame tSavedGame;
		
		tSavedGame = getSavedGameFor (aGameID);
		tSavedGame.addPlayer (aPlayerName);
	}
	
	public SavedGame getSavedGameFor (String aGameID) {
		SavedGame tFoundSavedGame = SavedGame.NO_GAME;
		
		for (SavedGame tSavedGame : games) {
			if (aGameID.equals (tSavedGame.getGameID ())) {
				tFoundSavedGame = tSavedGame;
			}
		}
		
		return tFoundSavedGame;
	}

	public String getSavedGamesFor (String aPlayerName) {
		String tSavedGames = "";
		String tSavedGameXML = "";
		
		for (SavedGame tSavedGame: games) {
			if (tSavedGame.hasPlayer (aPlayerName)) {
				tSavedGameXML = tSavedGame.getSavedGameXML ();
				tSavedGames += tSavedGameXML;
			}
		}
		
		return tSavedGames;
	}
}
