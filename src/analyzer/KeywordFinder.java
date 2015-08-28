package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KeywordFinder {

	// a static finder shared by all analyzers
	private static KeywordFinder finder;
	// filenames
	private static String filenameT = new String("./docs/training.txt");
	private static String filenameA = new String("./docs/answer.txt");
	private static int NTHREADS = 4;
	// set the branch for SO-PMI result
	private static double pos_SO_rate = 3d;
	private static double neg_SO_rate = 3d;
	// setup Segmenter
	private static SegChinese seg = SegChinese.getInstance();  

	// create dictionary and reader
	private SentimentalDictionary dict = new SentimentalDictionary();
	private TextReader txt_rdr = new TextReader();	
	// create frequency recorder
	private FrequencyRecorder f_rec = new FrequencyRecorder();
	// an ArrayList holding the answers of the training data
	private ArrayList<Boolean> ans = new ArrayList<Boolean>();
	private int ans_pos = 0;
	private int ans_neg = 0;
	
	// each Runnable Object holds one opinion, separating the opinion into words
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
						if( subSentence.length() <= 1 )	continue;
						else if( ans.get(index) )	f_rec.addPosFrequency(subSentence);
						else	f_rec.addNegFrequency(subSentence);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	// each Runnable Object holds one string, determining whether if should be added to the dictionary or not
	public class DictRunnable implements Runnable {
		
		private String s;
		
		DictRunnable(String _s) {
			s = _s;
		}
		
		public void run() {
			if( SO(s) > pos_SO_rate )	 dict.addPositiveWords(s);
			else if( SO(s) < -neg_SO_rate )	dict.addNegativeWords(s);
		}
	
	}

	// return the prepared finder. if not found, create one
	public static KeywordFinder getInstance() {
        if (finder == null) {
            synchronized (KeywordFinder.class) {
                if (finder == null) {
                	finder = new KeywordFinder();
                	long beginTime = System.currentTimeMillis();
                	finder.readTrainingData();
                	finder.train();
                	finder.printToFile();
                	System.out.println("Time for Training: " + (System.currentTimeMillis() - beginTime) / 1000.0 + " second(s)");
                	return finder;
                }
            }
        }
        return finder;
    }
	
	// remove the current finder due to some setting changes
	public static void removeInstance() {
		finder = null;
	}
	
	public static void setFileame(String _filenameT, String _filenameA) {
		filenameT = _filenameT;
		filenameA = _filenameA;
	}
	
	public static void setSORate(double pos_rate, double neg_rate) {
		pos_SO_rate = pos_rate;
		neg_SO_rate = neg_rate;
	}
	
	public static void setNTHREADS(int _nthreads) {
		NTHREADS = _nthreads;
	}
	
	// SO = PMI(_string, positive) - PMI(_string, negative)
	private double SO(String _string) {
		return Math.log( ((double)f_rec.getPosFrequency(_string) + 0.1) / ((double)f_rec.getNegFrequency(_string) + 0.1) * ((double)ans_neg + 0.1) / ((double)ans_pos + 0.1) );
	}
	
	public void readTrainingData() {
		try {
			// readAnswer
			System.out.println("Accessing " + filenameA);
			FileReader fr = new FileReader(filenameA);
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
			// readText
			txt_rdr.readText(filenameT);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void train() {
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

	public void printToFile() {
		try {
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
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
