package iec104;

//базовый класс поля управления [3] стр.5
public abstract class CtrlFormat {

    public String formatType = "";

    byte byte_1;
    byte byte_2;
    byte byte_3;
    byte byte_4;

    public IecAPDU iecAPDU;

    public static CtrlFormat GetCtrlFormat(IecAPDU iecAPDU) throws Exception {
        CtrlFormat result = null;

        if ((iecAPDU.data[2] & 0x3) == 0x3) {
            Logger.AddToLog("In APDU - U");
            result = new CtrlFormatU(iecAPDU);
        }

        if ((iecAPDU.data[2] & 0x1) == 0) {
            Logger.AddToLog("In APDU - I");
            result = new CtrlFormatI(iecAPDU);
        }

        if ((iecAPDU.data[2] & 0x3) == 1) {
            Logger.AddToLog("In APDU - S");
            result = new CtrlFormatS(iecAPDU);
        }

        if (result == null) {
            new Exception("Error #3");
        }
        return result;
    }

    public abstract String toString();

    public byte[] GetAsBytes() {
        byte[] result = new byte[4];
        result[0] = byte_1;
        result[1] = byte_2;
        result[2] = byte_3;
        result[3] = byte_4;
        return result;
    }
}
