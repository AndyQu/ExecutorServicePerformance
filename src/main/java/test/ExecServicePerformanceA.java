package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecServicePerformanceA {

	private static int COUNT = 100000;

	public static void main(String[] args) throws InterruptedException {

		// warmup
		simpleCompuation();
		computationWithObjCreation();
		computationWithObjCreationAndExecutors();

		long start = System.currentTimeMillis();
		simpleCompuation();
		long stop = System.currentTimeMillis();
		System.out.println("simpleCompuation:" + (stop - start));

		start = System.currentTimeMillis();
		computationWithObjCreation();
		stop = System.currentTimeMillis();
		System.out.println("computationWithObjCreation:" + (stop - start));

		start = System.currentTimeMillis();
		computationWithObjCreationAndExecutors();
		stop = System.currentTimeMillis();
		System.out.println("computationWithObjCreationAndExecutors:" + (stop - start));

	}

	private static void computationWithObjCreation() {
		for (int i = 0; i < COUNT; i++) {
			new Runnable() {

				@Override
				public void run() {
					double x = Math.random() * Math.random();
				}

			}.run();
		}

	}

	private static void simpleCompuation() {
		for (int i = 0; i < COUNT; i++) {
			double x = Math.random() * Math.random();
		}

	}

	private static void computationWithObjCreationAndExecutors() throws InterruptedException {

		ExecutorService es = Executors.newFixedThreadPool(8);
		for (int i = 0; i < COUNT; i++) {
			es.submit(new Runnable() {
				@Override
				public void run() {
					double x = Math.random() * Math.random();
				}
			});
		}
		es.shutdown();
		es.awaitTermination(10, TimeUnit.SECONDS);
	}
}