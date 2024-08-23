package netGameServer.primary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import geUtilities.FileUtils;
import geUtilities.xml.AttributeName;
import geUtilities.xml.ElementName;
import geUtilities.xml.XMLNode;

public class NetworkAction {
	public static final ElementName EN_ACTION = new ElementName ("Action");
	public static final AttributeName AN_NUMBER = new AttributeName ("number");
	public static final NetworkAction NO_ACTION = null;
	public static final String NO_STATUS = null;
	public static final String NO_ACTION_RECIEVED_XML = "<ActionNotRecieved/>";
	public static final int NO_ACTION_NUMBER = -1;
	public static final int MIN_ACTION_NUMBER = 100;
	public static final String ACTION_COMPLETE = "Complete";
	public static final String ACTION_PENDING = "Pending";
	public static final String ACTION_RECEIVED = "Received";
	private final static String REQUEST_ACTOR = "<Action.* actor=\"([A-Za-z0-9&amp; ]+)\" ";
	private final static Pattern REQUEST_ACTOR_PATTERN = Pattern.compile (REQUEST_ACTOR);
	private final static String REQUEST_NAME = "<Action.*? name=\"([A-Za-z0-9 ]+)\"";
	private final static Pattern REQUEST_NAME_PATTERN = Pattern.compile (REQUEST_NAME);
	int actionNumber;
	String actionXML;
	String status;
	
	public NetworkAction (int aActionNumber, String aStatus) {
		actionNumber = aActionNumber;
		setStatus (aStatus);
		setActionXML (NO_ACTION_RECIEVED_XML);
	}
	
	public NetworkAction (XMLNode aXMLAction) {
		String tAction;
		
		actionNumber = aXMLAction.getThisIntAttribute (AN_NUMBER);
		setStatus (ACTION_COMPLETE);
		tAction = aXMLAction.toString ();
		tAction = tAction.replaceAll ("\r", "").replaceAll ("\n", "");
		tAction = tAction.replaceAll ("&", "&amp;").replaceAll ("&amp;amp;", "&amp;");
		setActionXML (tAction);
	}
	
	public void printInfo () {
		System.out.println ("Action Number " + actionNumber + " Status " + status);
		System.out.println ("XML [" + actionXML + "]");
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
	
	public String getActorName () {
		String tActorName;
		Matcher tMatcher = REQUEST_ACTOR_PATTERN.matcher (actionXML);

		tActorName = "NO_ACTOR";
		
		if (tMatcher.find ()) {
			tActorName = tMatcher.group (1);
		}
		
		return tActorName;
	}
	
	public String getActionName () {
		String tActionName;
		Matcher tMatcher = REQUEST_NAME_PATTERN.matcher (actionXML);

		tActionName = "NO_ACTION";
		
		if (tMatcher.find ()) {
			tActionName = tMatcher.group (1);
		}
		
		return tActionName;
	}
	
	public String getCompactAction () {
		String tCompactAction;
		
		tCompactAction = getNumber () + " " + getActorName () + " [" + getActionName () + "]";
		
		return tCompactAction;
	}
}
