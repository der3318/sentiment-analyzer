package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SentimentalDictionary {
	
	// a static dictionary shared by all analyzers 
	private static SentimentalDictionary dictionary;
	// filenames
	private static String filenameP = new String("./docs/positive.txt");
	private static String filenameN = new String("./docs/negative.txt");
	private static String filenameADV = new String("./docs/adv.txt");
	
	// a HashMap holding sentimental words as keys
	private HashMap<String, Integer> mydict = new HashMap<String, Integer>();
	// a HashMap holding adverbs as keys
	private HashMap<String, Boolean> myadv = new HashMap<String, Boolean>();
	
	// return the prepared dictionary. if not found, create one
	public static SentimentalDictionary getInstance() {
        if (dictionary == null) {
            synchronized (SentimentalDictionary.class) {
                if (dictionary == null) {
                	dictionary = new SentimentalDictionary();
                	dictionary.makeDict();
                	return dictionary;
                }
            }
        }
        return dictionary;
    }
	
	// remove the current dictionary due to some setting changes
	public static void removeInstance() {
		dictionary = null;
	}
	
	public static void setFilename(String _filenameP, String _filenameN, String _filenameADV) {
		filenameP = _filenameP;
		filenameN = _filenameN;
		filenameADV = _filenameADV;
	}
	
	// add a positive word into dictionary
	public synchronized void addPositiveWords(String _string) {
		if( mydict.containsKey(_string) )	mydict.put(_string, mydict.get(_string) + 1);
		else	mydict.put(_string, 1);
	}
	
	// add a negative word into dictionary
	public synchronized void addNegativeWords(String _string) {
		if( mydict.containsKey(_string) )	mydict.put(_string, mydict.get(_string) - 1);
		else	mydict.put(_string, -1);
	}
	
	// get the score of the sentimental word, and return 0 when not found
	public int checkWord(String _string) {
		if( _string.isEmpty() || !mydict.containsKey(_string) )	return 0;
		if( mydict.get(_string) > 0 )	return 1;
		return -1;
	}
	
	// check if the input word is adv or not
	public boolean checkAdv(String _string) {
		if( myadv.containsKey(_string) && !_string.isEmpty() )	return true;
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
	public void makeDict() {		
		try {
			// access positive words
			String[] filenames = {filenameP, "./docs/pos_by_training.txt"};
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
		catch (Exception e) {
			System.out.println("File of Positive Words Not Found");
			e.printStackTrace();
		}
		try {
			// access negative words
			String[] filenames = {filenameN, "./docs/neg_by_training.txt"};
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
		catch (Exception e) {
			System.out.println("File of Negative Words Not Found");
			e.printStackTrace();
		}
		try {
			System.out.println("Accessing " + filenameADV);
			// access negative words
			FileReader fr = new FileReader(filenameADV);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				myadv.put(tmp.trim() , true);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (Exception e) {
			System.out.println("File of Adverbs Not Found");
			e.printStackTrace();
		}
	}
	
}
