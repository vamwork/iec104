package iec104;

public class ElementType100 extends InfromationElement {

    byte qoi;

    public ElementType100() {
        elementType = 100;
    }

    public byte Size() {
        return 1;
    }

    public byte[] GetAsBytes() {
        byte[] result = new byte[1];
        result[0] = qoi;
        return result;
    }

    public String GetAsSring() {
        return "Type100 ";
    }

    public void SetBadQuality() {
        quality = 0x40;      //bit NT = 1 [1] 7.2.6.3
    }
}
