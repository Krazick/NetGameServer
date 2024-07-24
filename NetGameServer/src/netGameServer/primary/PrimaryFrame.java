package netGameServer.primary;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import swingTweaks.KButton;

public class PrimaryFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public static final PrimaryFrame NO_PRIMARY_FRAME = null;
	private final static int PORT_18XX = 18300;
	private final static int PORT_CARDS = 52000;
	private final static String NAME_18XX = "18XX";
	private final static String NAME_CARDS = "Cards";
	private String version = "0.6";
	JPanel primaryPanel;
	KButton btnQuit;
	KButton btnManage18XX;
	KButton btnManageCards;
	ServerThread serverThread18XX;
	ServerThread serverThreadCards;
	private Logger logger;
	
	public PrimaryFrame () {
		super ("Server Manager");
		setupLogger ();
		primaryPanel = setupPrimaryPanel ();
		add (primaryPanel);
		setupActions ();
		setSize (200, 140);
		setVisible (true);
	}
	
	private void setupLogger () {
		String tXMLConfigFile;
	    String tJavaVersion = System.getProperty ("java.version");
	    String tOSName = System.getProperty ("os.name");
	    String tOSVersion = System.getProperty( "os.version");
	    String tLog4JVersion;

	    tXMLConfigFile = "XML Data" + File.separator + "log4j2.xml";
		System.setProperty ("log4j.configurationFile", tXMLConfigFile);
		logger = LogManager.getLogger (ServerFrame.class);
		logger.info ("Network Game Server, Version " + getNGSVersion ());
		logger.info ("Java Version " + tJavaVersion + 
					" OS Name " + tOSName + " OS Version " + tOSVersion);
		tLog4JVersion = org.apache.logging.log4j.LogManager.class.getPackage ().getImplementationVersion ();
		logger.info ("Log4J2 LogManager Version " + tLog4JVersion);
	}

	private String getNGSVersion () {
		return version;
	}
	
	public Logger getLogger () {
		return logger;
	}
	
	public ServerThread newServerThread (String aName, int aPort, LinkedList<String> aGameNames) {
		ServerThread tServerThread;
		
		tServerThread = new ServerThread (aName, aPort, this, aGameNames);
		tServerThread.setLogger (logger);
		tServerThread.start ();
		
		return tServerThread;
	}
	
	public JPanel setupPrimaryPanel () {
		JPanel tPrimaryPanel;
		
		tPrimaryPanel = new JPanel ();
		tPrimaryPanel.setLayout (new BoxLayout (tPrimaryPanel, BoxLayout.PAGE_AXIS));

		btnQuit = new KButton ("Quit");
		btnQuit.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		btnManage18XX = setupManageButton (NAME_18XX);
		btnManageCards = setupManageButton (NAME_CARDS);
		
		tPrimaryPanel.add (Box.createVerticalStrut (10));
		tPrimaryPanel.add (btnManage18XX);
		tPrimaryPanel.add (Box.createVerticalStrut (10));
		tPrimaryPanel.add (btnManageCards);
		tPrimaryPanel.add (Box.createVerticalStrut (10));
		tPrimaryPanel.add (btnQuit);
		tPrimaryPanel.add (Box.createVerticalStrut (10));
		
		return tPrimaryPanel;
	}
	
	public Point getOffsetFrame () {
		Point tFramePoint, tNewPoint;
		double tX, tY;
		int tNewX, tNewY;

		tFramePoint = getLocation ();
		tX = tFramePoint.getX ();
		tY = tFramePoint.getY ();
		tNewX = (int) tX + 100;
		tNewY = (int) tY + 100;
		tNewPoint = new Point (tNewX, tNewY);

		return tNewPoint;
	}
	
	private KButton setupManageButton (String aButtonName) {
		KButton tManageButton;
		
		tManageButton = new KButton ("Manage Button");
		setStartText (tManageButton, aButtonName);
		tManageButton.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		return tManageButton;
	}
	
	public JFrame getThisFrame () {
		return this;
	}

	public LinkedList<String> loadGameNames (String aType) {
		LinkedList<String> tGameNames;
		
		tGameNames = new LinkedList<String> ();
		if (aType.equals (NAME_18XX)) {
			tGameNames.add ("1830");
			tGameNames.add ("1835");
			tGameNames.add ("1853");
			tGameNames.add ("1856");
			tGameNames.add ("1870");
			tGameNames.add ("1830+");
			tGameNames.add ("1830TEST");
		} else if (aType.equals (NAME_CARDS)) {
			tGameNames.add ("Hearts");
			tGameNames.add ("Spades");
		}
		
		return tGameNames;
	}

	private void setupActions () {
		btnQuit.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed (ActionEvent e) {
				if (serverThread18XX != ServerThread.NO_SERVER_THREAD) {
					serverThread18XX.quitThread ();
				}
				if (serverThreadCards != ServerThread.NO_SERVER_THREAD) {
					serverThreadCards.quitThread ();
				}
				System.exit(0);
			}
		});
		btnManage18XX.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed (ActionEvent e) {
				serverThread18XX = manageAction (btnManage18XX, serverThread18XX, PORT_18XX, NAME_18XX);
			}
		});

		btnManageCards.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed (ActionEvent e) {
				serverThreadCards = manageAction (btnManageCards, serverThreadCards, PORT_CARDS, NAME_CARDS);
			}
		});
	}

	public ServerThread manageAction (KButton aServerButton, ServerThread aServerThread, int aPort, String aThreadName) {
		LinkedList<String> tGameNames;
		ServerThread tServerThread;
		
		if (aServerThread != ServerThread.NO_SERVER_THREAD) {
			if (aServerThread.isRunning ()); {
				aServerThread.quitThread ();
			}
			tServerThread = ServerThread.NO_SERVER_THREAD;
			setStartText (aServerButton, aThreadName);
			frameQuitting (aPort);
		} else {
			tGameNames = loadGameNames (aThreadName);
			tServerThread = newServerThread (aThreadName, aPort, tGameNames);
			setStopText (aServerButton, aThreadName);
		}
		
		return tServerThread;
	}
	
	public void frameQuitting (int serverPort) {
		if (serverPort == PORT_18XX) {
			serverThread18XX = ServerThread.NO_SERVER_THREAD;
			setStartText (btnManage18XX, NAME_18XX);
		} else if (serverPort == PORT_CARDS) {
			serverThreadCards = ServerThread.NO_SERVER_THREAD;
			setStartText (btnManageCards, NAME_CARDS);
		}
	}
	
	private void setStartText (KButton aManageButton, String aThreadName) {
		aManageButton.setText("Start " + aThreadName + " Server");
	}
	
	private void setStopText (KButton aManageButton, String aThreadName) {
		aManageButton.setText("Stop " + aThreadName + " Server");
	}
}
