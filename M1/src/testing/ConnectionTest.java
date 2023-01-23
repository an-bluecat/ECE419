package testing;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.rules.TestWatcher;

import client.KVStore;
import jdk.jfr.Description;
import junit.framework.TestCase;


public class ConnectionTest extends TestCase {

//	@Rule
//	public TestWatcher watcher = new TestWatcher() {
//		@Override
//		protected void failed(Throwable e, Description description) {
//			if (e instanceof NullPointerException) {
//				Assume.assumeTrue(false);
//			}
//		}
//	};
	public void testUnknownHost() {
		System.out.println("unknown");
		Exception ex = null;
		KVStore kvClient = new KVStore("unknown", 50000);
		
		try {
			kvClient.connect();
		} catch (Exception e) {
			ex = e; 
		}
		
		assertTrue(ex instanceof UnknownHostException);
	}
	
	
	public void testIllegalPort() {
		System.out.println("illegal");
		Exception ex = null;
		KVStore kvClient = new KVStore("localhost", 123456789);
		
		try {
			kvClient.connect();
		} catch (Exception e) {
			ex = e; 
		}
		
		assertTrue(ex instanceof IllegalArgumentException);
	}
	
	

	public void testConnectionSuccess() {
		System.out.println("ss");
		Exception ex = null;
		
		KVStore kvClient;
		kvClient = new KVStore("localhost", 50000);
		System.out.println("herre1");
		try {
			kvClient.connect();
			System.out.println("here2");
		} catch (Exception e) {
			ex = e;
			System.out.println("here3");
		}	
		System.out.println("here4");
		assertNull(ex);
	}
	
	
}

