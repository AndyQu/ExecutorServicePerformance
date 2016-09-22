package test;

import org.junit.Test;

public class Testor {
	@Test
	public void testA(){
		try {
			ExecServicePerformanceB.main(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
