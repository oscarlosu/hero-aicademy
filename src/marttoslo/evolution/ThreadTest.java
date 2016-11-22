package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.winterbe.java8.samples.concurrent.ConcurrentUtils;

public class ThreadTest {
	public static int taskCount = 100;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newWorkStealingPool();		
		// Define experiment
		Callable<String> task = () -> {
		    try {
		        TimeUnit.SECONDS.sleep(1);
		        return Thread.currentThread().getName();
		    }
		    catch (InterruptedException e) {
		        throw new IllegalStateException("task interrupted", e);
		    }
		};
		
		List<Callable<String>> callables = new ArrayList<Callable<String>>();
		for(int i = 0; i < taskCount; ++i) {
			callables.add(task);
		}

		List<Future<String>> futures = executor.invokeAll(callables);
		for(Future<String> f : futures) {
			System.out.println(f.get());
		}
		
		ConcurrentUtils.stop(executor);
		System.out.println("Done!");
	}
}
