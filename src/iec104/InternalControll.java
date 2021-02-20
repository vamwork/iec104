package iec104;

import com.cinterion.io.I2cBusConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class InternalControll {

    static I2cBusConnection cc;
    static InputStream inI2C;
    static OutputStream outI2C;

    private static final String SimStateFile = "state.sim";

    public static int currentSim;

    private static String i2CEnd = "00";
    private static byte atEnd = '\r';

    private static byte[] bytes = new byte[520];
    private static char[] chars = new char[260];

    public static void InitI2C() {
        try {
            cc = (I2cBusConnection) Connector.open("i2c:0;baudrate=100");
            inI2C = cc.openInputStream();
            outI2C = cc.openOutputStream();
        } catch (Exception ex) {
            Logger.AddToLog("*I2C: " + ex.toString());
        }
    }

    private static void CloseI2C() {
        if (inI2C != null) {
            try {
                inI2C.close();
            } catch (IOException ex) {
                Logger.AddToLog("*CloseI2C[1] " + ex.toString());
            }
        }
        if (outI2C != null) {
            try {
                outI2C.close();
            } catch (IOException ex) {
                Logger.AddToLog("*CloseI2C[2] " + ex.toString());
            }
        }
        if (cc != null) {
            try {
                cc.close();
            } catch (IOException ex) {
                Logger.AddToLog("*CloseI2C[3] " + ex.toString());
            }
        }
    }

    public static void ReadCurrentSimState() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + SimStateFile);
            if (fConn.exists()) {
                InputStream is = fConn.openDataInputStream();

                if (is.available() > 0) {
                    currentSim = is.read();
                }
                is.close();
                Logger.AddToLog("Read simstate: sim", currentSim);
            } else {
                currentSim = 1;
                Logger.AddToLog("Set SIM1 by default.");
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.AddToLog("ReadCurrentSimState: " + ex.toString());
        }
    }

    public static void SaveCurrentSimState() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + SimStateFile);

            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);
            os.write((byte) currentSim);
            os.close();
            fConn.close();
            Logger.AddToLog("Saved simstate to: ", currentSim);
        } catch (Exception ex) {
            Logger.AddToLog("*Mercury[0004]" + ex.toString());
        }
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int start, int length) {
        int j;
        for (j = start; j < length; j++) {
            int v = bytes[j] & 0xFF;
            chars[(j - start) * 2] = hexArray[v >>> 4];
            chars[(j - start) * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(chars, 0, (j - start) * 2);   //TODO: +2 was here
    }

    public static int hexToBytes(String hex) {
        int pos = hex.indexOf(i2CEnd);
        while (pos % 2 == 1 && pos != -1) {
            pos = hex.indexOf(i2CEnd, pos + 1);
        }
        if (pos > -1) {
            hex = hex.substring(0, pos);
        }

        final int len = hex.length();
        if (len % 2 != 0) {
            return 0;
        }

        int i;
        for (i = 0; i < len; i += 2) {
            int h = hexToBin(hex.charAt(i));
            int l = hexToBin(hex.charAt(i + 1));
            if (h == -1 || l == -1) {
                return 0;
            }
            bytes[i / 2] = (byte) (h * 16 + l);
        }

        return i / 2;
    }

    private static int readI2CAnswer() throws IOException {
        int pos = 0;
        for (int i = 0; i < 4; i++) {
            if (inI2C.available() > 0) {
                inI2C.read(bytes, pos++, 1);
                i--;
                if (bytes[pos - 1] == (byte) '}') {
                    break;
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.AddToLog("*readI2CAnswer: " + ex.toString());
                }
            }
        }
        return pos;
    }

    public static byte[] PutCommand(String command) {
        byte[] result = null;
        try {
            CloseI2C();
            Thread.sleep(150);
            InitI2C();

            outI2C.write(command.getBytes());
            outI2C.flush();

            int pos = readI2CAnswer();
            if (pos == 0) {
                throw new IOException("null answer");
            }

            command = "<bA300FA>";
            outI2C.write(command.getBytes());
            outI2C.flush();

            pos = readI2CAnswer();
            if (pos == 0) {
                throw new IOException("null answer 2");
            }
            result = bytes;
        } catch (Exception ex) {
            Logger.AddToLog("Error.InternalController.PutCommand = " + ex.getMessage());
            result = null;
        }
        return result;
    }

    public static byte GetDisState() {
        byte result = 2;
        String command = "<aA26174246770696F313D3F0D>";     //   at$gpio1=?cr

        PutCommand(command);

        if (bytes[22] == 0x30) {
            result = 0;
        }
        if (bytes[22] == 0x31) {
            result = 1;
        }
        Logger.AddToLog("DIS STATE ", result);
        return result;
    }

    public static void SwitchSim() {
        Logger.AddToLog("Switch sim card.");
        if (currentSim == 1) {
            Logger.CreateLogFile("(" + Iec.ipAddress + Shh120.GetStatus() + " @ " + InDiscreteModule.GetStatus());
            currentSim = 2;
            SaveCurrentSimState();
            SetSim2();
        } else {
            currentSim = 1;            
            SaveCurrentSimState();
            SetSim1();
        }
    }

    public static void SetSim1() {
        Logger.AddToLog("Set SIM1");
        String command = "<aA261742473696d310D>";     //   at$sim1        
        PutCommand(command);
    }

    public static void SetSim2() {
        Logger.AddToLog("Set SIM2");
        String command = "<aA261742473696d320D>";     //   at$sim2        
        PutCommand(command);
    }

    public static void SetWD() {
        Logger.AddToLog("Set WD");
        String command = "<aA2617424636f6e74726f6c3d31300D>";     //   at$control=10        
        PutCommand(command);
    }

    public static void IamWork() {
        Logger.AddToLog("Set IamWork");
        String command = "<aA26174246a6176610D>";     //   at$java        
        PutCommand(command);
    }

}
