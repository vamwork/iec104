package iec104;

//объект информации [1] стр.17, стр.28
public class InformationObject {

    int address;   //адрес объекта информации, 3 байта [1] стр.28    
    InfromationElement informationElement;
    boolean isProcessing = false;                           //используется при подготовке ответа на общий опрос станции
    
    public byte Size() {
        return (byte) (3 + informationElement.Size());
    }

    public int AddToByteArray(int startPos, byte[] data) {
        int p = startPos;
        byte[] addr = Global.IntToBytes(address);
        data[p++] = addr[0];
        data[p++] = addr[1];
        data[p++] = addr[2];

        byte[] elmnt = informationElement.GetAsBytes();
        for (int i = 0; i < elmnt.length; i++) {
            data[p++] = elmnt[i];
        }
        return p;
    }

    public String GetAsString() {
        String result = "  /" + Integer.toString(address) + "_" + informationElement.GetAsSring();       
        return result;
    }

}
