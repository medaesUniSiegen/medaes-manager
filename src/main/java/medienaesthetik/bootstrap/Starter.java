package medienaesthetik.bootstrap;

import java.io.IOException;
import java.util.TimeZone;

import medienaesthetik.coverPage.PDFMerger;
import medienaesthetik.elasticsearch.EsUtilities;
import medienaesthetik.elasticsearch.Indexer;
import medienaesthetik.http.RestAPI;
import medienaesthetik.utilities.ConfigHandler;
import medienaesthetik.utilities.ScheduledTasks;
import medienaesthetik.utilities.WatchDirectory;

public class Starter {
	
	public static void main(String[] args) {
		
		try {
			EsUtilities.prepareIndex();
			Indexer _indexer	 = new Indexer();
			PDFMerger _pdfMerger = new PDFMerger(); // starts Pdfmerger Module
			RestAPI _restAPI 	 = new RestAPI(); // starts restAPI Module
			
			Thread scheduledTasksThread = new Thread(new ScheduledTasks());
			scheduledTasksThread.start();
			
			Thread watchDirThread1 = new Thread(new WatchDirectory(ConfigHandler.getInstance().getValue("deckblattSchleuse.path"), _pdfMerger));
			Thread watchDirThread2 = new Thread(new WatchDirectory(ConfigHandler.getInstance().getValue("document_bearb.path"), _indexer));
			watchDirThread1.start();
			watchDirThread2.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}