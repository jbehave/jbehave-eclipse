package org.jbehave.eclipse.util;

public class Bytes {
    public static byte[] longToBytes(long v) {
        byte[] buffer = new byte[ 8 ];

        buffer[0] = (byte)(v >>> 56);
        buffer[1] = (byte)(v >>> 48);
        buffer[2] = (byte)(v >>> 40);
        buffer[3] = (byte)(v >>> 32);
        buffer[4] = (byte)(v >>> 24);
        buffer[5] = (byte)(v >>> 16);
        buffer[6] = (byte)(v >>>  8);
        buffer[7] = (byte)(v >>>  0);

        return buffer;
    }
    
    public static boolean areDifferents(byte[] b1, byte[] b2) {
        return !areEquals(b1, b2);
    }
    
    public static boolean areEquals(byte[] b1, byte[] b2) {
        if(b1==null || b2==null) {
            if(b1==b2) // both null
                return true;
            else
                return false;
        }
        else if(b2.length!=b1.length) {
            return false;
        }
        
        for(int i=0,n=b1.length;i<n;i++) {
            if(b1[i]!=b2[i])
                return false;
        }
        return true;
    }
}
