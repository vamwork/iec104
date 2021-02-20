package iec104;

import java.util.Enumeration;
import java.util.Vector;

public class IecASDU {

    byte typeIdentifier;        //идентификатор типа [1] 7.2.1
    byte informationStructure;  //классификатор переменной структуры [1] 7.2.2
    byte causeOfTransmission;   //причина передачи [1] 7.2.3
    short asduAddress;            //общий адрес ASDU [1] 7.2.4

    Vector informationObjects;   //объекты информации [1] стр.28

    public IecAPDU iecAPDU;

    public static IecASDU CreateType100(byte cause) {
        IecASDU result = new IecASDU();
        result.typeIdentifier = 100;
        result.informationStructure = 1;
        result.causeOfTransmission = cause;
        result.asduAddress = 1;
        result.informationObjects = new Vector();
        InformationObject obj = new InformationObject();
        obj.address = 0;
        ElementType100 elm = new ElementType100();
        elm.qoi = 0x14;   //20 опрос станции общий
        obj.informationElement = elm;
        result.informationObjects.addElement(obj);
        return result;
    }

    //формиует ответ формата ответа на опрос станции для typeIdnt для объекта информации infObj
    public static IecASDU ForOneInformationElement(InformationObject infObj, byte typeIdnt, byte cause) {
        IecASDU asdu = new IecASDU();
        asdu.typeIdentifier = typeIdnt;      //приведены [1] стр.19 (пока только для одного типа)
        asdu.informationStructure = (byte) Heap.CountTypes(typeIdnt);  // классификатор переменной структуры [1] 7.2.2
        asdu.causeOfTransmission = cause;   //активация act [1] стр.26
        asdu.asduAddress = 1;
        asdu.informationObjects = new Vector();
        asdu.informationObjects.addElement(infObj);
        return asdu;
    }

    //формируется в ответ на полный опрос станции для typeIdnt
    public static IecASDU AllInformationElements(byte typeIdnt, byte cause) {
        IecASDU asdu = new IecASDU();
        asdu.typeIdentifier = typeIdnt;      //приведены [1] стр.19 (пока только для одного типа)
        asdu.informationStructure = (byte) Heap.CountTypes(typeIdnt);  // классификатор переменной структуры [1] 7.2.2
        asdu.causeOfTransmission = cause;   //активация act [1] стр.26
        asdu.asduAddress = 1;
        asdu.informationObjects = new Vector();

        for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            if (obj.informationElement.elementType == typeIdnt) {
                asdu.informationObjects.addElement(obj);
            }
        }
        return asdu;
    }

    //формируется в ответ на полный опрос станции для typeIdnt, не обработанных ранее (взамен AllInformationElements)
    //если все обработаны, возвращает null
    public static IecASDU GetNotProcessingInformationElements(byte typeIdnt, byte cause) {
        IecASDU asdu = null;
        //посчитаем, сколько есть не обработанных объектов информации нужного типа
        int cnt = 0;
        for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            if (obj.informationElement.elementType == typeIdnt) {
                if (!obj.isProcessing) {
                    cnt++;
                }
            }
        }

        if (cnt > 0) {
            //если не обработанные есть    
            asdu = new IecASDU();
            asdu.typeIdentifier = typeIdnt;      //приведены [1] стр.19 (пока только для одного типа)
            asdu.informationStructure = (byte) Heap.CountTypes(typeIdnt);  // классификатор переменной структуры [1] 7.2.2
            asdu.causeOfTransmission = cause;   //активация act [1] стр.26
            asdu.asduAddress = 1;
            asdu.informationObjects = new Vector();
            cnt = 0;
            for (Enumeration e = Heap.objects.elements(); e.hasMoreElements();) {
                InformationObject obj = (InformationObject) e.nextElement();
                if (obj.informationElement.elementType == typeIdnt) {
                    if (cnt < 200) {
                        if (!obj.isProcessing) {
                            asdu.informationObjects.addElement(obj);
                            obj.isProcessing = true;
                            cnt++;
                        }
                    }
                }
            }
        }
        return asdu;
    }

    public byte[] GetAsBytes() {
        //определим размер
        int size = 6;
        for (Enumeration e = informationObjects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            size = size + obj.Size();  //определеям общий размер всех объектов информации            
        }
        byte[] result = new byte[size];
        //заполненение
        result[0] = typeIdentifier;
        result[1] = informationStructure;
        result[2] = causeOfTransmission;

        result[3] = 0;  //test or notest [1] 7.2.3.1
        result[4] = (byte) asduAddress;

        int i = 6;
        for (Enumeration e = informationObjects.elements(); e.hasMoreElements();) {
            InformationObject obj = (InformationObject) e.nextElement();
            i = obj.AddToByteArray(i, result);
        }
        return result;
    }

    public IecASDU() {
    }

    public IecASDU(IecAPDU iecAPDU) {
        this.iecAPDU = iecAPDU;
        typeIdentifier = iecAPDU.data[6];       //идентификатор типа [1] 7.2.1            
        informationStructure = iecAPDU.data[7]; //классификатор переменной структуры [1] 7.2.2
        causeOfTransmission = iecAPDU.data[8];  //причина передачи [1] 7.2.3
        asduAddress = 1;                        //пока игнорируем;  //общий адрес ASDU [1] 7.2.4

        if (typeIdentifier == 100) {
            informationObjects = new Vector();
            InformationObject infObj = new InformationObject();
            infObj.address = 0;
            ElementType100 elm = new ElementType100();
            infObj.informationElement = elm;
            elm.qoi = iecAPDU.data[15];
            informationObjects.addElement(infObj);
        }
    }
}
