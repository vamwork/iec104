package iec104;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class Logger {

    private static final String logFileName = "log.txt";

    private static String logData = " ";

    private static boolean isWorkChar = false;

    private static int cnt = 0;

    public static void AddToLog(String s1, int value) {
        String h = Integer.toString(value);
        AddToLog(s1 + ": " + h);
    }

    public static void AddToLog(String s1, int[] data) {
        String s = "";
        for (int i = 0; i < data.length; i++) {
            String h = Integer.toString(data[i]);
            s = s + h + ", ";
        }
        AddToLog(s1 + ": " + s);
    }

    public static String ByteToString(byte b) {
        String result = Integer.toHexString(Global.ToShort(b));
        if (result.length() == 1) {
            result = "0" + result;
        }
        return result;
    }
   
    public static void AddToLog(String s1, byte[] data) {
        String s = "";
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                String h = Integer.toHexString(Global.ToShort(data[i]));
                if (h.length() == 1) {
                    h = "0" + h;
                }
                s = s + h + " ";
            }
        }
        AddToLog(s1 + ": " + s);
    }

    public static void AddToLog(InfromationElement e) {
        Logger.AddToLog("----Information Element-----");
        Logger.AddToLog("quality=", e.quality);
        Logger.AddToLog("elementType=", e.elementType);
        Logger.AddToLog("size=", e.Size());
        Logger.AddToLog("----------------------------");
    }

    public static void AddToLog(InformationObject obj) {
        Logger.AddToLog("----Information Object-----");
        Logger.AddToLog("Address=", obj.address);
        Logger.AddToLog("Size=", obj.Size());
        Logger.AddToLog("----------------------------");
    }

    public static void IamWork(String c) {
        System.out.print(c);
        isWorkChar = true;
    }

    public static void AddToLog(String s) {
        if (isWorkChar) {
            System.out.println("");
            isWorkChar = false;
        }
        cnt++;
        System.out.println(Long.toString(System.currentTimeMillis()) + " [" + Integer.toString(cnt) + "] " + s);
    }

    public static String ReadLogFile() {
        String result = "";
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + logFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openDataInputStream();

                if (is.available() > 0) {
                    byte[] str = new byte[is.available()];
                    is.read(str);

                    for (int i = 0; i < str.length; i++) {
                        result = result + (char) (str[i]);
                    }
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.AddToLog("*Logger[ReadLogFile]" + ex.toString());
        }

        Logger.AddToLog("Read log: " + logData);
        return result;
    }

    public static void CreateLogFile(String s) {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + logFileName);
            if (!fConn.exists()) {
                fConn.create();
            } else {
                fConn.delete();
                fConn.create();
            }
            OutputStream os = fConn.openDataOutputStream();
            byte[] str = new byte[s.length()];
            for (int i = 0; i < s.length(); i++) {
                str[i] = (byte) s.charAt(i);
            }
            os.write(str);
            os.close();
            fConn.close();

        } catch (Exception ex) {
            Logger.AddToLog("*Logger[AddToLogFile]" + ex.toString());
        }
    }

    public static byte[] GetLogDataAsBytes() {
        byte[] str = new byte[logData.length()];
        for (int i = 0; i < logData.length(); i++) {
            str[i] = (byte) logData.charAt(i);
        }
        return str;
    }

}
