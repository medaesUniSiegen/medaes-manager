package medienaesthetik.elasticsearch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.carrotsearch.hppc.ObjectLookupContainer;

import medienaesthetik.utilities.ConfigHandler;
import medienaesthetik.utilities.UtilityFunctions;

public class EsUtilities {
	
	private static String lastUsedIndexName = null;
	private static String currentIndexName = null;
	private static String untouchedIndexName = "untouched_pdf_index";
	private static String aliasName = "pdf_index";
	private static final Logger logger = LogManager.getLogger("EsUtilities");

	/**
	 * Builds & returns a JSON Object that represents the Index Settings
	 * 
	 * @return
	 * @throws IOException
	 */
    public static XContentBuilder getSettings() throws IOException{
		
    	XContentBuilder IndexSettings = XContentFactory.jsonBuilder()
				.startObject()
					.startObject("analysis")
						.startObject("filter")
							.startObject("my_stop_german")
								.field("type", "stop")
								.field("name", "german")
								.field("stopwords_path", "scripts/stopwords.txt")
							.endObject()
							.startObject("my_stop_en")
								.field("type", "stop")
								.field("stopwords", "_english_")
							.endObject()
						.endObject()
						.startObject("analyzer")
							.startObject("custom_medaes_analyzer")
								.field("type", "custom")
								.field("tokenizer", "standard")
								.startArray("filter")
									.value("lowercase")
									.value("my_stop_german")
									.value("my_stop_en")
								.endArray()
							.endObject()
						.endObject()
					.endObject()
				.endObject();
    	
		return IndexSettings;
    }
    
    /**
     * Builds & returns a JSON Object for the untouched_pdf_index
     * 
     * @return
     * @throws IOException
     */
    public static XContentBuilder getUntouchedMapping() throws IOException{
    	
    	XContentBuilder IndexMapping = XContentFactory.jsonBuilder()
    		.startObject()
                .startObject("text")
    				.startObject("properties")
						.startObject("content")
							.field("type", "string")
							.field("term_vector", "with_positions_offsets_payloads")
							.field("store", "true")
						.endObject()
						.startObject("autocomplete")
							.field("type", "completion")
							.field("analyzer", "simple")
							.field("search_analyzer", "simple")
							.field("payloads", "true")
						.endObject()
					.endObject()
    			.endObject()
    		.endObject();
    	
    	return IndexMapping;
    }
    
	/**
	 * Builds & returns a JSON Object for the pdf_index_vX
	 * 
	 * @return
	 * @throws IOException
	 */
    public static XContentBuilder getTouchedMapping() throws IOException{
    	XContentBuilder IndexMapping = XContentFactory.jsonBuilder()
    		.startObject()
                .startObject("text")
    				.startObject("properties")
						.startObject("content")
							.field("type", "string")
							.field("analyzer", "custom_medaes_analyzer")
							.field("term_vector", "with_positions_offsets_payloads")
							.field("store", "true")
						.endObject()
						.startObject("autocomplete")
							.field("type", "completion")
							.field("analyzer", "simple")
							.field("search_analyzer", "simple")
							.field("payloads", "true")
						.endObject()
					.endObject()
    			.endObject()
    		.endObject();
    	
    	return IndexMapping;
    }
    
    public static String getUntouchedIndexName(){
    	return untouchedIndexName;
    }
    
    /**
     * Updates the Version of the current Indexname (pdf_index_vX)
     * 
     * returns new or old index name
     * @throws NoIndexException 
     */
    public static String updateIndexName(){
    	String newIndexName = "";
    	
    	Object[] indexArray = getAllIndices();
		if(indexArray.length > 0){
			for(Object indexName: indexArray){
				if(indexName.toString().contains("pdf_index_v")){
					lastUsedIndexName = indexName.toString();
					String[] IndexWithVersion = indexName.toString().split("_v");
					int Version = Integer.parseInt(IndexWithVersion[1]);
					int NewVersion = ++Version;
					newIndexName = IndexWithVersion[0]+"_v"+NewVersion;
					currentIndexName = newIndexName;
					
					return currentIndexName;
				}
			}	
		}
		else {
			logger.warn("Achtung! Der Index ist noch nicht angelegt und kann daher nicht aktualisiert werden");
		}
		
		return "pdf_index_v0";
    }
    
   /**
    * Returns the current touched Index Name (pdf_index_vX)
    * Initializes Index if necessary
    * @return
    */
    public static String getCurrentIndexName(){
    	
		Object[] indexArray = getAllIndices();
		
		for(Object indexName : indexArray){
			if(indexName.toString().contains("pdf_index_v")){
				return indexName.toString();
			}
		}
		
		return "";
    }
    
    public static String getLastUsedIndexName(){
    	if(lastUsedIndexName == null){
    		lastUsedIndexName = "pdf_index_v0";
    	}
    	return lastUsedIndexName;
    }
    
    public static String getAliasName(){
    	return aliasName;
    }
    
    public static Object[] getAllIndices(){
    	return EsTransporter.getInstance().admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().keys().toArray();
    }
    
	/**
	 * Checks if a document with this id exists in the untouched index
	 * 
	 * @param id
	 * @return
	 */
	public static boolean documentExistsInIndex(String id){
		URL url;
		int statusCode = 0;
		try {
			url = new URL("http://localhost:9200/" + ConfigHandler.getInstance().getValue("index.untouched"));
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			statusCode = http.getResponseCode();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(statusCode == 200){
			SearchResponse response = EsTransporter.getInstance().prepareSearch(ConfigHandler.getInstance().getValue("index.untouched"))
					.setTypes(ConfigHandler.getInstance().getValue("index.type"))
					.setQuery(QueryBuilders.termQuery("documentId", id))
					.setFrom(0).setSize(60).setExplain(true)
					.execute()
					.actionGet();
			if(response.getHits().getTotalHits() >= 1 || id.isEmpty()){
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * deletes a document from untouched & touched index
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean deleteDocument(String filename){
		
		String _id = UtilityFunctions.parseIdFromFilename(filename);
		String _index_touched = getCurrentIndexName();
		
		if(!_id.isEmpty()){
			SearchResponse untouchedResponse = EsTransporter.getInstance().prepareSearch(ConfigHandler.getInstance().getValue("index.untouched"))
					.setTypes(ConfigHandler.getInstance().getValue("index.type"))
					.setQuery(QueryBuilders.termQuery("documentId", _id))
					.setFrom(0).setSize(60).setExplain(true)
					.execute()
					.actionGet();
			
			for (SearchHit hit: untouchedResponse.getHits()) {
				DeleteResponse response = EsTransporter.getInstance().prepareDelete(ConfigHandler.getInstance().getValue("index.untouched"), ConfigHandler.getInstance().getValue("index.type"), hit.getId())
						        .execute()
						        .actionGet();
			}
			
			SearchResponse touchedResponse = EsTransporter.getInstance().prepareSearch(_index_touched)
					.setTypes(ConfigHandler.getInstance().getValue("index.type"))
					.setQuery(QueryBuilders.termQuery("documentId", _id))
					.setFrom(0).setSize(60).setExplain(true)
					.execute()
					.actionGet();
			
			for (SearchHit hit: touchedResponse.getHits()) {
				DeleteResponse response = EsTransporter.getInstance().prepareDelete(_index_touched, ConfigHandler.getInstance().getValue("index.type"), hit.getId())
						        .execute()
						        .actionGet();
			}
			
			logger.info("Es wurde ein Dokument gelöscht: "+filename);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Initializes the needed indices
	 * 
	 * @throws IOException
	 */
	public static void prepareIndex() throws IOException{
		
		Object[] indexArray = getAllIndices();
		
		for(Object indexName : indexArray){
			if(indexName.toString().contains("pdf_index")){
				return;
			}
		}
		CreateIndexRequest createRequestUntouched = new CreateIndexRequest(ConfigHandler.getInstance().getValue("index.untouched"));
		createRequestUntouched.mapping("text", getUntouchedMapping());
		EsTransporter.getInstance().admin().indices().create(createRequestUntouched).actionGet();
		
		CreateIndexRequest createRequestTouched = new CreateIndexRequest("pdf_index_v0");
		createRequestTouched.settings(getSettings());
		createRequestTouched.mapping("text", getTouchedMapping());
		EsTransporter.getInstance().admin().indices().create(createRequestTouched).actionGet();
		EsTransporter.getInstance().admin().indices().prepareAliases().addAlias("pdf_index_v0", aliasName).get();
	}
}