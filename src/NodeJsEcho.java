import java.io.BufferedReader;
import java.io.File;
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.io.PrintWriter; 
import java.net.Socket; 
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwi.nfc.NFCCard;
import com.kiwi.nfc.NFCCardException;
import com.kiwi.nfc.NFCCommunication;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class NodeJsEcho {
		
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		File libPcscLite = new File("/lib/x86_64-linux-gnu/libpcsclite.so.1");
		if (libPcscLite.exists()) {
		  System.setProperty("sun.security.smartcardio.library", libPcscLite.getAbsolutePath());
		}
		
        NFCCommunication nfcComm = new NFCCommunication();

        // Wait for an available terminal
        while(!nfcComm.connectToDefaultTerminal()) {
        	try {
        		System.out.println("No terminal available");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // Wait for an available NFC Card
        NFCCard nfcCard = null;
        try {
        	nfcCard = nfcComm.getCurrentNFCCard();
        	
        } catch (NFCCardException e) {
			System.out.println(e.getMessage());
			System.exit(0);
        }
        
        nfcCard.getGid();
        String message = nfcCard.getJSONData();
		
        try {
			BasicExample ioSocket = new BasicExample();
			ioSocket.getSocket().emit("message", message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
}
