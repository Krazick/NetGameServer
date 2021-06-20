package netGameServer.primary;

import java.util.LinkedList;
import java.util.List;

public class NetworkActions {
	List<NetworkAction> actions;
	
	public NetworkActions () {
		actions = new LinkedList<NetworkAction> ();
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
		
		for (NetworkAction networkAction : actions) {
			if (aActionNumber == networkAction.getNumber ()) {
				tActionXMLFor = networkAction.getActionXML ();
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
}
