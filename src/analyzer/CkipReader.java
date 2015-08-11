package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CkipReader {
	
	// an ArrayList holding many opinions, and each opinion has an ArrayList to store its NPs
	ArrayList< ArrayList<String> > NPlist = new ArrayList< ArrayList<String> >();
	// an ArrayList holding many opinions, and each opinion has an ArrayList to store its NPs
	ArrayList< ArrayList<String> > VPlist = new ArrayList< ArrayList<String> >();
	// an ArrayList holding many opinions, and each opinion has an ArrayList to store its (DFA+VH)s
	ArrayList< ArrayList<String> > DfaVHlist = new ArrayList< ArrayList<String> >();
	Pattern NP_pattern = Pattern.compile("NP\\([^\\(\\)]*?\\)");
	Pattern VP_pattern = Pattern.compile("VP\\([^\\(\\)]*?\\)");
	Pattern DfaVH_pattern = Pattern.compile("(Dfa.*?)?VH:[^\\:]+?[\\|\\)]");
	Pattern keyword_pattern = Pattern.compile("\\:[^\\:]+?[\\|\\)]");

	// read from the input file and arrange them into the NPlist
	void readCkip(String _filename) throws IOException {
		try {
			System.out.println("Accessing " + _filename);
			FileReader fr = new FileReader(_filename);
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			while(tmp != null) {
				// create a new ArrayList<String> when new opinion is found
				if( tmp.startsWith("#1:1.[0] NP(Head:Neu:") ) {
					NPlist.add( new ArrayList<String>() );					
					VPlist.add( new ArrayList<String>() );
					DfaVHlist.add( new ArrayList<String>() );
				}
				else {
					// replace the wrong NP POS tag
					if( tmp.startsWith("#1:1") )	tmp = tmp.replaceFirst("NP", "S");
					// find NP and VP
					Pattern[] patterns = {NP_pattern, VP_pattern, DfaVH_pattern};
					for(Pattern pattern : patterns) {
						Matcher matcher = pattern.matcher(tmp);
						while( matcher.find() ) {
							// if NP or VP is found, find the keyword of the NP or VP
							Matcher keyword_matcher = keyword_pattern.matcher( matcher.group() );
							String keyword = new String();
							while( keyword_matcher.find() ) {
								String new_keyword = keyword_matcher.group().replaceAll("\\.", "");
								keyword += new_keyword.substring(1, new_keyword.length() - 1);
							}
							if(pattern == NP_pattern)	NPlist.get(NPlist.size() - 1).add(keyword);
							else if(pattern == VP_pattern)	VPlist.get(VPlist.size() - 1).add(keyword);
							else	DfaVHlist.get(DfaVHlist.size() - 1).add(keyword);
						}
					}
				}
				tmp = br.readLine();
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Ckip File not Found");
			e.printStackTrace();
		}
	}
	
	// return the size of NPlist(VPlist, DfaVHlist) as the number of opinions
	public int getSize() {
		return NPlist.size();
	}
	
	// return an ArrayList containing the NPs of the "_index th" opinion
	public ArrayList<String> getNPbyIndex(int _index) {
		if( _index > NPlist.size() )	return new ArrayList<String>();
		return NPlist.get(_index);
	}
	
	// return an ArrayList containing the VPs of the "_index th" opinion
	public ArrayList<String> getVPbyIndex(int _index) {
		if( _index > VPlist.size() )	return new ArrayList<String>();
		return VPlist.get(_index);
	}
		
	// return an ArrayList containing the (DFA+VH)s of the "_index th" opinion
	public ArrayList<String> getDfaVHbyIndex(int _index) {
		if( _index > DfaVHlist.size() )	return new ArrayList<String>();
		return DfaVHlist.get(_index);
	}
	
}
