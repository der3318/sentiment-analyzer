package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class KeywordFinder {
	
	// create dictionary and reader
	private SentimentalDictionary dict = new SentimentalDictionary();
	private TextReader txt_rdr = new TextReader();	
	// create SegChinese
	private SegChinese seg = new SegChinese();  
	// create frequency recorder
	private FrequencyRecorder f_rec = new FrequencyRecorder();
	// an ArrayList holding the answers of the training data
	private ArrayList<Boolean> ans = new ArrayList<Boolean>();
	// set the branch for SO-PMI result
	private double SO_rate = 4.5d;
	
	public void setSORate (double _rate) {
		SO_rate = _rate;
	}
	
	// SO = PMI(_string, positive) - PMI(_string, negative)
	private double SO(String _string) {
		int ans_pos = 0, ans_neg = 0;
		for(boolean a : ans) {
			ans_pos += (a ? 1 : 0);
			ans_neg += (a ? 0 : 1);
		}
		return Math.log( ((double)f_rec.getPosFrequency(_string) + 0.1) / ((double)f_rec.getNegFrequency(_string) + 0.1) * ((double)ans_neg + 0.1) / ((double)ans_pos + 0.1) );
	}
	
	public void readTrainingData(String _filenameT, String _filenameA) throws IOException {
		// readAnswer
		try {
			System.out.println("Accessing " + _filenameA);
			FileReader fr = new FileReader(_filenameA);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				if(tmp.trim().equals("P"))	ans.add(true);
				else	ans.add(false);
				tmp = br.readLine();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Answer File not Found");
			e.printStackTrace();
		}
		// readText
		txt_rdr.readText(_filenameT);
	}
	
	public void train() throws IOException {
		int n = txt_rdr.getSize();
		assert( n == ans.size() && n != 0);
		for(int i = 0 ; i < n ; i++) {
			// get the "i th" opinion
			ArrayList<String> opinion = txt_rdr.getTextbyIndex(i);
			for(String sentence : opinion)
				for( String subSentence : seg.getSegList(sentence) ) {
					if( ans.get(i) )	f_rec.addPosFrequency(subSentence);
					else	f_rec.addNegFrequency(subSentence);
				}
		}
		for( String s : f_rec.getRecordedStrings() ) {
			if( SO(s) > SO_rate )	 dict.addPositiveWords(s);
			else if( SO(s) < -SO_rate )	dict.addNegativeWords(s);
		}
	}

	public void printToFile() throws IOException {
		System.out.println("Saving Results into \"pos_by_training.txt\" and \"neg_by_training\"");
		FileWriter fw_p = new FileWriter("pos_by_training.txt");
		for( String s : dict.getPositiveWords() )	fw_p.write(s + "\n");
		FileWriter fw_n = new FileWriter("neg_by_training.txt");
		for( String s : dict.getNegativeWords() )	fw_n.write(s + "\n");
		fw_p.flush();
		fw_n.flush();
		fw_p.close();
		fw_n.close();
	}
	
}
