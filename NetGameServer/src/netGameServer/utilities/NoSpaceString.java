package netGameServer.utilities;

public class NoSpaceString {
	public final String NO_STRING = null;
	String string;
	
	public NoSpaceString () {
		string = NO_STRING;
	}
	
	public NoSpaceString (String aString) {
		if (aString == NO_STRING) {
			string = aString;
		} else {
			if (aString.length () == 0) {
				string = NO_STRING;
			} else if (aString.indexOf (" ") >= 0) {
				string = NO_STRING;
			} else {
				string = aString;
			}
		}
	}
	
	public boolean hasValue () {
		return (string != NO_STRING);
	}
	
	public boolean equals (String aString) {
		boolean tEquals;
		
		if (hasValue ()) {
			if (aString == NO_STRING) {
				tEquals = false;
			} else {
				tEquals = string.equals (aString);
			}
		} else {
			tEquals = false;
		}
		
		return tEquals;
	}
	
	public String getString () {
		return string;
	}
	
	@Override
	public String toString () {
		return getString ();
	}
}
