package netGameServer.primary;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GameServer {
	
	GameServer () {
		createAndShowGUI ();
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI () {
        //Create and set up the window.
        PrimaryFrame frame;
        
        frame = new PrimaryFrame ();
        frame.setDefaultCloseOperation (PrimaryFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
	public static void main (String [] args) throws IOException {
//		new GameServer ();
		
	    //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
		
        SwingUtilities.invokeLater (new Runnable () {
        		@Override
			public void run () {
        			//Turn off metal's use of bold fonts
        			UIManager.put ("swing.boldMetal", Boolean.FALSE);
                 
        			createAndShowGUI ();
            }
        });
	}
}