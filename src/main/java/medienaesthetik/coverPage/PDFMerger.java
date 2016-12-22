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
			this.merge(coverPage);
		}
	}
	
	public void merge(File coverPage){
		String documentId = getDocumentIdFromFilename(coverPage.getName());
		if(!documentId.isEmpty()){
			File foundTextFile = new File(UtilityFunctions.findFileByID(documentId));
			if(foundTextFile.exists()){
				try {
					PDDocument PDDoc = PDDocument.load(foundTextFile);
					String textContent = new PDFTextStripper().getText(PDDoc);
					// Does the new Document already have a cover Page? (Every Cover Page has "medaes13" printed on it)
					if(!textContent.contains("medaes13")){
						logger.info("Dokument ohne Deckblatt gefunden: "+foundTextFile);
						// Add the found text document to the cover page
						try {
							PDFMergerUtility mut = new PDFMergerUtility();
							mut.addSource(coverPage); // 1
							mut.addSource(foundTextFile); // 2
							// Cover Page  + Text moved to create Commons 
							// Cover Page moved to cover Page archive
							mut.setDestinationFileName(UtilityFunctions.getAllDocumentsFolder() + "/" + foundTextFile.getName());
							coverPage.renameTo(new File(UtilityFunctions.getCoverPageFolder() + "/Deckblatt_Archiv/" + coverPage.getName()));
							
							mut.mergeDocuments(null);
						} catch (IOException e){
							e.printStackTrace();
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
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
