package iec104;

import java.util.Enumeration;
import java.util.Vector;

public class IecAPDU {

    public static int currentState = 0;         //отслеживание состояний
    // 0 - ничего нет
    // 1 - установлено соединение и произлшел обмен STARTDT.act & STARTDT.con
    // 2 - получена команда на ОБЩИЙ ОПРОС от станции управления
    // 3 - по общиему опросу выдана информацйия и управляющей станции была отправлен команда ЗАВЕРШЕНИЕ АКТИВАЦИИ
    // 4 - от управляющей станции получено уведомление о готовности примема данных - можно отправлять данные

    public static short nsCounter = 0;   //счетчик входящих пакетов
    public static short nrCounter = 0;  //счетчик исходящих пакетов

    public static Vector globalTransmitOrder = new Vector();  //очередь сообщений на отправку

    public byte[] data;
    public CtrlFormat ctrlFormat = null;
    public IecASDU iecASDU = null;

    public static void SetCurrentState(int newState) {
        currentState = newState;
    }

    public static Vector CreateAPDUFromInData(byte[] data) throws Exception {
        Logger.AddToLog("Input data:", data);

        Vector result = new Vector();
        try {
            int pos = 0;

            while (pos < data.length) {
                if (data[pos] == 0x68) {
                    //начало пакета верное - 0x68
                    byte len = data[pos + 1];

                    byte[] part = new byte[len + 2];
                    for (int i = 0; i <= len + 1; i++) {
                        part[i] = data[pos + i];
                    }
                    pos = pos + len + 2;

                    IecAPDU apdu = new IecAPDU();

                    apdu.CopyData(part);
                    apdu.ctrlFormat = CtrlFormat.GetCtrlFormat(apdu);

                    if (len == 4) {
                        //APDU короткий, не содержит ASDU
                        Logger.AddToLog("No ASDU");
                        apdu.iecASDU = null;
                    } else {
                        //ASDU есть
                        Logger.AddToLog("Yes ASDU");
                        apdu.iecASDU = new IecASDU(apdu);
                    }
                    result.addElement(apdu);
                } else {
                    throw new Exception("IecADPU.GetAPDU Error#1 (no 0x68 in first byte)");
                }
            }
        } catch (Exception ex) {
            Logger.AddToLog("Error. CreateAPDUFromInData[0]: " + ex.getMessage());
            result.removeAllElements();
        }
        return result;
    }

    public static IecAPDU AnswerStartdtCon() {
        IecAPDU result = new IecAPDU();
        result.iecASDU = null;
        result.ctrlFormat = CtrlFormatU.CreateStartdtCon();
        return result;
    }

    //подтверждение на запрос общего опроса
    public static IecAPDU AnswerTotalSurveyCommit() {
        IecAPDU result = new IecAPDU();
        result.iecASDU = IecASDU.CreateType100((byte) 7); //подтверждение активации
        result.ctrlFormat = CtrlFormatI.CreateAnswerPart();
        return result;
    }

    //текущее состояние объектов информации cause - причина передачи [1] стр. 26
    public static IecAPDU _AnswerAllElements11(byte cause) {
        IecAPDU result = new IecAPDU();
        result.iecASDU = IecASDU.AllInformationElements((byte) 11, cause);
        result.ctrlFormat = CtrlFormatI.CreateAnswerPart();
        return result;
    }

    //список текущих состояний объектов информации cause - причина передачи [1] стр. 26 для дискретных
    public static Vector AnswerVectorElements1(byte cause) {
        Vector result = new Vector();
        while (true) {
            IecAPDU apdu = new IecAPDU();
            apdu.iecASDU = IecASDU.GetNotProcessingInformationElements((byte) 1, cause);
            if (apdu.iecASDU != null) {
                apdu.ctrlFormat = CtrlFormatI.CreateAnswerPart();
                result.addElement(apdu);
            } else {
                break;
            }
        }

        
        
        
        return result;
    }

    //список текущих состояний объектов информации cause - причина передачи [1] стр. 26
    public static Vector AnswerVectorElements11(byte cause) {
        Vector result = new Vector();
//        result.addElement(AnswerAllElements11(cause));

        //IecASDU asdu = IecASDU.GetNotProcessingInformationElements((byte) 11, cause);   - убрать
        while (true) {
            IecAPDU apdu = new IecAPDU();
            apdu.iecASDU = IecASDU.GetNotProcessingInformationElements((byte) 11, cause);
            if (apdu.iecASDU != null) {
                apdu.ctrlFormat = CtrlFormatI.CreateAnswerPart();
                result.addElement(apdu);
            } else {
                break;
            }
        }

//        while (asdu != null) {
//            IecAPDU apdu = new IecAPDU();                       
//            apdu.iecASDU = asdu;                   
//            apdu.ctrlFormat = CtrlFormatI.CreateAnswerPart();
//            result.addElement(apdu);
//            asdu = IecASDU.GetNotProcessingInformationElements((byte) 11, cause);
//        }
        return result;
    }

    //текущее состояние объектов информации cause - причина передачи [1] стр. 26
    public static IecAPDU AnswerAllElements1(byte cause) {
        IecAPDU result = new IecAPDU();
        result.iecASDU = IecASDU.AllInformationElements((byte) 1, cause);
        result.ctrlFormat = CtrlFormatI.CreateAnswerPart();
        return result;
    }

    //завершение передачи состояния объектов
    public static IecAPDU AnswerTotalSurveyComplit() {
        IecAPDU result = new IecAPDU();
        result.iecASDU = IecASDU.CreateType100((byte) 10); //завершение активации
        result.ctrlFormat = CtrlFormatI.CreateAnswerPart();
        return result;
    }

    public byte[] GetBytes() {
        byte[] result = null;
        try {
            result = Global.MergeArray(new byte[]{0x68, 0x0}, ctrlFormat.GetAsBytes());

            if (iecASDU != null) {
                result = Global.MergeArray(result, iecASDU.GetAsBytes());
            }
            result[1] = (byte) (result.length - 2);
        } catch (Exception ex) {
            Logger.AddToLog("IecAPDU.GetBytes(): " + ex.getMessage());
        }
        return result;
    }

    public void InfoToLog(String info) {
        Logger.AddToLog("*****APDU** " + info + "************************");
        if (data != null) {
            Logger.AddToLog("*   Data ", data);
        }
        if (ctrlFormat != null) {
            Logger.AddToLog("*   CtrlFormat " + ctrlFormat.toString());
        } else {
            Logger.AddToLog("*   CtrlFormat NULL");
        }
        if (iecASDU != null) {
            //iecASDU.InfoToLog();
        } else {
            Logger.AddToLog("*   IecASDU NULL");
        }
        byte[] recover = GetBytes();
        if (recover != null) {
            Logger.AddToLog("*   Recover-", recover);
        } else {
            Logger.AddToLog("*   No recover");
        }
        Logger.AddToLog("*************************************************");
    }

    public void CopyData(byte[] d) {
        data = new byte[d.length];
        for (int i = 0; i < d.length; i++) {
            data[i] = d[i];
        }
    }
}
