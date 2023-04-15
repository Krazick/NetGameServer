package netGameServer.utilities;

//
//  XMLNode.java
//  Game_18XX
//
//  Created by Mark Smith on 10/13/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//
//import ge18xx.utilities.AttributeName;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLNode {
	Node node;
	
	
	public XMLNode (Node aNode) {
		node = aNode;
	}
	
	public String getNodeName () {
		return node.getNodeName ();
	}
	
	public NamedNodeMap getAttributes () {
		return node.getAttributes ();
	}
	
	public NodeList getChildNodes () {
		return node.getChildNodes ();
	}
	
	public String getThisAttribute (AttributeName aAttributeName) {
		String tAttributeValue = null;
		String tAttributeName;
		
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisAttribute (tAttributeName);
		}
		
		return tAttributeValue;
	}
	
	public String getThisAttribute (AttributeName aAttributeName, String aDefaultValue) {
		String tAttributeValue = aDefaultValue;
		String tAttributeName;
		
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisAttribute (tAttributeName, aDefaultValue);
		}
		
		return tAttributeValue;
	}
	
	/* PRIVATE */
	private String getThisAttribute (String aAttributeName) {
		String tAttributeValue = null;
		NamedNodeMap tAttributesNNM;
		Attr tAttribute;
		int tAttributeCount;
		int tAttributeIndex;
		
		tAttributesNNM = node.getAttributes ();
		tAttributeCount = tAttributesNNM.getLength ();
		for (tAttributeIndex = 0; tAttributeIndex < tAttributeCount; tAttributeIndex++) {
			tAttribute = (Attr) tAttributesNNM.item (tAttributeIndex);
			if (aAttributeName.equals (tAttribute.getNodeName ())) {
				tAttributeValue = tAttribute.getNodeValue();
			}
		}
		
		return tAttributeValue;
	}
	
	/* PRIVATE */
	private String getThisAttribute (String aAttributeName, String aDefaultValue) {
		String tValue = getThisAttribute (aAttributeName);
		
		if (tValue == null) {
			return aDefaultValue;
		} else {
			return tValue;
		}
	}
	
	/* Parse out a Boolean Attribute, return -false- if attribute is not found */
	public boolean getThisBooleanAttribute (AttributeName aAttributeName) {
		boolean tAttributeValue = false;
		String tAttributeName;
		
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisBooleanAttribute (tAttributeName);
		}
		
		return tAttributeValue;
	}

	/* PRIVATE */
	private boolean getThisBooleanAttribute (String aAttributeName) {
		String tValue = getThisAttribute (aAttributeName);
		boolean retValue = false;
		
		if (tValue == null) {
			retValue = false;
		} else if ((tValue.equals ("TRUE")) || (tValue.equals ("true")) ||
				   (tValue.equals ("True")) || (tValue.equals ("T")) || (tValue.equals ("t")) ||
				   (tValue.equals ("YES")) || (tValue.equals ("yes")) || (tValue.equals ("Yes")) ||
				   (tValue.equals ("Y")) || (tValue.equals ("y"))) {
			retValue = true;
		} else if ((tValue.equals ("FALSE")) || (tValue.equals ("false")) ||
				   (tValue.equals ("False")) || (tValue.equals ("F")) || (tValue.equals ("f")) ||
				   (tValue.equals ("NO")) || (tValue.equals ("no")) || (tValue.equals ("No")) ||
				   (tValue.equals ("N")) || (tValue.equals ("n"))) {

			retValue = false;
		}
		
		return retValue;
	}
	
	public int getThisIntAttribute (AttributeName aAttributeName) {
		int tAttributeValue = 0;
		String tAttributeName;
		
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisIntAttribute (tAttributeName, 0);
		}
		
		return tAttributeValue;
	}
	
	public int getThisIntAttribute (AttributeName aAttributeName, int aDefaultValue) {
		int tAttributeValue = aDefaultValue;
		String tAttributeName;
		
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisIntAttribute (tAttributeName, aDefaultValue);
		}
		
		return tAttributeValue;
	}
	 
	/* PRIVATE */
	private int getThisIntAttribute (String aAttributeName, int aDefaultValue) {
		String tValue = getThisAttribute (aAttributeName);
		
		if (tValue == null) {
			return aDefaultValue;
		} else {
			return Integer.parseInt (tValue);
		}
	}

	public long getThisLongAttribute (AttributeName aAttributeName) {
		long tAttributeValue;
		String tAttributeName;

		tAttributeValue = 0;
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisLongAttribute (tAttributeName, tAttributeValue);
		}

		return tAttributeValue;
	}

	public long getThisLongAttribute (AttributeName aAttributeName, long aDefaultValue) {
		long tAttributeValue;
		String tAttributeName;

		tAttributeValue = aDefaultValue;
		if (aAttributeName.hasValue ()) {
			tAttributeName = aAttributeName.getString ();
			tAttributeValue = getThisLongAttribute (tAttributeName, aDefaultValue);
		}

		return tAttributeValue;
	}

	/* PRIVATE */
	private long getThisLongAttribute (String aAttributeName, long aDefaultValue) {
		String tValue;
		long tLongValue;
		
		tValue = getThisAttribute (aAttributeName);
		if (tValue == null) {
			tLongValue = aDefaultValue;

		} else {
			tLongValue = Long.parseLong (tValue);
		}
		
		return tLongValue;
	}

	@Override
	public String toString () {
		return convertNodeToString (node);
	}
	
	private static String convertNodeToString (Node aNode) {
	     try {
	        StringWriter writer = new StringWriter();

	        Transformer trans = TransformerFactory.newInstance ().newTransformer ();
	        trans.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty (OutputKeys.INDENT, "yes");
	        trans.transform(new DOMSource (aNode), new StreamResult (writer));

	        return writer.toString ();
	    } catch (TransformerException te) {
	        te.printStackTrace ();
	    }

	    return "";
	}
}
