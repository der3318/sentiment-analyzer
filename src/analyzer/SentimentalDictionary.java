package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SentimentalDictionary {
	
	// a HashMap holding sentimental words as keys
	private HashMap<String, Boolean> mydict = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> myadv = new HashMap<String, Boolean>();
	
	// add a positive word into dictionary
	public void addPositiveWords(String _string) {
		mydict.put(_string, true);
	}
	
	// add a positive word into dictionary
	public void addNegativeWords(String _string) {
		mydict.put(_string, false);
	}
	
	// get the score of the sentimental word, and return 0 when not found
	public int checkWord(String _string) {
		if( !mydict.containsKey(_string) )	return 0;
		if( mydict.get(_string) )	return 1;
		return -1;
	}
	
	// check if the input word is adv or not
	public boolean checkAdv(String _string) {
		if( myadv.containsKey(_string) )	return true;
		return false;
	}
	
	// return an ArrayList containing positive words
	public ArrayList<String> getPositiveWords() {
		ArrayList<String> output_list = new ArrayList<String>();
		for( String key : mydict.keySet() )	if( mydict.get(key) )	output_list.add(key);
		return output_list;
	}
	
	// return an ArrayList containing negative words
	public ArrayList<String> getNegativeWords() {
		ArrayList<String> output_list = new ArrayList<String>();
		for( String key : mydict.keySet() )	if( !mydict.get(key) )	output_list.add(key);
		return output_list;
	}
	
	// print dictionary
	public void printDict() {
		for( String key : mydict.keySet() )	System.out.println(key + ", " + mydict.get(key));
	}
	
	// put the words into the the HashMaps from 3 input files(positive sentimental words, negative sentimental words, adverbs)
	public void makeDict(String _filenameP, String _filenameN, String _filenameADV) throws IOException {		
		try {
			// access positive words
			String[] filenames = {_filenameP, "pos_by_training.txt"};
			for(String filename : filenames) {
				System.out.println("Accessing " + filename);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				String tmp = br.readLine();
				while(tmp != null) {
					mydict.put(tmp.trim() , true);
					tmp = br.readLine();
				}
				br.close();
			}	
		}
		catch (FileNotFoundException e) {
			System.out.println("File of Positive Words Not Found");
			e.printStackTrace();
		}
		try {
			// access negative words
			String[] filenames = {_filenameN, "neg_by_training.txt"};
			for(String filename : filenames) {
				System.out.println("Accessing " + filename);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				String tmp = br.readLine();
				while(tmp != null) {
					mydict.put(tmp.trim() , false);
					tmp = br.readLine();
				}
				br.close();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("File of Negative Words Not Found");
			e.printStackTrace();
		}
		try {
			System.out.println("Accessing " + _filenameADV);
			// access negative words
			FileReader fr = new FileReader(_filenameADV);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				myadv.put(tmp.trim() , true);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File of Adverbs Not Found");
			e.printStackTrace();
		}
	}
	
}
