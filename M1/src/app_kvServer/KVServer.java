/* 
usage: java -jar m1-server.jar 1025
*/

package app_kvServer;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;



import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;




public class KVServer extends Thread implements IKVServer  {
	/**
	 * Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache
	 *           is full and there is a GET- or PUT-request on a key that is
	 *           currently not contained in the cache. Options are "FIFO", "LRU",
	 *           and "LFU".
	 */
	private static Logger logger = Logger.getRootLogger();
	
	private int port;
	private ServerSocket serverSocket;
    private boolean running;
	
	public static String STORAGE_DIRECTORY = "./storage"; // static keyword is used to indicate that a variable or a method belongs to a class, rather than an instance of a class

	public KVServer(int port) {
		// TODO Auto-generated method stub
		this.port = port;
	}

	public KVServer(int port, int cacheSize, String strategy) {
		// TODO Auto-generated method stub
	}

	/**
     * Initializes and starts the server. 
     * Loops until the the server should be closed.
     */
	@Override
    public void run() {
        
    	running = initializeServer();
        
        if(serverSocket != null) {
	        while(isRunning()){
	            try {
	                Socket client = serverSocket.accept();                
	                ClientConnection connection = 
	                		new ClientConnection(client);
	                new Thread(connection).start();
	                
	                logger.info("Connected to " 
	                		+ client.getInetAddress().getHostName() 
	                		+  " on port " + client.getPort());
	            } catch (IOException e) {
	            	logger.error("Error! " +
	            			"Unable to establish connection. \n", e);
	            }
	        }
        }
        logger.info("Server stopped.");
    }

	private boolean isRunning() {
        return this.running;
    }
	    /**
     * Stops the server insofar that it won't listen at the given port any more.
     */
    public void stopServer(){
        running = false;
        try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! " +
					"Unable to close socket on port: " + port, e);
		}
    }

    private boolean initializeServer() {
    	logger.info("Initialize server ...");
    	try {
            serverSocket = new ServerSocket(port);
            logger.info("Server listening on port: " 
            		+ serverSocket.getLocalPort());    
            return true;
        
        } catch (IOException e) {
        	logger.error("Error! Cannot open server socket:");
            if(e instanceof BindException){
            	logger.error("Port " + port + " is already bound!");
            }
            return false;
        }
    }
	
	@Override
	public int getPort(){
		return this.port;
	}

	@Override
    public String getHostname(){
		// TODO Auto-generated method stub
		return this.serverSocket.getInetAddress().getHostName(); 
	}

	@Override
    public CacheStrategy getCacheStrategy(){
		// TODO Auto-generated method stub
		return IKVServer.CacheStrategy.None;
	}

	@Override
    public int getCacheSize(){
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
    public boolean inStorage(String key){
		// TODO Auto-generated method stub

		File file = new File(STORAGE_DIRECTORY + "/" + key);
		if (file.exists()) {
			return true;
		}else{
			return false;
		}

	}

	@Override
    public boolean inCache(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public String getKV(String key) throws Exception{
		File file = new File(STORAGE_DIRECTORY + "/" + key);
		if (!file.exists()) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String value = reader.readLine();
		reader.close();
		return value;
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		// create a new file with the key as the name under the storage directory
		// write the value to the file
		// add the key to the index file ?
		// create a new file with the key as the name under the storage directory or update if already exists
		
		File file = new File(STORAGE_DIRECTORY + "/" + key + ".txt");
		boolean isFileCreated = true;
		if (file.exists()) {
			isFileCreated = false;
		} else {
			isFileCreated = file.createNewFile();
		}

		// write the value to the file
		FileWriter fileWriter = new FileWriter(file, false); // false for overwrite mode
		fileWriter.write(value);
		fileWriter.close();

		// // if file is created add the key to the index file
		// if (isFileCreated) {
		// 	File indexFile = new File(INDEX_FILE);
		// 	FileWriter indexFileWriter = new FileWriter(indexFile, true); // true for append mode
		// 	indexFileWriter.write(key + "\n");
		// 	indexFileWriter.close();
		// }

	}

	@Override
    public void clearCache(){
		// TODO Auto-generated method stub
	}

	@Override
	public void clearStorage(){
    File storageDirectory = new File(STORAGE_DIRECTORY);
    File[] files = storageDirectory.listFiles();
    if (files != null) {
        for (File file : files) {
            // if (!file.delete()) {
            //     throw new IOException("Failed to delete file: " + file.getName());
            // }
			try{
				file.delete();
			} catch(Exception e){
				e.printStackTrace();
			}
				
        }
    }
}


	@Override
    public void kill(){
		// TODO Auto-generated method stub
	}

	@Override
    public void close(){
		// TODO Auto-generated method stub
	}

	    /**
     * Main entry point for the echo server application. 
     * @param args contains the port number at args[0].
     */
    public static void main(String[] args) {
    	try {
			new LogSetup("logs/server.log", Level.ALL);
			if(args.length != 1) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port>!");
			} else {
				int port = Integer.parseInt(args[0]);
				System.out.println("Server is starting...");
				new KVServer(port).start();	
			}
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Invalid argument <port>! Not a number!");
			System.out.println("Usage: Server <port>!");
			System.exit(1);
		}
    }
}
