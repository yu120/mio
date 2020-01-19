package io.mio.core.commons;

/**
 * ByteUtils
 *
 * @author lry
 */
public class ByteUtils {

    /**
     * byte转hex
     *
     * @param b byte
     * @return hex
     */
    public static String printHexByte(byte b) {
        return String.format("0x%02X", b);
    }

    /**
     * byte[]转为hex
     *
     * @param bytes byte[]
     * @return hex
     */
    public static String printHexBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte tempByte : bytes) {
            sb.append(printHexByte(tempByte)).append(" ");
        }

        return sb.toString();
    }

    /**
     * short转hex
     *
     * @param s short
     * @return hex
     */
    public static String printHexShort(int s) {
        byte[] bytes = highLowHexShort(s);
        return printHexBytes(bytes);
    }

    /**
     * short转byte[2]
     *
     * @param s short
     * @return byte[2]
     */
    public static byte[] highLowHexShort(int s) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((s << 24) >> 24);
        bytes[1] = (byte) ((s << 16) >> 24);
        return bytes;
    }

    public static byte[] lowHighHexShort(int s) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) ((s << 24) >> 24);
        bytes[0] = (byte) ((s << 16) >> 24);
        return bytes;
    }

    /**
     * int转byte[4]
     *
     * @param n int
     * @return byte[4]
     */
    public static byte[] hexInt(int n) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((n) >> 24);
        bytes[2] = (byte) ((n << 8) >> 24);
        bytes[1] = (byte) ((n << 16) >> 24);
        bytes[0] = (byte) ((n << 24) >> 24);
        return bytes;
    }

    /**
     * 将int数值转换为占四个字节的byte数组
     * <p>
     * 低位在前，高位在后
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] lowHighHex(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组
     * <p>
     * 高位在前，低位在后
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] highLowHex(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值
     * <p>
     * 低位在前,高位在后
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int lowHighInt(byte[] src, int offset) {
        return (src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24);
    }

    /**
     * byte数组中取int数值
     * <p>
     * 高位在前,低位在后
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int highLowInt(byte[] src, int offset) {
        return (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
    }

}
