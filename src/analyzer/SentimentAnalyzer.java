package analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SentimentAnalyzer {
	
	private int NTHREADS = 4;
	private String filenameP = new String("./docs/positive.txt");
	private String filenameN = new String("./docs/negative.txt");
	private String filenameADV = new String("./docs/adv.txt");
	private String filenameT = new String("./docs/training.txt");
	private String filenameA = new String("./docs/answer.txt");
	private String filenameO = new String("./docs/opinion.txt");
	// create dictionary and reader
	private SentimentalDictionary dict;
	private TextReader txt_rdr;
	// number of positive answers and the total number of opinions
	private int positive;
	private int total_opinions;
	// create SegChinese
	private SegChinese seg = new SegChinese();  
	// create frequency recorder
	private FrequencyRecorder f_rec;
	// create trainer
	private KeywordFinder trainer;
	// OutputWriter
	private FileWriter fw;
	// SORate
	private double SORate = 3.0d;
	
	public class SACallable implements Callable<String> {

		private int index;
		
		SACallable(int _index) {
			index = _index;
		}
		
		public String call() {
			String output = new String();
			try {
				int total_rate = 0;
				ArrayList<String> opinion = txt_rdr.getTextbyIndex(index);
				ArrayList<String> keywords = new ArrayList<String>();
				ArrayList<String> keyadvs = new ArrayList<String>();
				for(int i = 0 ; i < opinion.size() ; i++) {
					// get the "i th" sentence in the "index th" opinion
					int rate = 0, flag = 1;
					String sentence = opinion.get(i);
					// disassemble the sentence and check adverbs
					for(int length = sentence.length(); length > 0 ; length--) {
						for(int endIndex = sentence.length() ; endIndex >= length ; endIndex--) {
							String word = sentence.substring(endIndex - length, endIndex);
							// key adverb found
							if( dict.checkAdv(word) ) {
								flag = 2;
								keyadvs.add(word);
								sentence = sentence.replaceAll(word, "");
								length = sentence.length() + 1;
								break;
							}
						}
					}
					// disassemble the sentence and check sentimental words
					for(int length = sentence.length(); length > 0 ; length--) {
						for(int endIndex = sentence.length() ; endIndex >= length ; endIndex--) {
							String word = sentence.substring(endIndex - length, endIndex);
							// keyword found
							if(dict.checkWord(word) != 0) {
								keywords.add(word);
								rate += flag * dict.checkWord(word);
								sentence = sentence.replaceAll(word, "");
								length = sentence.length() + 1;
								break;
							}
						}
					}
					// check if the shifter exists
					if( sentence.contains("不") || sentence.contains("沒") )		rate *= -1;
					total_rate += rate;
				}
				if(total_rate >= 0)	positive += 1;
				output = "NO." + (index + 1) + ": rate = " + total_rate + (total_rate >= 0 ? " (Positive)\n" : " (Negative)\n");
				for(String sentence : opinion) {
						output += (seg.segWords(sentence, " ") + " "); // print detail
					for( String segSentence : seg.getSegList(sentence) ){
						if(total_rate >= 0)	f_rec.addPosFrequency(segSentence);
						else	f_rec.addNegFrequency(segSentence);
					}
				}
				output += "\nKeyWords Found: ";
				for(String word : keywords)	output += (word + "(" + dict.checkWord(word) + ") ");
				for(String word : keyadvs)	output += (word + "(adv) ");
				output += "\n\n";
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return output;
		}
		
	}
	
	public SentimentAnalyzer(String _filenameP, String _filenameN, String _filenameADV, String _filenameT, String _filenameA, String _filenameO) {
		filenameP = _filenameP;
		filenameN = _filenameN;
		filenameADV = _filenameADV;
		filenameT = _filenameT;
		filenameA = _filenameA;
		filenameO = _filenameO;
	}
	
	public SentimentAnalyzer() {
	}

	public void setSORate(double _rate) {
		SORate = _rate;
	}
	
	public void setNTHREADS(int _nthreads) {
		NTHREADS = _nthreads;
	}
	
	private void analyze() throws IOException, InterruptedException, ExecutionException {
		positive = 0;
		total_opinions = txt_rdr.getSize();
		System.out.println("Now Analyzing...");
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		ArrayList< Future<String> > resList = new ArrayList< Future<String> >();
		for(int i = 0 ; i < total_opinions ; i++) {
			Callable<String> task = new SACallable(i);
			Future<String> result = executor.submit(task);
			resList.add(result);
		}
		executor.shutdown();
		for(Future<String> result : resList)	fw.write( result.get() );
	}
	
	public void work() throws IOException {
		long beginTime, trainTime, dictTime, analyzeTime;
		dict = new SentimentalDictionary();
		txt_rdr = new TextReader();
		f_rec = new FrequencyRecorder();
		trainer = new KeywordFinder();
		fw = new FileWriter("result.txt");
		
		// training
		beginTime = System.currentTimeMillis();
		trainer.setSORate(SORate);
		trainer.setNTHREADS(NTHREADS);
		trainer.readTrainingData(filenameT, filenameA);
		trainer.train();
		trainer.printToFile();
		trainTime = System.currentTimeMillis() - beginTime;
		
		// make dictionary
		beginTime = System.currentTimeMillis();
		dict.makeDict(filenameP, filenameN, filenameADV);
		dictTime = System.currentTimeMillis() - beginTime;
		
		beginTime = System.currentTimeMillis();
		// read text file
		txt_rdr.readText(filenameO);
		// analyzing
		try {
			analyze();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		analyzeTime = System.currentTimeMillis() - beginTime;
		
		System.out.println("Completed!");
		System.out.println("Time for Training: " + trainTime / 1000.0 + " second(s)");
		System.out.println("Time for Making Dictionary: " + dictTime / 1000.0 + " second(s)");
		System.out.println("Time for Analyzing: " + analyzeTime / 1000.0 + " second(s)");
		System.out.println( "Number of Words in Dictionary: " + dict.getSize() );
		System.out.println( "Positive/Negative: " + positive + "/" + (total_opinions - positive) );	
		System.out.println( "Frequent Words: " + f_rec.getFrequentWordsString(500) );
		fw.write("Top Ten Keywords from Positive Opinions: ");
		for( String s : f_rec.getTopTenPosWords() )	fw.write(s + "(" + f_rec.getPosFrequency(s) + ") ");
		fw.write("\n");
		fw.write("Top Ten Keywords from Negative Opinions: ");
		for( String s : f_rec.getTopTenNegWords() )	fw.write(s + "(" + f_rec.getNegFrequency(s) + ") ");
		fw.write("\n");
		fw.flush();
		fw.close();	
	}
	
}
