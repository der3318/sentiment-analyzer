package analyzer;

import java.io.IOException;
import java.util.ArrayList;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class SegChinese {

	private static SegChinese seg;
	
	protected JiebaSegmenter segmenter;

	public SegChinese() {
		segmenter = new JiebaSegmenter();
	}

	public static SegChinese getInstance() {
        if (seg == null) {
            synchronized (SegChinese.class) {
                if (seg == null) {
                	seg = new SegChinese();
                	return seg;
                }
            }
        }
        return seg;
    }
	
	public ArrayList<String> getSegList(String txt) throws IOException {
		ArrayList<String> output = new ArrayList<String>();
		for( SegToken token : segmenter.process(txt, SegMode.INDEX) )	if( !token.word.isEmpty() )	output.add(token.word);
		return output;
	}

	public String segWords(String txt, String wordSpilt) throws IOException {
		String output = new String("");
		for(  SegToken token : segmenter.process(txt, SegMode.INDEX) )	output += (token.word + wordSpilt);
		return output;
	}

}
