package medienaesthetik.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest.FilterSettings;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;

import medienaesthetik.coverPage.PDFMerger;
import medienaesthetik.listener.FolderChangeListener;
import medienaesthetik.utilities.ConfigHandler;
import medienaesthetik.utilities.UtilityFunctions;

public class Indexer implements FolderChangeListener{
	
	private static final Logger logger = LogManager.getLogger("Indexer");
	private File[] listOfTextFiles;
	
	public Indexer(){
		File folder = new File(ConfigHandler.getInstance().getValue("document.path"));
		listOfTextFiles = folder.listFiles();
		
		for(File file : listOfTextFiles){
			index(file);
		}
		logger.info("Index Modul gestartet und bereit");
	}
	
	/**
	 * Index a File into untouched & touched indices
	 * 
	 * @param file
	 */
	public void index(File file){
		String id = UtilityFunctions.parseIdFromFilename(file.getName());
		
		if(!EsUtilities.documentExistsInIndex(id) && !file.getName().toLowerCase().contains("ds_store")){
			
			try {
				XContentBuilder esContent = buildJsonForTextFile(file);
				
				IndexResponse responseUntouched = EsTransporter.getInstance().prepareIndex(
													ConfigHandler.getInstance().getValue("index.untouched"), ConfigHandler.getInstance().getValue("index.type"))
														.setSource(esContent).execute().actionGet();
				
				IndexResponse responseTouched = EsTransporter.getInstance().prepareIndex(
													EsUtilities.getCurrentIndexName(), ConfigHandler.getInstance().getValue("index.type"))
														.setSource(esContent).execute().actionGet();
				
				// returns the 20 most frequent words from the file
				// based on the touched index, because the stopwords are left out
				TermVectorsResponse termVectRespTouched = EsTransporter.getInstance()
															.prepareTermVectors().setIndex(EsUtilities.getCurrentIndexName())
															.setType(ConfigHandler.getInstance().getValue("index.type"))
															.setId(responseTouched.getId())
															.setFilterSettings(new FilterSettings(25,20,200,1,3000,0,0))
															.setFieldStatistics(true).setTermStatistics(true).execute().actionGet();
				
				FrequentWord frequentWordsTouched = new FrequentWord(termVectRespTouched);
				if(frequentWordsTouched.getFrequentWords() != null){
					UpdateRequest updateRequestUntouched = new UpdateRequest();
					updateRequestUntouched.index(ConfigHandler.getInstance().getValue("index.untouched"))
								 .type(ConfigHandler.getInstance().getValue("index.type"))
								 .id(responseUntouched.getId());
					
					updateRequestUntouched.doc(jsonBuilder()
					        .startObject()
					        	.startObject("autocomplete")
					        		.field("input", frequentWordsTouched.getFrequentWords())
					        		.field("weight", frequentWordsTouched.getWeight())
					        	.endObject()
					        .endObject());
					EsTransporter.getInstance().update(updateRequestUntouched).get();
					
					UpdateRequest updateRequestTouched = new UpdateRequest();
					updateRequestTouched.index(EsUtilities.getCurrentIndexName()).type(ConfigHandler.getInstance().getValue("index.type")).id(responseTouched.getId());
					updateRequestTouched.doc(jsonBuilder()
				            .startObject()
				            	.startObject("autocomplete")
				            		.field("input", frequentWordsTouched.getFrequentWords())
				            		.field("weight", frequentWordsTouched.getWeight())
				            	.endObject()
				            .endObject());
				    EsTransporter.getInstance().update(updateRequestTouched).get();
				}
				PDFMerger.merge(file);
				logger.info("Dokument indiziert: " + file.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Builds the json for a Text File that is moved into ES
	 * 
	 * @param file
	 * @return
	 */
	private XContentBuilder buildJsonForTextFile(File file){
		XContentBuilder builder = null;
		try {
			builder = jsonBuilder()
				.startObject()
					.field("filename", file.getName())
					.field("documentId", UtilityFunctions.parseIdFromFilename(file.getName()))
					.field("path", file.getAbsolutePath())
					.field("desc", UtilityFunctions.getComments(file))
					.field("size", file.length())
					.field("content", UtilityFunctions.parseContent(file))
				.endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return builder;
	}
	
	@Override
	public void folderChanged(File changedFile, String event) {
		if(event.contains("ENTRY_CREATE")){
			index(changedFile);
		}
		else if(event.contains("ENTRY_DELETE")){
			EsUtilities.deleteDocument(changedFile.getName());
		}
	}
}
