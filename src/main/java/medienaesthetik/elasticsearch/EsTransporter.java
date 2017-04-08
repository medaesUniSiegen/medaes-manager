package medienaesthetik.elasticsearch;

import java.net.InetAddress;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import medienaesthetik.utilities.ConfigHandler;

public class EsTransporter {
	
	private static Client client = null;
	
	public EsTransporter() {
		Settings setngs = Settings.settingsBuilder()
				.put("cluster.name", ConfigHandler.getInstance().getValue("cluster.name"))
				.put("node.name", ConfigHandler.getInstance().getValue("node.name"))
				.build();
		
		client = TransportClient.builder().settings(setngs).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getLoopbackAddress(), 9300));
	}
	
	public static synchronized Client getInstance() {
		if(client == null){
			new EsTransporter();
		}
		return client;
	}
}