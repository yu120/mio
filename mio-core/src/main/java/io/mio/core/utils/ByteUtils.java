package io.mio.core.utils;

import java.util.Arrays;

/**
 * ByteUtils
 * <p>
 * 1.大端模式（Big-endian）：高位字节排放在内存的低地址端，低位字节排放在内存的高地址端。即高位在前,低位在后
 * 2.小端模式（Little-endian）：低位字节排放在内存的低地址端，高位字节排放在内存的高地址端。即低位在前高位在后
 * <p>
 * 案例：int value = 0x12345678
 * 地址顺序：低地址--->高地址(即下标：0,1,2,3)
 * 大端模式：byte[4]{0x12, 0x34, 0x56, 0x78}
 * 小端模式：byte[4]{0x78, 0x56, 0x34, 0x12}
 *
 * @author lry
 */
public class ByteUtils {

    /**
     * byte to hex
     *
     * @param b byte
     * @return hex
     */
    public static String byte2Hex(byte b) {
        return String.format("0x%02X", b);
    }

    /**
     * bytes to hex
     *
     * @param bytes byte[]
     * @return hex
     */
    public static String bytes2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte tempByte : bytes) {
            sb.append(byte2Hex(tempByte)).append(" ");
        }

        return sb.toString();
    }

    /**
     * 异或运算
     *
     * @param bytes bytes
     * @return byte
     */
    public static byte xor(byte[] bytes) {
        byte result = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            result ^= bytes[i];
        }

        return result;
    }

    /**
     * byte,byte[] concat
     *
     * @param firstByte first byte
     * @param second    second byte[]
     * @return byte[]
     */
    public static byte[] concat(byte firstByte, byte[] second) {
        byte[] result = new byte[1 + second.length];
        result[0] = firstByte;
        System.arraycopy(second, 0, result, 1, second.length);
        return result;
    }

    /**
     * byte[],byte[] concat
     *
     * @param first  first byte[]
     * @param second second byte[]
     * @return byte[]
     */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    // === short

    /**
     * byte[2] to short [Big-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return short
     */
    public static short bytes2ShortBig(byte[] bytes, int offset) {
        int tmp = 0;
        tmp += (bytes[offset] & 0xFF) << 8;
        tmp += bytes[offset + 1] & 0xFF;
        return (short) tmp;
    }

    /**
     * byte[2] to short [Little-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return short
     */
    public static short bytes2ShortLittle(byte[] bytes, int offset) {
        int tmp = 0;
        tmp += (bytes[offset + 1] & 0xFF) << 8;
        tmp += bytes[offset] & 0xFF;
        return (short) tmp;
    }

    /**
     * short to byte[2] [Big-Endian]
     *
     * @param num short number
     * @return byte[2]
     */
    public static byte[] short2BytesBig(short num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (num >>> 8);
        bytes[1] = (byte) (num);
        return bytes;
    }

    /**
     * short to byte[2]  [Little-Endian]
     *
     * @param num short number
     * @return byte[2]
     */
    public static byte[] short2BytesLittle(short num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (num);
        bytes[1] = (byte) (num >>> 8);
        return bytes;
    }

    // === int

    /**
     * byte[4] to int [Big-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return int
     */
    public static int bytes2IntBig(byte[] bytes, int offset) {
        int result = 0;
        result += (bytes[offset] & 0xFF) << 24;
        result += (bytes[offset + 1] & 0xFF) << 16;
        result += (bytes[offset + 2] & 0xFF) << 8;
        result += bytes[offset + 3] & 0xFF;
        return result;
    }

    /**
     * byte[4] to int [Little-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return int
     */
    public static int bytes2IntLittle(byte[] bytes, int offset) {
        int result = 0;
        result += bytes[offset] & 0xFF;
        result += (bytes[offset + 1] & 0xFF) << 8;
        result += (bytes[offset + 2] & 0xFF) << 16;
        result += (bytes[offset + 3] & 0xFF) << 24;
        return result;
    }

    /**
     * int to byte[4] [Big-Endian]
     *
     * @param num int number
     * @return byte[4]
     */
    public static byte[] int2bytesBig(int num) {
        byte[] result = new byte[4];
        result[0] = (byte) (num >>> 24);
        result[1] = (byte) (num >>> 16);
        result[2] = (byte) (num >>> 8);
        result[3] = (byte) num;
        return result;
    }

    /**
     * int to byte[4] [Little-Endian]
     *
     * @param num int number
     * @return byte[4]
     */
    public static byte[] int2bytesLittle(int num) {
        byte[] result = new byte[4];
        result[0] = (byte) num;
        result[1] = (byte) (num >>> 8);
        result[2] = (byte) (num >>> 16);
        result[3] = (byte) (num >>> 24);
        return result;
    }

    // === long

    /**
     * long to byte[8] [Big-Endian]
     *
     * @param num long number
     * @return byte[8]
     */
    public static byte[] long2bytesBig(long num) {
        byte[] result = new byte[8];
        result[7] = (byte) num;
        result[6] = (byte) (num >>> 8);
        result[5] = (byte) (num >>> 16);
        result[4] = (byte) (num >>> 24);
        result[3] = (byte) (num >>> 32);
        result[2] = (byte) (num >>> 40);
        result[1] = (byte) (num >>> 48);
        result[0] = (byte) (num >>> 56);
        return result;
    }

    /**
     * long to byte[8] [Little-Endian]
     *
     * @param num long number
     * @return byte[8]
     */
    public static byte[] long2bytesLittle(long num) {
        byte[] result = new byte[8];
        result[0] = (byte) num;
        result[1] = (byte) (num >>> 8);
        result[2] = (byte) (num >>> 16);
        result[3] = (byte) (num >>> 24);
        result[4] = (byte) (num >>> 32);
        result[5] = (byte) (num >>> 40);
        result[6] = (byte) (num >>> 48);
        result[7] = (byte) (num >>> 56);
        return result;
    }

    /**
     * byte[8] to long [Big-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return long
     */
    public static long bytes2LongBig(byte[] bytes, int offset) {
        long result = 0;
        result += ((long) (bytes[offset] & 0xFF)) << 56;
        result += ((long) (bytes[offset + 1] & 0xFF)) << 48;
        result += ((long) (bytes[offset + 2] & 0xFF)) << 40;
        result += ((long) (bytes[offset + 3] & 0xFF)) << 32;
        result += ((long) (bytes[offset + 4] & 0xFF)) << 24;
        result += ((long) (bytes[offset + 5] & 0xFF)) << 16;
        result += ((long) ((bytes[offset + 6] & 0xFF))) << 8;
        result += ((long) (bytes[offset + 7]) & 0xFF);
        return result;
    }

    /**
     * byte[8] to long [Little-Endian]
     *
     * @param bytes  bytes
     * @param offset start offset
     * @return long
     */
    public static long bytes2LongLittle(byte[] bytes, int offset) {
        long result = 0;
        result += ((long) bytes[offset] & 0xFF);
        result += ((long) (bytes[offset + 1] & 0xFF)) << 8;
        result += ((long) (bytes[offset + 2] & 0xFF)) << 16;
        result += ((long) (bytes[offset + 3] & 0xFF)) << 24;
        result += ((long) (bytes[offset + 4] & 0xFF)) << 32;
        result += ((long) (bytes[offset + 5] & 0xFF)) << 40;
        result += ((long) (bytes[offset + 6] & 0xFF)) << 48;
        result += ((long) (bytes[offset + 7] & 0xFF)) << 56;
        return result;
    }

}
