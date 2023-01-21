package client;

import shared.messages.KVMessage;

// import client.TextMessage;
// import app_kvClient.TextMessage;
import client.ClientSocketListener.SocketStatus;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.Socket;
import logger.LogSetup;
import java.util.HashSet;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class KVStore extends Thread implements KVCommInterface {
	private Logger logger = Logger.getRootLogger();
	private Set<ClientSocketListener> listeners;
	private boolean running;
	
	private Socket clientSocket;
	private OutputStream output;
 	private InputStream input;
	
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;
	
	String serverAddress;
	int portNumber;

	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public KVStore(String address, int port) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		// System.out.println("Connect\n");
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

	public void addListener(ClientSocketListener listener){
		listeners.add(listener);
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
		setRunning(false);
		logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			logger.info("connection closed!");
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean run) {
		running = run;
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
		// code curtesy: https://www.tutorialspoint.com/json/json_java_example.htm
		JSONObject request = new JSONObject();
		request.put(key, value);

		StringWriter reqWritter = new StringWriter();
		request.writeJSONString(reqWritter);

		TextMessage reqText = new TextMessage(reqWritter.toString());

		// sending request
		try {	
			sendMessage(reqText);
		} catch (IOException e) {
			logger.error("Request forwarding not successful");
			System.exit(1);
		}

		// TextMessage response = new TextMessage();
		// receiving response
		try {
			TextMessage response = receiveMessage();
			return (KVMessage) response;
		} catch (IOException e) {
			logger.error("Response receiving not successful");
			// TODO: message content checking
			System.exit(1);
		}
		return KVMessage();
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// constructing json data

		return null;
	}
}
