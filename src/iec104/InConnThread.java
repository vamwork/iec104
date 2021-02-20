package iec104;

import com.cinterion.io.ATCommand;
import java.io.InputStream;
import java.io.OutputStream;

public class InConnThread extends Thread {

    private int waitMsec = 250; // время "ожидания" пакета
    private static final long GSM_WAIT = 30000; // время неактивности канала GSM    
    private InputStream dataIn;
    private OutputStream dataOut;
    public ATCommand atc;

    public InConnThread(ATCommand a) {
        atc = a;
    }

    public void run() {
        try {
            dataIn = atc.getDataInputStream();
            dataOut = atc.getDataOutputStream();

            //ControlObject.dataOut = dataOut; // пусть ControlObject "знает" поток вывода
            long lastActivityTime = System.currentTimeMillis(); // время последней активности

            while (true) {
                // попытаемся получить пакет. Ошраничение по временни неактивности                        
                // подготовим буфер и т.д.
                StringBuffer strPackage = new StringBuffer();
                strPackage.delete(0, strPackage.length());
                long firstMsec = System.currentTimeMillis();
                int c = -1;

                if (dataIn.available() > 0) {
                    while (firstMsec + waitMsec > System.currentTimeMillis()) {
                        try {
                            if (dataIn.available() > 0) {
                                c = dataIn.read();
                                if (c >= 0) {
                                    strPackage.append((char) c);
                                    // данные пришли - отодвинем метку времени                                            
                                    firstMsec = System.currentTimeMillis();
                                }
                            }
                        } catch (Exception e) {
                            Logger.AddToLog("*InConnThread[0008]" + e.toString());
                        }
                    }
                }
                if (strPackage.length() > 0) {
                    // получен пакет
                    byte[] buffer = strPackage.toString().getBytes();

                    lastActivityTime = System.currentTimeMillis(); // передвинем время последней активности

                    Logger.AddToLog("CSD buffer ", buffer);

                    if ((buffer.length > 2) && (Global.CheckCRC(buffer))) {
                        if (buffer[0] == 1) {
                            InternalControll.SetSim1();
                        }
                        if (buffer[0] == 2) {
                            InternalControll.SetSim2();
                        }
                        if (buffer[0] == 3) {
                             dataOut.write(Controller.version.getBytes());
                        }
                        
                        if (buffer[0] == 4) {
                             dataOut.write(Logger.GetLogDataAsBytes());
                        }
                        
                    }
                }
                if ((lastActivityTime + GSM_WAIT) < System.currentTimeMillis()) {
                    // канал долго был в неактивном состоянии
                    dataIn.close();
                    dataOut.close();
                    try {
                        atc.breakConnection();
                        atc.send("ATH\r");
                    } catch (Exception e) {
                        Logger.AddToLog("*InConnThread[0005]" + e.toString());
                    }
                }

            }
        } catch (Exception e) {

            Logger.AddToLog("*InConnThread[0007]" + e.toString());
        } finally {
            // Разблокируем выполнение остальных задач               
            try {
                if (dataIn != null) {
                    dataIn.close();
                }
                if (dataOut != null) {
                    dataOut.close();
                }

                atc.breakConnection();
                atc.send("ATH\r");

            } catch (Exception ex) {
                Logger.AddToLog("*InConnThread[0006]" + ex.toString());
            }
        }
    }

}
