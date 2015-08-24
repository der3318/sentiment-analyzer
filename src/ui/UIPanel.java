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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import analyzer.SentimentAnalyzer;

@SuppressWarnings("serial")
public class UIPanel extends JPanel implements ActionListener {

	private JLabel labelT, labelA, labelP, labelN, labelADV, labelO, topic, warning, labelSO, labelTHREADS;
	private JTextField textT, textA, textP, textN, textADV, textO, textSO, textTHREADS;
	private JTextArea status;
	private JButton btnT, btnA, btnP, btnN, btnADV, btnO, start, cancel, showResult;
	Timer timer;
	
	class TTask extends TimerTask {
		
		public void run() {
			warning.setText("     ");
			start.setEnabled(true);
			showResult.setEnabled(true);
			if( !new File( textT.getText() ).exists() )	warning.setText("File of Training Data not Found");
			if( !new File( textA.getText() ).exists() )	warning.setText("File of Training Answers not Found");
			if( !new File( textP.getText() ).exists() )	warning.setText("File of Positive Words not Found");
			if( !new File( textN.getText() ).exists() )	warning.setText("File of Negative Words not Found");
			if( !new File( textADV.getText() ).exists() )	warning.setText("File of Adverbs not Found");
			if( !new File( textO.getText() ).exists() )	warning.setText("File of Testing Opinions not Found");
			if( !warning.getText().equals("     ") )	start.setEnabled(false);
			if( !new File( "result.txt" ).exists() )	showResult.setEnabled(false);
		}
		
	}
	
	public UIPanel() {
		setLayout(new FlowLayout());
		add( topic = new JLabel("Sentiment Analyzer") );
		add( labelP = new JLabel("File of Positive Sentimental Words: ") );
		add( textP = new JTextField("./docs/positive.txt", 25) );
		add( btnP = new JButton("Browse...") );
		add( labelN = new JLabel("File of Negative Sentimental Words: ") );
		add( textN = new JTextField("./docs/negative.txt",25) );
		add( btnN = new JButton("Browse...") );
		add( labelADV = new JLabel("File of Adverbs: ") );
		add( textADV = new JTextField("./docs/adv.txt",25) );
		add( btnADV = new JButton("Browse...") );
		add( labelT = new JLabel("File of Training Data: ") );
		add( textT = new JTextField("./docs/training.txt",25) );
		add( btnT = new JButton("Browse...") );
		add( labelA = new JLabel("File of the Answers of the Training Data: ") );
		add( textA = new JTextField("./docs/answer.txt",25) );
		add( btnA = new JButton("Browse...") );
		add( labelO = new JLabel("File of Testing Opinions: ") );
		add( textO = new JTextField("./docs/opinion.txt",25) );
		add( btnO = new JButton("Browse...") );
		add( labelSO = new JLabel("SO-PMI Rate: ") );
		add( textSO = new JTextField("3.0",4) );
		add( labelTHREADS = new JLabel("Number of Threads: ") );
		add( textTHREADS = new JTextField("4",4) );
		add( start = new JButton("Start Analyzing") );
		add( cancel = new JButton("Cancel") );
		add( showResult = new JButton("Show Results") );
		add( new JScrollPane( status = new JTextArea(5, 28) ) );
		add( warning = new JLabel("     ") );
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
		cancel.addActionListener(this);
		cancel.setForeground(Color.WHITE);
		cancel.setBackground(Color.BLACK);
		cancel.setFont( new Font("Pepsi", Font.PLAIN, 16) );
		showResult.addActionListener(this);
		showResult.setForeground(Color.WHITE);
		showResult.setBackground(Color.BLACK);
		showResult.setFont( new Font("Pepsi", Font.PLAIN, 16) );
		topic.setFont( new Font("Serif", Font.BOLD, 30) );
		topic.setForeground(Color.darkGray);
		labelP.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelN.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelADV.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelT.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelA.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelO.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelSO.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		labelTHREADS.setFont( new Font("Tahoma", Font.PLAIN, 16) );
		warning.setFont( new Font("consolas", Font.PLAIN, 14) );
		warning.setForeground(Color.RED);
		status.setFont( new Font(Font.DIALOG, Font.PLAIN, 16) );
		status.setBackground(Color.LIGHT_GRAY);
		status.setAutoscrolls( getAutoscrolls() );
		timer = new Timer();
		timer.schedule(new TTask(), 500, 1000);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == start) {
			try {
				status.setText("");
				System.setOut( new PrintStream("./log/log.txt") );
				System.setErr( new PrintStream("./log/log.txt") );
				SentimentAnalyzer.setDictionary( textP.getText(), textN.getText(), textADV.getText() );
				SentimentAnalyzer.setTrainingData( textT.getText(), textA.getText() );
				SentimentAnalyzer.setSORate( Double.parseDouble( textSO.getText() ) );
				SentimentAnalyzer.setNTHREADS( Integer.parseInt( textTHREADS.getText() ) );
				SentimentAnalyzer sa = new SentimentAnalyzer(textO.getText(), "result.txt");
				sa.work();
				BufferedReader br = new BufferedReader( new FileReader("./log/log.txt") );
				String tmp = br.readLine();
				while(tmp != null)	{
					status.append(tmp + "\n");
					tmp = br.readLine();
				}
				if( !status.getText().contains("Exception") )	status.append("\nResults are now Availabe in \"result.txt\"");
				else	status.append("Process Failed due to Some Errors");
				br.close();
			}
			catch (IOException ioe)	{
				ioe.printStackTrace();
			}
			return;
		}
		if(ae.getSource() == cancel)	System.exit(0);
		if(ae.getSource() == showResult) {
			try {
				Process process = Runtime.getRuntime().exec("showResult.bat");
				if( process.exitValue() != 0)	System.out.println("Fail to Show Result");
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
			if(ae.getSource() == btnP)	textP.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnN)	textN.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnADV)	textADV.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnT)	textT.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnA)	textA.setText( chooser.getSelectedFile().getAbsolutePath() );
			if(ae.getSource() == btnO)	textO.setText( chooser.getSelectedFile().getAbsolutePath() );
		}
	}

}