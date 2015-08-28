package analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SentimentAnalyzer {
	
	private static int NTHREADS = 4;
	// dictionary and Segmenter
	private static SentimentalDictionary dict;
	private static SegChinese seg;  

	// reader and IO filenames
	private TextReader txt_rdr;
	private String filenameO = new String("./docs/opinion.txt");
	private String filenameR = new String("./result.txt");
	// number of positive answers and the total number of opinions
	private int positive;
	private int total_opinions;
	// frequency recorder
	private FrequencyRecorder f_rec;
	// OutputWriter
	private FileWriter fw;
	
	// each Callable holds one opinion, returning the output string generated  
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
				for(String sentence : opinion) {
					// get one sentence in the "index th" opinion
					// flag = 2 if any adv is found 
					int rate = 0, flag = 1;
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
								rate += flag * dict.checkWord(word);
								keywords.add(word);
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
				output = String.format(Locale.getDefault(), "NO.%d: rate = ", total_rate) + (total_rate >= 0 ? " (Positive)\n" : " (Negative)\n");
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
				for(String word : keywords)	output += String.format( Locale.getDefault(), "%s(%d) ", word, dict.checkWord(word) );
				for(String word : keyadvs)	output += String.format(Locale.getDefault(), "%s(adv) ", word);
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
	
	public static void setPosNegSORate(double pos_rate, double neg_rate) {
		SentimentalDictionary.removeInstance();
		KeywordFinder.removeInstance();
		KeywordFinder.setSORate(pos_rate, neg_rate);
	}
	
	public static void setSORate(double _rate) {
		SentimentalDictionary.removeInstance();
		KeywordFinder.removeInstance();
		KeywordFinder.setSORate(_rate, _rate);
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
			// reading
			txt_rdr = new TextReader();
			txt_rdr.readText(filenameO);
			// create recorder
			f_rec = new FrequencyRecorder();
			fw = new FileWriter(filenameR);
			// start analyzing
			analyze();
			// output message
			System.out.println("Completed!");
			System.out.println( "Time for Analyzing: " + ( (System.currentTimeMillis() - beginTime) / 1000.0 ) + " second(s)");
			System.out.println( "Number of Words in Dictionary: " + dict.getSize() );
			System.out.println( String.format(Locale.getDefault(), "Positive/Negative: %d/%d", positive , total_opinions - positive) );	
			System.out.println( "Frequent Words(>=500): " + f_rec.getFrequentWordsString(500) );
			fw.write("Top Ten Keywords from Positive Opinions: ");
			for( String s : f_rec.getTopTenPosWords() )	fw.write( String.format( Locale.getDefault(), "%s(%d) ", s, f_rec.getPosFrequency(s) ) );
			fw.write("\nTop Ten Keywords from Negative Opinions: ");
			for( String s : f_rec.getTopTenNegWords() )	fw.write( String.format( Locale.getDefault(), "%s(%d) ", s, f_rec.getNegFrequency(s) ) );
			fw.write("\n");
			fw.flush();
			fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
