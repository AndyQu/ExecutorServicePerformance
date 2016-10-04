package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ExecServicePerformanceC {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecServicePerformanceC.class);
	private static final int COUNT = 50000;
	
	private static final int START = 2;
	private static final int STEP = 4;
//	private static final int END = 2000;
	private static final int END = 30;
	
	private static final int WorkLoad=10000;
//	private static final int WorkLoad=5000;
//	private static final int WorkLoad=1;

	public static void main(String[] args) throws InterruptedException, IOException {
		long prevTime = 0;
		long runTime = 0;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF8")));
		LOGGER.info("press Enter to start working:");
		reader.readLine();//使用gradle执行时，被忽略
		Thread.sleep(10*1000);

		LOGGER.info("WorkLoad:"+WorkLoad);
		
		List<List<Object>> stat = Lists.newArrayList();
		stat.add(Arrays.asList(new Object[]{"threadsNum","run-time(ms)", "percentage"}));
		for (int threadsNum = START; threadsNum <= END; threadsNum += STEP) {
			//LOGGER.info("New test begin. Run Jstack");
			runTime = runTest(threadsNum);
			//LOGGER.info("sleep 10s");
			double percentage =prevTime==0?0:(runTime - prevTime)*1.0 / prevTime;
			stat.add(Arrays.asList(new Object[]{
					threadsNum, 
					runTime, 
					Math.round(percentage*100.0)/100.0
			}));
			prevTime=runTime;
			Thread.sleep(2*1000);
		}
		writeStat(stat,"/tmp/ExecServicePerformanceC.csv");
		LOGGER.info("Good Night~~~(Please Say Goodbye, Otherwise I won't quit!!!)");
		reader.readLine();//使用gradle执行时，被忽略
		Thread.sleep(50*1000);
	}
	
	private static void writeStat(List<List<Object>> stat, String filePath){
		try {
			//dont append
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));
			for(List<Object> row:stat){
				for(Object e:row){
					writer.write(String.format("%s,", e));
				}
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static long runTest(int threadNum) throws InterruptedException {
		final ExecutorService es = Executors.newFixedThreadPool(threadNum);

		final Vector<Batch> batches = new Vector<Batch>(threadNum);

		final Double batchComputations = Math.ceil(COUNT*1.0 / threadNum);

		for (int i = 0; i < threadNum; i++) {
			batches.add(new Batch(batchComputations.intValue()));
		}

		long start=0;
		long stop=0;
		LOGGER.info("provisioned " + threadNum + " batches to be executed");
/*
		start = System.currentTimeMillis();
		simpleCompuation(COUNT);
		stop = System.currentTimeMillis();
		LOGGER.info("simpleCompuation:" + (stop - start));
*/
/*
		start = System.currentTimeMillis();
		computationWithObjCreation();
		stop = System.currentTimeMillis();
		LOGGER.info("computationWithObjCreation:" + (stop - start));
*/
		// Executor

		start = System.currentTimeMillis();
		computationWithObjCreationAndExecutors(es, batches);
		es.shutdown();
		es.awaitTermination(10, TimeUnit.MINUTES);
		// Note: Executor#shutdown() and Executor#awaitTermination() requires
		// some extra time. But the result should still be clear.
		stop = System.currentTimeMillis();
		LOGGER.info("computationWithObjCreationAndExecutors:" + (stop - start));
		return stop - start;
	}

	private static void unitWork() {
		/**
		 * 不要使用Math.random()*Math.random()。因为这种用法只产生一个Random作种子，后续的Math.random()
		 * 调用都会使用同一个种子。 这样，多个线程之间产生竞争，使得ExecutorService的优势完全无法发挥。
		 */
		Random r = new java.util.Random();
		for (int i = 0; i < WorkLoad; i++) {
			double x = r.nextDouble() * r.nextDouble();
		}
	}

	private static void computationWithObjCreation() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < COUNT; i++) {
			new Runnable() {

				@Override
				public void run() {
					unitWork();
				}

			}.run();
		}
		long endTime = System.currentTimeMillis();
		println("thread:" + Thread.currentThread().getId() + ".     time end:" + endTime + ".      time consumed:"
				+ (endTime - startTime));
	}

	private static void simpleCompuation(int count) {
		Thread t1 = new Thread() {
			public void run() {
				long startTime = System.currentTimeMillis();
				for (int i = 0; i < count; i++) {
					if (i % 1000 == 0) {
						long t1 = System.nanoTime();
						unitWork();
						long t2 = System.nanoTime();
						println("thread:" + Thread.currentThread().getId() + ".      priority:"
								+ Thread.currentThread().getPriority() + ".      time consumed:" + (t2 - t1));
					} else {
						unitWork();
					}
				}
				long endTime = System.currentTimeMillis();
				println("thread:" + Thread.currentThread().getId() + ".     time end:" + endTime
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

		println("computationWithObjCreationAndExecutors thread:" + Thread.currentThread().getId() + ".     time begin:"
				+ System.currentTimeMillis());
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
			LOGGER.info("event=thread_begin name={} startTime={}", Thread.currentThread().getName(), startTime);
			/*
			 * while ( countdown-- > -1 ) { double x = Math.random() *
			 * Math.random(); }
			 */
			for (int i = 0; i < countdown; i++) {
				if (i % 1000 == 0) {
					long t1 = System.nanoTime();
					unitWork();
					long t2 = System.nanoTime();
					println("thread:" + Thread.currentThread().getId() + ".      priority:"
							+ Thread.currentThread().getPriority() + ".      time consumed:" + (t2 - t1));
				} else {
					unitWork();
				}
			}
			long endTime = System.currentTimeMillis();
			LOGGER.debug("event=thread_finish name={} startTime={} endTime={} time={}", Thread.currentThread().getName(), startTime, endTime, endTime-startTime);
		}
	}

	private static void println(String str) {
		 LOGGER.debug(str);
	}
}
