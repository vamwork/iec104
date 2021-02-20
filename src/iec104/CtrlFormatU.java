package iec104;

public class CtrlFormatU extends CtrlFormat {

    public CtrlFormatU(IecAPDU iecAPDU) {
        formatType = "U";
        this.iecAPDU = iecAPDU;
        if (iecAPDU != null) {
            byte_1 = iecAPDU.data[2];
            byte_2 = iecAPDU.data[3];
            byte_3 = iecAPDU.data[4];
            byte_4 = iecAPDU.data[5];
        }
    }

    public static CtrlFormat CreateStartdtCon() {
        CtrlFormatU result = new CtrlFormatU(null);
        result.byte_1 = 0xb;
        result.byte_2 = 0;
        result.byte_3 = 0;
        result.byte_4 = 0;
        return result;
    }

    public String toString() {
        String result = "Format type: " + formatType;
        return result;
    }
}
