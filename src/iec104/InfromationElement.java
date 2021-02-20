package iec104;

public abstract class InfromationElement {

    protected byte quality = 0x0;    //описатель качества [1] 7.2.6.3
    byte elementType;
    boolean toTransmit = false;     //устанавливается в true если значение обновилось
    int noTransmitCounter = 8;
        
    public abstract byte Size();

    public abstract byte[] GetAsBytes();

    public abstract String GetAsSring();

    public abstract void SetBadQuality();
}
