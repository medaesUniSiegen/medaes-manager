package medienaesthetik.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteToFilemakerField {
	
	final Logger logger = LogManager.getLogger("WriteToFilemakerField");
	private static WriteToFilemakerField instance = null;
	
	private URL url = null;
	private byte[] postDataBytes = null;
	
	WriteToFilemakerField(){
		try {
			url = new URL(ConfigHandler.getInstance().getValue("http.ajaxApiURL.editRecord"));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static synchronized WriteToFilemakerField getInstance() {
		if(instance == null){
			instance = new WriteToFilemakerField();
		}
		return instance;
	}	
	
	public void write(String filemakerFile, String layoutName, String fieldName, String fieldContent){
	    
		String currentTime = new Date().toString();
		
		Map<String,Object> params = new LinkedHashMap<>();        
			params.put("layoutName", layoutName);
			params.put("fieldName", fieldName);
			params.put("fieldValue", fieldContent + "\n\n(Stand: "+currentTime + ")");
			params.put("FM_FILE", filemakerFile);
		
	    StringBuilder postData = new StringBuilder();
	    for (Map.Entry<String,Object> param : params.entrySet()) {
	        if (postData.length() != 0) postData.append('&');
	        try {
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				
				postDataBytes = postData.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    try {
	    	String userPassword = ConfigHandler.getInstance().getValue("http.auth.username") + ":" + ConfigHandler.getInstance().getValue("http.auth.password");
	    	String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
	    	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setRequestProperty("Authorization", "Basic " + encoding);
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
