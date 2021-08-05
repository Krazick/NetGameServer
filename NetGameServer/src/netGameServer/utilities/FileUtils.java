package netGameServer.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.Logger;

public class FileUtils {
	public static final FileWriter NO_FILE_WRITER = null;
	public static final XMLDocument NO_VALID_XML_DOCUMENT = null;
	private Logger logger;
	private FileWriter fileWriter;
	private File file;
	
	public FileUtils (Logger aLogger) {
		setLogger (aLogger);
	}
	
	public void setLogger (Logger aLogger) {
		logger = aLogger;
	}

	public void setFile (File aFile) {
		file = aFile;
	}
	
	public static void createDirectory (String aDirectoryName) {
	    File tDirectory = new File (aDirectoryName);
	    
	    if (! tDirectory.exists ()){
	    	tDirectory.mkdir ();
	    }
	}
	
	public boolean setupFileWriter () {
		boolean tGoodFileWriter = false;
		
		fileWriter = NO_FILE_WRITER;
		
		try {
			fileWriter = new FileWriter (file, false); // Overwrite the file if it exists
			tGoodFileWriter = true;
		} catch (IOException tException) {
			logger.error ("FileUtils problem creating FileWriter", tException);
		}
		
		return tGoodFileWriter;
	}
	
	public boolean fileWriterIsSetup () {
		boolean tFileWriterIsSetup = false;
		
		if (fileWriter != NO_FILE_WRITER) {
			tFileWriterIsSetup = true;
		}
		
		return tFileWriterIsSetup;
	}
	
	public void startXMLFileOutput () {
		outputToFile ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}
	
	public void closeFile () {
		if (fileWriterIsSetup ()) {
			try {
				fileWriter.close ();
				fileWriter = NO_FILE_WRITER;
			} catch (IOException tException) {
				logger.error("FileUtils problem Closing FileWriter", tException);
			}
		}
	}
	
	public void outputToFile (String aDataString) {
		if (fileWriterIsSetup ()) {
			try {
				fileWriter.write (aDataString + "\n");
				fileWriter.flush ();
			} catch (Exception tException) {
				logger.error ("FileUtils problem Writing to FileWriter", tException);
			}
		}
	}
	
	public XMLDocument loadXMLFile (File aSaveGame) {
		XMLDocument tXMLDocument = NO_VALID_XML_DOCUMENT;
		
		if (aSaveGame != null) {
			try {
				tXMLDocument = new XMLDocument (aSaveGame);
				if (! tXMLDocument.ValidDocument ()) {
					logger.error ("XML Document for the Saved File did not load a Valid Document");
				}
			} catch (Exception tException) {
				logger.error ("Oops, mucked up the XML AutoSaved File [" + aSaveGame.getName () + "].");
				logger.error ("Exception Message [" + tException.getMessage () + "].", tException);
			}
		} else {
			logger.error ("No File Object for XML AutoSaved Game");
		}
		
		return tXMLDocument;
	}

}
