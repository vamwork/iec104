package iec104;

//обработчик входящих пакетов
import java.util.Enumeration;
import java.util.Vector;

public class IecProcessor {

    public static void Parse(IecAPDU apdu) {
        Logger.AddToLog("Parse...");
        if (apdu.ctrlFormat instanceof CtrlFormatI) {
            //поле управления формата передачи информации I  
            if (apdu.iecASDU.typeIdentifier == 100) {
                //команда опроса                               
                InformationObject obj = (InformationObject) apdu.iecASDU.informationObjects.elementAt(0);

                if (obj.informationElement instanceof ElementType100) {
                    //элемент информации 100
                    ElementType100 elm = (ElementType100) obj.informationElement;

                    if (elm.qoi == 0x14) {
                        //общий опрос станции
                        Logger.AddToLog("CtrlFormatI - obchi opros stancii");

                        IecAPDU.SetCurrentState(2);

                        IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerTotalSurveyCommit());     //подтверждение активации

                        Logger.AddToLog("Gotovim otvet na obchi opros stancii");
                        
                        //отметим все объекты информации, как не отправленные в общем опросе                       
                        for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
                            InformationObject o = (InformationObject) e.nextElement();
                            o.isProcessing = false;
                        }
                                                                     
                        Vector elements = IecAPDU.AnswerVectorElements11((byte) 6);    //возвращает набор APDU
                                                
                        Logger.AddToLog("Otvet na obchi opros gotov. Vsego cformirovano APDU ", elements.size());
                        
                        for (int i = 0; i < elements.size(); i++) {
                            IecAPDU.globalTransmitOrder.addElement(elements.elementAt(i));
                        }

                        //IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerAllElements11((byte) 6));
                        IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerTotalSurveyComplit());     //завершение активации
                        IecAPDU.SetCurrentState(3);
                    }
                }
            }
        }

        if (apdu.ctrlFormat instanceof CtrlFormatS) {
            //поле управления формата функции контроля с нумерацией S            
            Logger.AddToLog("CtrlFormatS");
            IecAPDU.SetCurrentState(4);
        }

        if (apdu.ctrlFormat instanceof CtrlFormatU) {
            //поле управления формата функции управления без нумерации U            
            switch (apdu.ctrlFormat.byte_1) {
                case 0x7:
                    //STARTDT.act                    
                    Logger.AddToLog("CtrlFormatU - STARTDT.act");

                    IecAPDU.globalTransmitOrder.addElement(IecAPDU.AnswerStartdtCon());
                    IecAPDU.SetCurrentState(1);
                    break;
                default:
                    Logger.AddToLog("CtrlFormatU - NO IDENTIFIER");
                    break;
            }
        }

    }
}
