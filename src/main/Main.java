package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
 
import vista.VistaPrincipal;
 

public class Main {
	public static void main(String[] args) {
		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		SwingUtilities.invokeLater(() -> new VistaPrincipal().setVisible(true));
	}
}