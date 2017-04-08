package medienaesthetik.utilities;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import medienaesthetik.http.SoftwareStatusTask;

public class ScheduledTasks implements Runnable{
	Calendar today;
	
	public ScheduledTasks(){
		today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ConfigHandler.getInstance().getValue("scheduledTask.hour")));
		today.set(Calendar.MINUTE, Integer.parseInt(ConfigHandler.getInstance().getValue("scheduledTask.minute")));
		today.set(Calendar.SECOND, Integer.parseInt(ConfigHandler.getInstance().getValue("scheduledTask.second")));
	}
	
	@Override
	public void run() {
		Timer timer = new Timer();
		timer.schedule(new SoftwareStatusTask(), today.getTime(), TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)); // Softwarestatus wird alle 30 Minuten durchgeführt
		timer.schedule(new MissingPDFTask(), today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // 1* Täglich
		timer.schedule(new ArchiveIntegrityTask(), today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // 1* Täglich
	}
}	