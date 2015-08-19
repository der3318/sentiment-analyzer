package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KeywordFinder {

	private int NTHREADS = 4;
	// create dictionary and reader
	private SentimentalDictionary dict = new SentimentalDictionary();
	private TextReader txt_rdr = new TextReader();	
	// create SegChinese
	private SegChinese seg = new SegChinese();  
	// create frequency recorder
	private FrequencyRecorder f_rec = new FrequencyRecorder();
	// an ArrayList holding the answers of the training data
	private ArrayList<Boolean> ans = new ArrayList<Boolean>();
	private int ans_pos = 0;
	private int ans_neg = 0;
	// set the branch for SO-PMI result
	private double SO_rate = 3d;
	
	public class FreRunnable implements Runnable {

		private int index;
		
		FreRunnable(int _index) {
			index = _index;
		}
		
		public void run() {
			ArrayList<String> opinion = txt_rdr.getTextbyIndex(index);
			for(String sentence : opinion) {
				try {
					for( String subSentence : seg.getSegList(sentence) ) {
						if( ans.get(index) )	f_rec.addPosFrequency(subSentence);
						else	f_rec.addNegFrequency(subSentence);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public class DictRunnable implements Runnable {
		
		private String s;
		
		DictRunnable(String _s) {
			s = _s;
		}
		
		public void run() {
			if( SO(s) > SO_rate )	 dict.addPositiveWords(s);
			else if( SO(s) < -SO_rate )	dict.addNegativeWords(s);
		}
	
	}

	public void setSORate(double _rate) {
		SO_rate = _rate;
	}
	
	public void setNTHREADS(int _nthreads) {
		NTHREADS = _nthreads;
	}
	
	// SO = PMI(_string, positive) - PMI(_string, negative)
	private double SO(String _string) {
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
				if(tmp.trim().equals("P")) {
					ans.add(true);
					ans_pos += 1;
				}
				else {
					ans.add(false);
					ans_neg += 1;
				}
				tmp = br.readLine();
			}
			br.close();
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
		ExecutorService fre_executor = Executors.newFixedThreadPool(NTHREADS);
		ExecutorService dict_executor = Executors.newFixedThreadPool(NTHREADS);
		for(int i = 0 ; i < n ; i++) {
			Runnable task = new FreRunnable(i);
			fre_executor.execute(task);
		}
		fre_executor.shutdown();
		while( !fre_executor.isTerminated() ) {
		}
		for( String s : f_rec.getRecordedStrings() ) {
			Runnable task = new DictRunnable(s);
			dict_executor.execute(task);
		}
		dict_executor.shutdown();
		while( !dict_executor.isTerminated() ) {
		}
	}

	public void printToFile() throws IOException {
		System.out.println("Saving Results into \"./docs/pos_by_training.txt\" and \"./docs/neg_by_training.txt\"");
		FileWriter fw_p = new FileWriter("./docs/pos_by_training.txt");
		for( String s : dict.getPositiveWords() )	fw_p.write(s + "\n");
		FileWriter fw_n = new FileWriter("./docs/neg_by_training.txt");
		for( String s : dict.getNegativeWords() )	fw_n.write(s + "\n");
		fw_p.flush();
		fw_n.flush();
		fw_p.close();
		fw_n.close();
	}
	
}
