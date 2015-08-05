package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SentimentAnalyzer {
	
	public static void main(String[] args) throws IOException
	{
		// create dictionary
		SentimentalDictionay dict = new SentimentalDictionay();
		// create text reader
		TextReader txt_rdr = new TextReader();
		
		// read file name from stander input
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the File Name of Positive Sentimental Words:");
		String filenameP = scanner.nextLine();
		System.out.println("Enter the File Name of Negative Sentimental Words:");
		String filenameN = scanner.nextLine();
		
		// make dictionary
		// dict.makeDict(filenameP, filenameN);
		dict.makeDict("pos.txt", "neg.txt");
		
		// read text file
		System.out.println("Enter the File Name of the Opinions:");
		String filenameO = scanner.nextLine();
		// txt_rdr.readText(filenameO);
		txt_rdr.readText("training.txt");
		
		// analyzing
		int correct = 0;
		System.out.println("Now Analyzing...");
		for(int i = 0 ; i < txt_rdr.getSize() ; i++) {
			// get the "i th" opinion
			int total_rate = 0;
			ArrayList<String> opinion = txt_rdr.getTextbyIndex(i);
			ArrayList<String> keywords = new ArrayList<String>();
			for(int j = 0 ; j < opinion.size() ; j++) {
				// get the "j th" sentence in the "i th" opinion
				String sentence = opinion.get(j);
				int rate = 0;
				// disassemble the sentence
				for(int endIndex = sentence.length(); endIndex > 0 ; endIndex--) {
					for(int beginIndex = 0 ; beginIndex < endIndex ; beginIndex++) {
						String word = sentence.substring(beginIndex, endIndex);
						// keyword found
						if(dict.checkWord(word) != 0) {
							keywords.add(word);
							rate += dict.checkWord(word);
							sentence = sentence.replaceAll(word, "");
							endIndex = sentence.length() + 1;
							break;
						}
					}
				}
				if( sentence.contains("不") || sentence.contains("沒") )		rate *= -1;
				total_rate += rate;
			}
			if( (total_rate >= 0 && i < 1500) || (total_rate <= 0 && i >= 1500) )	correct += 1;
			System.out.println("NO." + (i + 1) + ": rate = " + total_rate);
			for(String s : keywords)	System.out.print(s + "(" + dict.checkWord(s) + ") ");
			System.out.println("");
			System.out.println("");
		}
		System.out.println(correct + "/3000, (" + (float)correct / (float)30 + "%)");
	}
	
}
