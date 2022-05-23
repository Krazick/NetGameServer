package netGameServer.primaryTests;

import java.io.File;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import netGameServer.primary.ClientHandler;
import netGameServer.primary.GameSupport;
import netGameServer.primary.NetworkAction;
import netGameServer.primary.ServerFrame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName ("Game Support Tests")
@ExtendWith (MockitoExtension.class)
class GameSupportTests {
	GameSupport gameSupport;
	GameSupport gameSupportNoID;
	Logger logger;
	
    @Mock
    ClientHandler mClientHandlerAlpha;
    
    @Mock
    ClientHandler mClientHandlerBeta;
	
	@Mock
	ServerFrame mServerFrame;
	
	@InjectMocks
	@Spy
	private GameSupport mGameSupport; 

	@BeforeEach
	void setUp() throws Exception {
		String tGameID;
		
		
		setupLogger ();
		tGameID = "2020-07-31-2005";
		Mockito.doReturn ("NetworkAutoSaves/18XXTests").when (mServerFrame).getFullASDirectory ();

		gameSupport = new GameSupport (mServerFrame, tGameID, logger);
		gameSupportNoID = new GameSupport (mServerFrame, "NOID", logger);
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
	
    private String concatPlayers (String aPlayerA, String aPlayerB) {
        return aPlayerA + ", " + aPlayerB;
	}
	
	private String concatPlayers (String aPlayerA, String aPlayerB, String aPlayerC) {
	        return aPlayerA + ", " + aPlayerB + ", " + aPlayerC;
	}

    private String prepareExpectedResponse (String aPlayerName) {
        String tPlayer1 = "GSPlayerAlpha";
        String tPlayer2 = "GSPlayerBeta";
        String tPlayer3 = "GSPlayerGamma";
        String tPlayer4 = "GSPlayerDelta";
        String tPlayer5 = "GSPlayerEpsilon";
        String tGame_12 = "<Game gameID=\"2021-04-07-1748\" lastActionNumber=\"101\" players=\"" +
                                                        concatPlayers (tPlayer1, tPlayer2) +"\" status=\"Active\">";
        String tGame_421 = "<Game gameID=\"2021-04-08-1951\" lastActionNumber=\"100\" players=\"" +
                                                        concatPlayers (tPlayer4, tPlayer2, tPlayer1) + "\" status=\"Prepared\">";
        String tGame_42 = "<Game gameID=\"2021-04-08-2034\" lastActionNumber=\"108\" players=\"" +
                                                        concatPlayers (tPlayer4, tPlayer2) + "\" status=\"Active\">";
        String tGame_251 = "<Game gameID=\"2021-04-09-2141\" lastActionNumber=\"108\" players=\"" +
                                                        concatPlayers (tPlayer2, tPlayer5, tPlayer1) + "\" status=\"Active\">";
        String tGame_14 = "<Game gameID=\"2021-04-08-2006\" lastActionNumber=\"120\" players=\"" +
                                                        concatPlayers (tPlayer1, tPlayer4) + "\" status=\"Active\">";
        String tGame_15 = "<Game gameID=\"2021-04-09-1541\" lastActionNumber=\"103\" players=\"" +
                                                        concatPlayers (tPlayer1, tPlayer5) + "\" status=\"Active\">";
        String tAllGames_with_Player1 = tGame_12 + tGame_421 + tGame_14 + tGame_15;
        String tAllGames_with_Jeff = "";
        String tAllGames_with_Player2 = tGame_12 + tGame_421 + tGame_42 + tGame_251;
        String tExpectedResponse1 = "<GSResponse><SavedGames name=\"" + tPlayer1 + "\">" + tAllGames_with_Player1 + "</SavedGames></GSResponse>";
        String tExpectedResponse2 = "<GSResponse><SavedGames name=\"" + tPlayer2 + "\">" + tAllGames_with_Player2 + "</SavedGames></GSResponse>";
        String tExpectedResponse3 = "<GSResponse><SavedGames name=\"" + tPlayer3 + "\"></SavedGames></GSResponse>";
        String tExpectedResponse = "";

        if (tPlayer1.equals (aPlayerName)) {
                Mockito.doReturn (tAllGames_with_Player1).when (mServerFrame).getSavedGamesFor (tPlayer1);
                tExpectedResponse = tExpectedResponse1;
        }
        if (tPlayer2.equals (aPlayerName)) {
                Mockito.doReturn (tAllGames_with_Jeff).when (mServerFrame).getSavedGamesFor (tPlayer3);
                tExpectedResponse = tExpectedResponse2;
        }
        if (tPlayer3.equals (aPlayerName)) {
                Mockito.doReturn (tAllGames_with_Player2).when (mServerFrame).getSavedGamesFor (tPlayer2);
                tExpectedResponse = tExpectedResponse3;
        }

        return tExpectedResponse;
    }

    @Nested
    @DisplayName ("Game Support ClientHandler Tests")
    class verifyGameSupportClientHandlerTests {
    	@Test
    	@DisplayName ("Adding a ClientName to the List")
    	void addClientNameTest () {
    		String tPlayerName1 = "GSTesterAlpha";
    		String tPlayerName2 = "GSTesterBeta";
   		
    		gameSupport.addClientName (tPlayerName1);
    		assertEquals (1, gameSupport.getPlayerCount ());
    		
    		gameSupport.addClientName (tPlayerName1);
    		assertEquals (1, gameSupport.getPlayerCount ());
    		
    		gameSupport.addClientName (tPlayerName2);
    		assertEquals (2, gameSupport.getPlayerCount ());
    	}
    	
    	@Test
    	@DisplayName ("Getting ClientName by Index")
    	void getClientNameByIndexText () {
    		String tPlayerName1 = "GSTesterAlpha";
    		String tPlayerName2 = "GSTesterBeta";
   		
    		gameSupport.addClientName (tPlayerName1);
    		gameSupport.addClientName (tPlayerName2);
    		assertEquals ("GSTesterAlpha", gameSupport.getPlayerIndex (0));
       		assertEquals ("GSTesterBeta", gameSupport.getPlayerIndex (1));

    	}
    	
    	@Test
    	@DisplayName ("Getting ClientHandler by Name")
    	void getClientHandlerByNameTest () {
    		String tPlayerName1 = "GSTesterAlpha";
    		ClientHandler tFoundClientHandler;

    		tFoundClientHandler = gameSupport.getClientHandlerFor (tPlayerName1);
    		assertEquals (ClientHandler.NO_CLIENT_HANDLER, tFoundClientHandler);
    	}
    	
    	@Test
    	@DisplayName ("Setting ClientHandlers 1 in Game Support")
    	void setClientHandlerTest1 () {
    		gameSupport.setClientHandlers (ClientHandler.NO_CLIENT_HANDLERS);
       		assertEquals (0, gameSupport.getPlayerCount ());
    	}
    	
       	@Test
    	@DisplayName ("Setting ClientHandlers 2 in Game Support")
    	void setClientHandlerTest2 () {
       		LinkedList<ClientHandler> tClientHandlers;
    		ClientHandler tFoundClientHandler;
    		
        	tClientHandlers = new LinkedList<ClientHandler> ();
        	
			Mockito.doReturn ("GSTesterAlpha").when (mClientHandlerAlpha).getName ();
			tClientHandlers.add (mClientHandlerAlpha);
 
			Mockito.doReturn ("GSTesterBeta").when (mClientHandlerBeta).getName ();
			tClientHandlers.add (mClientHandlerBeta);

			gameSupport.setClientHandlers (tClientHandlers);
      		assertEquals (2, gameSupport.getPlayerCount ());
      		
      		tFoundClientHandler = gameSupport.getClientHandlerFor ("GSTesterAlpha");
      		assertEquals (mClientHandlerAlpha, tFoundClientHandler);
      		
      		tFoundClientHandler = gameSupport.getClientHandlerFor ("GSTesterBeta");
      		assertEquals (mClientHandlerBeta, tFoundClientHandler);
      		
      		tFoundClientHandler = gameSupport.getClientHandlerFor ("GSTesterGamma");
      		assertNotEquals (mClientHandlerAlpha, tFoundClientHandler);
    	}
  }
    
	@Nested
	@DisplayName ("Game ID Tests")
	class verifyGameIDFunctionalityTests {
		@Test
		@DisplayName ("Test Game ID matches") 
		void verifyGameIDMatches () {
			String tFoundGameID;
			
			tFoundGameID = gameSupport.getGameID ();
			assertEquals ("2020-07-31-2005", tFoundGameID);
			assertNotEquals ("2019-07-30-1333", tFoundGameID);
		}
	
		@Test
		@DisplayName ("Test Retrieving Game ID from Action Number Request")
		void getGameIDFromRequestTest () {
			String tGoodRequest = "<GS gameID=\"2021-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<GS><LastActionNumber requestNew=\"TRUE\"></GS>";
			String tFoundGameID;
			
			tFoundGameID = gameSupport.getGameID (tGoodRequest);
			assertEquals ("2021-07-31-2005", tFoundGameID);
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
			
			Mockito.doReturn ("GSTester").when (mClientHandlerAlpha).getName ();
			Mockito.doReturn ("GSGame Name").when (mClientHandlerAlpha).getGameName ();
			gameSupportNoID.addClientHandler (mClientHandlerAlpha);
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("GSTester is Ready to play the Game", tGSResponse);
			tGameID = gameSupportNoID.getGameID ();
			assertEquals ("2021-02-26-1001", tGameID);
		}
		
		@Test
		@DisplayName ("Heartbeat Request with no GameID")
		void heartbeatRequestTest () {
			String tGoodRequest = "Game Support <GS><Heartbeat></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><Heartbeat></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Heartbeat Request with GameID")
		void heartbeatRequestWithGameIDTest () {
			String tGoodRequest = "Game Support <GS gameID=\"2020-07-31-2005\"><Heartbeat></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><Heartbeat></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Reconnect Request with GameID")
		void reconnectRequestWithGameIDTest () {
			String tGoodRequest = "Game Support <GS gameID=\"2020-07-31-2005\"><Reconnect name=\"Fred\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
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
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			assertTrue (gameSupport.isRequestForThisGame (tGoodRequest));
			assertFalse (gameSupport.isRequestForThisGame (tBadRequest));			
		}
		
		@Test
		@DisplayName ("Game Activity Action")
		void isGameActivityRequestTest () {
			int tCurrentActionNumber;
			String tGAResponse;
			String tGoodGameActivity = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BuyStockAction\" name=\"Buy Stock Action\" number=\"101\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"<Effect class=\"ge18xx.round.action.effects.TransferOwnershipEffect\" companyAbbrev=\"C&amp;SL\" fromActor=\"Start Packet\" isAPrivate=\"false\" name=\"Transfer Ownership\" percentage=\"100\" president=\"true\" toActor=\"Mark\"/>" +
					"<Effect actor=\"C&amp;SL\" class=\"ge18xx.round.action.effects.StateChangeEffect\" isAPrivate=\"true\" name=\"State Change\" newState=\"Owned\" previousState=\"Unowned\"/>" +
					"<Effect actor=\"Mark\" class=\"ge18xx.round.action.effects.BoughtShareEffect\" isAPrivate=\"false\" name=\"Bought Share\"/>" +
					"<Effect actor=\"Mark\" class=\"ge18xx.round.action.effects.StateChangeEffect\" isAPrivate=\"false\" name=\"State Change\" newState=\"Bought\" previousState=\"No Action\"/>" +
					"</Effects></Action>";
			String tGoodGARequest = "<GA>" + tGoodGameActivity + "</GA>";
			NetworkAction tInjectedLastAction;
			
			Mockito.doNothing ().when (mGameSupport).autoSave ();
			tInjectedLastAction = new NetworkAction (101, "Complete");
			mGameSupport.addNewNetworkAction (tInjectedLastAction);
			Mockito.doReturn (tInjectedLastAction).when (mGameSupport).getLastNetworkAction ();
			
			assertTrue (mGameSupport.isRequestForGameActivity (tGoodGARequest));
			tCurrentActionNumber = 101;
			
			mGameSupport.handleGameActivityRequest (tGoodGARequest);
			mGameSupport.setActionNumber (tCurrentActionNumber);
			tGAResponse = mGameSupport.generateGSReponseRequestLast ();
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGAResponse);
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
		
        @Test
        @DisplayName ("Saved Games List for a Player")
        void isValidRequestSavedGamesTest () {
                String tGoodRequest = "<RequestSavedGames player=\"GSPlayerAlpha\">";
                String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";

                assertTrue (gameSupport.isRequestForSavedGamesFor (tGoodRequest));
                assertFalse (gameSupport.isRequestForSavedGamesFor (tBadRequest));
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
			
			tGSResponse = gameSupport.generateGSResponseGameID (mClientHandlerAlpha);
			assertEquals ("<GSResponse gameID=\"2020-07-31-2005\">", tGSResponse);			
		}
		
		
		@Test
		@DisplayName ("Game ID Retrieve from LoadGame") 
		void generateGameIDResponseFromLoadTest () {
			String tGSResponse;
			String tGoodRequest = "<LoadGameSetup gameID=\"2021-07-31-2005\" actionNumber=\"234\" gameName=\"1830\">";
			String tBadRequest = "<LastActionNumber requestNew=\"TRUE\">";
			
			tGSResponse = gameSupport.getGameIDFromLoadRequest (tGoodRequest);
			assertEquals ("2021-07-31-2005", tGSResponse);	
			
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
			
			tStatusUpdated = gameSupport.setStatus ("Received");
			if (tStatusUpdated) {
				tGSResponseReceived = gameSupport.generateGSReponseRequestLast ();
				assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Received\"></GSResponse>", tGSResponseReceived);
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
			
			assertEquals ("<GSResponse><GOOD></GSResponse>", gameSupport.handleGSResponseGameLoadSetup (tGoodRequest, mClientHandlerAlpha));
		}
		
        @Test
        @DisplayName ("Request Saved Games For")
        void generateRequestForSavedGamesTest () {
                String tPlayer1 = "GSPlayerAlpha";
                String tPlayer2 = "GSPlayerBeta";
                String tPlayer3 = "GSPlayerGamma";
                String tGoodRequest1 = "<RequestSavedGames player=\"" + tPlayer1 + "\">";
                String tGoodRequest2 = "<RequestSavedGames player=\"" + tPlayer2 + "\">";
                String tGoodRequest3 = "<RequestSavedGames player=\"" + tPlayer3 + "\"/>";
                String tExpectedResponse1;
                String tExpectedResponse2;
                String tExpectedResponse3;

                tExpectedResponse1 = prepareExpectedResponse (tPlayer1);
                tExpectedResponse2 = prepareExpectedResponse (tPlayer2);
                tExpectedResponse3 = prepareExpectedResponse (tPlayer3);

                assertEquals (tExpectedResponse1, gameSupport.handleGSResponseRequestSavedGamesFor (tGoodRequest1));
                assertEquals (tExpectedResponse2, gameSupport.handleGSResponseRequestSavedGamesFor (tGoodRequest2));
                assertEquals (tExpectedResponse3, gameSupport.handleGSResponseRequestSavedGamesFor (tGoodRequest3));
        }

	}
	
	@Nested
	@DisplayName ("Generate Responses when Client sends request for")
	class generateClientResonseTests {
		@Test
		@DisplayName ("New Action Number")
		void newActionNumberResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadGameIDRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tBadRequest = "<GS gameID=\"2020-07-31-2005\"><LastActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNumber newNumber=\"101\"></GSResponse>", tGSResponse);
		
			tGSResponse = gameSupport.handleGameSupportRequest (tBadGameIDRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNotComplete></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><BadRequest/></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNotComplete></GSResponse>", tGSResponse);
			
			gameSupport.setStatus ("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNumber newNumber=\"102\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Last Action")
		void lastActionResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestLast=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandlerAlpha);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Pending\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Last is Complete")
			void lastActionIsCompleteResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><LastAction isComplete=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandlerAlpha);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Pending\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><LastAction actionNumber=\"101\" status=\"Complete\"></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Action is Pending")
		void actionIsPendingResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestPending=\"TRUE\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandlerAlpha);
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNumber pendingNumber=\"101\"></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNotPending></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Action is Requested")
		void actionIsRequestActionResponseTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><RequestAction actionNumber=\"101\"></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-07-31-2005\"><RequestAction actionNumber=\"92\"></GS>";
			String tBadRequest2 = "<GS gameID=\"2020-07-31-2005\"><RequestAction actionNumber=\"201\"></GS>";
			String tNewActionRequest = "<GS gameID=\"2020-07-31-2005\"><ActionNumber requestNew=\"TRUE\"></GS>";
			String tGSResponse;
			
			gameSupport.handleGameSupportRequest (tNewActionRequest, mClientHandlerAlpha);
			//"<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"100\">"
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionNotRecieved/></GSResponse>", tGSResponse);
					
			gameSupport.setStatus("Complete");
			
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionOutOfRange find=\"92\" min=\"100\" max=\"101\" /></GSResponse>", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest2, mClientHandlerAlpha);
			assertEquals ("<GSResponse><ActionOutOfRange find=\"201\" min=\"100\" max=\"101\" /></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Player Ready is Requested")
		void playerReadyIsRequestedTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><Ready></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-07-31-2005\"><NotReady></GS>";
			String tGSResponse;
			
			Mockito.doReturn ("GSTester").when (mClientHandlerAlpha).getName ();
			Mockito.doReturn ("GSGame Name").when (mClientHandlerAlpha).getGameName ();
			gameSupport.addClientHandler (mClientHandlerAlpha);
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("GSTester is Ready to play the Game", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandlerAlpha);
			assertEquals ("<GSResponse><BadRequest/></GSResponse>", tGSResponse);
		}
		
		@Test
		@DisplayName ("Game Start is Requested")
		void gameStartIsRequestedTest () {
			String tGoodRequest = "<GS gameID=\"2020-07-31-2005\"><Start></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-07-31-2005\"><NotStarting></GS>";
			String tGSResponse;
			
			Mockito.doReturn ("GSTester").when (mClientHandlerAlpha).getName ();
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("GSTester Starts the Game", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandlerAlpha);
			assertEquals ("<GSResponse><BadRequest/></GSResponse>", tGSResponse);	
		}
		
		@Test
		@DisplayName ("Game ID is Requested")
		void gameIDIsRequestedTest () {
			String tGoodRequest = "<GS><GameIDRequest></GS>";
			String tBadRequest1 = "<GS gameID=\"2020-02-26-1001\"><NotStarting></GS>";
			String tGSResponse;
			
			Mockito.doReturn ("2021-02-28-1833").when (mClientHandlerAlpha).generateGameID ();
			tGSResponse = gameSupport.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse gameID=\"2020-07-31-2005\">", tGSResponse);
			tGSResponse = gameSupport.handleGameSupportRequest (tBadRequest1, mClientHandlerAlpha);
			assertEquals ("<GSResponse><BadGameID/></GSResponse>", tGSResponse);
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);
			assertEquals ("<GSResponse gameID=\"2021-02-28-1833\">", tGSResponse);
		}
		
		@Test
		@DisplayName ("Load Game Setup Final")
		void generateLoadSetupRequestTest () {
			String tGoodRequest = "<GS><LoadGameSetup gameID=\"2021-07-31-2005\" actionNumber=\"171\" gameName=\"1830\"></GS>";
			String tGSResponse;
			
			tGSResponse = gameSupportNoID.handleGameSupportRequest (tGoodRequest, mClientHandlerAlpha);

			assertEquals ("<GSResponse><GOOD></GSResponse>", tGSResponse);
		}

        @Test
        @DisplayName ("Request Saved Games For Final")
        void generateSavedGamesForRequestTest () {
                String tPlayer1 = "GSPlayerAlpha";
                String tPlayer2 = "GSPlayerBeta";
                String tPlayer3 = "GSPlayerGamma";
                String tGoodRequest1 = "<GS><RequestSavedGames player=\"" + tPlayer1 + "\"></GS>";
                String tGoodRequest2 = "<GS><RequestSavedGames player=\"" + tPlayer2 + "\"></GS>";
                String tGoodRequest3 = "<GS><RequestSavedGames player=\"" + tPlayer3 + "\"/></GS>";
                String tExpectedResponse1;
                String tExpectedResponse2;
                String tExpectedResponse3;

                tExpectedResponse1 = prepareExpectedResponse (tPlayer1);
                tExpectedResponse2 = prepareExpectedResponse (tPlayer2);
                tExpectedResponse3 = prepareExpectedResponse (tPlayer3);

                assertEquals (tExpectedResponse1, gameSupport.handleGameSupportRequest (tGoodRequest1, mClientHandlerAlpha));
                assertEquals (tExpectedResponse2, gameSupport.handleGameSupportRequest (tGoodRequest2, mClientHandlerAlpha));
                assertEquals (tExpectedResponse3, gameSupport.handleGameSupportRequest (tGoodRequest3, mClientHandlerAlpha));
        }		
	}

	@Nested
	@DisplayName ("Integration Test with NetworkActions")
	class integrationNetworkActionsTests {
		@Test
		@DisplayName ("Game Activity add Action")
		void isGameActivityRequestTest () {
//			int tCurrentActionNumber;
//			String tGAResponse;
			String tGoodGameActivity1 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BuyStockAction\" name=\"Buy Stock Action\" number=\"101\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
			String tGoodGameActivity2 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BidStockAction\" name=\"Bid Stock Action\" number=\"102\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
			String tGoodGameActivity3 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.SellStockAction\" name=\"Sell Stock Action\" number=\"103\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
//			String tRemoveLastAction = "<RemoveAction number=\"103\">";
//			String tGoodGARequest = "<GA>" + tGoodGameActivity1 + "</GA>";
			NetworkAction tInjectedLastAction;
			
			gameSupport.setDoAutoSave (false);
			
			tInjectedLastAction = new NetworkAction (101, "Complete");
			tInjectedLastAction.setActionXML (tGoodGameActivity1);
			gameSupport.addNewNetworkAction (tInjectedLastAction);
			assertEquals (101, gameSupport.getLastActionNumber ());

			tInjectedLastAction = new NetworkAction (102, "Complete");
			tInjectedLastAction.setActionXML (tGoodGameActivity2);
			gameSupport.addNewNetworkAction (tInjectedLastAction);
			assertEquals (102, gameSupport.getLastActionNumber ());
			
			tInjectedLastAction = new NetworkAction (103, "Complete");
			tInjectedLastAction.setActionXML (tGoodGameActivity3);
			gameSupport.addNewNetworkAction (tInjectedLastAction);
			assertEquals (103, gameSupport.getLastActionNumber ());
		}
		
		@Test
		@DisplayName ("Game Activity add Action via GA Request")
		void isGameActivityRequest2Test () {
			int tCurrentActionNumber;
			String tGoodGameActivity1 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BuyStockAction\" name=\"Buy Stock Action\" number=\"101\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
			String tGoodGameActivity2 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.BidStockAction\" name=\"Bid Stock Action\" number=\"102\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
			String tGoodGameActivity3 = "<Action actor=\"Mark\" chainPrevious=\"false\" class=\"ge18xx.round.action.SellStockAction\" name=\"Sell Stock Action\" number=\"103\" roundID=\"1\" roundType=\"Stock Round\" totalCash=\"12000\">" +
					"<Effects><Effect cash=\"40\" class=\"ge18xx.round.action.effects.CashTransferEffect\" fromActor=\"Mark\" isAPrivate=\"false\" name=\"Cash Transfer\" toActor=\"Bank\"/>" +
					"</Action>";
//			String tRemoveLastAction1 = "<RemoveAction number=\"103\">";
			String tRemoveLastAction2 = "<RemoveAction number=\"102\">";
			String tGoodGARequest;
			
			gameSupport.setDoAutoSave (false);
			
			//handleGameActivityRequest
			tGoodGARequest = "<GA>" + tGoodGameActivity1 + "</GA>";
			tCurrentActionNumber = gameSupport.generateNewActionNumber ();
			gameSupport.handleGameActivityRequest (tGoodGARequest);
			assertEquals (101, gameSupport.getLastActionNumber ());

			tGoodGARequest = "<GA>" + tGoodGameActivity2 + "</GA>";
			tCurrentActionNumber = gameSupport.generateNewActionNumber ();
			gameSupport.handleGameActivityRequest (tGoodGARequest);
			assertEquals (102, gameSupport.getLastActionNumber ());
			
			tGoodGARequest = "<GA>" + tGoodGameActivity3 + "</GA>";
			tCurrentActionNumber = gameSupport.generateNewActionNumber ();
			gameSupport.handleGameActivityRequest (tGoodGARequest);
			assertEquals (103, gameSupport.getLastActionNumber ());
			
			gameSupport.removeAction (tCurrentActionNumber);
			assertEquals (102, gameSupport.getLastActionNumber ());
			
			tGoodGARequest = "<GA>" + tRemoveLastAction2 + "</GA>";
			gameSupport.handleGameActivityRequest (tGoodGARequest);
			assertEquals (101, gameSupport.getLastActionNumber ());
			
		}

	}
}
