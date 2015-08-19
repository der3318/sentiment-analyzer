package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SentimentalDictionary {
	
	// a HashMap holding sentimental words as keys
	private HashMap<String, Integer> mydict = new HashMap<String, Integer>();
	private HashMap<String, Boolean> myadv = new HashMap<String, Boolean>();
	
	// add a positive word into dictionary
	public synchronized void addPositiveWords(String _string) {
		if( mydict.containsKey(_string) )	mydict.put(_string, mydict.get(_string) + 1);
		else	mydict.put(_string, 1);
	}
	
	// add a positive word into dictionary
	public synchronized void addNegativeWords(String _string) {
		if( mydict.containsKey(_string) )	mydict.put(_string, mydict.get(_string) - 1);
		else	mydict.put(_string, -1);
	}
	
	// get the score of the sentimental word, and return 0 when not found
	public int checkWord(String _string) {
		if( !mydict.containsKey(_string) )	return 0;
		if( mydict.get(_string) > 0 )	return 1;
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
		for( String key : mydict.keySet() )	if( mydict.get(key) > 0 )	output_list.add(key);
		return output_list;
	}
	
	// return an ArrayList containing negative words
	public ArrayList<String> getNegativeWords() {
		ArrayList<String> output_list = new ArrayList<String>();
		for( String key : mydict.keySet() )	if( mydict.get(key) < 0 )	output_list.add(key);
		return output_list;
	}
	
	// get the size(numbers of words) of the dictionary
	public int getSize() {
		return mydict.size() + myadv.size();
	}
	
	// print dictionary
	public void printDict() {
		for( String key : mydict.keySet() )	System.out.println(key + ", " + mydict.get(key));
	}
	
	// put the words into the the HashMaps from 3 input files(positive sentimental words, negative sentimental words, adverbs)
	public void makeDict(String _filenameP, String _filenameN, String _filenameADV) throws IOException {		
		try {
			// access positive words
			String[] filenames = {_filenameP, "./docs/pos_by_training.txt"};
			for(String filename : filenames) {
				System.out.println("Accessing " + filename);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				String tmp = br.readLine();
				while(tmp != null) {
					addPositiveWords( tmp.trim() );
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
			String[] filenames = {_filenameN, "./docs/neg_by_training.txt"};
			for(String filename : filenames) {
				System.out.println("Accessing " + filename);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				String tmp = br.readLine();
				while(tmp != null) {
					addNegativeWords( tmp.trim() );
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
