package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SentimentalDictionay {
	
	// a HashMap holding sentimental words as keys
	private HashMap<String, Boolean> mydict = new HashMap<String, Boolean>();
	
	// get the score of the sentimental word, and return 0 when not found
	public int checkWord(String _string) {
		if( !mydict.containsKey(_string) )	return 0;
		if( mydict.get(_string) )	return 1;
		return -1;
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
	
	// put the words into the the HashMaps from 3 input files(positive sentimental words, negative sentimental words, shifters)
	public void makeDict(String _filenameP, String _filenameN) throws IOException {
		try {
			System.out.println("Accessing " + _filenameP);
			// access positive words
			FileReader fr = new FileReader(_filenameP);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				mydict.put(tmp.trim() , true);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File of Positive Words Not Found");
			e.printStackTrace();
		}
		try {
			System.out.println("Accessing " + _filenameN);
			// access negative words
			FileReader fr = new FileReader(_filenameN);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				mydict.put(tmp.trim() , false);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File of Negative Words Not Found");
			e.printStackTrace();
		}
	}
	
}
