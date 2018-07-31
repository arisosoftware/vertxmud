package com.arisosoftware.vertbench;

import com.arisosoftware.vertbench.cpu.Murmur3;

public class OldMultiThreadDemo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int n = 20; // Number of threads
		for (int i = 0; i < n; i++) {
			StopWatchInfo info = new StopWatchInfo();
			MultithreadingDemo object = new MultithreadingDemo(info, "hello world #");
			object.start();
		}
	}

}

// Java code for thread creation by extending
// the Thread class
class MultithreadingDemo extends Thread {

	public MultithreadingDemo(StopWatchInfo info, String body) {
		this.info = info;
		this.body = body;
		HashResultMask = 0xfffff000;
		HashResultPattern = 0x12300000;
	}

	public void run() {
		try {
			// Displaying the thread that is running
			this.info.Start();
			String message = ResolveTheHashQuestion();

			System.out.println(String.format("%s", message));
		} catch (Exception e) {
			// Throwing an exception
			System.out.println("Exception is caught");
		}
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
}
