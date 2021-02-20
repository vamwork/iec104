package iec104;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class TaskManager extends TimerTask {

    public AtListener atc;

    public static Timer mainTimer = new Timer();

    public void StartMainTimer() {
        mainTimer.schedule(this, 5000, 5000);
    }

    public void run() {
        Iec.waitCounter++;
        if (Iec.waitCounter > 50) {
            try {
                Iec.waitCounter = 0;
                IecAPDU.currentState = 0;
                Logger.AddToLog("Start STOP procedure.");
                Logger.AddToLog("Close 1a");
                Iec.is.close();
                Logger.AddToLog("Close 2a");
                Iec.os.close();
                Logger.AddToLog("Close 3a");
                Iec.sc.close();
                Logger.AddToLog("Close 4a");
                Iec.scn.close();
                Logger.AddToLog("Success STOP procedure.");
            } catch (Exception ex) {
                Logger.AddToLog("TaskManager.StartMainTimer: " + ex.getMessage());
            }
        }
        Logger.IamWork("t");

        atc.switching = false;

        Heap.ReadDIChannels();

        Heap.ReadAnalogChannels();
        //Heap.SetRandomValues();

        // Для тестирования
        //Heap.SetRandomValues();
        if (IecAPDU.currentState == 4) {
            if (IecAPDU.globalTransmitOrder.size() == 0) {
                //передающая станция находится в сосотянии передачи данных и исходящая очередь пуста                                
                //IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerAllElements11((byte) 3));                             

                //отметим не все объекты информации, как не отправленные в общем опросе                       
                for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
                    InformationObject o = (InformationObject) e.nextElement();
//      почему-то так не работет (
//                    if (o.informationElement.toTransmit) {
//                        o.isProcessing = false;
//                    }
                    o.isProcessing = false;

                    //сформируем текст
                    Logger.AddToLog(o.GetAsString());
                }

                Vector elements = IecAPDU.AnswerVectorElements11((byte) 3);    //возвращает набор APDU
                for (int i = 0; i < elements.size(); i++) {                                        
                    IecAPDU.globalTransmitOrder.addElement(elements.elementAt(i));
                }

                Vector disElements = IecAPDU.AnswerVectorElements1((byte) 3);  //возвращает набор APDU для дискретных
                for (int i = 0; i < elements.size(); i++) {                                        
                    IecAPDU.globalTransmitOrder.addElement(disElements.elementAt(i));
                }
                
                
//                IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerAllElements1((byte) 3));
//                if (Controller.isSim1) {
//                    InternalControll.IamWork();
//                }
            } else {
                //Iec.TransmitOrder();
            }
        }
    }
}
