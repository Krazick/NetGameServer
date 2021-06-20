package netGameServer.primaryTests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import netGameServer.primary.ClientHandler;
import netGameServer.primary.GameSupport;
import netGameServer.primary.ServerFrame;

@DisplayName ("Game Support")
@ExtendWith (MockitoExtension.class)
class GameSupportTests {
	GameSupport gameSupport;
	GameSupport gameSupportNoID;
    @Mock
    ClientHandler mClientHandler;
	Logger logger;
	
	@BeforeEach
	void setUp() throws Exception {
		String tGameID;
		
		setupLogger ();
		tGameID = "2020-02-26-1001";
		gameSupport = new GameSupport (tGameID, logger);
		gameSupportNoID = new GameSupport ("NOID", logger);
	}
	
	private void setupLogger () {
		String tXMLConfigFIle;
	    String tJavaVersion = System.getProperty ("java.version");
	    String tOSName = System.getProperty ("os.name");
	    String tOSVersion = System.getProperty( "os.version");
	    
	    tXMLConfigFIle = "XML Data" + File.separator + "log4j2.junit.xml";
		System.setProperty ("log4j.configurationFile", tXMLConfigFIle);
		logger = LogManager.getLogger (ServerFrame.class);
		logger.info ("Network Game Server - JUNIT TEST");
		logger.info ("Java Version " + tJavaVersion + 
					" OS Name " + tOSName + " OS Version " + tOSVersion);
	}
		
	@Test
	@DisplayName ("Generate a New Action Number") 
	void generateNewActionNumberTest () {
		int tCurrentActionNumber;
		
		assertEquals ("Complete", gameSupport.getStatus ());
		tCurrentActionNumber = gameSupport.getNewActionNumber ();
		assertEquals (101, tCurrentActionNumber);
		assertEquals ("Pending", gameSupport.getStatus ());
		assertEquals (-1, gameSupport.getNewActionNumber ());
		assertFalse (gameSupport.isLastActionComplete ());
	}
	
	@Nested
	@DisplayName ("Game ID Tests")
	class verifyGameIDFunctionalityTests {
		@Test
		@DisplayName ("Test Game ID matches") 
		void verifyGameIDMatches () {
			String tFoundGameID;
			
			tFoundGameID = gameSupport.getGameID ();
			assertEquals ("2020-02-26-1001", tFoundGameID);
			assertNotEquals ("2019-07-30-1333", tFoundGameID);
		}
	
		@Test
		@DisplayName ("Test Retrieving Game ID from Action Number Request")
		void getGameIDFromRequestTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<GS><LastActionNumber requestNew=\"TRUE\"></GS>";
			String tFoundGameID;
			
			tFoundGameID = gameSupport.getGameID (tGoodRequest);
			assertEquals ("2020-02-26-1001", tFoundGameID);
			tFoundGameID = gameSupport.getGameID (tBadRequest);
			assertEquals ("NOID", tFoundGameID);
		}
		
		@Test
		@DisplayName ("Test Resetting Game ID") 
		void resetGameIDMatchesTest () {
			String tGoodGameID = "2021-02-27-1134";
			String tBadGameID = "2021-02-28";
			
			assertTrue (gameSupport.resetGameID (tGoodGameID));
			assertFalse (gameSupport.resetGameID (tBadGameID));
		}
	
		@Test
		@DisplayName ("Test setting Game ID when No ID")
		void setGameIDWhenNoID () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><Ready></GS>";
			String tFoundGameID;
			
			gameSupportNoID.updateGameID (tGoodRequest);
			tFoundGameID = gameSupportNoID.getGameID ();
			assertEquals ("2020-02-26-1001", tFoundGameID);
		}
		
		@Test
		@DisplayName ("Test setting Game ID when No ID")
		void setGameIDWhenNoIDBad () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><NotReady></GS>";
			String tFoundGameID;
			
			gameSupportNoID.updateGameID (tGoodRequest);
			tFoundGameID = gameSupportNoID.getGameID ();
			assertEquals ("NOID", tFoundGameID);
		}
		
		@Test
		@DisplayName ("Player Ready is Requested with no GameID")
		void playerReadyIsRequestedTest () {
			String tGoodRequest = "<GS gameID=\"2021-02-26-1001\"><Ready></GS>";
			String tGSResponse;
			String tGameID;
			
			Mockito.doReturn ("GSTester").when (mClientHandler).getName ();
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("GSTester is Ready to play the Game", tGSResponse);
			tGameID = gameSupportNoID.getGameID ();
			assertEquals ("2021-02-26-1001", tGameID);
		}
		
		@Test
		@DisplayName ("Heartbeat Request with no GameID")
		void heartbeatRequestTest () {
			String tGoodRequest = "Game Support <GS><Heartbeat></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><Heartbeat></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Heartbeat Request with GameID")
		void heartbeatRequestWithGameIDTest () {
			String tGoodRequest = "Game Support <GS gameID=\"2020-02-26-1001\"><Heartbeat></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><Heartbeat></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Reconnect Request with GameID")
		void reconnectRequestWithGameIDTest () {
			String tGoodRequest = "Game Support <GS gameID=\"2020-02-26-1001\"><Reconnect name=\"Fred\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><Reconnected></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Get GameID From Ready or Load Request")
		void getGameIDfromReadyORLoadRequestTest () {
			String tGoodRequest1 = "<GS gameID=\"2021-03-01-1001\"><Ready></GS>";
			String tGoodRequest2 = "<GS><LoadGameSetup gameID=\"2021-03-01-1121\" actionNumber=\"234\" gameName=\"1830\"></GS>";
			String tGameID;
			
			tGameID = gameSupportNoID.getGameIdFromRequest (tGoodRequest1);
			assertEquals ("2021-03-01-1001", tGameID);
			
			tGameID = gameSupportNoID.getGameIdFromRequest (tGoodRequest2);
			assertEquals ("2021-03-01-1121", tGameID);
		}
	}
	
	@Nested
	@DisplayName ("Verify if a Request is for a")
	class VerifyRequestTests {
		
		@Test
		@DisplayName ("Matching Game ID")
		void isMatchingGameIDTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForThisGame (tGoodRequest));
			assertFalse (gameSupport.isRequestForThisGame (tBadRequest));			
		}
		
		@Test
		@DisplayName ("Game Activity Action")
		void isGameActivityRequestTest () {
			int tCurrentActionNumber;
			String tGAResponse;
			String tLastActionRequest;
			String tGoodGameActivity = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BuyStockAction\" name=\"Buy Stock Action\" number=\"101\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"<Effect class=\"ge18xx.round.action.effects.TransferOwnershipEffect\" companyAbbrev=\"C&amp;SL\" fromActor=\"Start Packet\" isAPrivate=\"false\" name=\"Transfer Ownership\" percentage=\"100\" president=\"true\" toActor=\"Mark\"/>" +
					"<Effect actor=\"C&amp;SL\" class=\"ge18xx.round.action.effects.StateChangeEffect\" isAPrivate=\"true\" name=\"State Change\" newState=\"Owned\" previousState=\"Unowned\"/>" +
					"<Effect actor=\"Mark\" class=\"ge18xx.round.action.effects.BoughtShareEffect\" isAPrivate=\"false\" name=\"Bought Share\"/>" +
					"<Effect actor=\"Mark\" class=\"ge18xx.round.action.effects.StateChangeEffect\" isAPrivate=\"false\" name=\"State Change\" newState=\"Bought\" previousState=\"No Action\"/>" +
					"</Effects></Action>";
			String tGoodGARequest = "<GA>" + tGoodGameActivity + "</GA>";
			String tGoodGAResponse = "<GSResponse>" + tGoodGameActivity + "</GSResponse>";
			
			assertTrue (gameSupport.isRequestForGameActivity (tGoodGARequest));
			tCurrentActionNumber = gameSupport.getNewActionNumber ();
			
			gameSupport.handleGameActivityRequest (tGoodGARequest);
			tGAResponse = gameSupport.generateGSReponseRequestLast ();
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGAResponse);
			tLastActionRequest = gameSupport.generateGSResponseRequestAction ("<RequestAction actionNumber=\"" + tCurrentActionNumber + "\">");
			assertEquals (tGoodGAResponse, tLastActionRequest);
		}
		
		@Test
		@DisplayName ("Game ID")
		void isRequestForGameIDTest () {
			String tGoodRequest = "<GameIDRequest>";
			String tBadRequest = "<GS><LastActionNumber requestNew=\"TRUE\"></GS>";
			
			assertTrue (gameSupport.isRequestForGameID (tGoodRequest));
			assertFalse (gameSupport.isRequestForGameID (tBadRequest));			
		}
		
		@Test
		@DisplayName ("Base Pattern Retrieval")
		void baseRequestRetrievalTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<GS><LastActionNumber requestNew=\"TRUE\"></GS>";
			
			assertEquals ("<ActionNumber requestNew=\"TRUE\">", gameSupport.getBaseRequest (tGoodRequest));
			assertNotEquals ("<ActionNumber requestNew=\"TRUE\">", gameSupport.getBaseRequest (tBadRequest));
		}
		
		@Test
		@DisplayName ("Action Number")
		void isValidActionNumberRequestTest () {
			String tGoodRequest = "<ActionNumber requestNew=\"TRUE\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForActionNumber (tGoodRequest));
			assertFalse (gameSupport.isRequestForActionNumber (tBadRequest));
			assertTrue (gameSupport.isLastActionComplete ());
		}
		
		@Test
		@DisplayName ("Ready")
		void isValidReadyRequestTest () {
			String tGoodRequest = "<Ready>";
			String tBadRequest = "<NotReady>";
			
			assertTrue (gameSupport.isRequestForReady (tGoodRequest));
			assertFalse (gameSupport.isRequestForReady (tBadRequest));
		}
		
		@Test
		@DisplayName ("Start")
		void isValidStartRequestTest () {
			String tGoodRequest = "<Start>";
			String tBadRequest = "<NotReady>";
			
			assertTrue (gameSupport.isRequestForStart (tGoodRequest));
			assertFalse (gameSupport.isRequestForStart (tBadRequest));
		}
	
		@Test
		@DisplayName ("Last Action")
		void isValidLastActionRequestTest () {
			String tGoodRequest = "<ActionNumber requestLast=\"TRUE\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForLastAction (tGoodRequest));
			assertFalse (gameSupport.isRequestForLastAction (tBadRequest));
		}
		
		@Test
		@DisplayName ("Completed Last Action")
		void isValidLastActionisCompleteRequestTest () {
			String tGoodRequest = "<LastAction isComplete=\"TRUE\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForLastActionIsComplete (tGoodRequest));
			assertFalse (gameSupport.isRequestForLastActionIsComplete (tBadRequest));
		}
	
		@Test
		@DisplayName ("Pending Last Action")
		void isValidLastActionisPendingRequestTest () {
			String tGoodRequest = "<ActionNumber requestPending=\"TRUE\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForLastActionIsPending (tGoodRequest));
			assertFalse (gameSupport.isRequestForLastActionIsPending (tBadRequest));
		}
		
		@Test
		@DisplayName ("Action")
		void isValidActionRequestTest () {
			String tGoodRequest = "<RequestAction actionNumber=\"101\">";
			String tBadRequest = "<LastAction requestNew=\"102\">";
			
			assertTrue (gameSupport.isRequestForAction (tGoodRequest));
			assertFalse (gameSupport.isRequestForAction (tBadRequest));
			
			tGoodRequest = "<RequestAction actionNumber=\"234\">";
			assertTrue (gameSupport.isRequestForAction (tGoodRequest));
			tGoodRequest = "<RequestAction actionNumber=\"61X\">";
			assertFalse (gameSupport.isRequestForAction (tGoodRequest));
		}
		
		@Test
		@DisplayName ("Load Game Setup")
		void isValidLoadSetupRequestTest () {
			String tGoodRequest = "<LoadGameSetup gameID=\"2021-03-01-1121\" actionNumber=\"234\" gameName=\"1830\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForGameLoadSetup (tGoodRequest));
			assertFalse (gameSupport.isRequestForGameLoadSetup (tBadRequest));
		}
		
//		REQUEST_GAME_LOAD_SETUP = "<LoadGameSetup gameID=\"" + GAME_ID + "\" " + ACTION_NUMBER + " gameName=\"([A-Z0-9]+)\">";
	}
	
	@Nested
	@DisplayName ("Generate Response for a")
	class generateResponseTests {
		
		@Test
		@DisplayName ("New Action Number Request") 
 		void generateNewActionNumberResponseTest () {
			int tCurrentActionNumber;
			String tGSResponse;
			
			tCurrentActionNumber = gameSupport.getNewActionNumber ();
			tGSResponse = gameSupport.generateGSReponseNewAN (tCurrentActionNumber);
			assertEquals ("<GSResponse><ActionNumber newNumber=\"101\"></GSResponse>", tGSResponse);
			
			tCurrentActionNumber = gameSupport.getNewActionNumber ();
			tGSResponse = gameSupport.generateGSReponseNewAN (tCurrentActionNumber);
			assertEquals ("<GSResponse><ActionNotComplete></GSResponse>", tGSResponse);	
		}
		
		@Test
		@DisplayName ("Game ID Request") 
		void generateGameIDResponseTest () {
			String tGSResponse;
			
			tGSResponse = gameSupport.generateGSResponseGameID (mClientHandler);
			assertEquals ("<GSResponse gameID=\"2020-02-26-1001\">", tGSResponse);			
		}
		
		
		@Test
		@DisplayName ("Game ID Retrieve from LoadGame") 
		void generateGameIDResponseFromLoadTest () {
			String tGSResponse;
			String tGoodRequest = "<LoadGameSetup gameID=\"2021-03-01-1121\" actionNumber=\"234\" gameName=\"1830\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			tGSResponse = gameSupport.getGameIDFromLoadRequest (tGoodRequest);
			assertEquals ("2021-03-01-1121", tGSResponse);	
			
			tGSResponse = gameSupport.getGameIDFromLoadRequest (tBadRequest);
			
			assertEquals ("NOID", tGSResponse);
		}
		
		@Test
		@DisplayName ("Last Action Request") 
		void generateLastActionResponseTest () {
			int tCurrentActionNumber;
			String tGSResponsePending, tGSResponseReceived, tGSResponseComplete, tGSResponseAcknowledged;
			boolean tStatusUpdated;
			
			tCurrentActionNumber = gameSupport.getNewActionNumber ();
			assertEquals (101, tCurrentActionNumber);
			tGSResponsePending = gameSupport.generateGSReponseRequestLast ();
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Pending\"></GSResponse>", tGSResponsePending);
			
			tStatusUpdated = gameSupport.setStatus ("Recieved");
			if (tStatusUpdated) {
				tGSResponseReceived = gameSupport.generateGSReponseRequestLast ();
				assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Recieved\"></GSResponse>", tGSResponseReceived);
			} else {
				fail ("Status was not Updated to Received");
			}
			
			tStatusUpdated = gameSupport.setStatus ("Complete");
			if (tStatusUpdated) {
				tGSResponseComplete = gameSupport.generateGSReponseRequestLast ();
				assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGSResponseComplete);
			} else {
				fail ("Status was not Updated to Complete");
			}
			
			tStatusUpdated = gameSupport.setStatus ("Acknowleged");
			if (! tStatusUpdated) {
				tGSResponseAcknowledged = gameSupport.generateGSReponseRequestLast ();
				assertNotEquals ("<GSResponse><LastAction actionNumber=\"101\" sStatus=\"Acknowleged\"></GSResponse>", tGSResponseAcknowledged);
			}
		}
		
		@Test
		@DisplayName ("Pending Action Number Request") 
		void generatePendingRequestResponseTest () {
			int tActionNumber, tAnotherActionNumber;
			String tGSResponse;
			
			tActionNumber = gameSupport.getNewActionNumber ();
			tGSResponse = gameSupport.generateGSReponsePending (tActionNumber);
			assertEquals ("<GSResponse><ActionNumber pendingNumber=\"101\"></GSResponse>", tGSResponse);
			
			tAnotherActionNumber = gameSupport.getNewActionNumber ();
			tGSResponse = gameSupport.generateGSReponsePending (tAnotherActionNumber);
			assertEquals ("<GSResponse><ActionNotPending></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Load Game Setup")
		void generateLoadSetupRequestTest () {
			String tGoodRequest = "<LoadGameSetup gameID=\"2021-03-01-1121\" actionNumber=\"234\" gameName=\"1830\">";
			
			assertEquals ("<GSResponse><GOOD></GSResponse>", gameSupport.handleGSResponseGameLoadSetup (tGoodRequest, mClientHandler));
		}
	}
	
	@Nested
	@DisplayName ("Generate Responses when Client sends request for")
	class generateClientResonseTests {
		@Test
		@DisplayName ("New Action Number")
		void newActionNumberResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadGameIDRequest = "<GS gameID=\"2021-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<GS gameID=\"2020-02-26-1001\"><LastActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNumber newNumber=\"101\"></GSResponse>", tGSResponse);
		
			tGSResponse = gameSupport.handleGameSupportRequest (tBadGameIDRequest, mClientHandler);
			assertEquals ("<GSResponse><BadGameID></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest, mClientHandler);
			assertEquals ("<GSResponse><BadRequest></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNotComplete></GSResponse>", tGSResponse);
			
			gameSupport.setStatus ("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNumber newNumber=\"102\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Last Action")
		void lastActionResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestLast=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandler);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Pending\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Last is Complete")
			void lastActionIsCompleteResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><LastAction isComplete=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandler);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Pending\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Action is Pending")
		void actionIsPendingResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestPending=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandler);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNumber pendingNumber=\"101\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNotPending></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Action is Requested")
		void actionIsRequestActionResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><RequestAction actionNumber=\"101\"></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-02-26-1001\"><RequestAction actionNumber=\"92\"></GS>";
			String tBadRequest2 = "<GS gameID=\"2020-02-26-1001\"><RequestAction actionNumber=\"201\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-02-26-1001\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandler);
			//"<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"100\">"
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse><ActionNotRecieved></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandler);
			assertEquals ("<GSResponse><ActionOutOfRange></GSResponse>", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest2, mClientHandler);
			assertEquals ("<GSResponse><ActionOutOfRange></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Player Ready is Requested")
		void playerReadyIsRequestedTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><Ready></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-02-26-1001\"><NotReady></GS>";
			String tGSResponse;
			
			Mockito.doReturn ("GSTester").when (mClientHandler).getName ();
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("GSTester is Ready to play the Game", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandler);
			assertEquals ("<GSResponse><BadRequest></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Game Start is Requested")
		void gameStartIsRequestedTest () {
			String tGoodRequest = "<GS gameID=\"2020-02-26-1001\"><Start></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-02-26-1001\"><NotStarting></GS>";
			String tGSResponse;
			
			Mockito.doNothing ().when (mClientHandler).handleClientIsStarting ();
			Mockito.doReturn ("GSTester").when (mClientHandler).getName ();
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("GSTester Starts the Game", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandler);
			assertEquals ("<GSResponse><BadRequest></GSResponse>", tGSResponse);	
		}
		
		@Test
		@DisplayName ("Game ID is Requested")
		void gameIDIsRequestedTest () {
			String tGoodRequest = "<GS><GameIDRequest></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-02-26-1001\"><NotStarting></GS>";
			String tGSResponse;
			
			Mockito.doReturn ("2021-02-28-1833").when (mClientHandler).generateGameID ();
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse gameID=\"2020-02-26-1001\">", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandler);
			assertEquals ("<GSResponse><BadRequest></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandler);
			assertEquals ("<GSResponse gameID=\"2021-02-28-1833\">", tGSResponse);
		}
		
		@Test
		@DisplayName ("Load Game Setup Final")
		void generateLoadSetupRequestTest () {
			String tGoodRequest = "<GS><LoadGameSetup gameID=\"2021-03-01-1121\" actionNumber=\"234\" gameName=\"1830\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandler);

			assertEquals ("<GSResponse><GOOD></GSResponse>", tGSResponse);
		}
	}
}
