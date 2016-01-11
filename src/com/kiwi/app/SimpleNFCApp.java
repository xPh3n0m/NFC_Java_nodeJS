package com.kiwi.app;

import com.kiwi.nfc.NFCCommunication;

public class SimpleNFCApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NFCCommunication nfcComm = new NFCCommunication();
		
		(new Thread(nfcComm)).start();
	}

}
