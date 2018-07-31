package com.arisosoftware.vertbench;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.arisosoftware.vertbench.cpu.Murmur3;

class Task implements Runnable {

	public Task(String body) {
		this.info = new StopWatchInfo();
		this.body = body;
		info.Start();
		HashResultMask = 0xfffff000;
		HashResultPattern = 0x12300000;
	}

	public int HashResultMask;
	public int HashResultPattern;
	StopWatchInfo info;
	String body;

	String ResolveTheHashQuestion() {
		String reply = "";
		// Do the blocking operation in here
		info.Message = Thread.currentThread().getName();
		for (int i = 0; i < 100000000; i++) {
			String Bx = body + i;

			int hash32 = Murmur3.hash32(Bx.getBytes());
			if ((hash32 & HashResultMask) == HashResultPattern) {
				reply = String.format("[%s] + [%d] = [%X]", body, i, hash32);
				break;
			}
		}
		info.Stop();
		reply = String.format("Worker#%d %s  // %s", Thread.currentThread().getId(), reply, info.Report());
		return reply;
	}

	// Prints task name and sleeps for 1s
	// This Whole process is repeated 5 times
	public void run() {
		try {

			String message = ResolveTheHashQuestion();

			System.out.println(String.format("%s", message));

		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public class ThreadPools {
	// Maximum number of threads in thread pool
	static final int MAX_T = 3;

	public static void main(String[] args) {

		StopWatchInfo sf = new StopWatchInfo();
		sf.Message = "Main";
		sf.Start();

		//
		ArrayList<Runnable> tasklist = new ArrayList<Runnable>();

		for (int i = 0; i < 20; i++) {
			Runnable r2 = new Task("hello world #");
			tasklist.add(r2);
		}

		// creates a thread pool with MAX_T no. of
		// threads as the fixed pool size(Step 2)
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);

		// passes the Task objects to the pool to execute (Step 3)

		for (int i = 0; i < 20; i++) {
			pool.execute(tasklist.get(i));
		}

		sf.Stop();

// pool shutdown ( Step 4)
		pool.shutdown();
		System.out.println("=================\n\nTotal:" + sf.Report());

	}

}
