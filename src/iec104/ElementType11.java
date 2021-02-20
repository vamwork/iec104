package iec104;

public class ElementType11 extends InfromationElement {

    private short value;  //[1] 7.2.6.7
    private short prevValue;

    public ElementType11() {
        elementType = 11;
    }

    public byte Size() {
        return 3;
    }

    public byte[] GetAsBytes() {
        byte[] result = new byte[3];
        byte[] v = Global.ShortToBytes(value);
        result[0] = v[0];
        result[1] = v[1];
        result[2] = quality;
        return result;
    }

    public String GetAsSring() {
        String result = "v11=" + Global.ToString(value) + "[" + Global.ToString(prevValue) + "]";
        if (toTransmit) {
            result = result + "->";
        } else {
            result = result + " #" + Integer.toString(noTransmitCounter);
        }
        return result;
    }

    public void SetNewValue(short v) {
        if ((prevValue + 2 < v) || (prevValue - 2 > v)) {
            toTransmit = true;
            prevValue = value;
            value = v;
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

    public void SetBadQuality() {
        quality = 0x40;      //bit NT = 1 [1] 7.2.6.3
    }
}
