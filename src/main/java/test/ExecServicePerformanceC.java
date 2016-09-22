package test;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecServicePerformanceC {

	private static int COUNT = 100000;

	public static void main(String[] args) throws InterruptedException {
		System.out.println(".      priority:" + Thread.currentThread().getPriority());

		int cpus = Runtime.getRuntime().availableProcessors();
//		cpus = 4;

		final ExecutorService es = Executors.newFixedThreadPool(cpus);

		final Vector<Batch> batches = new Vector<Batch>(cpus);

		final int batchComputations = COUNT / cpus;

		for (int i = 0; i < cpus; i++) {
			batches.add(new Batch(batchComputations));
		}

		System.out.println("provisioned " + cpus + " batches to be executed");

		// warmup
		// simpleCompuation();
		// computationWithObjCreation();
		// computationWithObjCreationAndExecutors( es, batches );

		long start = System.currentTimeMillis();
		simpleCompuation(COUNT);
		long stop = System.currentTimeMillis();
		System.out.println("simpleCompuation:" + (stop - start));

		start = System.currentTimeMillis();
		computationWithObjCreation();
		stop = System.currentTimeMillis();
		System.out.println("computationWithObjCreation:" + (stop - start));

		// Executor

		start = System.currentTimeMillis();
		computationWithObjCreationAndExecutors(es, batches);
		es.shutdown();
		es.awaitTermination(10, TimeUnit.MINUTES);
		// Note: Executor#shutdown() and Executor#awaitTermination() requires
		// some extra time. But the result should still be clear.
		stop = System.currentTimeMillis();
		System.out.println("computationWithObjCreationAndExecutors:" + (stop - start));
	}

	private static void loop() {
		/**
		 * 不要使用Math.random()*Math.random()。因为这种用法只产生一个Random作种子，后续的Math.random()调用都会使用同一个种子。
		 * 这样，多个线程之间产生竞争，使得ExecutorService的优势完全无法发挥。
		 */
		Random r = new java.util.Random();
		for (int i = 0; i < 2000; i++) {
			double x = r.nextDouble()*r.nextDouble();
		}
	}

	private static void computationWithObjCreation() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < COUNT; i++) {
			new Runnable() {

				@Override
				public void run() {
					loop();
				}

			}.run();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("thread:" + Thread.currentThread().getId() + ".     time end:" + endTime
				+ ".      time consumed:" + (endTime - startTime));
	}

	private static void simpleCompuation(int count) {
		Thread t1 = new Thread() {
			public void run() {
				long startTime = System.currentTimeMillis();
				for (int i = 0; i < count; i++) {
					if (i % 1000 == 0) {
						long t1 = System.nanoTime();
						loop();
						long t2 = System.nanoTime();
						println("thread:" + Thread.currentThread().getId() + ".      priority:"
								+ Thread.currentThread().getPriority()
								+ ".      time consumed:" + (t2 - t1));
					} else {
						loop();
					}
				}
				long endTime = System.currentTimeMillis();
				System.out.println("thread:" + Thread.currentThread().getId() + ".     time end:" + endTime
						+ ".      time consumed:" + (endTime - startTime));
			}
		};
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void computationWithObjCreationAndExecutors(ExecutorService es, List<Batch> batches)
			throws InterruptedException {

		System.out.println("computationWithObjCreationAndExecutors thread:" + Thread.currentThread().getId()
				+ ".     time begin:" + System.currentTimeMillis());
		for (Batch batch : batches) {
			es.submit(batch);
		}

	}

	private static class Batch implements Runnable {

		private final int computations;

		public Batch(final int computations) {

			this.computations = computations;
		}

		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			int countdown = computations;
			System.out.println("thread:" + Thread.currentThread().getId() + ".      priority:"
					+ Thread.currentThread().getPriority() + ".     count:" + countdown + ".     time begin:"
					+ startTime);
			/*
			 * while ( countdown-- > -1 ) { double x = Math.random() *
			 * Math.random(); }
			 */
			for (int i = 0; i < countdown; i++) {
				if (i % 1000 == 0) {
					long t1 = System.nanoTime();
					loop();
					long t2 = System.nanoTime();
					println("thread:" + Thread.currentThread().getId() + ".      priority:"
							+ Thread.currentThread().getPriority() + ".      time consumed:"
							+ (t2 - t1));
				} else {
					loop();
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println("thread:" + Thread.currentThread().getId() + ".      priority:"
					+ Thread.currentThread().getPriority() + ".     time end:" + endTime + ".      time consumed:"
					+ (endTime - startTime));

		}
	}
	private static void println(String str){
//		System.out.println(str);
	}
}