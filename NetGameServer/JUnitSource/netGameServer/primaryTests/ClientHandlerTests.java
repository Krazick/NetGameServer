package netGameServer.primaryTests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	private ClientHandler buildClientHandler (String aClientName) {
		ClientHandler tClientHandler;
		
		Mockito.doReturn (logger).when (mServerFrame).getLogger ();

		tClientHandler = new ClientHandler (mServerFrame, mClientSocket, ClientHandler.NO_CLIENT_HANDLERS,
				mClientListModel, mGameListModel, false);
		tClientHandler.setName (aClientName);
		
		return tClientHandler;
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
	// Need to build configureMockSocket
	
//	public ClientHandler (ServerFrame aServerFrame, Socket aClientSocket, 
//			ArrayList<ClientHandler> aClients, 
//			DefaultListModel<String> aClientListModel,
//			DefaultListModel<String> aGameListModel) {

}
