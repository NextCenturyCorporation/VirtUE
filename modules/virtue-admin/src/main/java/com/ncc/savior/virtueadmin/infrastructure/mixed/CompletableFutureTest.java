package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.util.JavaUtil;

public class CompletableFutureTest {
	private static final Logger logger = LoggerFactory.getLogger(CompletableFutureTest.class);

	public static void main(String[] args) throws InterruptedException, ExecutionException {


		CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
				.thenCombine(CompletableFuture.supplyAsync(() -> " World"), (s1, s2) -> s1 + s2);

		String result = completableFuture.get();
		logger.debug("result: " + result);

		// CompletableFuture<String>[] cfs = new CompletableFuture[20];
		// for (int i = 0; i < 20; i++) {
		// cfs[i] = CompletableFuture.supplyAsync(new SupplierVal(i));
		// }
		//
		// CompletableFuture<Void> combined = CompletableFuture.allOf(cfs);
		// Void vResult = combined.get();

		CompletableFuture<Integer>[] cfi = new CompletableFuture[20];
		cfi[0] = CompletableFuture.supplyAsync(new Supplier<Integer>() {

			@Override
			public Integer get() {
				return 0;
			}
		});
		cfi[0] = new CompletableFuture();
		for (int i = 1; i < 20; i++) {
			cfi[i] = cfi[i - 1].thenApplyAsync(new Adder(i));
		}

		JavaUtil.sleepAndLogInterruption(3000);
		cfi[0].complete(0);

		logger.debug("result " + cfi[19].get());

	}

	private static class SupplierVal implements Supplier<String> {

		private int i;

		public SupplierVal(int i) {
			this.i = i;
		}

		@Override
		public String get() {

			int sleep = 300 + (i % 4 == 0 ? 6000 : 0);
			logger.debug("started get " + i + "  sleeping " + sleep);
			JavaUtil.sleepAndLogInterruption(sleep);
			logger.debug("after sleep " + i);
			return "i";
		}

	}

	private static class Adder implements Function<Integer, Integer> {

		private int i;

		public Adder(int i) {
			this.i = i;
		}

		@Override
		public Integer apply(Integer t) {
			logger.debug("" + t + " + " + i + " = " + (t + i));
			return t + i;
		}

	}

}
