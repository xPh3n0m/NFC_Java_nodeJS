package com.kiwi.nfc;


public final class Utility {
	
	public static int currentOrder;
	
	public static final boolean ONLINE_MODE = true;
	public static final String DB_URL = "jdbc:postgresql://localhost:5432/nfc_project";
	public static final String DB_ONLINE_URL = "jdbc:postgresql://nfcprojectinstance.cpx69rsmkoux.us-west-2.rds.amazonaws.com:5432/nfcprojectdb";

	public static final String DB_USER = "postgres";
	public static final String DB_PASSWORD = "EricDebiole#1";
	
	public static final String DB_ONLINE_USER = "root";
	public static final String DB_ONLINE_PASSWORD = "nfcproject";
	
	public static boolean CANCELED = false;
	
	public static final double INITIAL_BALANCE = 0.0;

	public static final int NEW_NFC_CARD = 1;
	public static final int CARD_PRESENT = 2;
	public static final int CARD_ABSCENT = 2;

	public static final int CASH_ORDER_ID = 1;

	public static final String DEFAULT_NFC_PROTOCOL = "T=1";


	
	private static int currentGid = 0;

	public static String byteArrayToHexString(byte[] b) 
    {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
        	if(b[i] == 92) {
        		break;
        	}
          int v = b[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
     }
    
    public static String hexToASCII(String hexValue)
    {
       StringBuilder output = new StringBuilder("");
       for (int i = 0; i < hexValue.length(); i += 2)
       {
          String str = hexValue.substring(i, i + 2);
          output.append((char) Integer.parseInt(str, 16));
       }
       return output.toString();
    }

    public static String asciiToHex(String asciiValue)
    {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++)
        {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }
    
    public static byte[] hexStringToByteArray(String s) 
    {     
            int len = s.length();     
            byte[] data = new byte[len / 2];     
            for (int i = 0; i < len; i += 2) 
            {         
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));     
            }     
            return data; 
    }
    
    public static byte[] hexStringToByteArray1(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static int generateGuestId() {
    	currentGid++;
    	return currentGid;
    }

}
