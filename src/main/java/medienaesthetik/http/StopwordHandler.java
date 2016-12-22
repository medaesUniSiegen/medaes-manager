package medienaesthetik.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import medienaesthetik.utilities.ConfigHandler;


/*
 * (/) stopwords
 * POST: Send a POST request with a stopwordlist and saves the List in a file
 * GET: returns stopwords
 */
public class StopwordHandler implements HttpHandler{
	
	private static final Logger logger = LogManager.getLogger("StopwordHandler");

	@SuppressWarnings("restriction")
	@Override
	public void handle(HttpExchange he) throws IOException {
		String method = he.getRequestMethod();
		
		// reads the body (stopwords) of the post request, this stopword string is converted into an ArrayList (sorted & removes doubles)
		// the cleansed stopword string is saved as an UTF-8 File 
		if(method.equals("POST")){
			OutputStream os;
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            
            String query = br.readLine();            
            parseQuery(query, parameters);
            
            Object stopwords = parameters.get("stopwords");	            
            
            List<String> stopwordList = stopwordStringToList((String) stopwords);
            
            String resultString = listToString(stopwordList);
            
            File fileDir = new File(ConfigHandler.getInstance().getValue("stopword.path"));
            if(fileDir.exists()){	            	
            	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileDir), "UTF-8");
            	
            	out.write(resultString);
            	out.close();
            	
            	he.sendResponseHeaders(200, 0);
            	os = he.getResponseBody();
            	os.write("200".getBytes());
            	os.close();
            	logger.info("Aktualisierte Stoppwortliste wurde gespeichert");
            }
            else{
            	he.sendResponseHeaders(404, 0);
            	os = he.getResponseBody();
            	os.write("404".getBytes());
            	os.close();
            }
            
		}
		
		else if(method.equals("GET")){
			String encoding = "UTF-8";
			OutputStream os;
			
			File fileDir = new File(ConfigHandler.getInstance().getValue("stopword.path"));
			
			if(fileDir.exists()){
				String fileContents = readUsingScanner(fileDir.toString());
				
				List<String> stopwordList = stopwordStringToList(fileContents);
				
				String resultString = listToString(stopwordList);
				
				he.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);
				he.sendResponseHeaders(200, resultString.getBytes().length);
				
				os = he.getResponseBody();
				os.write(resultString.getBytes());
			}
			else {
				he.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);
				he.sendResponseHeaders(200, 0);
				
				os = he.getResponseBody();
				String errorMsg =  "Medaes13 Freigabe nicht erreichbar: " + fileDir.toString();
				
				os.write(errorMsg.getBytes());
			}
			os.close();
		}
        
	}
	
    /*
     * Convertes a word String in an ArrayList and removes double entries and sorts the words
     */
    private static  List<String> stopwordStringToList(String fileContents){
    	
    	String pairs[] = fileContents.split("\n");
    	
    	List<String> stopwordList = new ArrayList<String>();
		for(String pair : pairs){
			//Filemaker hat noch das Alte \r als neue Line implementiert
			if(pair.contains("\r")){
				String filemakerStrings[] = pair.split("\r");
				for(String fmString : filemakerStrings){
					if(!stopwordList.contains(fmString)){
						stopwordList.add(fmString);
					}
				}
			}
			else {
				if(!stopwordList.contains(pair)){
					stopwordList.add(pair);
				}
			}
		}
		
		Collections.sort(stopwordList, String.CASE_INSENSITIVE_ORDER);
		
    	return stopwordList; 
    }
    
    /*
     * Converts an ArrayList into a String with a new Line at the end of each word
     */
    private static  String listToString(List<String> stopwordList){
		
    	String resultString = "";
    	for(String item : stopwordList){
			resultString = resultString + item + "\n";
		}
    	
    	return resultString;
    }
    
    /*
     * Reads the content of a File
     */
    private static String readUsingScanner(String fileName) throws IOException {
        
    	Scanner scanner = new Scanner(Paths.get(fileName), "UTF-8");
        String data = scanner.useDelimiter("\\A").next();
        scanner.close();
        
        return data;
    }
    
    public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
       	 String pairs[] = query.split("[&]");
       	 for (String pair : pairs) {
       		 String param[] = pair.split("[=]");
       		 String key = null;
       		 String value = null;
       		 if (param.length > 0) {
       			 key = URLDecoder.decode(param[0], 
       			 System.getProperty("file.encoding"));
                }

       		 if (param.length > 1) {
       			 value = URLDecoder.decode(param[1], 
       			 System.getProperty("file.encoding"));
       		 }

       		 if (parameters.containsKey(key)) {
       			 Object obj = parameters.get(key);
       			 if (obj instanceof List<?>) {
       				 List<String> values = (List<String>) obj;
       				 values.add(value);

       			 } else if (obj instanceof String) {
       				 List<String> values = new ArrayList<String>();
       				 values.add((String) obj);
       				 values.add(value);
       				 parameters.put(key, values);
       			 }
       		 } else {
       			 parameters.put(key, value);
       		 }
       	 }
        }
   }
}

