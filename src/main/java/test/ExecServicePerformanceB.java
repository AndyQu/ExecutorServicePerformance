package test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecServicePerformanceB {

	private static int COUNT = 100000;

	public static void main(String[] args) throws InterruptedException {
		System.out.println(".      priority:"+	Thread.currentThread().getPriority());

		 int cpus = Runtime.getRuntime().availableProcessors();

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
		for (int i = 0; i < 2000; i++) {
			double x = Math.random() * Math.random();
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
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			loop();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("thread:" + Thread.currentThread().getId() + ".     time end:" + endTime
				+ ".      time consumed:" + (endTime - startTime));

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
			System.out.println("thread:" + Thread.currentThread().getId() + ".      priority:"+	Thread.currentThread().getPriority()+".     time begin:" + startTime);
			/*
			 * while ( countdown-- > -1 ) { double x = Math.random() *
			 * Math.random(); }
			 */
			for (int i = 0; i < countdown; i++) {
				loop();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("thread:" + Thread.currentThread().getId() + ".      priority:"+	Thread.currentThread().getPriority()+".     time end:" + endTime
					+ ".      time consumed:" + (endTime - startTime));

		}
	}
}