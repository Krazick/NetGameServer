package netGameServer.primaryTests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
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

@DisplayName ("Client Handler Tests")
@ExtendWith (MockitoExtension.class)
class ClientHandlerTests {
    @Mock
    ServerFrame mServerFrame;
    @Mock
    Socket mClientSocket;
    @Mock
    DefaultListModel<String> mClientListModel;
    @Mock
    DefaultListModel<String> mGameListModel;
    @Mock
    OutputStream mOutputStream;
    @Mock
    InputStream mInputStream;
    @Mock
    GameSupport mGameSupport1;
    @Mock
    GameSupport mGameSupport2;
    @Mock
    PrintWriter mPrintWriter;
    
    ArrayList<ClientHandler> clients;
	Logger logger;
   
	@BeforeEach
	void setUp () throws Exception {
		clients = new ArrayList <ClientHandler> ();
		setupLogger ();
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
	@DisplayName ("Building Client Handler and Clients List Test")
	void testBuildingClientHandler () {
		ClientHandler tClientHandlerTesterAlpha;
		ClientHandler tClientHandlerTesterBeta;
		
		tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
		clients.add (tClientHandlerTesterAlpha);
		tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
		clients.add (tClientHandlerTesterBeta);
		assertEquals ("TesterAlpha", tClientHandlerTesterAlpha.getName ());
		assertEquals ("TesterBeta", tClientHandlerTesterBeta.getName ());
		
		logger.info ("TesterAlpha has a List of the following Clients:");
		tClientHandlerTesterAlpha.printAllClientHandlerNames ();
		logger.info ("TesterBeta has a List of the following Clients:");
		tClientHandlerTesterBeta.printAllClientHandlerNames ();
	}
	
	private ClientHandler buildClientHandler (ArrayList <ClientHandler> aClients,
			String aClientName) {
		ClientHandler tClientHandler;
		
		Mockito.doReturn (logger).when (mServerFrame).getLogger ();

		tClientHandler = new ClientHandler (mServerFrame, mClientSocket, aClients,
				mClientListModel, mGameListModel, false);
		tClientHandler.setName (aClientName);
		
		return tClientHandler;
	}
	
	private ClientHandler buildClientHandler1 (ArrayList <ClientHandler> aClients,
			String aClientName) {
		ClientHandler tClientHandler;
		DefaultListModel<String> tClientListModel = new DefaultListModel<String> ();
		DefaultListModel<String> tGameListModel = new DefaultListModel<String> ();
		
		Mockito.doReturn (logger).when (mServerFrame).getLogger ();

		tClientHandler = new ClientHandler (mServerFrame, mClientSocket, aClients,
				tClientListModel, tGameListModel, false);
		tClientHandler.setName (aClientName);
		
		return tClientHandler;
	}
	
	private ClientHandler buildClientHandler (String aClientName) {
		ClientHandler tClientHandler;
		
		Mockito.doReturn (logger).when (mServerFrame).getLogger ();

		tClientHandler = new ClientHandler (mServerFrame, mClientSocket, ClientHandler.NO_CLIENT_HANDLERS,
				mClientListModel, mGameListModel, false);
		tClientHandler.setName (aClientName);
		
		return tClientHandler;
	}
	
	@Nested
	@DisplayName ("Test working with GameID and GameName")
	class testWorkingWithGameIDandName {
		@Test 
		@DisplayName ("Test getting GameSelectPrefix")
		void testGettingGameSelectPrefix () {
			ClientHandler tClientHandlerTesterAlpha;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			assertEquals ("Game Activity <GA><GameSelection ", tClientHandlerTesterAlpha.getGameSelectPrefix ());
			
		}
		
		@Test
		@DisplayName ("Test adding Game Name via String to List")
		void testAddingGameNameStringToList () {
			ClientHandler tClientHandlerTesterAlpha;
			String tGameName = "TestGameNameAlpha";
			
			tClientHandlerTesterAlpha = buildClientHandler1 (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);

			Mockito.doReturn ("Mocked GameID Alpha").when (mGameSupport1).getGameID ();
			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			tClientHandlerTesterAlpha.addGameNameToList (tGameName);
			assertEquals ("TestGameNameAlpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName ());
			assertEquals (1, tClientHandlerTesterAlpha.getGameListCount ());
			assertEquals ("TestGameNameAlpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName (0));
			
			tClientHandlerTesterAlpha.addGameNameToList (tGameName);
			assertEquals ("TestGameNameAlpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName (0));
			assertNull (tClientHandlerTesterAlpha.getGameName (1));
			assertNull (tClientHandlerTesterAlpha.getGameName (-31));
		}
		
		@Test
		@DisplayName ("Test adding Game Name via Int to List")
		void testAddingGameNameIntToList () {
			ClientHandler tClientHandlerTesterAlpha;
			
			Mockito.doReturn ("Mocked GameID Alpha").when (mGameSupport1).getGameID ();
			Mockito.doReturn ("Mocked GameName Alpha").when (mServerFrame).getGameName (Mockito.anyInt ());
			tClientHandlerTesterAlpha = buildClientHandler1 (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);

			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			tClientHandlerTesterAlpha.addGameNameToList (0);
			assertEquals (1, tClientHandlerTesterAlpha.getGameListCount ());
			assertEquals ("Mocked GameName Alpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName (0));
			
			tClientHandlerTesterAlpha.addGameNameToList (0);
			assertEquals ("Mocked GameName Alpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName (0));
			assertNull (tClientHandlerTesterAlpha.getGameName (1));
			assertNull (tClientHandlerTesterAlpha.getGameName (-31));
		}
		
		@Test
		@DisplayName ("Test getting a GameID") 
		void testGettingGameID () {
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			clients.add (tClientHandlerTesterBeta);
			assertEquals ("NOID", tClientHandlerTesterAlpha.getGameID ());
			
			Mockito.doReturn ("Mocked GameID").when (mGameSupport1).getGameID ();
			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			assertEquals ("Mocked GameID", tClientHandlerTesterAlpha.getGameID ());
			assertEquals ("NOID", tClientHandlerTesterBeta.getGameID ());
			
			tClientHandlerTesterBeta.setGameSupport (mGameSupport1);
			assertEquals ("Mocked GameID", tClientHandlerTesterBeta.getGameID ());
		}
		
		@Test
		@DisplayName ("Test getting GameIndex via GameActivity")
		void testGettingGameIndexGameActivity () {
			ClientHandler tClientHandlerTesterAlpha;
			String tGameActivityGood = "Game Activity <GA><GameSelection gameID=\"2021-08-17-0936\" gameIndex=\"0\" z_broadcast=\"TesterAlpha has Selected [1830] Are you ready to Play?\"/></GA>";
			String tGameActivityBad1 = "Game Activity <GA><GameSelection gameID=\"2021-08-17-0936\" z_broadcast=\"TesterAlpha has Selected [1830] Are you ready to Play?\"/></GA>";
			String tGameActivityBad2 = "Game Activity <GA><PlayerOrder gameID=\"2021-08-17-0936\" gameIndex=\"0\" z_broadcast=\"TesterAlpha has Selected [1830] Are you ready to Play?\"/></GA>";

			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			assertEquals (0, tClientHandlerTesterAlpha.getGameIndex (tGameActivityGood));
			assertEquals (-1, tClientHandlerTesterAlpha.getGameIndex (tGameActivityBad2));
			assertEquals (-1, tClientHandlerTesterAlpha.getGameIndex (tGameActivityBad1));

		}
		
		@Test
		@DisplayName ("Test getting GameName via GameActivity")
		void testGettingGameNameGameActivity () {
			ClientHandler tClientHandlerTesterAlpha;
			String tGameActivityGood = "Game Activity <GA><GameSelection gameID=\"2021-08-17-0936\" gameIndex=\"0\" z_broadcast=\"TesterAlpha has Selected [1830] Are you ready to Play?\"/></GA>";
			String tGameActivityBad1 = "Game Activity <GA><GameSelection gameID=\"2021-08-17-0936\" z_broadcast=\"TesterAlpha has Selected [1830] Are you ready to Play?\"/></GA>";
			
			Mockito.doReturn ("Mocked GameID Alpha").when (mGameSupport1).getGameID ();
			Mockito.doReturn ("Mocked GameName Alpha").when (mServerFrame).getGameName (0);
			Mockito.doReturn (mGameSupport1).when (mServerFrame).createNewGameSupport (Mockito.any ());
			tClientHandlerTesterAlpha = buildClientHandler1 (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.addGameName (tGameActivityGood);
			assertEquals (1, tClientHandlerTesterAlpha.getGameListCount ());
			assertEquals ("Mocked GameName Alpha Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameName (0));

			tClientHandlerTesterAlpha.addGameName (tGameActivityGood);
			assertEquals (1, tClientHandlerTesterAlpha.getGameListCount ());

			tClientHandlerTesterAlpha.addGameName (tGameActivityBad1);
			assertEquals (1, tClientHandlerTesterAlpha.getGameListCount ());

		}
	}
	
	@Nested
	@DisplayName ("Test working with GameSupport")
	class testWorkingWithGameSupport {
		@Test
		@DisplayName ("Getting GameSupport")
		void testGettingGameSupport () {
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			GameSupport tFoundGameSupport;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			clients.add (tClientHandlerTesterBeta);
			
			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			tFoundGameSupport = tClientHandlerTesterAlpha.getGameSupport ();
			assertEquals (mGameSupport1, tFoundGameSupport);
			
			tFoundGameSupport = tClientHandlerTesterBeta.getGameSupport ();
			assertNull (tFoundGameSupport);
		}
		
		@Test
		@DisplayName ("Finding GameSupport")
		void testFindingGameSupport () {
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			ClientHandler tClientHandlerTesterLambda;
			GameSupport tFoundGameSupport;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			clients.add (tClientHandlerTesterBeta);
			tClientHandlerTesterLambda = buildClientHandler (clients, "TesterLambda");
			clients.add (tClientHandlerTesterLambda);
			
			Mockito.doReturn ("Mocked GameID Alpha").when (mGameSupport1).getGameID ();
			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			
			Mockito.doReturn ("Mocked GameID Beta").when (mGameSupport2).getGameID ();
			tClientHandlerTesterBeta.setGameSupport (mGameSupport2);
			
			assertEquals ("Mocked GameID Alpha", tClientHandlerTesterAlpha.getGameID ());
			assertEquals ("Mocked GameID Beta", tClientHandlerTesterBeta.getGameID ());
			
			tFoundGameSupport = tClientHandlerTesterAlpha.getMatchingGameSupport ("Mocked GameID Alpha");
			assertEquals (mGameSupport1, tFoundGameSupport);
			
			tFoundGameSupport = tClientHandlerTesterAlpha.getMatchingGameSupport ("Mocked GameID Beta");
			assertEquals (mGameSupport2, tFoundGameSupport);
			
			tFoundGameSupport = tClientHandlerTesterAlpha.getMatchingGameSupport ("Mocked GameID Gamma");
			assertNull (tFoundGameSupport);
			
			tFoundGameSupport = tClientHandlerTesterLambda.getMatchingGameSupport ("Mocked GameID Gamma");
			assertNull (tFoundGameSupport);
		}
		
		@Test
		@DisplayName ("Updating GameSupport")
		void testUpdatingGameSupport () {
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			ClientHandler tClientHandlerTesterLambda;
			GameSupport tFoundGameSupport;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			clients.add (tClientHandlerTesterBeta);
			tClientHandlerTesterLambda = buildClientHandler (clients, "TesterLambda");
			clients.add (tClientHandlerTesterLambda);

			Mockito.doReturn ("Mocked GameID Alpha").when (mGameSupport1).getGameID ();
			tClientHandlerTesterAlpha.setGameSupport (mGameSupport1);
			
			Mockito.doReturn ("Mocked GameID Beta").when (mGameSupport2).getGameID ();
			tClientHandlerTesterBeta.setGameSupport (mGameSupport2);

			assertNull (tClientHandlerTesterLambda.getGameSupport ());
			tClientHandlerTesterLambda.updateGameSupport ("Mocked GameID Delta");
			tFoundGameSupport = tClientHandlerTesterLambda.getGameSupport ();
			assertNull (tClientHandlerTesterLambda.getGameSupport ());
			tClientHandlerTesterLambda.updateGameSupport ("Mocked GameID Alpha");
			tFoundGameSupport = tClientHandlerTesterLambda.getGameSupport ();
			assertEquals (mGameSupport1, tFoundGameSupport);
		}
		
		@Test
		@DisplayName ("Setting ClientHandlers in GameSupport")
		void testSettingClientHandlersInGameSupport () {
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			ClientHandler tClientHandlerTesterLambda;
			GameSupport tFoundGameSupport;
			GameSupport tGameSupport;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			clients.add (tClientHandlerTesterBeta);
			tClientHandlerTesterLambda = buildClientHandler ("TesterLambda");
			tGameSupport = new GameSupport (mServerFrame, "Client Handler 1", logger);
			tClientHandlerTesterLambda.setGameSupport (tGameSupport);
			
			tClientHandlerTesterLambda.setGSClientHandlers (clients);
			tFoundGameSupport = tClientHandlerTesterLambda.getGameSupport ();
			assertEquals (clients, tFoundGameSupport.getClientHandlers ());
		}
	}

	@Nested
	@DisplayName ("Test Handling Messages")
	class testHandlingMessages {
		
		@Test
		@DisplayName ("Handle New Player Test")
		void handleNewPlayerTest () {
			String tMessageGood1 = "name TesterAlpha";
			String tMessageGood2 = "name TesterBeta";
			String tMessage1Bad = "name";
			String tMessage2Bad = "nameTesterBeta";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			ClientHandler tClientHandlerTesterDelta;
			boolean tHandleResult;
			
			Mockito.doNothing ().when (mPrintWriter).println (Mockito.anyString ());
			
			tClientHandlerTesterAlpha = buildClientHandler1 (clients, "");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			
			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood1);
			assertTrue (tHandleResult);
		
			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood1);
			assertFalse (tHandleResult);
		

			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			
			tHandleResult = tClientHandlerTesterAlpha.handleMessage(true, tMessageGood2);
			assertTrue (tHandleResult);
			
			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessage1Bad);
			assertEquals (false, tHandleResult);
			
			tClientHandlerTesterDelta = buildClientHandler (clients, "TesterDelta");
			tClientHandlerTesterDelta.setOutputWriter (mPrintWriter);
			tHandleResult = tClientHandlerTesterDelta.handleMessage (true, tMessage2Bad);
			assertEquals (false, tHandleResult);
		}
		
		@Test
		@DisplayName ("Handle Who Test")
		void handleWhoTest () {
			String tMessageGood = "who";
			ClientHandler tClientHandlerTesterAlpha;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
		}
		
		
		@Test
		@DisplayName ("Handle Say Test")
		void handleSayTest () {
			String tMessageGood = "say This is a test message";
			String tMessageBad = "say";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			
			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageBad);
			assertEquals (false, tHandleResult);
		}
		
		@Test
		@DisplayName ("Handle GEVersion Test")
		void handleGEVersionTest () {
			String tMessageGood = "GEVersion 0.7.X";
			String tMessageBad = "GEVersion0.7.X";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertTrue (tHandleResult);
			assertEquals ("0.7.X", tClientHandlerTesterAlpha.getGEVersion ());
			
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);

			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageBad);
			assertFalse (tHandleResult);
			assertEquals ("", tClientHandlerTesterBeta.getGEVersion ());
		}
		
		@Test
		@DisplayName ("Handle AFK Test")
		void handleAFKTest () {
			String tMessageGood = "AFK";
			String tMessageBad = "afk";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			assertTrue (tClientHandlerTesterAlpha.getClientIsAFK ());
			
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);

			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageBad);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterBeta.getClientIsAFK ());
		}
		
		@Test
		@DisplayName ("Handle NotAFK Test")
		void handleNotAFKTest () {
			String tMessageGood = "Not AFK";
			String tMessageBad = "Not afk";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterAlpha.getClientIsAFK ());
			
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);

			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageBad);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterBeta.getClientIsAFK ());
		}
		
		@Test
		@DisplayName ("Handle Player is Ready Test")
		void handlePlayerReadyTest () {
			String tMessageGood = "Ready";
			String tMessageBad = "ready";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			assertTrue (tClientHandlerTesterAlpha.getClientIsReady ());
			
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);

			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageBad);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterBeta.getClientIsReady ());
		}
		
		@Test
		@DisplayName ("Handle Player is Not Ready Test")
		void handlePlayerNotReadyTest () {
			String tMessageGood = "Not Ready";
			String tMessageBad = "Not ready";
			ClientHandler tClientHandlerTesterAlpha;
			ClientHandler tClientHandlerTesterBeta;
			boolean tHandleResult;
			
			tClientHandlerTesterAlpha = buildClientHandler (clients, "TesterAlpha");
			tClientHandlerTesterAlpha.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterAlpha);

			tHandleResult = tClientHandlerTesterAlpha.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterAlpha.getClientIsReady ());
			
			tClientHandlerTesterBeta = buildClientHandler (clients, "TesterBeta");
			tClientHandlerTesterBeta.setOutputWriter (mPrintWriter);
			clients.add (tClientHandlerTesterBeta);
			
			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageGood);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterBeta.getClientIsReady ());

			tHandleResult = tClientHandlerTesterBeta.handleMessage (true, tMessageBad);
			assertEquals (true, tHandleResult);
			assertFalse (tClientHandlerTesterBeta.getClientIsReady ());
		}

	}
}
