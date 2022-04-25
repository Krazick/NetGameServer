/**
 * 
 */
package netGameServer.primaryTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import netGameServer.primary.SavedGame;

/**
 * @author marksmith
 *
 */
@DisplayName ("Saved Game Tests")
class SavedGameTests {
	SavedGame savedGame;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp () throws Exception {
		savedGame = new SavedGame ("SavedGameFilePath", "TestGameID", "SGTestPlayer1");
	}

	@Test
	@DisplayName ("Test Basic Constructor with GameID and Player")
	void basicConstructorTest () {
		assertEquals ("TestGameID", savedGame.getGameID ());
		assertEquals ("NO_STATUS", savedGame.getGameStatus ());
		assertEquals (0, savedGame.getLastActionNumber ());
		assertEquals (1, savedGame.getPlayerCount ());
	}

	@Test
	@DisplayName ("Test Finding Player")
	void findPlayerTest () {
		assertFalse (savedGame.hasPlayer ("SG_BusterPlayer"));
		assertTrue (savedGame.hasPlayer ("SGTestPlayer1"));
		savedGame.removeFirstPlayer ();
		assertFalse (savedGame.hasPlayer ("SGTestPlayer1"));
	}
	
	@Test
	@DisplayName ("Test adding Multiple Players")
	void addMultiplePlayersTest () {
		savedGame.addPlayer ("SGTestPlayer1");
		assertEquals (1, savedGame.getPlayerCount ());
		assertEquals ("SGTestPlayer1", savedGame.getPlayers ());
		savedGame.addPlayer ("SGTestPlayer2");
		assertEquals (2, savedGame.getPlayerCount ());
		savedGame.addPlayer ("SGTestPlayer3");
		savedGame.addPlayer ("SGTestPlayer4");
		assertEquals (4, savedGame.getPlayerCount ());
		assertEquals ("SGTestPlayer1, SGTestPlayer2, SGTestPlayer3, SGTestPlayer4", savedGame.getPlayers ());

		savedGame.removeFirstPlayer ();
		savedGame.removeFirstPlayer ();
		savedGame.removeFirstPlayer ();
		savedGame.removeFirstPlayer ();
		assertEquals ("", savedGame.getPlayers ());
	}
	
	@Test
	@DisplayName ("Test changing GameID and Game Status")
	void changeGameInfoTests () {
		savedGame.setGameID ("2022-04-25.ID");
		savedGame.setGameStatus ("ACTIVE");
		assertEquals ("2022-04-25.ID", savedGame.getGameID ());
		assertEquals ("ACTIVE", savedGame.getGameStatus ());
	}
	
	@Test
	@DisplayName ("Test SavedGameXML Result")
	void savedGameXMLTest () {
		savedGame.addPlayer ("SGTestPlayer2");
		savedGame.addPlayer ("SGTestPlayer3");
		savedGame.addPlayer ("SGTestPlayer4");
		assertEquals ("<Game gameID=\"TestGameID\" lastActionNumber=\"0\" players=\"SGTestPlayer1, SGTestPlayer2, SGTestPlayer3, SGTestPlayer4\" status=\"NO_STATUS\">", savedGame.getSavedGameXML ());
	}
	
	@Test
	@DisplayName ("Test for reading a JUNIT Test File")
	void readFileWithJunitTestFile () {
		SavedGame tTestSavedGameRead;
		
		try {
			tTestSavedGameRead = new SavedGame (null);
		} catch (FileNotFoundException e) {
			assertEquals ("Null File Name", e.getMessage ());
		}
		try {
			tTestSavedGameRead = new SavedGame ("JunitTestFile");
			assertEquals ("", tTestSavedGameRead.getGameID ());
			assertEquals ("", tTestSavedGameRead.getGameStatus ());
			assertEquals (0, tTestSavedGameRead.getLastActionNumber ());
			assertEquals (0, tTestSavedGameRead.getPlayerCount ());
		
		} catch (FileNotFoundException e) {
			assertEquals ("Null File Name", e.getMessage ());
		}
	}
	
	@Nested
	@DisplayName ("Parsing Data from input")
	class parsingDataFromInputLineTests {
		
		@Test
		@DisplayName ("Extracting the Last Action Number")
		void LastActionNumberExtractionTest () {
			String tNSG_XML_StringGood = "<NetworkSaveGame gameID=\"2021-04-12-1353\" status=\"Active\" lastActionNumber=\"103\">";
			String tNSG_XML_StringBad = "<NetworkSaveGame gameID=\"2021-04-12-1353\" status=\"Active\" lastActionNumber=\"ABLE\">";
			
			assertEquals (103, savedGame.getLastActionNumberFromLine (tNSG_XML_StringGood));
			assertEquals (-1, savedGame.getLastActionNumberFromLine (tNSG_XML_StringBad));
		}
			
		@Test
		@DisplayName ("Extracting the GameID")
		void GameIDExtractionTest () {
			String tNSG_XML_StringGood = "<NetworkSaveGame gameID=\"2021-04-12-1353\" status=\"Active\" lastActionNumber=\"103\">";
			String tNSG_XML_StringBad = "<NetworkSaveGame gameID=\"2021-04-12-1353333\" status=\"Active\" lastActionNumber=\"ABLE\">";
			
			assertEquals ("2021-04-12-1353", savedGame.getGameIDFromLine (tNSG_XML_StringGood));
			assertEquals ("NOID", savedGame.getGameIDFromLine (tNSG_XML_StringBad));
		}
		
		@Test
		@DisplayName ("Extracting the Game Status")
		void GameStatusExtractionTest () {
			String tNSG_XML_StringGood = "<NetworkSaveGame gameID=\"2021-04-12-1353\" status=\"Active\" lastActionNumber=\"103\">";
			String tNSG_XML_StringBad = "<NetworkSaveGame gameID=\"2021-04-12-1353\" gameStatus=\"Active\" lastActionNumber=\"ABLE\">";
			
			assertEquals ("Active", savedGame.getGameStatusFromLine (tNSG_XML_StringGood));
			assertEquals ("NO_STATUS", savedGame.getGameStatusFromLine (tNSG_XML_StringBad));
		}
		
		@Test
		@DisplayName ("Extracting the Player Name")
		void PlayerNameExtractionTest () {
			String tClient_XML_StringGood = "<Player name=\"SGTesterAlpha\" status=\"Active\">";
			String tClient_XML_StringBad = "<Player playerName=\"SGTesterBeta\" status=\"Active\">";
			
			assertEquals ("SGTesterAlpha", savedGame.getPlayerNameFromLine (tClient_XML_StringGood));
			assertEquals ("NO_NAME", savedGame.getPlayerNameFromLine (tClient_XML_StringBad));
		}
	}
}
