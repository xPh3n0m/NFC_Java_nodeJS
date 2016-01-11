package com.kiwi.nfc;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import org.json.simple.parser.ParseException;

public class NFCCommunication implements Runnable{
	
	private CardTerminal terminal;
	private boolean terminalIsPresent;
		
	private String buffer = "";
	
	public NFCCommunication() {
		File libPcscLite = new File("/lib/x86_64-linux-gnu/libpcsclite.so.1");
		if (libPcscLite.exists()) {
		  System.setProperty("sun.security.smartcardio.library", libPcscLite.getAbsolutePath());
		}
		terminalIsPresent = false;
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			System.out.println("Connecting to default terminal...");
			terminal = null;
			while(!connectToDefaultTerminal()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Connected to default terminal");
			
			while(true) {
					try {
						System.out.println("Waiting for NFC Card");
						terminal.waitForCardPresent(0);
					} catch (CardException e) {
						System.out.println("Problem connecting to NFC Card");
						break;
					}
					System.out.println("Connected to new card.");
					
					String protocol = Utility.DEFAULT_NFC_PROTOCOL;
					Card card = null;
					try {
						card = terminal.connect(protocol);
					} catch (CardException e) {
						System.out.println("Not possible to connect to current NFC Card");
						break;
					}
					System.out.println("Succesfully connected to current NFC Card");
					
					byte[] uid;
					try {
						uid = readUid(card);
					} catch (NFCCardException e) {
						System.out.println("Unable to read UID");
						break;
					}
					
					String uidString = new String(uid, StandardCharsets.UTF_8);
					System.out.println("Read a new card. UID: " + uidString);
					
					
					try {
						terminal.waitForCardAbsent(0);
					} catch (CardException e) {
						System.out.println("Unable to disconnect card");
						break;
					}
					System.out.println("Card disconnected");
			}
		}
	}
	
	/**
	 * Connect to the the default terminal
	 * @throws CardException 
	 * @throws TerminalException 
	 */
	public boolean connectToDefaultTerminal() {
		try {
			terminal = _selectDefaultCardTerminal();
			return true;
		} catch (TerminalException e) {
			return false;
		}
	}
	
	/**
	 * Returns the current NFC card availbale on the terminal with its ATR and the data found on it
	 * @return
	 * @throws NFCCardException if a problem occurs when reading the card
	 * @throws ParseException 
	 */
	public NFCCard getCurrentNFCCard() throws NFCCardException {
		boolean cardPresent;
		try {
			cardPresent = terminal.isCardPresent();
		} catch (CardException e) {
			throw new NFCCardException("Unable to find the status of the card");
		}
		
		if(cardPresent) {
			//Select automatically protocol T=1
		    String protocol = "T=1";
		    Card newCard;
		    try {
				newCard = terminal.connect(protocol);
			} catch (CardException e) {
				throw new NFCCardException("Unable to connect to the card");
			}
		    
		    byte[] uid = readUid(newCard);
		    String data;
			try {
				data = _readDataFromCard(newCard);
			} catch (CardException e) {
				throw new NFCCardException("Unable to read from the current card");
			}
		    
		    return NFCCard.nfcWristbandFromWristbandData(newCard, uid, data);
		    
		} else {
			throw new NFCCardException("No NFC Card available");
		}
	}
	

	public void eraseDataFromWristband(NFCCard nfcCard) throws NFCCardException {
		this.writeDataToNFCCard(nfcCard.getJSONData(), nfcCard);
	}
	
	/**
	 * Reads the data contained in the card
	 * @param card
	 * @return
	 * @throws CardException
	 */
	private String _readDataFromCard(Card card) throws CardException {
        // Get the card channel
        CardChannel cc = card.getBasicChannel();
        String cardData = _readData(cc);
        
        return cardData;
	}

	/**
	 * method selectDefaultCardTerminal
	 * Selects the default terminal among all available terminals and returns it
	 * @return
	 * @throws TerminalException 
	 * @throws CardException if the card operation failed
	 */
	private CardTerminal _selectDefaultCardTerminal() throws TerminalException {
		TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals;
		try {
			terminals = factory.terminals().list();
	        ListIterator<CardTerminal> terminalsIterator = terminals.listIterator();
	        
	        if(terminalsIterator.hasNext()) {
	        	CardTerminal terminal = terminalsIterator.next();
	        	return terminal;
	        } else {
	        	throw new TerminalException("No terminals found");
	        }
		} catch (CardException e) {
			throw new TerminalException("Problem listing terminals");
		}
   }
	
	/**
	 * Read the data from the wristband
	 * @param cc
	 * @return
	 * @throws CardException
	 */
	private String _readData(CardChannel cc) throws CardException {
        buffer = "";
        
        for(int i = 4; i < 35; i = i+2) {
        	if(_readPage(cc, i)) {
        		break;
        	}
        }
        
        return Utility.hexToASCII(buffer);
	}
	

	public boolean writeDBWristbandToNFCWristband(NFCCard dbWristband) throws NFCCardException {
		NFCCard currentWristband;
				
		try {
			currentWristband = getCurrentNFCCard();
			if(currentWristband.uidEquals(dbWristband)) {
				Card c = currentWristband.getCard();
				CardChannel cc = c.getBasicChannel();
				
				String data = dbWristband.getJSONData();
				if(data != null) {
					try {
						if(_writeData(data, cc)) {
							return true;
						} else {
							return false;
						}
					} catch (CardException e) {
						throw new NFCCardException("Unable to write to the NFC Card");
					}
				} else {
					return false;
				}
			} else {
				throw new NFCCardException("The card has changed. Try again");
			}

			
		} catch (NFCCardException e) {
			throw new NFCCardException("No card available");
		}
	}
	
	public boolean writeDataToNFCCard(String data, NFCCard card) throws NFCCardException {
		boolean t = true;
		NFCCard currentCard = getCurrentNFCCard();
		
		try {
			// TODO: Add the below in order to verify that the card we are writing on is still the same card
			//currentCard = getCurrentNFCCard();
			if(currentCard.uidEquals(card)) {
				Card c = currentCard.getCard();
				CardChannel cc = c.getBasicChannel();

				if(data != null) {
					try {
						if(_writeData(data, cc)) {
							return true;
						} else {
							return false;
						}
					} catch (CardException e) {
						throw new NFCCardException("Unable to write to the NFC Card");
					}
				} else {
					return false;
				}
			} else {
				throw new NFCCardException("The card has changed. Try again");
			}

			
		} catch (NFCCardException e) {
			throw new NFCCardException("No card available");
		}
	}
	
	public byte[] readUid(Card card) throws NFCCardException {
		try {
			CardChannel cc = card.getBasicChannel();
			
			byte[] uid = new byte[7];
	        
	        // Read the first 8 bytes
	        byte[] read8FirstBytesCommand = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x08};
	    	CommandAPDU readData = new CommandAPDU(read8FirstBytesCommand);
	    	ResponseAPDU responseReadData;
			try {
				responseReadData = cc.transmit(readData);
		    	byte[] firstbytes = responseReadData.getData();
		    	
		    	if(firstbytes.length >= 8) {
			    	for(int i = 0; i < 3; i++) {
			    		uid[i] = firstbytes[i];
			    	}
			    	for(int i = 4; i < 8; i++) {
			    		uid[i-1] = firstbytes[i];
			    	}
		    	} else {
		    		throw new NFCCardException("There was a problem reading the UID bytes");
		    	}
		    	return uid;
			} catch (CardException e) {
				throw new NFCCardException("Unable to read UID from NFC Card");
			}

		} catch (NFCCardException e) {
			throw new NFCCardException("No card available");
		}
		
	}
		
	private boolean _writeData(String data, CardChannel cc) throws CardException {
		// Add the halt character to the string, and change it to a HEX string
		String d = Utility.asciiToHex(new String(data + "\\"));
		
		// Change the string to a byte array
        byte[] b = new BigInteger(d,16).toByteArray();
        
        // Split the array in chunks of 8 bytes
        byte[][] b_split = new byte[b.length/8 + 1][b.length];
        int j = 0;
        for(int i = 0; i < b.length; i=i+8) {
        	b_split[j] = Arrays.copyOfRange(b, i, i+8);
        	j++;
        }
        
        //Write all pages
        for(int i = 0; i < b_split.length; i++) {
        	_writePage(cc, 4+(2*i), b_split[i]);
        }
        
        return true;
	}
	
	/**
	 * Returns true if the page contains a halt character ("\")
	 * @param bufferint
	 * @return
	 * @throws CardException 
	 */
	private boolean _readPage(CardChannel cc, int offset) throws CardException {
        
        byte[] readCommand =  {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) offset, (byte) 0x08};
    	
    	CommandAPDU readData = new CommandAPDU(readCommand);
    	ResponseAPDU responseReadData = cc.transmit(readData);
    	
    	byte[] dataOut = responseReadData.getData();
    	
		StringBuilder sb = new StringBuilder();
		for(byte b : dataOut) {
			if(b == 92) {
				buffer += sb.toString();
				return true;
			}
			sb.append(String.format("%02X", b));
		}
    		
    	buffer += sb.toString();
        return false;
	}
	
	
	private void _writePage(CardChannel cc, int offset, byte[] dataOut) throws CardException {
        // Encode the length of the string in a byte
        byte bArraySize = new Integer(dataOut.length).byteValue();
        
        // Write command starting in page offset
        byte[] writeCommand = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) offset};
        
        // The byte array for the full command
        byte[] command = new byte[writeCommand.length + 1 + dataOut.length];
        for(int i = 0; i < writeCommand.length; i++) {
        	command[i] = writeCommand[i];
        }
        command[writeCommand.length] = bArraySize;
        for(int i = 0; i < dataOut.length; i++) {
        	command[i + writeCommand.length + 1] = dataOut[i];
        }
        
        CommandAPDU writeData = new CommandAPDU(command);
        ResponseAPDU responseWriteData = cc.transmit(writeData);	
	}



}
