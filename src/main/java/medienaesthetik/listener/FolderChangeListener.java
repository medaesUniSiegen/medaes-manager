package medienaesthetik.listener;

import java.io.File;

public interface FolderChangeListener {
	void folderChanged(File changedFile, String event);
}
