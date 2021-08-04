package netGameServer.primary;

import netGameServer.utilities.FileUtils;

public class NetworkAction {
	public static final NetworkAction NO_ACTION = null;
	public static final String NO_STATUS = null;
	public static final String NO_ACTION_RECIEVED_XML = "<ActionNotRecieved/>";
	public static final int NO_ACTION_NUMBER = -1;
	public static final int MIN_ACTION_NUMBER = 100;
	public static final String ACTION_COMPLETE = "Complete";
	public static final String ACTION_PENDING = "Pending";
	public static final String ACTION_RECEIVED = "Received";
	int actionNumber;
	String actionXML;
	String status;
	
	public NetworkAction (int aActionNumber, String aStatus) {
		
		actionNumber = aActionNumber;
		setStatus (aStatus);
		setActionXML (NO_ACTION_RECIEVED_XML);
	}
	
	public void setStatus (String aStatus) {
		status = aStatus;
	}
	
	public String getStatus () {
		return status;
	}
	
	public int getNumber () {
		return actionNumber;
	}

	public String getActionXML () {
		return actionXML;
	}
	
	public void setActionXML (String aActionXML) {
		actionXML = aActionXML;
	}
	
	public void writeAction (FileUtils aFileUtils) {
		aFileUtils.outputToFile (actionXML);
	}
}
