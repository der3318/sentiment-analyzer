package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SentimentAnalyzer {
	
	private static String filenameP = new String("positive.txt");
	private static String filenameN = new String("negative.txt");
	private static String filenameADV = new String("adv.txt");
	private static String filenameT = new String("training.txt");
	private static String filenameA = new String("answer.txt");
	private static String filenameO = new String("opinion.txt");
	// create dictionary and reader
	private static SentimentalDictionary dict = new SentimentalDictionary();
	private static TextReader txt_rdr = new TextReader();
	// number of correct answers and the total number of opinions
	private static int correct = 0;
	private static int total_opinions = 0;
	// create SegChinese
	private static SegChinese seg = new SegChinese();  
	// create frequency recorder
	private static FrequencyRecorder f_rec = new FrequencyRecorder();
	// create trainer
	private static KeywordFinder trainer = new KeywordFinder();
	
	public static void read_filename_from_stdin() {
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

	public static void analyze() throws IOException {
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
			System.out.println("NO." + (i + 1) + ": rate = " + total_rate + (total_rate >= 0 ? "\t (Positive)" : "\t (Negative)"));
			for(String sentence : opinion) {
				System.out.print(seg.segWords(sentence, " ") + " ");	// print detail
				for( String segSentence : seg.getSegList(sentence) ){
					if(total_rate >= 0)	f_rec.addPosFrequency(segSentence);
					else	f_rec.addNegFrequency(segSentence);
				}
			}
			System.out.print("\nKeyWords Found: "); // print detail
			for(String word : keywords)	System.out.print(word + "(" + dict.checkWord(word) + ") ");	// print detail
			for(String word : keyadvs)	System.out.print(word + "(adv) ");	// print detail
			System.out.println("\n"); // print detail
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		//read_filename_from_stdin();
		// training
		trainer.setSORate(4.5);
		trainer.readTrainingData(filenameT, filenameA);
		trainer.train();
		trainer.printToFile();
		// make dictionary
		dict.makeDict(filenameP, filenameN, filenameADV);
		// read text file
		txt_rdr.readText(filenameO);
		// analyzing
		analyze();
		System.out.println("Accuracy: " + correct + "/" + total_opinions +  "(" + (float)correct / (float)total_opinions * 100 + "%)");	
		System.out.print("Frequent Words: ");
		f_rec.printFrequentWords(500);
	}
	
}
