package medienaesthetik.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import medienaesthetik.elasticsearch.EsTransporter;
import medienaesthetik.elasticsearch.EsUtilities;

/*
 * (/) reindex
 * reindex the touched_pdf_index (based on the untouched_pdf_index)
 */
public class ReindexHandler implements HttpHandler{
	
	private static final Logger logger = LogManager.getLogger("ReindexHandler");
	private static int BULK_ACTIONS_THRESHOLD = 1000;
	private static int BULK_CONCURRENT_REQUESTS = 1;

	@Override
	public void handle(HttpExchange he) throws IOException {
		
		String _futureIndexName = EsUtilities.updateIndexName();
		String _lastUsedIndexName = EsUtilities.getLastUsedIndexName();
		
		CreateIndexRequest createRequest = new CreateIndexRequest(_futureIndexName);
		createRequest.settings(EsUtilities.getSettings());
		createRequest.mapping("text", EsUtilities.getUntouchedMapping());
		EsTransporter.getInstance().admin().indices().create(createRequest).actionGet();
		
		SearchResponse scrollResp = EsTransporter.getInstance().prepareSearch(EsUtilities.getUntouchedIndexName()) // Specify index
				.setScroll(new TimeValue(60000))
			    .setQuery(QueryBuilders.matchAllQuery()) // Match all query
			    .setSize(100).execute().actionGet(); //100 hits pro shard werden zurückgegeben pro scroll
		
		BulkProcessor bulkProcessor = BulkProcessor.builder(EsTransporter.getInstance(), new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request,
					BulkResponse response) {
				
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request,
					Throwable failure) {
				
			}
		    }).setBulkActions(BULK_ACTIONS_THRESHOLD)
		      .setConcurrentRequests(BULK_CONCURRENT_REQUESTS)
		      .setFlushInterval(TimeValue.timeValueMillis(5)).build();
		
		while (true) {
		    //Break condition: No hits are returned
		    if (scrollResp.getHits().getHits().length == 0) {
		        bulkProcessor.close();
		        break; 
		    }
		    // Get results from a scan search and add it to bulk ingest
		    for (SearchHit hit: scrollResp.getHits()) {
		        IndexRequest request = new IndexRequest(_futureIndexName, hit.type(), hit.id());
		        Map source = ((Map) ((Map) hit.getSource()));
		        request.source(source);
		        bulkProcessor.add(request);
		   }
		   scrollResp = EsTransporter.getInstance().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
		}
		//create alias
		EsTransporter.getInstance().admin().indices().prepareAliases().addAlias(_futureIndexName, EsUtilities.getAliasName()).get();
		//remove old alias 
		EsTransporter.getInstance().admin().indices().prepareAliases().removeAlias(_lastUsedIndexName, EsUtilities.getAliasName()).get();
		// remove old Index 
		EsTransporter.getInstance().admin().indices().prepareDelete(_lastUsedIndexName).get();
		
    	he.sendResponseHeaders(200, 0);
    	OutputStream os = he.getResponseBody();
    	os.write("200".getBytes());
    	os.close();
    	
    	logger.info("Neue Indizierung gestartet: " + _futureIndexName);
	}

}
