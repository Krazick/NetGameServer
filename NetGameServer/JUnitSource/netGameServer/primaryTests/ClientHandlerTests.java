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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import netGameServer.primary.ClientHandler;
import netGameServer.primary.ServerFrame;

@DisplayName ("Client Handler")
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
		ClientHandler tClientHandlerFred;
		ClientHandler tClientHandlerGeorge;
		
		tClientHandlerFred = buildClientHandler (clients, "Fred");
		clients.add (tClientHandlerFred);
		tClientHandlerGeorge = buildClientHandler (clients, "George");
		clients.add (tClientHandlerGeorge);
		assertEquals ("Fred", tClientHandlerFred.getName ());
		assertEquals ("George", tClientHandlerGeorge.getName ());
		
		logger.info ("George has a List of the following Clients:");
		tClientHandlerGeorge.printAllClientHandlerNames ();
		logger.info ("Fred has a List of the following Clients:");
		tClientHandlerFred.printAllClientHandlerNames ();
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
	// Need to build configureMockSocket
	
//	public ClientHandler (ServerFrame aServerFrame, Socket aClientSocket, 
//			ArrayList<ClientHandler> aClients, 
//			DefaultListModel<String> aClientListModel,
//			DefaultListModel<String> aGameListModel) {

}
