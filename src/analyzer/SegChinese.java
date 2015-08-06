package analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import com.chenlb.mmseg4j.ComplexSeg;  
import com.chenlb.mmseg4j.Dictionary;  
import com.chenlb.mmseg4j.MMSeg;  
import com.chenlb.mmseg4j.Seg;  
import com.chenlb.mmseg4j.Word;  

public class SegChinese {
	
	protected Dictionary dic;  
    
	public SegChinese() {  
	    dic = Dictionary.getInstance(); 
	}

	protected Seg getSeg() {  
	    return new ComplexSeg(dic);  
	} 
	
	public ArrayList<String> getSegList(String txt) throws IOException {
		ArrayList<String> output = new ArrayList<String>();
		Reader input = new StringReader(txt);  
		Seg seg = getSeg();
		Word word = null;
		MMSeg mmSeg = new MMSeg(input, seg); 
		while( ( word = mmSeg.next() ) != null ) output.add( word.getString() );
		return output;
	}
	
    public String segWords(String txt, String wordSpilt) throws IOException {  
        Reader input = new StringReader(txt);  
        StringBuilder sb = new StringBuilder();  
        Seg seg = getSeg();  
        MMSeg mmSeg = new MMSeg(input, seg);  
        Word word = null;  
        boolean first = true;  
        while((word=mmSeg.next())!=null) {  
            if(!first) {  
                sb.append(wordSpilt);  
            }  
            String w = word.getString();  
            sb.append(w);  
            first = false;        
        }  
        return sb.toString();  
    }  
    
}
