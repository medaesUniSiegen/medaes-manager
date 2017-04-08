package medienaesthetik.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MissingPDFTask extends TimerTask {
	private static final Logger logger = LogManager.getLogger("MissingPDFTask");
	
	@Override
	public void run() {
		File[] OCRFiles = new File(ConfigHandler.getInstance().getValue("document_bearb.path")).listFiles();
		File[] allFiles = new File(ConfigHandler.getInstance().getValue("document_original.path")).listFiles();
		
		ArrayList<String> MissingOCRFiles = new ArrayList<String>();
		
		ArrayList<String> ArrayListOCRFileNames = new ArrayList<String>();
		ArrayList<String> ArrayListAllFileNames = new ArrayList<String>();
		
		for(File ocrFile : OCRFiles){
			if(ocrFile.isFile()){
				ArrayListOCRFileNames.add(ocrFile.getName());
			}
		}
		for(File file : allFiles){
			if(file.isFile()){
				ArrayListAllFileNames.add(file.getName());
			}
		}

		for(String filename : ArrayListAllFileNames){
			if(!ArrayListOCRFileNames.contains(filename)){
				MissingOCRFiles.add(filename);
			}
		}
		
		if(MissingOCRFiles.isEmpty()){
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), "MissingPDF_result", "---");
		}
		else {
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), "MissingPDF_result", MissingOCRFiles.toString());
		}
	}
}