package org.jbehave.eclipse.util;

public class IO {
    
    /**
     * End of file, termination character/code.
     */
    public static final int EOF = -1;
    
    /**
     * Carriage Return code.
     */
    public static final byte CR               = '\r';

    /**
     * Line Feed code.
     */
    public static final byte LF               = '\n';

    /**
     *
     */
    public enum LineSeparator {
        /**
         * Line Feed code.
         */
        LineFeed(LF),
        /**
         * Carriage Return code.
         */
        CarriageReturn(CR),
        
        CRLF(CR,LF);
        
        private byte[] bytes;

        private LineSeparator(byte... bytes) {
            this.bytes = bytes;
        }
        public int length() {
            return bytes.length;
        }
    }
    
    public static BidirectionalStream toBidirectionalStream(final CharSequence seq) {
        return new BidirectionalStream() {
            private int index;
            
            @Override
            public void unread() {
                index--;
            }
            
            @Override
            public int read() {
                if(index<seq.length())
                    return seq.charAt(index++);
                else
                    return IO.EOF;
            }
        };
    }
    
    public static BidirectionalReader toBidirectionalReader(final CharSequence seq) {
        return new BidirectionalReader(toBidirectionalStream(seq));
    }
    
}
