package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SentimentAnalyzer {
	
	private String filenameP = new String("./docs/positive.txt");
	private String filenameN = new String("./docs/negative.txt");
	private String filenameADV = new String("./docs/adv.txt");
	private String filenameT = new String("./docs/training.txt");
	private String filenameA = new String("./docs/answer.txt");
	private String filenameO = new String("./docs/opinion.txt");
	// create dictionary and reader
	private SentimentalDictionary dict = new SentimentalDictionary();
	private TextReader txt_rdr = new TextReader();
	// number of correct answers and the total number of opinions
	private int correct = 0;
	private int total_opinions = 0;
	// create SegChinese
	private SegChinese seg = new SegChinese();  
	// create frequency recorder
	private FrequencyRecorder f_rec = new FrequencyRecorder();
	// create trainer
	private KeywordFinder trainer = new KeywordFinder();
	// OutputWiter
	private FileWriter fw;
	
	public SentimentAnalyzer(String _filenameP, String _filenameN, String _filenameADV, String _filenameT, String _filenameA, String _filenameO) {
		filenameP = _filenameP;
		filenameN = _filenameN;
		filenameADV = _filenameADV;
		filenameT = _filenameT;
		filenameA = _filenameA;
		filenameO = _filenameO;
	}
	
	public void read_filename_from_stdin() {
		// read file name from stander input
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the File Name of Positive Sentimental Words:");
		filenameP = scanner.nextLine();
		System.out.println("Enter the File Name of Negative Sentimental Words:");
		filenameN = scanner.nextLine();
		System.out.println("Enter the File Name of Adverbs:");
		filenameADV = scanner.nextLine();
		System.out.println("Enter the File Name of Training Data:");
		filenameT = scanner.nextLine();
		System.out.println("Enter the File Name of the Answers:");
		filenameA = scanner.nextLine();
		System.out.println("Enter the File Name of the Opinions:");
		filenameO = scanner.nextLine();
	}

	public void analyze() throws IOException {
		total_opinions = txt_rdr.getSize();
		System.out.println("Now Analyzing...");
		for(int i = 0 ; i < total_opinions ; i++) {
			// get the "i th" opinion
			int total_rate = 0;
			ArrayList<String> opinion = txt_rdr.getTextbyIndex(i);
			ArrayList<String> keywords = new ArrayList<String>();
			ArrayList<String> keyadvs = new ArrayList<String>();
			for(int j = 0 ; j < opinion.size() ; j++) {
				// get the "j th" sentence in the "i th" opinion
				int rate = 0, flag = 1;
				String sentence = opinion.get(j);
				// disassemble the sentence and check adverbs
				for(int endIndex = sentence.length(); endIndex > 0 ; endIndex--) {
					for(int beginIndex = 0 ; beginIndex < endIndex ; beginIndex++) {
						String word = sentence.substring(beginIndex, endIndex);
						// key adverb found
						if( dict.checkAdv(word) ) {
							flag = 2;
							keyadvs.add(word);
							sentence = sentence.replaceAll(word, "");
							endIndex = sentence.length() + 1;
							break;
						}
					}
				}
				// disassemble the sentence and check sentimental words
				for(int endIndex = sentence.length(); endIndex > 0 ; endIndex--) {
					for(int beginIndex = 0 ; beginIndex < endIndex ; beginIndex++) {
						String word = sentence.substring(beginIndex, endIndex);
						// keyword found
						if(dict.checkWord(word) != 0) {
							keywords.add(word);
							rate += flag * dict.checkWord(word);
							sentence = sentence.replaceAll(word, "");
							endIndex = sentence.length() + 1;
							break;
						}
					}
				}
				// check if the shifter exists
				if( sentence.contains("不") )		rate *= -1;
				total_rate += rate;
			}
			if( (total_rate >= 0 && i < 750) || (total_rate < 0 && i >= 750) )	correct += 1;
			fw.write("NO." + (i + 1) + ": rate = " + total_rate + (total_rate >= 0 ? "\t (Positive)\n" : "\t (Negative)\n"));
			for(String sentence : opinion) {
				fw.write(seg.segWords(sentence, " ") + " ");	// print detail
				for( String segSentence : seg.getSegList(sentence) ){
					if(total_rate >= 0)	f_rec.addPosFrequency(segSentence);
					else	f_rec.addNegFrequency(segSentence);
				}
			}
			fw.write("\nKeyWords Found: "); // print detail
			for(String word : keywords)	fw.write(word + "(" + dict.checkWord(word) + ") ");	// print detail
			for(String word : keyadvs)	fw.write(word + "(adv) ");	// print detail
			fw.write("\n\n"); // print detail
		}
	}
	
	public void work() throws IOException
	{
		long beginTime, trainTime, dictTime, analyzeTime;
		fw = new FileWriter("result.txt");
		
		//read_filename_from_stdin();
		
		// training
		trainer.setSORate(4.5);
		beginTime = System.currentTimeMillis();
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
		analyze();
		analyzeTime = System.currentTimeMillis() - beginTime;
		
		fw.write("Time for Training: " + trainTime / 1000.0 + " second(s)\n");
		fw.write("Time for Making Dictionary: " + dictTime / 1000.0 + " second(s)\n");
		fw.write("Time for Analyzing: " + analyzeTime / 1000.0 + " second(s)\n");
		fw.write("Number of Words in Dictionary: " + dict.getSize() + "\n");
		fw.write("Accuracy: " + correct + "/" + total_opinions +  "(" + (float)correct / (float)total_opinions * 100 + "%)\n");	
		fw.write( "Frequent Words: " + f_rec.getFrequentWordsString(500) );		
		fw.flush();
		fw.close();
		System.out.println("Completed!\n");
	}
	
}
