package netGameServer.primary;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.NodeList;

import netGameServer.utilities.ElementName;
import netGameServer.utilities.FileUtils;
import netGameServer.utilities.XMLNode;

public class NetworkActions {
	public static final ElementName EN_NETWORK_ACTIONS = new ElementName ("NetworkActions");
	List<NetworkAction> actions;
	
	public NetworkActions () {
		actions = new LinkedList<NetworkAction> ();
	}

	public void printInfo () {
		if (actions != null) {
			System.out.println ("Network Action Count is " + actions.size ());
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
		return getNetworkActionAt (getCount () - 1);
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
