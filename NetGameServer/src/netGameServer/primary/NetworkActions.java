package netGameServer.primary;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.NodeList;

import geUtilities.ElementName;
import geUtilities.FileUtils;
import geUtilities.XMLNode;

public class NetworkActions {
	public static final ElementName EN_NETWORK_ACTIONS = new ElementName ("NetworkActions");
	List<NetworkAction> actions;
	
	public NetworkActions () {
		actions = new LinkedList<NetworkAction> ();
	}

	public void printInfo () {
		if (actions != null) {
			for (NetworkAction tAction : actions) {
				tAction.printInfo ();
			}
		} else {
			System.err.println ("No Network Actions");
		}
			
	}
	public void addNetworkAction (NetworkAction aNetworkAction) {
		actions.add (aNetworkAction);
	}
	
	public int getCount () {
		return actions.size ();
	}
	
	public void clearAll () {
		actions.clear ();
	}
	
	public NetworkAction getNetworkActionAt (int aIndex) {
		NetworkAction tNetworkAction = NetworkAction.NO_ACTION;
		
		if ((aIndex >= 0) && (aIndex < getCount ())) {
			tNetworkAction = actions.get (aIndex);
		}
		
		return tNetworkAction;
	}
	
	public void addGameActionsToFrame (ServerFrame aServerFrame) {
		String tGameAction;
		
		for (NetworkAction tNetworkAction : actions) {
			tGameAction = tNetworkAction.getCompactAction ();
			aServerFrame.addGameAction (tGameAction);
		}
	}
	
	public int getLastNetworkActionNumber () {
		int tActionNumber = NetworkAction.MIN_ACTION_NUMBER;
		NetworkAction tLastNetworkAction;
		
		tLastNetworkAction = getLastNetworkAction ();
		if (tLastNetworkAction != NetworkAction.NO_ACTION) {
			tActionNumber = tLastNetworkAction.getNumber ();
		}
		
		return tActionNumber;
	}
	
	public NetworkAction getLastNetworkAction () {
		NetworkAction tLastNetworkAction = NetworkAction.NO_ACTION;
		int tLastIndex;
		
		if (! actions.isEmpty ()) {
			tLastIndex = actions.size () - 1;
			tLastNetworkAction = actions.get (tLastIndex);
		}
		
		return tLastNetworkAction;
	}
	
	public void remove (int aActionNumber) {
		int tIndex, tFoundIndex, tActionNumber;
		NetworkAction tNetworkAction;
		
		tFoundIndex = 0;
		for (tIndex = (getCount () - 1); (tIndex > 0) && (tFoundIndex == 0); tIndex--) {
			tNetworkAction = getNetworkActionAt (tIndex);
			tActionNumber = tNetworkAction.getNumber ();
			if (tActionNumber == aActionNumber) {
				tFoundIndex = tIndex;
			}
		}
		if (tFoundIndex > 0) {
			actions.remove (tFoundIndex);
		}
	}
	
	public String getStatusAt (int aIndex) {
		NetworkAction tNetworkAction;
		String tStatus = NetworkAction.NO_STATUS;
		
		tNetworkAction = getNetworkActionAt (aIndex);
		if (tNetworkAction != NetworkAction.NO_ACTION) {
			tStatus = tNetworkAction.getStatus ();
		}
		
		return tStatus;
	}
	
	public String getActionXMLFor (int aActionNumber) {
		String tActionXMLFor = NetworkAction.NO_ACTION_RECIEVED_XML;
		int tActionNumber, tCount, tIndex;
		NetworkAction tNetworkAction;
		
		tCount = actions.size ();
		for (tIndex = tCount - 1; 
				(tIndex > 0) && 
				(tActionXMLFor == NetworkAction.NO_ACTION_RECIEVED_XML); 
				tIndex--) {
			tNetworkAction = actions.get (tIndex);
			tActionNumber = tNetworkAction.getNumber ();
			if (aActionNumber == tActionNumber) {
				tActionXMLFor = tNetworkAction.getActionXML ();
			}
		}
		
		return tActionXMLFor;
	}
	
	public String getActionXMLAt (int aIndex) {
		NetworkAction tNetworkAction;
		String tActionXMLAt = NetworkAction.NO_ACTION_RECIEVED_XML;
		
		tNetworkAction = getNetworkActionAt (aIndex);
		if (tNetworkAction != NetworkAction.NO_ACTION) {
			tActionXMLAt = tNetworkAction.getActionXML ();
		}
		
		return tActionXMLAt;
	}
	
	public String getLastActionStatus () {
		NetworkAction tNetworkAction;
		String tStatus = NetworkAction.ACTION_COMPLETE;
		
		tNetworkAction = getLastNetworkAction ();
		if (tNetworkAction != NetworkAction.NO_ACTION) {
			tStatus = tNetworkAction.getStatus ();
		}
		
		return tStatus;
	}
	
	public void writeAllActions (FileUtils aFileUtils) {
		aFileUtils.outputToFile ("<" + EN_NETWORK_ACTIONS + ">");
		for (NetworkAction tNetworkAction : actions) {
			tNetworkAction.writeAction (aFileUtils);
		}
		aFileUtils.outputToFile ("</" + EN_NETWORK_ACTIONS + ">");
	}

	public void loadSavedActions (XMLNode aXMLNetworkActions) {
		XMLNode tChildNode;
		NodeList tChildren;
		int tChildrenCount, tIndex;
		String tChildName;
		NetworkAction tNetworkAction;
		
		tChildren = aXMLNetworkActions.getChildNodes ();
		tChildrenCount = tChildren.getLength ();
		for (tIndex = 0; tIndex < tChildrenCount; tIndex++) {
			tChildNode = new XMLNode (tChildren.item (tIndex));
			tChildName = tChildNode.getNodeName ();
			if (NetworkAction.EN_ACTION.equals (tChildName)) {
				tNetworkAction = new NetworkAction (tChildNode);
				addNetworkAction (tNetworkAction);
			}
		}
	}
}
