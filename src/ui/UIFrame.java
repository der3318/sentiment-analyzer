package ui;

import java.awt.Font;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class UIFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UIFrame() throws IOException {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		setContentPane(new UIPanel());
		setLocation(ALLBITS, ALLBITS);
		setSize(400,600);
		setUndecorated(true);
		setVisible(true);
	}
	
	public static void main(String[] args) throws IOException {
		/* Command Line version */ 
		//SentimentAnalyzer sa = new SentimentAnalyzer();
		//sa.work();
		
		/* GUI version */ 
		new UIFrame();
		setUIFont(new javax.swing.plaf.FontUIResource("微軟正黑體", Font.BOLD, 16));
	}
	
	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		Enumeration<?> keys = UIManager.getLookAndFeelDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value != null && value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}
	
}
