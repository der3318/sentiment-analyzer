package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FrequencyRecorder {
	
	private HashMap<String, Integer> frequency_pos = new HashMap<String, Integer>();
	private HashMap<String, Integer> frequency_neg = new HashMap<String, Integer>();
	
	// add the positive frequency of the specific string
	public void addPosFrequency(String _string) {
		if( !frequency_pos.containsKey(_string) )	frequency_pos.put(_string, 1);
		else frequency_pos.put(_string, frequency_pos.get(_string) + 1);
	}
	
	// add the negative frequency of the specific string
	public void addNegFrequency(String _string) {
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
	
	// print the frequencies of positive and negative
	public void printFrequency() {
		for( String key : frequency_pos.keySet() )	System.out.println(key + "(+" + frequency_pos.get(key) + ")");
		for( String key : frequency_neg.keySet() )	System.out.println(key + "(-" + frequency_neg.get(key) + ")");
	}
	
}
