
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSSender {

    //goto www.twilio.com signup a free trial account
    public static final String ACCOUNT_SID = "xxxxx";
    public static final String AUTH_TOKEN = "xxxx";
    
    private static final String FROM_PHONENUMBER = "xxxxx";

    static {
    	Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }
    
    public static void main(String[] args) {
    	SMSSender.sendSMS("647xxxxxxxx", "Test...");
    }
    
    public static void sendSMS(String toPhoneNumber, String body) {
    	System.out.println("SMSSender.sendSMS PhoneNumber: " + toPhoneNumber);
    	if(toPhoneNumber == null) {
    		System.out.println("Phone number can not be null.");
			return;
		}
    	
    	toPhoneNumber = toPhoneNumber.replaceAll(";", ",");
    	String[] phones = toPhoneNumber.split(",");
    	if(phones.length > 0) {
    		for(String phone : phones) {
    			System.out.println("Send SMS to " + toPhoneNumber);
    			Message message = Message.creator(new PhoneNumber(phone), new PhoneNumber(FROM_PHONENUMBER), body).create();
    			System.out.println("Sid:"+message.getSid()+" Status:"+message.getStatus());
    		}
    	}
    }
}
