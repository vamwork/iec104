package iec104;

import java.util.Enumeration;
import java.util.Vector;

public class InDiscreteModule {

    public static Vector InDiscreteModuleList = new Vector();
    public byte netAddr;
    public int objAddress = 0;  //адрес объекта информации
    public ElementType1[] elements = new ElementType1[16];
    public String answerData = "err";

    public static InDiscreteModule AddDiscreteModule(byte addr, int objAddr) {
        InDiscreteModule result = new InDiscreteModule();
        result.netAddr = addr;
        result.objAddress = objAddr;
        InDiscreteModuleList.addElement(result);

        //добавим 16 ТИ с адреса objAddr
        for (int i = 0; i < 16; i++) {
            InformationObject obj = new InformationObject();
            obj.address = result.objAddress + i;
            ElementType1 et1 = new ElementType1();
            et1.value = 0;
            et1.quality = 0x40;                 //bad quality
            obj.informationElement = et1;
            Heap.objects.addElement(obj);
            result.elements[i] = et1;
        }
        return result;
    }

    public static byte[] GetChannels(byte[] resp) {
        byte[] result = new byte[16];
        String data = "" + (char) resp[1] + (char) resp[2] + (char) resp[3] + (char) resp[4];
        int c = Integer.parseInt(data, 16);

        String bnr = Integer.toBinaryString(c);
        while (bnr.length() < 16) {
            bnr = "0" + bnr;
        }

        for (int i = 0; i < 16; i++) {
            if (bnr.charAt(i) == '1') {
                result[i] = 1;
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    public static String GetStatus() {
        String result = " ";
        for (Enumeration e = InDiscreteModule.InDiscreteModuleList.elements(); e.hasMoreElements();) {
            InDiscreteModule disc = (InDiscreteModule) e.nextElement();
            result = result + Integer.toString(disc.netAddr) + "=" + disc.answerData + ";";
        }
        return result;
    }
}
