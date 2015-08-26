package analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrequencyRecorder {
	
	private HashMap<String, Integer> frequency_pos = new HashMap<String, Integer>();
	private HashMap<String, Integer> frequency_neg = new HashMap<String, Integer>();
	
	// add the positive frequency of the specific string
	public synchronized void addPosFrequency(String _string) {
		if( !frequency_pos.containsKey(_string) )	frequency_pos.put(_string, 1);
		else frequency_pos.put(_string, frequency_pos.get(_string) + 1);
	}
	
	// add the negative frequency of the specific string
	public synchronized void addNegFrequency(String _string) {
		if( !frequency_neg.containsKey(_string) )	frequency_neg.put(_string, 1);
		else frequency_neg.put(_string, frequency_neg.get(_string) + 1);
	}
	
	// get the positive frequency of the specific string
	public int getPosFrequency(String _string) {
		if( frequency_pos.containsKey(_string) )	return frequency_pos.get(_string);
		return 0;
	}
	
	// get the negative frequency of the specific string
	public int getNegFrequency(String _string) {
		if( frequency_neg.containsKey(_string) )	return frequency_neg.get(_string);
		return 0;
	}
	
	// get a Set containing all the recorded strings
	public Set<String> getRecordedStrings() {
		Set<String> output = new HashSet<String>( frequency_pos.keySet() );
		output.addAll( frequency_neg.keySet() );
		return output;
	}
	
	public String getFrequentWordsString(int _base) {
		String output = new String();
		for( String key : getRecordedStrings() ) {
			int p = getPosFrequency(key);
			int n = getNegFrequency(key);
			if(p + n >= _base)	output += (key + "(" + (p + n) + ") ");
		}
		return output;
	}
	
	// return an ArrayList containing top ten positive words
	public ArrayList<String> getTopTenPosWords() {
		ArrayList<String> output = new ArrayList<String>();
		ArrayList<Map.Entry<String, Integer>> list_entry = new ArrayList<Map.Entry<String, Integer>>( frequency_pos.entrySet() );
		Collections.sort( 
				list_entry, 
				new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return o2.getValue() - o1.getValue();
					}
				}
			);
		for( Map.Entry<String, Integer> entry : list_entry ) {
			if(output.size() == 10)	break;
			output.add( entry.getKey() );
		}
		return output;
	}
	
	// return an ArrayList containing top ten negative words
	public ArrayList<String> getTopTenNegWords() {
		ArrayList<String> output = new ArrayList<String>();
		ArrayList<Map.Entry<String, Integer>> list_entry = new ArrayList<Map.Entry<String, Integer>>( frequency_neg.entrySet() );
		Collections.sort( 
				list_entry, 
				new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return o2.getValue() - o1.getValue();
					}
				}
			);
		for( Map.Entry<String, Integer> entry : list_entry ) {
			if(output.size() == 10)	break;
			output.add( entry.getKey() );
		}
		return output;
	}
	
	// print the frequencies of positive and negative
	public void printFrequency() {
		for( String key : frequency_pos.keySet() )	System.out.println(key + "(+" + frequency_pos.get(key) + ")");
		for( String key : frequency_neg.keySet() )	System.out.println(key + "(-" + frequency_neg.get(key) + ")");
	}
	
}
