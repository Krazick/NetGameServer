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
	NetworkActions networkActionsBeta;
	NetworkAction networkAction1;
	NetworkAction networkAction2;
	NetworkAction networkAction1B;
	NetworkAction networkAction2B;
	NetworkAction networkAction3B;
	NetworkAction networkAction4B;
	NetworkAction networkAction5B;
	
	@BeforeEach
	void setUp () throws Exception {
		networkActions = new NetworkActions ();
		networkAction1 = new NetworkAction (101, "Complete");
		networkAction1.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"101\">");
		networkAction2 = new NetworkAction (102, "Pending");

		networkActionsBeta = new NetworkActions ();
		networkAction1B = new NetworkAction (101, "Complete");
		networkAction1B.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"101\">");
		networkAction2B = new NetworkAction (102, "Complete");
		networkAction2B.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"102\">");
		networkAction3B = new NetworkAction (103, "Complete");
		networkAction3B.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"103\">");
		networkAction4B = new NetworkAction (104, "Complete");
		networkAction4B.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"104\">");
		networkAction5B = new NetworkAction (105, "Complete");
		networkAction5B.setActionXML ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"105\">");

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
			assertEquals ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"101\">", tActionXML);
			
			tStatus = networkActions.getStatusAt (0);
			assertEquals ("Complete", tStatus);
		}
	}
	
	@Nested
	@DisplayName ("Test Removal of Network Actions")
	class RemovalNetworkActionTests {
		@Test
		@DisplayName ("Based on an Action Number")
		void getRemovalWithActionNumberTests () {
			int tActionNumber;
			
			networkActionsBeta.addNetworkAction (networkAction1B);
			networkActionsBeta.addNetworkAction (networkAction2B);
			networkActionsBeta.addNetworkAction (networkAction3B);
			networkActionsBeta.addNetworkAction (networkAction4B);
			networkActionsBeta.addNetworkAction (networkAction5B);
			assertEquals (5, networkActionsBeta.getCount ());
			assertEquals (105, networkActionsBeta.getLastNetworkActionNumber ());
			
			tActionNumber = 105;
			networkActionsBeta.remove (tActionNumber);
			assertEquals (4, networkActionsBeta.getCount ());
			assertEquals (104, networkActionsBeta.getLastNetworkActionNumber ());
			
			tActionNumber = 104;
			networkActionsBeta.remove (tActionNumber);
			assertEquals (3, networkActionsBeta.getCount ());
			assertEquals (103, networkActionsBeta.getLastNetworkActionNumber ());
			
			networkActionsBeta.addNetworkAction (networkAction4B);
			networkActionsBeta.addNetworkAction (networkAction5B);
			assertEquals (5, networkActionsBeta.getCount ());
			tActionNumber = 106;
			networkActionsBeta.remove (tActionNumber);
			assertEquals (5, networkActionsBeta.getCount ());

		}

	}
	@Nested
	@DisplayName ("Test Get ActionXML of Network Actions")
	class GetActionXMLNetworkActionTests {
		@Test
		@DisplayName ("Based on an Action Number")
		void getGetActionXMLWithActionNumberTests () {
			int tActionNumber;
			
			networkActionsBeta.addNetworkAction (networkAction1B);
			networkActionsBeta.addNetworkAction (networkAction2B);
			networkActionsBeta.addNetworkAction (networkAction3B);
			networkActionsBeta.addNetworkAction (networkAction4B);
			networkActionsBeta.addNetworkAction (networkAction5B);
			assertEquals (5, networkActionsBeta.getCount ());
			tActionNumber = 106;
			assertEquals ("<ActionNotRecieved/>", networkActionsBeta.getActionXMLFor (tActionNumber));

			tActionNumber = 105;
			assertEquals ("<Action actor=\"GameServer\" class=\"ge18xx.round.action.Action\" number=\"105\">", networkActionsBeta.getActionXMLFor (tActionNumber));
		}
	}
}
