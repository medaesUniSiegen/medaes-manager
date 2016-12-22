package medienaesthetik.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

import medienaesthetik.http.ReindexHandler;

@SuppressWarnings("restriction")
public class RestAPI {
	
	private static final Logger logger = LogManager.getLogger("ReindexHandler");
	HttpServer webServer = null;

	public RestAPI(){
		try {
			webServer = HttpServer.create(new InetSocketAddress(8999), 0);
			webServer.createContext("/reindex", new ReindexHandler());
			webServer.createContext("/stopwords", new StopwordHandler());
			
			webServer.start();
			
			logger.info("Rest API Modul gestartet");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
