package netGameServer.primary;

import java.awt.HeadlessException;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.logging.log4j.Logger;

public class ServerThread extends Thread {
	ServerFrame serverFrame = null;
	PrimaryFrame primaryFrame = null;
	String name;
	int serverPort;
	boolean isRunning;
	LinkedList<String> gameNames;
	Logger logger;
	
	public ServerThread (String aName, int aServerPort, PrimaryFrame aPrimaryFrame, LinkedList<String> aGameNames) {
		super ();
		name = aName;
		serverPort = aServerPort;
		primaryFrame = aPrimaryFrame;
		logger = aPrimaryFrame.getLogger ();
		setGameNames (aGameNames);
	}
	
	public void setLogger (Logger aLogger) {
		logger = aLogger;
	}
	
	public Logger getLogger () {
		return logger;
	}
	
	@Override
	public void run () {
		if (serverFrame == null) {
			try {
				logger.info ("Server Thread trying to Run");
				serverFrame = new ServerFrame (name, serverPort, gameNames, this);
				serverFrame.operateFrame ();
				logger.info ("Server Up and Operating Frame");
			} catch (HeadlessException tException1) {
				serverFrame.log ("Setting up Server Frame throwing Headless Exception", tException1);
			} catch (IOException tException2) {
				serverFrame.log ("Setting up Server Frame throwing Exception", tException2);
			}
		} else {
			logger.error ("Server already started on this Thread with Port " + serverPort);
		}
	}
	
	private void setGameNames (LinkedList<String> aGameNames) {
		gameNames = aGameNames;
	}
	
	public LinkedList<String> getGameNames () {
		return gameNames;
	}
	
	public void setIsRunning (boolean aIsRunning) {
		isRunning = aIsRunning;
	}

	public void quitThread () {
		logger.error ("Tell Thread on Port " + serverPort + " to stop");
		serverFrame.quitFrame ();
	}
	
	public boolean isRunning () {
		return isRunning;
	}
	
	public void frameQuitting () {
		setIsRunning (false);
		logger.info ("Frame being told to Quit");
		primaryFrame.frameQuitting (serverPort);
	}
}
