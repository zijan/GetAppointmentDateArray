import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetAppointmentDateArray implements Job{

	 //Toronto 
	 static String url = "https://www.visaforchina.cn/server/yyInfo/api/v1/getAppointmentDateArray?nl=8e1ad3d5-f9cd-43a5-95be-37caf44ab4f9&txc=&use_type=center&visa_center_id=YTO2";
	 //Montreal
	 //static String url = "https://www.visaforchina.cn/server/yyInfo/api/v1/getAppointmentDateArray?nl=ecd47567-3c3f-4d1f-7ba-d839e133bfd8&txc=&use_type=center&visa_center_id=MTL2";
	
	static String PhoneNumbers = "647XXXXXXXX,416XXXXXXXX";

	final OkHttpClient client = new OkHttpClient.Builder()
			.readTimeout(60, TimeUnit.SECONDS)
			.connectTimeout(60, TimeUnit.SECONDS)
			.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
			.hostnameVerifier(SSLSocketClient.getHostnameVerifier()).build();

	private String run(String url) throws IOException {
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		return response.body().string();
	}
	
	private void checkAppointmentDate() throws IOException {
		String response = run(url);
		JSONObject json = JSON.parseObject(response);
		JSONArray appointmentDateArray = json.getJSONArray("appointmentDateArray");
		System.out.println(new Date() + ": " + appointmentDateArray.size());
		if (appointmentDateArray.size() > 0) {
			System.out.println(new Date() + ": " + appointmentDateArray);
			SMSSender.sendSMS(PhoneNumbers, appointmentDateArray.toString());
			Sound.play();
			sendEmail(appointmentDateArray.toString());
		}
	}

	public static void main(String[] args) throws IOException, SchedulerException {
		System.out.println("start~~~");
		
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("quartzTrigger", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();
        JobDetail job = JobBuilder.newJob(GetAppointmentDateArray.class).withIdentity("job1", "group1").build();  
        scheduler.scheduleJob(job, trigger);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
			try {
				checkAppointmentDate();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private void sendEmail(String body) {
		System.out.println("sendEmail");
		final String username = "xxxx@gmail.com";
		final String password = "xxxxx";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(props,
		    new javax.mail.Authenticator() {
		        protected PasswordAuthentication getPasswordAuthentication() {
		            return new PasswordAuthentication(username, password);
		        }
		    }
		);

		try {
		    Message message = new MimeMessage(session);
		    message.setFrom(new InternetAddress("no-reply@gmail.com"));
		    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("xxxxx@hotmail.com"));
		    message.setSubject("Check it!");
		    message.setText(body);
		    Transport.send(message);
		    System.out.println("Done");
		} catch (MessagingException e) {
			e.printStackTrace();
		    throw new RuntimeException(e);
		}
	}
	
	public static void main1(String[] s) {
		GetAppointmentDateArray main = new GetAppointmentDateArray();
		main.sendEmail("Subject");
	}
}
