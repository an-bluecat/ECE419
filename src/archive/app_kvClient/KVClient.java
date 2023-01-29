// package app_kvClient;

// import client.KVCommInterface;
// import client.KVStore;
// import org.apache.log4j.BasicConfigurator;

// public class KVClient implements IKVClient {
//     KVStore kvCommStore;

//     @Override
//     public void newConnection(String hostname, int port) throws Exception{
//         // TODO Auto-generated method stub
//         // System.out.println("Getting new connect");
//         kvCommStore = new KVStore(hostname, port); 
//         kvCommStore.connect();
//     }

//     @Override
//     public KVCommInterface getStore(){
//         // TODO Auto-generated method stub
//         return kvCommStore;
//     }

//     public void run() throws Exception {
//         // System.out.println("Running\n");
//         // TODO: hard-coded. will need to add command line parsing to extract port and host. 
//         // *** change this here to your server machine (testing)*** 
//         newConnection("ug161.eecg.utoronto.ca", 1500);
//     }

//     public static void main(String[] args) throws Exception {
//         BasicConfigurator.configure(); //or else log4j reports error: no appenders found
//         KVClient kvClient = new KVClient();
//         kvClient.run(); // runs the run() function
//     }
// }
