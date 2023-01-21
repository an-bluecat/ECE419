package messages;

import shared.messages.KVMessage;

public class KVMessageHandler implements KVMessage {
    String key;
	String value;
	StatusType status;

	public void KVMessageHandler(String k, String v, StatusType stat) {
		key = k;
		value = v;
		status = stat;
	}

    /**
	 * @return the key that is associated with this message, 
	 * 		null if not key is associated.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the value that is associated with this message, 
	 * 		null if not value is associated.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus() {
		return status;
	}

}