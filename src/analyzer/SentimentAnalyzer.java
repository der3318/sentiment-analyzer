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
	
	private static int NTHREADS = 4;
	// create dictionary and reader
	private static SentimentalDictionary dict;
	// create SegChinese
	private static SegChinese seg;  

	private TextReader txt_rdr;
	private String filenameO = new String("./docs/opinion.txt");
	private String filenameR = new String("./result.txt");
	// number of positive answers and the total number of opinions
	private int positive;
	private int total_opinions;
	// create frequency recorder
	private FrequencyRecorder f_rec;
	// OutputWriter
	private FileWriter fw;
	
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
				synchronized(SentimentAnalyzer.this) {
					if(total_rate >= 0)	positive += 1;
				}
				output = "NO." + (index + 1) + ": rate = " + total_rate + (total_rate >= 0 ? " (Positive)\n" : " (Negative)\n");
				for(String sentence : opinion) {
					String after_seg =  seg.segWords(sentence, " ");
					output += (after_seg + " "); // print detail
					for( String segSentence : after_seg.split(" ") ){
						if( segSentence.length() <= 1 )	continue;
						else if(total_rate >= 0)	f_rec.addPosFrequency(segSentence);
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
	
	// setup the static SegChinese, KeywordFinder and SentimentalDictionary before work()
	public SentimentAnalyzer() {
		seg = SegChinese.getInstance();
		KeywordFinder.getInstance();
		dict = SentimentalDictionary.getInstance();
	}

	// set specific I/O files
	public SentimentAnalyzer(String inputFile, String outputFile) {
		seg = SegChinese.getInstance();
		KeywordFinder.getInstance();
		dict = SentimentalDictionary.getInstance();
		filenameO = inputFile;
		filenameR = outputFile;
	}
	
	// set specific dictionary files and remove the old instance to make the new one available
	public static void setDictionary(String positiveDict, String negativeDict, String advDict) {
		SentimentalDictionary.removeInstance();
		SentimentalDictionary.setFilename(positiveDict, negativeDict, advDict);
	}
	
	// set specific training data and remove the old instance to make the new one available
	public static void setTrainingData(String trainingFile, String trainingAnswer) {
		SentimentalDictionary.removeInstance();
		KeywordFinder.removeInstance();
		KeywordFinder.setFileame(trainingFile, trainingAnswer);
	}
	
	public static void setSORate(double _rate) {
		SentimentalDictionary.removeInstance();
		KeywordFinder.removeInstance();
		KeywordFinder.setSORate(_rate);
	}
	
	public static void setNTHREADS(int _nthreads) {
		NTHREADS = _nthreads;
		KeywordFinder.setNTHREADS(_nthreads);
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
	
	public void work() {
		try {
			long beginTime = System.currentTimeMillis();
			txt_rdr = new TextReader();
			txt_rdr.readText(filenameO);
			f_rec = new FrequencyRecorder();
			fw = new FileWriter(filenameR);
			analyze();
			System.out.println("Completed!");
			System.out.println("Time for Analyzing: " + (System.currentTimeMillis() - beginTime) / 1000.0 + " second(s)");
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
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
