package testing;

import org.junit.Test;

import junit.framework.TestCase;
import app_kvServer.KVServer;
//import app_kvClient.KVClient;
import java.util.ArrayList;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
		
		/**
		 * test that the inserted value is what got returned by get of same key
		 */
	public void testPutPath() {
		Exception er = null;
		KVServer server = new KVServer(5000);
		try {
			server.putKV("testPutPath", "file key content");
		}  catch (Exception e) {
			er = e;
		}
		assertTrue (er == null);
		
		String res = "";
		try {
			res = server.getKV("testPutPath.txt");
		} catch (Exception e) {
			er = e;
		}
		assertTrue (er == null);
		assertEquals (res, "file key content");
	}
	
//	
//	public void testCommunicationModule() {
//		KVClient client = new KVClient();
//		
//		client.
//	}
	
	/**
	 * helper for re-usable code  in performance test
	 * 
	 * @param itest is the upper bound for ikey as a percent, indicates the percentage of put requests
	 * @param scaler is the number of keys to insert (pu)
	 */
	private double performanceTestHelper(int itest, int scaler, String value) {

		KVServer server = new KVServer(5000);
		
		double start = System.currentTimeMillis();
		
		// put
		for (int ikey = 0; ikey <= scaler; ikey ++){ 
			// max key value is itest * scale 
			Exception ex = null;
			try {
				server.putKV(Integer.toString(ikey), value);
			} catch (Exception e){
				e.printStackTrace();
				ex = e;
			}
			assertTrue(ex == null);
		}
		
		// get: in a loop, query the inserted keys until 
		// the limit is reached
		int limit = Math.round(scaler / itest * (100 - itest));
		while (limit > 0) {
			for (int ikey = 0; ikey <= scaler && limit > 0; ikey ++) {
				Exception ex = null;
				try {
					server.getKV(Integer.toString(ikey) + ".txt");
				} catch (Exception e) {
					ex = e;
				}
				assertTrue(ex == null);
				limit --;
			}
		}
		double elapsed = System.currentTimeMillis() - start;
		double reqs = limit + scaler;
	
		return (elapsed / reqs);
		
	}

	/**
	 * test for server performance: puts 80% VS 20% gets
	 * 								puts 60% VS 40% gets
	 * 								puts 40% VS 60% gets
	 * 								puts 20% VS 80% gets
	 */
	public void testPerformance() {
//		String testString = "M1 performance test";
		String testString = "Let me not to the marriage of true minds\r\n"
				+ "Admit impediments. Love is not love\r\n"
				+ "Which alters when it alteration finds,\r\n"
				+ "Or bends with the remover to remove:\r\n"
				+ "O, no! it is an ever-fixed mark,\r\n"
				+ "That looks on tempests and is never shaken;\r\n"
				+ "It is the star to every wandering bark,\r\n"
				+ "Whose worth’s unknown, although his height be taken.\r\n"
				+ "Love’s not Time’s fool, though rosy lips and cheeks\r\n"
				+ "Within his bending sickle’s compass come;\r\n"
				+ "Love alters not with his brief hours and weeks,\r\n"
				+ "But bears it out even to the edge of doom.\r\n"
				+ "If this be error and upon me proved,\r\n"
				+ "I never writ, nor no man ever loved.";
		
		System.out.printf("Performance test average payloads\n");
		for (int itest = 20; itest <= 80; itest += 10 ){
			Double res = performanceTestHelper(itest, itest, testString);
			System.out.printf("ratio of puts: " + Integer.toString(itest) + " is: "+ Double.toString(res)+"\n");
		}
	
	}
}
