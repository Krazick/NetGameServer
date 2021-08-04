package netGameServer.primaryTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import netGameServer.primary.NetworkAction;
import netGameServer.primary.NetworkActions;

@DisplayName ("Network Actions Tests")
class NetworkActionsTests {
	NetworkActions networkActions;
	NetworkAction networkAction1;
	NetworkAction networkAction2;
	
	@BeforeEach
	void setUp () throws Exception {
		networkActions = new NetworkActions ();
		networkAction1 = new NetworkAction (101, "Complete");
		networkAction1.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"100\">");
		networkAction2 = new NetworkAction (102, "Pending");
	}

	@Test
	@DisplayName ("Basic Constructor Tests")
	void BasicConstructorTests () {
		assertEquals (0, networkActions.getCount ());
	}

	@Test
	@DisplayName ("Empty Actions with Last Action Tests")
	void EmptyActionsWithLastActionTests () {
		int tActionNumber;
		String tStatusAction;
		String tActionXML;
		
		tActionNumber = networkActions.getLastNetworkActionNumber ();
		assertEquals (100, tActionNumber);
		
		tStatusAction = networkActions.getLastActionStatus ();
		assertEquals ("Complete", tStatusAction);
		
		tActionXML = networkActions.getActionXMLAt (0);
		assertEquals ("<ActionNotRecieved/>", tActionXML);
		
		tStatusAction = networkActions.getStatusAt (0);
		assertNull (tStatusAction);
	}
	
	@Nested
	@DisplayName ("Add Network Actions")
	class AddingNetworkActionTests {
		@Test
		@DisplayName ("One new Network Action Test")
		void AddInitialNetworkActionTests () {
			NetworkAction tFoundNetworkAction;
			
			networkActions.addNetworkAction (networkAction1);
			assertEquals (1, networkActions.getCount ());
			
			tFoundNetworkAction = networkActions.getLastNetworkAction ();
			assertEquals (networkAction1, tFoundNetworkAction);
		}
		
		@Test
		@DisplayName ("Two new Network Actions Test")
		void AddTwoNetworkActionsTests () {
			NetworkAction tFoundNetworkAction;
			
			networkActions.addNetworkAction (networkAction1);
			networkActions.addNetworkAction (networkAction2);
			assertEquals (2, networkActions.getCount ());
			
			tFoundNetworkAction = networkActions.getLastNetworkAction ();
			assertEquals (networkAction2, tFoundNetworkAction);
			tFoundNetworkAction = networkActions.getNetworkActionAt (0);
			assertEquals (networkAction1, tFoundNetworkAction);

			tFoundNetworkAction = networkActions.getNetworkActionAt (-1);
			assertNull (tFoundNetworkAction);
			tFoundNetworkAction = networkActions.getNetworkActionAt (5);
			assertNull (tFoundNetworkAction);
		}

		@Test
		@DisplayName ("Get Network Action subItems Tests")
		void getSubItemTests () {
			String tStatus;
			String tActionXML;
			int tActionNumber;
			
			networkActions.addNetworkAction (networkAction1);
			networkActions.addNetworkAction (networkAction2);
			assertEquals (2, networkActions.getCount ());
			
			tStatus = networkActions.getLastActionStatus ();
			assertEquals ("Pending", tStatus);
			tActionNumber = networkActions.getLastNetworkActionNumber ();
			assertEquals (102, tActionNumber);
			tActionXML = networkActions.getActionXMLAt (1);
			assertEquals ("<ActionNotRecieved/>", tActionXML);
			
			tActionXML = networkActions.getActionXMLAt (0);
			assertEquals ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"100\">", tActionXML);
			
			tStatus = networkActions.getStatusAt (0);
			assertEquals ("Complete", tStatus);
		}
	}
}
