package client;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import client.ClientSocketListener.SocketStatus;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

// import org.apache.log4j.Logger;

import shared.messages.KVMessage;

public class KVStore implements KVCommInterface {
	String address;
	int port;

	// private Logger logger = Logger.getRootLogger();
	private Set<ClientSocketListener> listeners;
	private boolean running;
	
	private Socket clientSocket;
	private OutputStream output;
 	private InputStream input;
	
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;
	
	

	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public KVStore(String address, int port) {
		// TODO Auto-generated method stub
		// address = address;
		// port = port;

		clientSocket = new Socket(address, port);
		listeners = new HashSet<ClientSocketListener>();
		setRunning(true);
		// logger.info("Connection established");
	}

	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		try {
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			
			while(isRunning()) { // while make sure server action consistent
				try {
					TextMessage latestMsg = receiveMessage();
					for(ClientSocketListener listener : listeners) {
						listener.handleNewMessage(latestMsg);
					}
				} catch (IOException ioe) {
					if(isRunning()) {
						// logger.error("Connection lost!");
						try {
							tearDownConnection();
							for(ClientSocketListener listener : listeners) {
								listener.handleStatus(
										SocketStatus.CONNECTION_LOST);
							}
						} catch (IOException e) {
							// logger.error("Unable to close connection!");
							;
						}
					}
				}				
			}
		} catch (IOException ioe) {
			// logger.error("Connection could not be established!");
			;
		} finally {
			if(isRunning()) {
				closeConnection();
			}
		}
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized void closeConnection() {
		// logger.info("try to close connection ...");
		
		try {
			tearDownConnection();
			for(ClientSocketListener listener : listeners) {
				listener.handleStatus(SocketStatus.DISCONNECTED);
			}
		} catch (IOException ioe) {
			// logger.error("Unable to close connection!");
			;
		}
	}

	private void tearDownConnection() throws IOException {
		setRunning(false);
		// logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			// logger.info("connection closed!");
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean run) {
		running = run;
	}

	public void addListener(ClientSocketListener listener){
		listeners.add(listener);
	}

	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream 
	 */
	public void sendMessage(TextMessage msg) throws IOException {
		byte[] msgBytes = msg.getMsgBytes();
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		// logger.info("Send message:\t '" + msg.getMsg() + "'");
    }
	
	
	private TextMessage receiveMessage() throws IOException {
		
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
		
		msgBytes = tmp;
		
		/* build final String */
		TextMessage msg = new TextMessage(msgBytes);
		// logger.info("Receive message:\t '" + msg.getMsg() + "'");
		return msg;
    }

}
