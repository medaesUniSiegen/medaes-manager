package medienaesthetik.coverPage;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.google.common.io.Files;

import medienaesthetik.listener.FolderChangeListener;
import medienaesthetik.utilities.UtilityFunctions;

public class PDFMerger implements FolderChangeListener{
	
	private static final Logger logger = LogManager.getLogger("pdfMerger");
	
	public PDFMerger(){
		
		File[] allCoverPages = UtilityFunctions.getCoverPageSchleuse().listFiles();
		
		for(File coverPage : allCoverPages) {
			merge(coverPage);
		}
		logger.info("PDF Merger Modul gestartet");
	}
	
	@Override
	public void folderChanged(File coverPage, String event) {
		// a new cover Page triggered the folderChanged Event
		if(event.contains("ENTRY_CREATE")){
			if(coverPage.getName().contains("Deckblatt")){
				this.merge(coverPage);
			}
		}
	}
	
	/*
	 * Merger erkennt automatisch ob ein Cover oder ein Dokument übergeben wird
	 */
	public static void merge(File file){
		String documentId = UtilityFunctions.parseIdFromFilename(file.getName());
		if(!documentId.isEmpty()){
			try {
				File textFile = new File(UtilityFunctions.findFileByID(documentId));
				File coverPage = new File(UtilityFunctions.findCoverPagebyID(documentId));
				// Does the new Document already have a cover Page? (Every Cover Page has "medaes14" printed on it)
				if(textFile.exists() && coverPage.exists()){
					PDDocument PDDoc = PDDocument.load(textFile);
					String textContent = new PDFTextStripper().getText(PDDoc);
					if(!textContent.contains("medaes14")){
						logger.info("Dokument ohne Deckblatt gefunden: "+textFile);
						// Add the found text document to the cover page
						try {
							PDFMergerUtility mut = new PDFMergerUtility();
							mut.addSource(coverPage); // 1
							mut.addSource(textFile); // 2
							// Cover Page  + Text moved to create Commons 
							// Cover Page moved to cover Page archive
							mut.setDestinationFileName(UtilityFunctions.getAllDocumentsFolder() + "/" + textFile.getName());
							Files.copy(coverPage.getAbsoluteFile(), new File(UtilityFunctions.getCoverPageFolder() + "/Deckblatt_Archiv/" + coverPage.getName()));
							//coverPage.renameTo(new File(UtilityFunctions.getCoverPageFolder() + "/Deckblatt_Archiv/" + coverPage.getName()));
							mut.mergeDocuments(null);
							coverPage.delete();
						} catch (IOException e){
							e.printStackTrace();
						} finally {
							if(PDDoc != null){
								PDDoc.close();
							}
						}
					}
					PDDoc.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private String getDocumentIdFromFilename(String filename){
		String id = "";
		
		Pattern pattern = Pattern.compile("\\d*+(?=Deckblatt)");
		Matcher matcher = pattern.matcher(filename);
		
		while(matcher.find()){
			if(!matcher.group().equals("")){
				id = matcher.group();
				return id;
			}
		}
		
		return id;
	}
}
