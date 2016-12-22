package medienaesthetik.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public final class UtilityFunctions {
	
	private UtilityFunctions(){}
	
	private static File allDocumentsFolder = new File(ConfigHandler.getInstance().getValue("document.path"));
	private static File coverPageFolder = new File(ConfigHandler.getInstance().getValue("deckblatt.path"));
	private static File coverPageSchleuse = new File(ConfigHandler.getInstance().getValue("deckblattSchleuse.path"));
	
	public static String findFileByID(String id){
		
		File[] allFiles = allDocumentsFolder.listFiles();
		
		for(File currentFile : allFiles){
			if(currentFile.getName().contains(id)){
				return currentFile.getAbsolutePath();
			}
		}
		return "";
	}
	
	/**
	 * Reads the filemaker id from the filename
	 * (id needs to be infront of the .pdf)
	 * Example: Testname_62049.pdf
	 * 
	 * @param filename
	 * @return
	 */
	public static String parseIdFromFilename(String filename){
		String id = "";
		
		Pattern pattern = Pattern.compile("\\d+(?=.*)");
		Matcher matcher = pattern.matcher(filename);
		
		while(matcher.find()){
			if(!matcher.group().equals("0")){
				id = matcher.group();
			}
		}
		
		return id;
	}
	
	
	/**
	 * Parses the content from a Text File 
	 * 
	 * @param file
	 * @return parsed Content (String)
	 */
	public static String parseContent(File file){
		
		String parsedContent = null;
		FileInputStream inputstream = null;
		
		try {
			BodyContentHandler handler 	= new BodyContentHandler(-1);
			Metadata metadata 			= new Metadata();
			inputstream 				= new FileInputStream(file);
			ParseContext pcontext 		= new ParseContext();
			PDFParser pdfparser 		= new PDFParser(); 
			
			pdfparser.parse(inputstream, handler, metadata, pcontext);

			parsedContent = handler.toString();
			
			// removes "VGL"
			parsedContent = parsedContent.replaceAll("vgl", "");
			
			// removes every number
			parsedContent = parsedContent.replaceAll("\\d", "");
			
			// removes every word with 1 or 2 Characters
			parsedContent = parsedContent.replaceAll("\\b[a-zA-Z0-9]{1,2}\\b", "");
			
			inputstream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		} finally {
			if (inputstream != null) {
				try {
					inputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return parsedContent;
	}
	
	public static File getAllDocumentsFolder (){
		return allDocumentsFolder;
	}
	
	public static File getCoverPageFolder (){
		return coverPageFolder;
	}
	
	public static File getCoverPageSchleuse(){
		return coverPageSchleuse;
	}
	
}
