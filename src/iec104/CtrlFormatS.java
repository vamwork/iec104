package iec104;

public class CtrlFormatS extends CtrlFormat {

    public CtrlFormatS(IecAPDU iecAPDU) {
        formatType = "S";
        this.iecAPDU = iecAPDU;
        if (iecAPDU != null) {
            byte_1 = iecAPDU.data[2];
            byte_2 = iecAPDU.data[3];
            byte_3 = iecAPDU.data[4];
            byte_4 = iecAPDU.data[5];
        }
        //счетчики               
        short nr = Global.BytesToShort(byte_4, byte_3);
        nr = (short) (nr >> 1);                  
    }

    public String toString() {
        String result = "Format type: " + formatType + ", byte_1 0x" + Integer.toHexString(iecAPDU.data[2]);
        return result;
    }
}
