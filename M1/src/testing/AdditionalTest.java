package testing;

import org.junit.Test;

import junit.framework.TestCase;
import app_kvServer.KVServer;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	
	/**
	 * helper for re-usable code  in performance test
	 * 
	 * @param itest is the upper bound for ikey as a percent, indicates the percentage of put requests
	 * @param scaler is the number of keys to insert (pu)
	 */
	private void performanceTestHelper(int itest, int scaler) {
		String value = "Test Content 2023";

		KVServer server = new KVServer(5000);
		
		// put
		for (int ikey = 0; ikey <= scaler; ikey ++){ 
			// max key value is itest * scale 
			Exception ex = null;
			try {
				server.putKV(Integer.toString(ikey), value);
			} catch (Exception e){
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
					server.getKV(Integer.toString(ikey));
				} catch (Exception e) {
					ex = e;
				}
				assertTrue(ex == null);
				limit --;
			}
		}
		
	}

	/**
	 * test for server performance: puts 80% VS 20% gets
	 * 								puts 60% VS 40% gets
	 * 								puts 40% VS 60% gets
	 * 								puts 20% VS 80% gets
	 */
	public void testPerformance() {
		for (int itest = 20; itest <= 80; itest += 20 ){
			performanceTestHelper(itest, itest);
		}
	}
}
