package iec104;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class Shh120 {

    public static int[] channels = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 30};   //список каналов, адресов, которые участвуют в передаче

    public static Vector shh120List = new Vector(); // список ЩП-120
    public byte addr = 0;   //сетевой адрес
    public int objAddress = 0;  //адрес объекта информации
    public ElementType11[] elements = new ElementType11[52];
    public int accessStatus = 0;   //статус доступа

    public static Shh120 AddShh120(byte addr, int objAddr) {
        Shh120 result = new Shh120();
        result.addr = addr;
        result.objAddress = objAddr;
        shh120List.addElement(result);

        //добавим  ТС с адреса objAddr
        for (int i = 0; i < channels.length; i++) {
//            InformationObject obj;
//            obj = new InformationObject();
//            obj.address = 8192;
//            ElementType11 et11 = new ElementType11();
//            et11.value = 1233;
//            obj.informationElement = et11;
//            Heap.objects.addElement(obj);                     
            InformationObject obj = new InformationObject();
            obj.address = result.objAddress + i;
            ElementType11 et11 = new ElementType11();
            //et11.value = 0;
            //et11.quality = 0x40;                 //bad quality
            et11.SetBadQuality();

            obj.informationElement = et11;
            Heap.objects.addElement(obj);
            result.elements[i] = et11;
        }
        return result;
    }

    public static void SaveConfig() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "Shh120.cnf");

            if (!fConn.exists()) {
                fConn.create();
            }

            OutputStream os = fConn.openOutputStream(0);

            for (Enumeration e = shh120List.elements(); e.hasMoreElements();) {
                Shh120 d = (Shh120) e.nextElement();
                os.write(d.addr);
                //добавить запись адреса объекта информации
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.AddToLog("*Merc200[0004]" + ex.toString());
        }
    }

    public static void LoadConfig() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "Shh120.cnf");

            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    Shh120 d = new Shh120();
                    d.addr = (byte) is.read();
                    //добавить чтение адреса объекта информации
                    shh120List.addElement(d);
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.AddToLog("*Mercury200[0005]" + ex.toString());
        }
    }

    public static int[] ParseAnswer(byte[] fullAnswer) {
        int[] result = null;
        try {
            int count = fullAnswer[2] >> 1;
            result = new int[count];

            for (int i = 0; i < count; i++) {
                result[i] = Global.BytesToInt(fullAnswer[3 + i * 2], fullAnswer[4 + i * 2]);

                //String s = "Analog " + Integer.toString(i) + ": " + Integer.toString(result[i]) + " [" + Logger.ByteToString(fullAnswer[3 + i * 2]) + "," + Logger.ByteToString(fullAnswer[4 + i * 2]) + "]";
                //Logger.AddToLog(s);
            }

        } catch (Exception ex) {
            Logger.AddToLog("Shh120.ParseAnswer: " + ex.toString());
        }
        return result;
    }

    public static String GetStatus() {
        String result = ")";
        for (Enumeration e = Shh120.shh120List.elements(); e.hasMoreElements();) {
            Shh120 anl = (Shh120) e.nextElement();
            result = result + Integer.toString(anl.addr) + "=" + Integer.toString(anl.accessStatus) + ";";
        }
        return result;
    }
}
