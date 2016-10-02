package test;

import java.io.IOException;

import org.junit.Test;

public class Testor {
	@Test
	public void testA(){
		try {
			ExecServicePerformanceC.main(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
