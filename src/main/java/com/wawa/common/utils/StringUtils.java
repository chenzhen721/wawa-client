package com.wawa.common.utils;

public class StringUtils {



    public static String bytes2HexString(byte[] b) {
        return bytes2HexString(b, b.length);
    }

    public static String bytes2HexString(byte[] b, int len) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String hex = Integer.toHexString(b[ i ] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret.append(hex.toUpperCase());
        }
        return ret.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte sum_data(byte[] data) {
        return sum_data(data, data.length);
    }

    public static byte sum_data(byte[] data, int i) {
        int check_total = 0;
        //check sum
        for(int j = 0; j < i; j++) {
            check_total += data[j] & 0xFF;
        }
        return (byte) (check_total % 256);
    }

}
