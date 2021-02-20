package iec104;

import com.cinterion.io.ATCommand;
import javax.microedition.midlet.*;

public class IMlet extends MIDlet {

    public AtListener atListener;

    public void startApp() {
        try {
            try {

                String ans;
                atListener = new AtListener();
                //Только GSM 2G
                //ans = atListener.SendAtCommand("at^sxrat=0\r");
                //Logger.AddToLog("2G ANSWER:" + ans);

                SmsService.atListener = atListener;
                ans = atListener.SendAtCommand("AT+CMGF=1\r");

                // отключить режим ЭХО
                ans = atListener.SendAtCommand("ATE0\r");
                // для того, что бы работал ats0=n, необходимо отключить привязку
                // текстовый режим SMS
                ans = atListener.SendAtCommand("AT+CMGF=1\r");

                ans = atListener.SendAtCommand("AT+CBST=71,0,1\r");
                Logger.AddToLog("AT+CBST=71,0,1 answr:" + ans);

                // брать трубку после первого звонка
                ans = atListener.SendAtCommand("ATS0=1\r");
                Logger.AddToLog("ATS0=1:" + ans);

            } catch (Exception e) {
                Logger.AddToLog("Imlet.startApp[0]: " + e.getMessage());
            }

            Logger.AddToLog("------------------------------------------------");
            Logger.AddToLog("Start application (" + Controller.version + ").");
            Logger.AddToLog("Free memory: " + String.valueOf(Controller.runtime.freeMemory() / 1024) + " Kbyte");
            Logger.AddToLog("Total memory: " + String.valueOf(Controller.runtime.totalMemory() / 1024) + " Kbyte");
            Logger.AddToLog("------------------------------------------------");

            //Logger.ReadLogFile();
            InternalControll.InitI2C();
            Thread.sleep(2000);
            InternalControll.SetWD();

            InternalControll.ReadCurrentSimState();

            //InternalControll.SetSim1();
            //Стенд-------------------------------------------------------------
//            Shh120.AddShh120((byte) 1, 8192);
//            Shh120.AddShh120((byte) 1, 8202);
//            InDiscreteModule.AddDiscreteModule((byte) 1, 4096);
            //Лебедино----------------------------------------------------------
//            Shh120.AddShh120((byte) 23, 8192);
//            Shh120.AddShh120((byte) 25, 8202);
//            Shh120.AddShh120((byte) 43, 8212);
//            Shh120.AddShh120((byte) 69, 8222);
//            Shh120.AddShh120((byte) 43, 8232);
////            Shh120.AddShh120((byte) 89, 8242);
////            Shh120.AddShh120((byte) 25, 8252);
////            Shh120.AddShh120((byte) 23, 8262);
////            Shh120.AddShh120((byte) 70, 8272);
////            Shh120.AddShh120((byte) 42, 8282);
////            Shh120.AddShh120((byte) 50, 8292);
////            Shh120.AddShh120((byte) 63, 8302);
////            Shh120.AddShh120((byte) 73, 8312);
////            Shh120.AddShh120((byte) 53, 8322);
////            Shh120.AddShh120((byte) 37, 8332);
////            Shh120.AddShh120((byte) 62, 8342);
//                        
//            InDiscreteModule.AddDiscreteModule((byte) 16, 4096);            
            //------------------------------------------------------------------    
            //Черемухово--------------------------------------------------------            
//            Shh120.AddShh120((byte) 9, 8192);
//            Shh120.AddShh120((byte) 11, 8202);
//            Shh120.AddShh120((byte) 10, 8212);
//            InDiscreteModule.AddDiscreteModule((byte) 6, 4096);
            //------------------------------------------------------------------                          
            //Кузнечиха--------------------------------------------------------            
            Shh120.AddShh120((byte) 1, 8192);
            Shh120.AddShh120((byte) 2, 8202);
            Shh120.AddShh120((byte) 3, 8212);
            Shh120.AddShh120((byte) 4, 8222);
            Shh120.AddShh120((byte) 6, 8232);
            Shh120.AddShh120((byte) 8, 8242);
                        
            InDiscreteModule.AddDiscreteModule((byte) 1, 4096);
            InDiscreteModule.AddDiscreteModule((byte) 3, 4112);
            InDiscreteModule.AddDiscreteModule((byte) 9, 4128);
            //------------------------------------------------------------------                          
//            InformationObject obj;
//            obj = new InformationObject();
//            obj.address = 8192;
//            ElementType11 et11 = new ElementType11();
//            et11.value = 1233;
//            obj.informationElement = et11;
//            Heap.objects.addElement(obj);
//
//            obj = new InformationObject();
//            obj.address = 8193;
//            et11 = new ElementType11();
//            et11.value = 3233;
//            obj.informationElement = et11;
//            Heap.objects.addElement(obj);
//
//            obj = new InformationObject();
//            obj.address = 8194;
//            et11 = new ElementType11();
//            et11.value = 3243;
//            obj.informationElement = et11;
//            Heap.objects.addElement(obj);
            //дискретные Type1 - 7.3.1.1
//            obj = new InformationObject();
//            obj.address = 4096;
//            ElementType1 et1 = new ElementType1();
//            et1.value = 1;
//            obj.informationElement = et1;
//            Heap.objects.addElement(obj);
//            
//            obj = new InformationObject();
//            obj.address = 4097;
//            et1 = new ElementType1();
//            et1.value = 1;
//            obj.informationElement = et1;
//            Heap.objects.addElement(obj);
//            IecASDU iecASDU = IecASDU.AllInformatuionElements((byte) 11);
//            IecAPDU apdu = IecAPDU.CreateStartdtCon();
//            apdu.InfoToLog();
            TaskManager taskManager = new TaskManager();
            taskManager.atc = atListener;
            taskManager.StartMainTimer();

            while (true) {
                try {
                    Iec.SocketListen();
                    Thread.sleep(10000);
                    Logger.IamWork(".");
                } catch (Exception ex) {
                    Logger.AddToLog("Imlet.startApp[1]: " + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            Logger.AddToLog("Imlet.startApp[2]: " + ex.getMessage());
        } finally {
            Logger.AddToLog("Imlet.Finally");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Destroy App");
        notifyDestroyed();
    }
}
