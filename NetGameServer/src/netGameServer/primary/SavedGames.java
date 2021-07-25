package netGameServer.primary;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class SavedGames {
	private ArrayList<SavedGame> games;
	
	public SavedGames (String aDirectoryName) {
		File tDirectory;
		String [] tFiles;
		SavedGame tGame;
		String tFilePath;
		
		tDirectory = new File (aDirectoryName);
		games = new ArrayList<SavedGame> ();
		tFiles = tDirectory.list ();
		for (String tFileName : tFiles) {
			if (tFileName.endsWith (".autoSave")) {
				tFilePath = aDirectoryName + File.separator + tFileName;
				try {
					tGame = new SavedGame (tFilePath);
					games.add (tGame);
				} catch (FileNotFoundException tException) {
					tException.printStackTrace ();
				}
			}
		}
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
