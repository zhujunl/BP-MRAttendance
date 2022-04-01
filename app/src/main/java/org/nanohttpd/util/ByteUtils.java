package org.nanohttpd.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Admin
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class ByteUtils {


    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    private static short SRN = 0;

    public static String bytes2hex(byte[] hex) {
        StringBuilder sb = new StringBuilder();
        if (hex != null) {
            for (byte b : hex) {
                sb.append(String.format("%02x", b).toUpperCase());
            }
        }
        return sb.toString();
    }

    public static String bytes2hexB(byte[] hex) {
        StringBuilder sb = new StringBuilder();
        if (hex != null) {
            for (byte b : hex) {
                sb.append(String.format("%02x ", b).toUpperCase());
            }
        }
        return sb.toString();
    }

    /**
     *      * n位字节数组转换为整型 n<=4
     *      * @param b
     *      * @return
     */
    public static int BigEndianBytes2Int(byte[] b) {
        int intValue = 0;
        if (b == null) {
            return intValue;
        }
        int length = b.length;
        if (length == 0) {
            return intValue;
        }
        if (length > 4) {
            return Integer.MAX_VALUE;
        }
        for (int j = 0; j < length; j++) {
            intValue += (b[j] & 0xFF) << (8 * (length - 1 - j));
        }
        return intValue;
    }

    public static int BigEndianBytesToInt(byte... b) {
        int intValue = 0;
        if (b == null) {
            return intValue;
        }
        int length = b.length;
        if (length == 0) {
            return intValue;
        }
        if (length > 4) {
            return Integer.MAX_VALUE;
        }
        for (int j = 0; j < length; j++) {
            intValue += (b[j] & 0xFF) << (8 * (length - 1 - j));
        }
        return intValue;
    }


    /**
     *      * n位字节数组异或值
     *      * @param b
     *      * @return
     */
    public static int bytes2IntXOR(byte[] bytes) {
        int intValue = 0;
        if (bytes == null || bytes.length == 0) {
            return intValue;
        }
        for (byte b : bytes) {
            intValue = (intValue & 0xFF) ^ b;
        }
        return intValue;
    }


    /**
     *      * n位字节数组转换为整型 n<=4  小端模式
     *      * @param b
     *      * @return
     */
    public static int LittleEndianBytes2Int(byte[] b) {
        int intValue = 0;
        if (b == null) {
            return intValue;
        }
        int length = b.length;
        if (length == 0) {
            return intValue;
        }
        if (length > 4) {
            return Integer.MAX_VALUE;
        }

        for (int i = length - 1; i >= 0; i--) {
            intValue += ((b[i] & 0xFF) << 8 * i);
        }

        return intValue;
    }

    public static int byte2Int(byte b) {
        return b & 0xff;
    }

    /**
     *      * n位字节数组之和  大端模式
     *      * @param b
     *      * @return
     */
    public static int bytesSumBigEndian(byte[] b) {
        int intValue = 0;
        if (b == null) {
            return intValue;
        }
        int length = b.length;
        if (length == 0) {
            return intValue;
        }
        for (byte value : b) {
            intValue += (value & 0xFF);
        }
        return intValue;
    }


    /**
     *      * n位字节数组之和  小端模式
     *      * @param b
     *      * @return
     */
    public static int bytesSumLittleEndian(byte[] b) {
        int intValue = 0;
        if (b == null) {
            return intValue;
        }
        int length = b.length;
        if (length == 0) {
            return intValue;
        }
        for (byte value : b) {
            intValue += value;
        }
        return intValue;
    }

    public static synchronized byte[] getSRNLittle() {
        if (SRN == Short.MAX_VALUE) {
            SRN = 0;
        }
        //        Log.d("CommandSM821", "SRN:" + SRN);
        byte[] SRNTem = new byte[2];
        //        SRNTem[0] = (byte) (SRN & 0xFF);
        //        SRNTem[1] = (byte) (SRN >> 8);
        SRNTem = shortToBytesLittleEndian(SRN);
        SRN++;
        return SRNTem;
    }

    public static void recoverySRN(byte[] lastSRN) {
        SRN = bytesToShortLittleEndian(lastSRN);
        Log.e("CommandSM821", "recoverySRN:" + bytes2hex(shortToBytesLittleEndian(SRN)));
    }

    public static void recoverySRN() {
        SRN = bytesToShortLittleEndian(new byte[2]);
        Log.e("CommandSM821", "recoverySRN:" + bytes2hex(shortToBytesLittleEndian(SRN)));
    }

    public static short bytesToShortBigEndian(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public static byte[] shortToBytesBigEndian(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
    }

    public static short bytesToShortLittleEndian(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] shortToBytesLittleEndian(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    public static byte[] getSRNFromCommand(byte[] command) {
        if (command == null || command.length < 3) {
            return null;
        }
        byte[] temp = new byte[2];
        temp[0] = command[1];
        temp[1] = command[2];
        return temp;
    }

    public static boolean compareSRN(byte[] SRN1, byte[] SRN2) {
        if (SRN1 == null || SRN2 == null || SRN1.length != 2 || SRN2.length != 2) {
            return false;
        }
        try {
            return SRN1[0] == SRN2[0] && SRN1[1] == SRN2[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean compare(byte[] data1, byte[] data2) {
        if (data1 == null) {
            return false;
        }
        if (data2 == null) {
            return false;
        }
        if (data1.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }
}
