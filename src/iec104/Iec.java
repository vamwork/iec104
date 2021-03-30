package iec104;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

public class Iec {

    public static int waitCounter = 0;

    public static int _tstCount = 0;

    public static String ipAddress;

    //Мой Билайн
    //private static String apn = "static.beeline.ru";
    //ЧЭС Мегафон
    private static String apn = "CETEVAY.VOLGA";
    //ЧЭС МТС
    //private static String apn = "KES.VOLGA";

    public static ServerSocketConnection scn;
    public static SocketConnection sc;
    public static DataInputStream is;
    public static DataOutputStream os;

    public static void ReOpenStreams() {
        try {
            Thread.sleep(250);
        } catch (Exception ex) {
            Logger.AddToLog("Iec.ReOpenStreams(): " + ex.getMessage());
        }
    }

    public static void TransmitOrder() throws Exception {
        try {
            //while (IecAPDU.globalTransmitOrder.size() > 0) {
            if (IecAPDU.globalTransmitOrder.size() > 0) {
                Thread.sleep(500);

                IecAPDU apdu = (IecAPDU) IecAPDU.globalTransmitOrder.elementAt(0);

                apdu.InfoToLog("to send");

                byte[] send = apdu.GetBytes();
                os.write(send);
                waitCounter = 0;
                IecAPDU.globalTransmitOrder.removeElementAt(0);
                Thread.sleep(250);
                Logger.AddToLog("Send data:", send);

                Thread.sleep(1000);
                os.flush();

            }
        } catch (Exception ex) {
            Logger.AddToLog("Iec.TransmitOrder(): " + ex.getMessage());
            throw ex;
        }
    }

    public static boolean SocketListen() {
        boolean result = false;

        try {
            StringBuffer buf = new StringBuffer();
            buf.append("socket://:2404");
            buf.append(";bearer_type=gprs");
            buf.append(";access_point=");
            buf.append(apn);
            //buf.append(";username=");
            //buf.append(user);
            //buf.append(";password=");
            //buf.append(password);

            String connString = buf.toString();
            Logger.AddToLog("Connection string " + connString);
            try {
                scn = (ServerSocketConnection) Connector.open(connString);
                Logger.AddToLog("IP " + scn.getLocalAddress());
                //Logger.AddToLogFile("IP " + scn.getLocalAddress());

                if (InternalControll.currentSim == 1) {
                    Logger.AddToLog("Current SIM is 1");
                    ipAddress = scn.getLocalAddress();
                } else {
                    SmsService.SendSms("+79033061297", "Start " + Controller.version);         
                    String log = Logger.ReadLogFile();
                    SmsService.SendSms("+79033061297", log);
                }

                boolean toWork = true;

                sc = (SocketConnection) scn.acceptAndOpen();

//                sc.setSocketOption(SocketConnection.KEEPALIVE, 0);
//                sc.setSocketOption(SocketConnection.DELAY, 5);
//                sc.setSocketOption(SocketConnection.RCVBUF, 10);
//                sc.setSocketOption(SocketConnection.SNDBUF, 10);
//                sc.setSocketOption(SocketConnection.LINGER, 5);
                is = sc.openDataInputStream();
                os = sc.openDataOutputStream();

                int cnt = 0;

                IecAPDU.globalTransmitOrder.removeAllElements();   //очистим очередь отправки

                while (cnt < 1000) {
                    Logger.IamWork("+");
                    cnt++;
                    waitCounter = 0;

                    Thread.sleep(1000);

                    TransmitOrder();
                    Thread.sleep(1000);

                    int len = is.available();

                    if (len > 0) {
                        cnt = 0;
                        byte[] data = new byte[len];
                        is.read(data);

                        Logger.AddToLog("data = ", data);
                        Vector parts = IecAPDU.CreateAPDUFromInData(data);

                        for (int i = 0; i < parts.size(); i++) {
                            IecAPDU iecAPDU = (IecAPDU) parts.elementAt(i);
                            iecAPDU.InfoToLog("in");
                            IecProcessor.Parse(iecAPDU);

                            Thread.sleep(250);

                            TransmitOrder();
//                            while (IecAPDU.globalTransmitOrder.size() > 0) {
//                                Thread.sleep(500);
//                                Logger.AddToLog("transmit order size=", IecAPDU.globalTransmitOrder.size());
//
//                                IecAPDU apdu = (IecAPDU) IecAPDU.globalTransmitOrder.elementAt(0);
//                                apdu.InfoToLog("to send");
//                                byte[] send = apdu.GetBytes();
//                                os.write(send);
//                                waitCounter = 0;
//                                IecAPDU.globalTransmitOrder.removeElementAt(0);
//                                Thread.sleep(250);
//                                Logger.AddToLog("Send data:", send);
//                            }
                        }

                        Logger.AddToLog("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

                        //IecAPDU iecAPDU = IecAPDU.CreateAPDUFromInData(data);
                        //iecAPDU.InfoToLog();
                        //IecProcessor.Parse(iecAPDU);
                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                Logger.AddToLog("Iec.SocketListen[1]: " + e.getMessage());
            } finally {
                IecAPDU.nsCounter = 0;
                IecAPDU.nrCounter = 0;
                IecAPDU.currentState = 0;

                Logger.AddToLog("Close 1");
                //Thread.sleep(10000);
                is.close();
                is = null;
                Logger.AddToLog("Close 2");
                //Thread.sleep(10000);
                os.close();
                os = null;
                Logger.AddToLog("Close 3");
                //Thread.sleep(10000);
                sc.close();
                sc = null;
                Logger.AddToLog("Close 4");
                //Thread.sleep(10000);
                scn.close();
                scn = null;
                //Thread.sleep(10000);
                Logger.AddToLog("All closing.");
            }
        } catch (Exception ex) {
            Logger.AddToLog("Iec.SocketListen[0]: " + ex.getMessage());
            //Logger.AddToLogFile("Iec.SocketListen[0]: " + ex.getMessage());
            result = false;
            IecAPDU.nsCounter = 0;
            IecAPDU.nrCounter = 0;
        }
        return result;
    }
}
