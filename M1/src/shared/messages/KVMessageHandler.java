package shared.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class KVMessageHandler implements KVMessage {
	private Logger logger = Logger.getRootLogger();
    private String key;
	private String value;
	private StatusType status;
	
	private OutputStream output;
	private InputStream input;
	private String msg;
	private Socket clientSocket;

	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;

	private static final char LINE_FEED = 0x0A;
	private static final char RETURN = 0x0D;

	/**
	 * given a key-value pair, initialize the message field
	 */
	public KVMessageHandler(String k, String v) {
		key = k;
		value = v;

		// code curtesy: https://www.tutorialspoint.com/json/json_java_example.htm
		JSONObject request = new JSONObject();
		try {
			request.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
//
//		StringWriter reqWritter = new StringWriter();
//		try {
//			request.writeJSONString(reqWritter);
//		} catch (IOException e) {
//			logger.error("Unable to initailize KVMessageHandler");
//		}

		msg = request.toString();
	}

	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream 
	 */
	public void sendKVRequest(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		output = this.clientSocket.getOutputStream();
		input = this.clientSocket.getInputStream();

		byte[] msgBytes = toByteArray(msg);
		msgBytes = addCtrChars(msgBytes);
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("Send message:\t '" + msg + "'");
    }


	public void receiveKVResponse() throws IOException {
		
		int index = 0;
		byte[] msgBytes = null, tmp = null;
		byte[] bufferBytes = new byte[BUFFER_SIZE];
		
		/* read first char from stream */
		byte read = (byte) input.read();	
		boolean reading = true;
		
		while(read != 13 && reading) {/* carriage return */
			/* if buffer filled, copy to msg array */
			if(index == BUFFER_SIZE) {
				if(msgBytes == null){
					tmp = new byte[BUFFER_SIZE];
					System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
				} else {
					tmp = new byte[msgBytes.length + BUFFER_SIZE];
					System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
					System.arraycopy(bufferBytes, 0, tmp, msgBytes.length,
							BUFFER_SIZE);
				}

				msgBytes = tmp;
				bufferBytes = new byte[BUFFER_SIZE];
				index = 0;
			} 
			
			/* only read valid characters, i.e. letters and numbers */
			if((read > 31 && read < 127)) {
				bufferBytes[index] = read;
				index++;
			}
			
			/* stop reading is DROP_SIZE is reached */
			if(msgBytes != null && msgBytes.length + index >= DROP_SIZE) {
				reading = false;
			}
			
			/* read next char from stream */
			read = (byte) input.read();
		}
		
		if(msgBytes == null){
			tmp = new byte[index];
			System.arraycopy(bufferBytes, 0, tmp, 0, index);
		} else {
			tmp = new byte[msgBytes.length + index];
			System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
			System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, index);
		}
    }

	/**
	 * Given a sring, return a byte array after applying utf-8 encoding
	 */
	private byte[] toByteArray(String s){
		byte[] bytes = s.getBytes();
		byte[] ctrBytes = new byte[]{LINE_FEED, RETURN};
		byte[] tmp = new byte[bytes.length + ctrBytes.length];
		
		System.arraycopy(bytes, 0, tmp, 0, bytes.length);
		System.arraycopy(ctrBytes, 0, tmp, bytes.length, ctrBytes.length);
		
		return tmp;		
	}

	private byte[] addCtrChars(byte[] bytes) {
		byte[] ctrBytes = new byte[]{LINE_FEED, RETURN};
		byte[] tmp = new byte[bytes.length + ctrBytes.length];
		
		System.arraycopy(bytes, 0, tmp, 0, bytes.length);
		System.arraycopy(ctrBytes, 0, tmp, bytes.length, ctrBytes.length);
		
		return tmp;		
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