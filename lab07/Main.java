package multipleProducersMultipleConsumers;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Main
{
	public static final int N = 5;
	public static final int N_CONSUMERS = 4;
	public static final int N_PRODUCERS = 3;
	public static int[] results = new int[N];

	public static void main(String[] args)
	{
		Buffer buffer = new Buffer();
		Thread threads[] = new Thread[N_CONSUMERS + N_PRODUCERS];

		for (int i = 0; i < N_PRODUCERS; i++)
			threads[i] = new Thread(new Producer(buffer, i));

		for (int i = N_PRODUCERS; i < N_CONSUMERS + N_PRODUCERS; i++)
			threads[i] = new Thread(new Consumer(buffer, i - N_PRODUCERS));

		for (int i = 0; i < N_CONSUMERS + N_PRODUCERS; i++)
			threads[i].start();

		for (int i = 0; i < N_CONSUMERS + N_PRODUCERS; i++)
		{
			try {
				threads[i].join();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		for (int i = 0; i < N; i++)
		{
			if (results[i] != N_PRODUCERS)
			{
				System.out.println("FAILED, the produced data does not match the consumed data");
				System.exit(1);
			}
		}
		System.out.println("CORRECT");
	}
}

class Buffer
{
	int[] a = new int[5];
	int last = -1;
	Semaphore s1 = new Semaphore(5);
	Semaphore s2 = new Semaphore(0);

	void put(int value)
	{
		if (last < 4)
		{
			try
			{
				s1.acquire();
			}
			catch (Exception e) {}

			a[++last] = value;
			s2.release();
		}
	}

	int get()
	{
		if (last > 0)
		{
			try
			{
				s2.acquire();
			}
			catch (Exception e) {}

			int b = a[last--];
			s1.release();
			return b;
		}
		return 0;
	}
}

class Consumer implements Runnable
{
	Buffer buffer;
	int id;
	static int i = 0;

	Consumer(Buffer buffer, int id)
	{
		this.buffer = buffer;
		this.id = id;
	}

	int getNextI()
	{
		int value;
		synchronized (Consumer.class)
		{
			value = i;
			i++;
		}
		return value;
	}

	@Override
	public void run()
	{
		while (true)
		{
			int i = getNextI();
			if (i >= Main.N_PRODUCERS * Main.N)
				break;
			synchronized (Main.results)
			{
				Main.results[buffer.get()]++;
			}
		}
		System.out.println("Consumer " + id + " finished Correctly");
	}
}

class Producer implements Runnable
{
	Buffer buffer;
	int id;

	Producer(Buffer buffer, int id)
	{
		this.buffer = buffer;
		this.id = id;
	}

	@Override
	public void run() {
		for (int i = 0; i < Main.N; i++)
		{
			buffer.put(i);
		}
		System.out.println("Producer " + id + " finished Correctly");
	}
}