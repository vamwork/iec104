package iec104;

import com.cinterion.io.ATCommand;
import com.cinterion.io.ATCommandFailedException;
import com.cinterion.io.ATCommandListener;

public class AtListener implements ATCommandListener {

    public ATCommand atc;
    private InConnThread inConnThread;

    public boolean switching = false;

    public AtListener() {
        try {
            atc = new ATCommand(false);
            atc.addListener(this);
        } catch (ATCommandFailedException e) {
            Logger.AddToLog("*AtListener[0001]" + e.toString());
        }
    }

    public String SendAtCommand(String cmd) {
        String answer = "";
        try {
            answer = atc.send(cmd);
        } catch (Exception e) {
            Logger.AddToLog("*AtListener[0002]" + e.toString());
        }
        return answer;
    }

    public void ATEvent(final String Event) {
        Logger.AddToLog("ATEvent: " + Event);

        if (Event.indexOf("RING") >= 0) {
            if (!switching) {
                switching = true;
                InternalControll.SwitchSim();
            }
        }
    }

    public void RINGChanged(boolean bln) {
        Logger.AddToLog("Ring.");
//        Logger.AddToLog("Switch sim-card.");                        
//        InternalControll.SwitchSim();
        //Controller.isSim1 = false;        
    }

    public void DCDChanged(boolean bln) {
        Logger.AddToLog("DCD.");
    }

    public void DSRChanged(boolean bln) {
        Logger.AddToLog("DSR.");
    }

    public void CONNChanged(boolean bln) {
        Logger.AddToLog("CONN.");
        inConnThread = new InConnThread(atc);
        inConnThread.start();
    }

    private String GetSmsContent(String arg0, boolean deleteSms) {
        // index of received SMS message in the memory
        int idx = arg0.indexOf(",");
        String sms_positionInMemory = arg0.substring(idx + 1, idx + 3);

        // get the SMS content
        String contentOfSms = "";
        try {

            // get the message from the index position
            String response = atc.send("AT+CMGR=" + sms_positionInMemory + "\r");

            Logger.AddToLog("response: " + response);

            // delete the SMS
            if (deleteSms) {
                atc.send("AT+CMGD=" + sms_positionInMemory + "\r");
            }

        } catch (Exception e) {
            contentOfSms = null;
            Logger.AddToLog("*GetSmsContent: " + e.toString());
        }

        return contentOfSms;
    }

}
