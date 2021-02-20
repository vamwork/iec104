package iec104;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

public class ComPort {

    // Объект
    public static String conStrPar1 = "comm:COM1;blocking=off;baudrate=9600;autorts=off;autocts=off";  //to NL
    public static String conStrPar2 = "comm:COM0;blocking=off;baudrate=19200;autorts=off;autocts=off";  //to analog

    // Стенд
//    public static String conStrPar1 = "comm:COM0;blocking=off;baudrate=9600;autorts=off;autocts=off";  //to NL
//    public static String conStrPar2 = "comm:COM1;blocking=off;baudrate=19200;autorts=off;autocts=off";  //to analog
    //public String conStr;
    private CommConnection connection;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    private int errCount = 0;
    private String cStr;

    public ComPort(String conStr) {
        try {
            cStr = conStr;
            connection = (CommConnection) Connector.open(conStr);
            input = connection.openDataInputStream();
            output = connection.openDataOutputStream();
        } catch (Exception ex) {
            Logger.AddToLog("ComPort.ComPort:" + ex.toString());
        }
    }

    public void CloseComPort() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    public void ReOpenComPort() {
//        try {
//            output.close();
//            input.close();
//            connection.close();
//
//            long firstMsec = System.currentTimeMillis();
//            while (firstMsec + 1000 > System.currentTimeMillis()) {
//                // null
//            }
//            connection = (CommConnection) Connector.open(conStr);
//            input = connection.openDataInputStream();
//            output = connection.openDataOutputStream();
//
//            errCount = 0;
//        } catch (Exception e) {
//            Logger.AddToLog("*ComPort[0005]" + e.toString());
//        }
//    }
    public byte[] Request(byte[] command, int wait) {
        StringBuffer strResponse = new StringBuffer();
        try {
            Thread.sleep(50);

//            Logger.AddToLog(cStr);
//            Logger.AddToLog("->", command);

            output.write(command);
            int c = -1;

            long firstMsec = System.currentTimeMillis();

            while (firstMsec + wait > System.currentTimeMillis()) {
                try {
                    if (input.available() > 0) {
                        c = input.read();
                        if (c >= 0) {
                            strResponse.append((char) c);
                            firstMsec = System.currentTimeMillis();
                            wait = 50;
                        }
                    }
                } catch (IOException ex) {
                    Logger.AddToLog("ComPort.Request[1]" + ex.toString());
                }
            }
        } catch (Exception ex) {
            Logger.AddToLog("ComPort.Request[2]" + ex.toString());
        }
        try {
            //dataOut.flush();
        } catch (Exception ex) {
            Logger.AddToLog("ComPort.Request[3]" + ex.toString());
        }
        //Logger.AddToLog("Resp:", strResponse.toString().getBytes());
        return strResponse.toString().getBytes();
    }
}
