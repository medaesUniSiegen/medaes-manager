package medienaesthetik.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ArchiveIntegrityTask extends TimerTask {
	private static final Logger logger = LogManager.getLogger("ArchiveIntegrityTask");
	
	private static File allDocumentsFolder = new File(ConfigHandler.getInstance().getValue("document_bearb.path"));
	private static File allOriginalDocumentsFolder = new File(ConfigHandler.getInstance().getValue("document_original.path"));
	
	private boolean isFileOk(File file){
		if(!file.getName().contains("doc")){
			try {
				PDDocument PDDoc = PDDocument.load(file);
				if(PDDoc.getNumberOfPages() > 0){
					PDDoc.close();
					return true;
				}
				else {
					PDDoc.close();
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException ilAE) {
				return false;
			}
			return false;	
		}
		else {
			return true;
		}
	}
	
	@Override
	public void run() {
		logger.info("Archive Integritätscheck wurde gestartet");
		
		ArrayList<File> corruptedFiles = new ArrayList<File>();
		File[] filePaths = {allDocumentsFolder, allOriginalDocumentsFolder};
		
		//loops through both folders and every file in it
		for(File filePath : filePaths){
			for(File file : filePath.listFiles()){
				if(!file.getName().contains("DS_Store") && file.isFile()){
					if(!isFileOk(file)){
						corruptedFiles.add(file);
					}
				}
			}
		}
		if(!corruptedFiles.isEmpty()){
			String corruptedFileString = "";
			for(File corruptFile : corruptedFiles){
				corruptedFileString = corruptedFileString + corruptFile.getAbsolutePath() + "\n";
			}
			//StatusMail.getInstance().sendMail("Archiv Integritäts Test", StatusMail.getInstance().buildMailText("Archive Integritätscheck", corruptedFileString, ""));
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), ConfigHandler.getInstance().getValue("filemaker.status.field.Archive_Integritycheck_result"), "Folgende Dateien sollten überprüft werden: \n" + corruptedFileString);
		}
		else {
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), ConfigHandler.getInstance().getValue("filemaker.status.field.Archive_Integritycheck_result"), "Integritätscheck fehlerfrei durchgeführt.");
		}
		logger.info("Archive Integritätscheck wurde beendet");
	}
}