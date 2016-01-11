package com.kiwi.nfc;
import java.util.Arrays;

import javax.smartcardio.Card;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NFCCard {
	
	private Card card;
	private UID uid;
	private int wid;
	private int gid;
	private double balance;
	private char status;
	private boolean readable;
	private boolean valid;
	
	/**
	 * General constructor for an NFCWristband object
	 * @param card
	 * @param atr
	 * @param wid
	 * @param gid
	 * @param balance
	 * @param status
	 */
	private NFCCard(Card card, byte[] uid, int wid, int gid, double balance, char status, boolean readable, boolean valid) {
		this.card = card;
		this.uid = new UID(uid);
		this.setWid(wid);
		this.setGid(gid);
		this.setBalance(balance);
		this.setStatus(status);
		this.setReadable(readable);
		this.setValid(valid);
	}
	
	/**
	 * Default constructor for a wristband that was not recognized (data not readable or non conform to protocol)
	 * @param card
	 * @param atr
	 */
	private NFCCard(Card card, byte[] uid) {
		this(card, uid, -1, -1, Utility.INITIAL_BALANCE, 'I', false, false);
	}
	
	public static NFCCard nfcWristbandFromWristbandData(Card card, byte[] uid, String jsonString) {
		JSONParser parser=new JSONParser();
		JSONObject wristbandJSON;
		
		try {
		wristbandJSON = (JSONObject) parser.parse(jsonString);
		} catch (ParseException e) {
			return new NFCCard(card, uid);
		}
		
		if(wristbandJSON.containsKey("wid") 
				&& wristbandJSON.containsKey("gid") 
				&& wristbandJSON.containsKey("balance")
				&& wristbandJSON.containsKey("status")) {
			
			long wid = (long) wristbandJSON.get("wid");
			long gid = (long) wristbandJSON.get("gid");		
			//String name = (String) guestJSON.get("guest_name");
			double balance = (double) wristbandJSON.get("balance");
			char status = ((String) wristbandJSON.get("status")).charAt(0);

			return new NFCCard(card, uid, (int) wid, (int) gid, balance, status, true, false);
		} else {
			return new NFCCard(card, uid);
		}
	}
	
	public boolean uidEquals(NFCCard otherCard) {
		if(otherCard == null) {
			return false;
		}
		
		if(uid.equals(otherCard.getUid())) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean equals(NFCCard otherCard) throws NFCCardException {
		if(otherCard == null || !(uid.equals(otherCard.getUid()))) {
			throw new NFCCardException("The card has been changed");
		}
		
		if(wid == otherCard.getWid()
				&& gid == otherCard.getGid()
				&& status == otherCard.getStatus()
				&& balance == otherCard.getBalance()) {
			return true;
		} else {
			return false;
		}
	}

	public UID getUid() {
		return uid;
	}

	public Card getCard() {
		// TODO Auto-generated method stub
		return card;
	}

	public int getWid() {
		return wid;
	}

	public void setWid(int wid) {
		this.wid = wid;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean recognized) {
		this.readable = recognized;
	}

	public String getJSONData() {
		JSONObject j = new JSONObject();
		j.put("wid", wid);
		j.put("gid", gid);
		j.put("status", status + "");
		j.put("balance", balance);

		return j.toJSONString();
	}

	public static NFCCard nfcWristbandFromDB(int wid2, int gid2, char status2, double balance2, byte[] uid2) {
		return new NFCCard(null, uid2, wid2, gid2, balance2, status2, false, true);
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public class UID {
		private byte[] uid;
		
		public UID(byte[] uid) {
			this.uid = uid;
		}
		
		public boolean equals(UID otherUid) {
			if(otherUid != null) {
				if(Arrays.equals(uid, otherUid.getBytes())) {
					return true;
				}
			}
			return false;
		}
		
		public byte[] getBytes() {
			return uid;
		}
		
		public String toString() {
			String uidString = "[";
			for(int i = 0; i < uid.length; i++) {
				if(i <uid.length-1)
					uidString += uid[i] + ", ";
				else
					uidString += uid[i];
			}
			uidString += "]";
			return uidString;
		}
	}

}
