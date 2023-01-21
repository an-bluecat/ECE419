package app_kvClient;

import client.KVCommInterface;
import client.KVStore;
import org.apache.log4j.BasicConfigurator;

public class KVClient implements IKVClient {
    @Override
    public void newConnection(String hostname, int port) throws Exception{
        // TODO Auto-generated method stub
    }

    @Override
    public KVCommInterface getStore(){
        // TODO Auto-generated method stub
        return null;
    }

    public void run() throws Exception {
        // System.out.println("Running\n");
        // TODO: hard-coded. will need to add command line parsing to extract port and host. 
        KVStore kvCommStore = new KVStore("localhost", 1500); 
        kvCommStore.connect();
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure(); //or else log4j reports error: no appenders found
        KVClient kvClient = new KVClient();
        kvClient.run(); // runs the run() function
    }
}
