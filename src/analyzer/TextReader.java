package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TextReader {

	// an ArrayList holding many opinions, and every opinion consists of some strings 
	private ArrayList< ArrayList<String> > opinion_list = new  ArrayList< ArrayList<String> >();
	
	// read texts from the input file and arrange them into the opinion_list
	public void readText(String _filename) throws IOException {
		try {
			System.out.println("Accessing " + _filename);
			FileReader fr = new FileReader(_filename);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				String raw_data = tmp.replaceAll("\\pP" , " ").replaceAll("[a-zA-Z0-9]", " ");
				String[] spilt_data = raw_data.split(" ");
				ArrayList<String> opinion = new ArrayList<String>();
				for( String s : spilt_data )	opinion.add(s);
				opinion_list.add(opinion);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Text File not Found");
			e.printStackTrace();
		}
	}
	
	// return the size of opinion_list as the number of opinions
	public int getSize() {
		return opinion_list.size();
	}
	
	// return an ArrayList containing the strings of the "_index th" opinion
	public ArrayList<String> getTextbyIndex(int _index) {
		if( _index > opinion_list.size() )	return new ArrayList<String>();
		return opinion_list.get(_index);
	}
	
}
