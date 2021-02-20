package iec104;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

//хранилище объектов информации
public class Heap {

    public static Random random = new Random();

    public static Vector objects = new Vector();   //for InformationObject

    //Randomize
    public static void SetRandomValues() {
        for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            if (obj.informationElement instanceof ElementType11) {
                ElementType11 elm = (ElementType11) obj.informationElement;
                elm.SetNewValue((short) random.nextInt());
                //elm.value = (short) random.nextInt();
                //elm.toTransmit = true;
                //elm.quality = 0;
            }
//            if (obj.informationElement instanceof ElementType1) {
//                ElementType1 elm = (ElementType1) obj.informationElement;
//                if ((short) random.nextInt() > 0) {
//                    elm.value = 1;
//                } else {
//                    elm.value = 0;
//                }
//            }
        }
    }

    public static void ReadAnalogChannels() {
        ComPort cp = new ComPort(ComPort.conStrPar2);
        try {
            for (Enumeration e = Shh120.shh120List.elements(); e.hasMoreElements();) {
                Shh120 anl = (Shh120) e.nextElement();
                byte[] cmd = new byte[]{0x03, 0x01, 0x65, 0x0, 0x35};    //без адреса и crc
                byte[] fullCommand = Global.MakeCommand(cmd, anl.addr);

                byte[] answer = cp.Request(fullCommand, 250);

                if ((answer.length > 3) && (Global.CheckCRC(answer))) {
                    //ответ коррктный                                        
                    Logger.IamWork("#1");

                    anl.accessStatus++;

                    int[] data = Shh120.ParseAnswer(answer);
                    if (data != null) {
                        for (int i = 0; i < Shh120.channels.length; i++) {
                            ElementType11 elm = (ElementType11) anl.elements[i];
                            elm.SetNewValue((short) data[Shh120.channels[i]]);
                            //elm.quality = 0;
                        }
                    }

//                    for (int i = 0; i < 3; i++) {
//                        ElementType11 elm = (ElementType11) anl.elements[i];
//                        elm.value = (short) i;
//                        elm.toTransmit = true;
//                        elm.quality = 0;
//                    }
                } else {
                    //ответ не корректный, или его нет                    
                    Logger.IamWork("#2");
                    for (int i = 0; i < Shh120.channels.length; i++) {
                        ElementType11 elm = (ElementType11) anl.elements[i];
                        //elm.value = (short) random.nextInt();
                        //elm.value = 0;
                        //elm.toTransmit = true;
                        //elm.quality = 0x40;
                    }

//                    for (int i = 0; i < 10; i++) {
//                        ElementType11 elm = (ElementType11) anl.elements[i];
//                        elm.value = (short) random.nextInt();
//                        elm.toTransmit = true;
//                        elm.quality = 0;
//                    }
                }
            }
        } catch (Exception ex) {
            Logger.AddToLog("Error. ReadAnalogChannels " + ex.getMessage());
        } finally {
            cp.CloseComPort();
        }
    }

    public static void ReadDIChannels() {
        ComPort cp = new ComPort(ComPort.conStrPar1);
        try {
            for (Enumeration e = InDiscreteModule.InDiscreteModuleList.elements(); e.hasMoreElements();) {
                InDiscreteModule discr = (InDiscreteModule) e.nextElement();

                // составим полный текст команды
                String addr = Integer.toHexString(discr.netAddr).toUpperCase();
                if (addr.length() < 2) {
                    addr = "0" + addr;
                }
                String command = "@" + addr + "\r";

                byte[] cmd = new byte[command.length()];
                for (int j = 0; j < cmd.length; j++) {
                    cmd[j] = (byte) command.charAt(j);
                }

                Logger.AddToLog("DI command:", cmd);

                byte[] data = cp.Request(cmd, 250);

                Logger.AddToLog("DI ans:", data);

                if (data.length == 6) {
                    discr.answerData = Global.ByteArrayToString(data);

                    byte[] channels = InDiscreteModule.GetChannels(data);

                    for (int i = 0; i < 16; i++) {
                        ElementType1 et1 = discr.elements[i];
                        et1.SetNewValue(channels[15 - i]);
                    }
                } else {
                    //ответ не тот
                    for (int i = 0; i < 16; i++) {
                        ElementType1 et1 = discr.elements[i];
                        et1.quality = 0x00;
//                        //Для теста
//                        byte v = 0;
//                        if (random.nextInt() > 5000) {
//                            v = 1;
//                        }
//                        et1.SetNewValue(v);
                    }
                }
            }
        } finally {
            cp.CloseComPort();
        }
    }

    //возвращает количество объектов информации с типом tp
    public static byte CountTypes(byte tp) {
        byte result = 0;

        for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            if (obj.informationElement.elementType == tp) {
                result++;
            }
        }

        return result;
    }
}
