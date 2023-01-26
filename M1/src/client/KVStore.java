package client;

import shared.messages.KVMessage;
import shared.messages.KVMessageHandler;
// import client.TextMessage;
// import app_kvClient.TextMessage;
import client.ClientSocketListener.SocketStatus;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.net.Socket;
import logger.LogSetup;
import java.util.HashSet;
import java.util.Set;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class KVStore extends Thread implements KVCommInterface {
	private Logger logger = Logger.getRootLogger();
	private Set<ClientSocketListener> listeners;
	
	private Socket clientSocket;
	private OutputStream output;
 	private InputStream input;
	private KVMessageHandler kvMessageHandler;
	
	String serverAddress;
	int portNumber;


	private boolean running;

	private boolean stop = false;
	private BufferedReader stdin;
	private static final String PROMPT = "EchoClient> ";
	private int serverPort;

	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;

	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	// public KVStore(String address, int port) {
	// 	System.out.println("Constructing KVStore");
	// 	serverAddress = address;
	// 	portNumber = port;
	// 	listeners = new HashSet<ClientSocketListener>();
	// 	logger.info("Connection established");

	// 	// clientSocket = new Socket(address, port);
	// 	// listeners = new HashSet<ClientSocketListener>();
	// 	// setRunning(true);
	// 	// logger.info("Connection established");
	// }

	public KVStore(String address, int port) 
			throws UnknownHostException, IOException {
		
		clientSocket = new Socket(address, port);
		listeners = new HashSet<ClientSocketListener>();
		setRunning(true);
		logger.info("Connection established");
	}

	/**
	 * Establishes a connection to the KV Server.
	 *
	 * @throws Exception
	 *             if connection could not be established.
	 */

	public void connect() throws Exception{
		clientSocket = new Socket(serverAddress, portNumber);
		
		System.out.println("Connect\n");
		try {
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
		}
			
//			while(isRunning()) {
//				try {
//					TextMessage latestMsg = receiveMessage();
//					for(ClientSocketListener listener : listeners) {
//						listener.handleNewMessage(latestMsg);
//					}
//				} catch (IOException ioe) {
//					if(isRunning()) {
//						logger.error("Connection lost!");
//						try {
//							tearDownConnection();
//							for(ClientSocketListener listener : listeners) {
//								listener.handleStatus(
//										SocketStatus.CONNECTION_LOST);
//							}
//						} catch (IOException e) {
//							logger.error("Unable to close connection!");
//						}
//					}
//				}				
//			}
//		} 
		catch (IOException ioe) {
			System.out.printf("Not connected!!!");
			logger.error("Connection could not be established!");
		} 
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
		logger.info("Send message:\t '" + msg.getMsg() + "'");
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
		logger.info("Receive message:\t '" + msg.getMsg() + "'");
		return msg;
    }

	public void addListener(ClientSocketListener listener){
		listeners.add(listener);
	}

	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean run) {
		running = run;
	}
	

	public synchronized void closeConnection() {
		// close the input and output sides of the socket

		logger.info("try to close connection ...");
		
		try {
			tearDownConnection();
			for(ClientSocketListener listener : listeners) {
				listener.handleStatus(SocketStatus.DISCONNECTED);
			}
		} catch (IOException ioe) {
			logger.error("Unable to close connection!");
		}
	}

	private void tearDownConnection() throws IOException {
		logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			logger.info("connection closed!");
		}
	}


	@Override
	public void disconnect() {
		try {
			tearDownConnection();
		} catch (IOException e) { 
			logger.error("connection unable to stop");
			System.exit(1);
		}
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		System.out.printf("putting");
		String command = "put";
		kvMessageHandler = new KVMessageHandler(command, key, value); //Message handler constructs json

		// sending request
		try {	
			kvMessageHandler.sendKVRequest(clientSocket);
		} catch (IOException e) {
			logger.error("Request forwarding not successful");
			System.exit(1);
		}
		System.out.printf("System sending finished" );
		// receiving response
		try {
			kvMessageHandler.receiveKVResponse();
			System.out.printf("Putting finished");
			return kvMessageHandler;
		} catch (IOException e) {
			System.out.printf("putting failed with exception");
			logger.error("Response receiving not successful");
			// TODO: message content checking
			System.exit(1);
		}
		System.out.printf("putting failed");
		return null;
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// constructing json data
		// Format: json with key and value being "" indicates a get request
		String command = "get";
		kvMessageHandler = new KVMessageHandler(command, key, "");

		try {
			kvMessageHandler.sendKVRequest(clientSocket);
		} catch (IOException e) {
			logger.error("Request forwarding not successful");
			System.exit(1);
		}

		try {
			kvMessageHandler.receiveKVResponse();
			return kvMessageHandler;
		} catch (IOException e) {
			logger.error("Response receiving not successful");
			System.exit(1);
		}
 		return null;
	}


	public void run() {
		try {
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			
			while(isRunning()) {
				try {
					TextMessage latestMsg = receiveMessage();
					for(ClientSocketListener listener : listeners) {
						listener.handleNewMessage(latestMsg);
					}
				} catch (IOException ioe) {
					if(isRunning()) {
						logger.error("Connection lost!");
						try {
							tearDownConnection();
							for(ClientSocketListener listener : listeners) {
								listener.handleStatus(
										SocketStatus.CONNECTION_LOST);
							}
						} catch (IOException e) {
							logger.error("Unable to close connection!");
						}
					}
				}				
			}
		} catch (IOException ioe) {
			logger.error("Connection could not be established!");
			
		} finally {
			if(isRunning()) {
				closeConnection();
			}
		}
	}



}
