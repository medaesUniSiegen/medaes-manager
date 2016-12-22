package medienaesthetik.utilities;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import medienaesthetik.listener.FolderChangeListener;

public class WatchDirectory implements Runnable {
	
	private static final Logger logger = LogManager.getLogger("WatchDirectory");
	private File folderToWatch;
	private List<FolderChangeListener> listeners = new ArrayList<FolderChangeListener>();
	
	private void watch(Path path){
		// Sanity check - Check if path is a folder
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path,"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path + " is not a folder");
			}
		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}
		
		logger.info("Warte auf neue Dateien: " + path);
		
		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem ();
		
		// We create the new WatchService using the new try() block
		try(WatchService service = fs.newWatchService()) {
			
			// We register the path to the service
			// We watch for creation events
			path.register(service, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
			
			// Start the infinite polling loop
			WatchKey key = null;
			while(true) {
				key = service.take();

				// Dequeueing events
				Kind<?> kind = null;
				for(WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; //loop
					} else if (ENTRY_CREATE == kind) {
						Path newPath = ((WatchEvent<Path>) watchEvent).context();
						File addedFile = new File(path.toString(), newPath.toString());
						
						for(FolderChangeListener fcl : listeners){
							fcl.folderChanged(addedFile, "ENTRY_CREATE");
						}
					} else if(ENTRY_DELETE == kind){
						Path newPath = ((WatchEvent<Path>) watchEvent).context();
						File addedFile = new File(path.toString(), newPath.toString());
						
						for(FolderChangeListener fcl : listeners){
							fcl.folderChanged(addedFile, "ENTRY_DELETE");
						}

					} else if(ENTRY_MODIFY == kind){

					} 
				}
				
				if(!key.reset()) {
					break; //loop
				}
			}
			
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	public void addListener(FolderChangeListener toAdd){
		listeners.add(toAdd);
	}
	
	public WatchDirectory(String folderToWatch, FolderChangeListener listener){
		this.folderToWatch = new File(folderToWatch);
		this.addListener(listener);
	}
	
	public WatchDirectory(String folderToWatch){
		this.folderToWatch = new File(folderToWatch);
	}
	
	public void run() {
		watch(Paths.get(folderToWatch.toURI()));
	}
}