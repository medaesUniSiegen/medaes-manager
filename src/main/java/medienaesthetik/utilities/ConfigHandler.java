package medienaesthetik.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigHandler{
	
	private static ConfigHandler instance = null;
	private Properties props = new Properties();;
	InputStream settingsFile = null;
	
	private ConfigHandler() {
		try {
			settingsFile = new FileInputStream("config.properties");
			this.props.load(settingsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized ConfigHandler getInstance() {
		if(instance == null){
			instance = new ConfigHandler();
		}
		return instance;
	}
	
	public String getValue(String stringPropkey){
		return this.props.getProperty(stringPropkey);
	}
}
