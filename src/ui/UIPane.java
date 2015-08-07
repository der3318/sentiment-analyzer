package ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import analyzer.SentimentAnalyzer;

public class UIPane extends JPanel implements ActionListener {

	private JLabel textT, textA, textP, textN, textADV, textO, topic;
	private JTextField viewT, viewA, viewP, viewN, viewADV, viewO;
	private JTextArea status;
	private JButton btnT, btnA, btnP, btnN, btnADV, btnO, start, cancle, showResult;
	private JScrollPane scrollPane;
	
	public UIPane() {
		setLayout(new FlowLayout());
		add( topic = new JLabel("Sentiment Analyzer") );
		add( textP = new JLabel("File of Positive Sentimental Words: ") );
		add( viewP = new JTextField("./docs/positive.txt", 25) );
		add( btnP = new JButton("Browse") );
		add( textN = new JLabel("File of Negative Sentimental Words: ") );
		add( viewN = new JTextField("./docs/negative.txt",25) );
		add( btnN = new JButton("Browse") );
		add( textADV = new JLabel("File of Adverbs: ") );
		add( viewADV = new JTextField("./docs/adv.txt",25) );
		add( btnADV = new JButton("Browse") );
		add( textT = new JLabel("File of Training Data: ") );
		add( viewT = new JTextField("./docs/training.txt",25) );
		add( btnT = new JButton("Browse") );
		add( textA = new JLabel("File of the Answers: ") );
		add( viewA = new JTextField("./docs/answer.txt",25) );
		add( btnA = new JButton("Browse") );
		add( textO = new JLabel("File of Testing Opinions: ") );
		add( viewO = new JTextField("./docs/opinion.txt",25) );
		add( btnO = new JButton("Browse") );
		add( start = new JButton("Start Analyzing") );
		add( cancle = new JButton("Cancle") );
		add( showResult = new JButton("Show Results") );
		add( scrollPane = new JScrollPane( status = new JTextArea(5, 30) ) );
		btnP.addActionListener(this);
		btnN.addActionListener(this);
		btnADV.addActionListener(this);
		btnT.addActionListener(this);
		btnA.addActionListener(this);
		btnO.addActionListener(this);
		start.addActionListener(this);
		start.setForeground(Color.WHITE);
		start.setBackground(Color.BLACK);
		start.setFont( new Font("Pepsi", Font.PLAIN, 16) );
		cancle.addActionListener(this);
		cancle.setForeground(Color.WHITE);
		cancle.setBackground(Color.BLACK);
		cancle.setFont( new Font("Pepsi", Font.PLAIN, 16) );
		showResult.addActionListener(this);
		showResult.setForeground(Color.WHITE);
		showResult.setBackground(Color.BLACK);
		showResult.setFont( new Font("Pepsi", Font.PLAIN, 16) );
		topic.setFont( new Font("Serif", Font.BOLD, 30) );
		topic.setForeground(Color.darkGray);
		textP.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		textN.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		textADV.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		textT.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		textA.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		textO.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		status.setFont( new Font(Font.DIALOG, Font.PLAIN, 16) );
		status.setBackground(Color.LIGHT_GRAY);
		status.setAutoscrolls( getAutoscrolls() );
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == start) {
			SentimentAnalyzer sa = new SentimentAnalyzer( viewP.getText(), viewN.getText(), viewADV.getText(), viewT.getText(), viewA.getText(), viewO.getText() );
			try {
				status.setText("");
				System.setOut( new PrintStream("./docs/log.txt") );
				sa.work();
				BufferedReader br = new BufferedReader( new FileReader("./docs/log.txt") );
				String tmp = br.readLine();
				while(tmp != null)	{
					status.append(tmp + "\n");
					tmp = br.readLine();
				}
				status.append("Results are now availabe in \"result.txt\"");
				br.close();
			}
			catch (IOException ioe)	{
				ioe.printStackTrace();
			}
			return;
		}
		if(ae.getSource() == cancle)	System.exit(0);
		if(ae.getSource() == showResult) {
			try {
				Process process = Runtime.getRuntime().exec("showResult.bat");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new File(".") );
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			if(ae.getSource() == btnP)	viewP.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnN)	viewN.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnADV)	viewADV.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnT)	viewT.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnA)	viewA.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnO)	viewO.setText( chooser.getSelectedFile().getAbsolutePath() );
		}
	}

}
