package medienaesthetik.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.json.JSONObject;

public class FrequentWord {
	
	private ArrayList<String> frequentWordsAL = new ArrayList<String>();
	private ArrayList<Integer> termFrequencyAL = new ArrayList<Integer>();
	private XContentBuilder termvectorBuilder;
	private String[] frequentWords;
	private int weight;
	
	FrequentWord(TermVectorsResponse termVectResp){
		try {
			termvectorBuilder = XContentFactory.jsonBuilder().startObject();
			termVectResp.toXContent(termvectorBuilder, ToXContent.EMPTY_PARAMS);
			termvectorBuilder.endObject();
			JSONObject jsonObject = new JSONObject(termvectorBuilder.string());
			
			if(jsonObject.has("term_vectors")){
				if(jsonObject.getJSONObject("term_vectors").has("content")){
					if(jsonObject.getJSONObject("term_vectors").getJSONObject("content").has("terms")){
						JSONObject terms = jsonObject.getJSONObject("term_vectors")
													 .getJSONObject("content")
													 .getJSONObject("terms");
						
						Iterator iterator = terms.keys();
						String _key = null;
						int _weight = 0;
						while (iterator.hasNext()){
							_key = (String) iterator.next();
							int _termFrequencyNumber = jsonObject.getJSONObject("term_vectors")
																.getJSONObject("content")
																.getJSONObject("terms")
																.getJSONObject(_key)
																.getInt("term_freq");
							frequentWordsAL.add(_key);
							termFrequencyAL.add(_termFrequencyNumber);
							_weight = _weight + _termFrequencyNumber;
						}
						
						/*String frequentALString = "";
						for(String word : frequentWordsAL){
							frequentALString += word + " ";
						}*/
						this.setFrequentWords(frequentWordsAL);
						this.setWeight(_weight);
						
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String[] getFrequentWords() {
		return frequentWords;
	}

	public void setFrequentWords(ArrayList<String> frequentWords) {
		this.frequentWords = Arrays.copyOf(frequentWords.toArray(), frequentWords.toArray().length, String[].class);
	}
}
