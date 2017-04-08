package medienaesthetik.utilities;

import java.util.ArrayList;

import javax.mail.Message.RecipientType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

public class StatusMail{
	private static final Logger logger = LogManager.getLogger("StatusMail");
	private static StatusMail instance = null;
	
	StatusMail(){}
	
	public static synchronized StatusMail getInstance() {
		if(instance == null){
			instance = new StatusMail();
		}
		return instance;
	}
	
	public String buildMailText(String task, String dataString, String additionalText){
		String mailtext;
		dataString = dataString.isEmpty() ? "-" : dataString;
		if(additionalText.isEmpty()){
			mailtext = "Hallo, \n es kam zu einem Problem bei: "+task+". \n Folgende Daten sind möglicherweise betroffen: \n "+dataString+" \n Viele Grüße \n \n Java Elasticsearch Manager";
		} else {
			mailtext = "Hallo, \n es kam zu einem Problem bei: "+task+". \n Folgende Daten sind möglicherweise betroffen: \n "+dataString+" \n \n Hinweis: "+additionalText+"\n \n Viele Grüße \n \n Java Elasticsearch Manager";
		}
		
		return mailtext;
	}
	
	public void sendMail(String subject, String mailText){
		Email email = new Email();
		
		email.setFromAddress("System Status", "medaesstatus@web.de");
		email.addRecipient("Admin", ConfigHandler.getInstance().getValue("email.admin"), RecipientType.TO);
		email.setSubject(subject);
		
		email.setText(mailText);
		
		new Mailer(ConfigHandler.getInstance().getValue("email.smtpServer"), Integer.parseInt(ConfigHandler.getInstance().getValue("email.Port")), ConfigHandler.getInstance().getValue("email.account"), ConfigHandler.getInstance().getValue("email.password"), TransportStrategy.SMTP_TLS).sendMail(email);
		logger.info("Statusmail wurde versendet");
	}
}
