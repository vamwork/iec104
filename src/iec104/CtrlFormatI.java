package iec104;

public class CtrlFormatI extends CtrlFormat {

    public static CtrlFormat CreateAnswerPart() {
        CtrlFormatI result = new CtrlFormatI(null);

        short n = IecAPDU.nrCounter++;
        n = (short) (n << 1);
        byte[] b = Global.ShortToBytes(n);
        result.byte_1 = b[0];
        result.byte_2 = b[1];

        n = IecAPDU.nsCounter;
        n = (short) (n << 1);
        b = Global.ShortToBytes(n);
        result.byte_3 = b[0];
        result.byte_4 = b[1];

        return result;
    }

    public CtrlFormatI(IecAPDU iecAPDU) {
        formatType = "I";
        this.iecAPDU = iecAPDU;
        if (iecAPDU != null) {
            byte_1 = iecAPDU.data[2];
            byte_2 = iecAPDU.data[3];
            byte_3 = iecAPDU.data[4];
            byte_4 = iecAPDU.data[5];
        }
        //счетчики       
        short ns = Global.BytesToShort(byte_2, byte_1);
        ns = (short) (ns >> 1);
        ns++;

        short nr = Global.BytesToShort(byte_4, byte_3);
        nr = (short) (nr >> 1);

        IecAPDU.nsCounter = ns;
    }

    public String toString() {
        String result = "Format type: " + formatType;
        return result;
    }

}
