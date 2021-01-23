package com.example.MyAppPubNub;

public class Utils {
    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    public static  int getMinorOrMajor(byte[] source) {
        if(source.length != 2) {
            // We need only two byte, no more no less
            return -1;
        }

        int first = Utils.getUnsigned((int) source[0]);
        int second = Utils.getUnsigned((int) source[1]);

        first = first << 8;

        return  first + second;
    }

    public static int getPower(byte[] source) {
        if (source.length != 1) {
            return -10000;
        }

        return (int) source[0];
    }

    public static int getUnsigned(int signed) {
        return signed >= 0 ? signed : (-1) * signed;
    }

    public  static  String getDistance(double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1) {
            return "Immediate";
        } else if (accuracy < 3) {
            return "Near";
        } else {
            return "Far";
        }
    }
}
