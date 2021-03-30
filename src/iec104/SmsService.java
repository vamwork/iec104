package iec104;

public class SmsService {

    public static AtListener atListener;

    public static void SendSms(String phone, String message) {
        Logger.AddToLog("Send sms");
        String ans = atListener.SendAtCommand("AT+CMGS=\"" + phone + "\"\r");
        Logger.AddToLog("Answer1:" + ans);
        if (ans.toUpperCase().indexOf("ERROR") >= 0) {
            Logger.AddToLog("ERROR");
        } else {
            ans = atListener.SendAtCommand(message + "\32\r");
            Logger.AddToLog("Answer2: " + ans);
        }
    }    
}
