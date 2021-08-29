package netGameServer.primary;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimaryFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final static int PORT_18XX = 18300;
	private final static int PORT_CARDS = 52000;
	private final static String NAME_18XX = "18XX";
	private final static String NAME_CARDS = "Cards";
	private String version = "0.5";
	JButton btnQuit;
	JButton btnManage18XX;
	JButton btnManageCards;
	ServerThread serverThread18XX;
	ServerThread serverThreadCards;
	private Logger logger;
	
	public PrimaryFrame () {
		super ("Server Manager");
		setupLogger ();
		setLayout (new GridLayout (4, 1));
		setupFrame ("Server Manager");
		setupActions ();
		setSize (300, 300);
		pack ();
		setVisible (true);
	}
	
	private void setupLogger () {
		String tXMLConfigFile;
	    String tJavaVersion = System.getProperty ("java.version");
	    String tOSName = System.getProperty ("os.name");
	    String tOSVersion = System.getProperty( "os.version");
	    
	    tXMLConfigFile = "XML Data" + File.separator + "log4j2.xml";
		System.setProperty ("log4j.configurationFile", tXMLConfigFile);
		logger = LogManager.getLogger (ServerFrame.class);
		logger.info ("Network Game Server, Version " + getNGSVersion ());
		logger.info ("Java Version " + tJavaVersion + 
					" OS Name " + tOSName + " OS Version " + tOSVersion);
	}

	private String getNGSVersion () {
		return version;
	}
	
	public Logger getLogger () {
		return logger;
	}
	
	public ServerThread newServerThread (String aName, int aPort, ArrayList<String> aGameNames) {
		ServerThread tServerThread;
		
		tServerThread = new ServerThread (aName, aPort, this, aGameNames);
		tServerThread.setLogger (logger);
		tServerThread.start ();
		
		return tServerThread;
	}
	
	public void setupFrame (String aTitle) {
		JLabel lblTitle = new JLabel (aTitle);
		lblTitle.setHorizontalAlignment (SwingConstants.CENTER);
		
		btnQuit = new JButton ("Quit");
		btnQuit.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		btnManage18XX = setupManageButton (NAME_18XX);
		btnManageCards = setupManageButton (NAME_CARDS);
		
		add (lblTitle);
		add (btnManage18XX);
		add (btnManageCards);
		add (btnQuit);
	}
	
	private JButton setupManageButton (String aButtonName) {
		JButton tManageButton;
		
		tManageButton = new JButton ("Manage Button");
		setStartText (tManageButton, aButtonName);
		tManageButton.setAlignmentX (Component.CENTER_ALIGNMENT);
		
		return tManageButton;
	}
	
	public JFrame getThisFrame () {
		return this;
	}

	public ArrayList<String> loadGameNames (String aType) {
		ArrayList<String> tGameNames;
		
		tGameNames = new ArrayList<String> ();
		if (aType.equals (NAME_18XX)) {
			tGameNames.add ("1830");
			tGameNames.add ("1835");
			tGameNames.add ("1853");
			tGameNames.add ("1856");
			tGameNames.add ("1870");
		} else if (aType.equals (NAME_CARDS)) {
			tGameNames.add ("Hearts");
			tGameNames.add ("Spades");
		}
		
		return tGameNames;
	}

	private void setupActions () {
		btnQuit.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				if (serverThread18XX != null) {
					serverThread18XX.quitThread ();
				}
				if (serverThreadCards != null) {
					serverThreadCards.quitThread ();
				}
				System.exit(0);
			}
		});
		btnManage18XX.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				serverThread18XX = manageAction (btnManage18XX, serverThread18XX, PORT_18XX, NAME_18XX);
			}
		});

		btnManageCards.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				serverThreadCards = manageAction (btnManageCards, serverThreadCards, PORT_CARDS, NAME_CARDS);
			}
		});
	}

	public ServerThread manageAction (JButton aServerButton, ServerThread aServerThread, int aPort, String aThreadName) {
		ArrayList<String> tGameNames;
		ServerThread tServerThread;
		
		if (aServerThread != null) {
			if (aServerThread.isRunning ()); {
				aServerThread.quitThread ();
			}
			tServerThread = null;
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
			serverThread18XX = null;
			setStartText (btnManage18XX, NAME_18XX);
		} else if (serverPort == PORT_CARDS) {
			serverThreadCards = null;
			setStartText (btnManageCards, NAME_18XX);
		}
	}
	
	private void setStartText (JButton aManageButton, String aThreadName) {
		aManageButton.setText("Start " + aThreadName + " Server");
	}
	
	private void setStopText (JButton aManageButton, String aThreadName) {
		aManageButton.setText("Stop " + aThreadName + " Server");
	}
}
