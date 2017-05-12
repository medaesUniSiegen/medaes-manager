package medienaesthetik.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

public class MissingPDFTask extends TimerTask {
	private static final Logger logger = LogManager.getLogger("MissingPDFTask");
	
	@Override
	public void run() {
		File[] OCRFiles = new File(ConfigHandler.getInstance().getValue("document_bearb.path")).listFiles();
		File[] allFiles = new File(ConfigHandler.getInstance().getValue("document_original.path")).listFiles();
		int copyCount = 0;
		
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
		
		for(String filename : MissingOCRFiles){
			if(filename.contains("pdf")){
				File from = new File(ConfigHandler.getInstance().getValue("document_original.path") + "/" + filename);
				File to = new File(ConfigHandler.getInstance().getValue("tempfolder.path") + "/" + filename);
				try {
					Files.copy(from, to);
				} catch (IOException e) {
					e.printStackTrace();
				}
				from.delete();
				copyCount = copyCount + 1;
			}
		}
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File[] tempFiles = new File(ConfigHandler.getInstance().getValue("tempfolder.path")).listFiles();
		for(File file : tempFiles){
			if(!file.getName().contains("DS_Store")){
				File from = new File(ConfigHandler.getInstance().getValue("tempfolder.path") + "/" + file.getName());
				File to = new File(ConfigHandler.getInstance().getValue("document_original.path") + "/" + file.getName());
				try {
					Files.copy(from, to);
				} catch (IOException e) {
					e.printStackTrace();
				}
				from.delete();
				logger.info(from.getName() + " wurde erneut zum OCR Prozess gesendet.");
			}
		}	
		
		if(MissingOCRFiles.isEmpty()){
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), "MissingPDF_result", "---");
		}
		else {
			WriteToFilemakerField.getInstance().write(ConfigHandler.getInstance().getValue("filemaker.status.file.portal"), ConfigHandler.getInstance().getValue("filemaker.status.layout"), "MissingPDF_result", MissingOCRFiles.toString());
		}
	}
	
	private int getTempFolderItemLength(){
		//Without DS_Store
		int numberOfItems = 0;
		File[] tempFiles = new File(ConfigHandler.getInstance().getValue("tempfolder.path")).listFiles();
		
		for(File file : tempFiles){
			if(!file.getName().contains("DS_Store")){
				numberOfItems = numberOfItems + 1; 
			}
		}
		
		return numberOfItems;
	}
}