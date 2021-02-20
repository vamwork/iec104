package iec104;

public class ElementType1 extends InfromationElement {

    public byte value;
    public byte prevValue;

    public ElementType1() {
        elementType = 1;
        quality = 0x40;      //bit NT = 1 [1] 7.2.6.3
    }

    public byte Size() {
        return 1;
    }

    public byte[] GetAsBytes() {
        return new byte[]{(byte) ((byte) value | (byte) quality)};    //7.2.6.4 and 7.3.1.1  качество всегда хорошее
    }

    public void SetNewValue(byte newValue) {
//        quality = 0;
//        if (newValue != value) {
//            value = newValue;
//            toTransmit = true;
//        }

        if ((prevValue != newValue)) {
            toTransmit = true;
            prevValue = value;
            value = newValue;
            quality = 0;
            noTransmitCounter = 0;
        } else {
            toTransmit = false;
            noTransmitCounter++;
            quality = 0;
            if (noTransmitCounter > 10) {
                noTransmitCounter = 0;
                prevValue = value;
                toTransmit = true;
            }
        }
    }

    public String GetAsSring() {
        String result = "v1=" + Global.ToString(value) + "[" + Global.ToString(prevValue) + "]";
        if (toTransmit) {
            result = result + "->";
        } else {
            result = result + " #" + Integer.toString(noTransmitCounter);
        }
        return result;
    }

    public void SetBadQuality() {
        quality = 0x40;      //bit NT = 1 [1] 7.2.6.3
    }

}
