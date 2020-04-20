/*******************************************************************************
 * Copyright (c) 2019 Paul Stahr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package main.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPool {
	private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);
	private final ArrayDeque<RunnableObject> toRun = new ArrayDeque<RunnableObject>();
	private final int timeout;
	private volatile int waiting, total;
	private int freeIds[];
	private int maxThreads;
	
	public ThreadPool(int timeout, int maxThreads) {
		this.timeout = timeout;
		this.maxThreads = maxThreads;
		freeIds = new int[maxThreads];
		ArrayUtil.iota(freeIds);
	}
	
	public ThreadPool(int timeout) {
		this(timeout, Runtime.getRuntime().availableProcessors());
	}
	
	public ThreadPool() {
		this(1000);
	}
	
	public final void run(Runnable r, String name) {
		run(r,name,null);
	}
	
	public int getMaxThreads()
	{
		return maxThreads;
	}
	
	public void setMaxThreads(int maxThreads)
	{
		this.maxThreads = maxThreads;
	}
	
	public static final int getCurrentId()
	{
		Thread current = Thread.currentThread();
		if (current instanceof RunnerThread)
		{
			return ((RunnerThread)current).id;
		}
		return -1;
	}
	
	public final void run(RunnableObject ro, boolean multipleQue){
		synchronized(toRun){
			if (ro.getState() == STATE_RUNNING)
			{
				ro.rerun = true;
				return;
			}
			if (ro.getState() == STATE_WAITING)
			{
				if (multipleQue)
				{
					ro.rerun = true;
				}
				return;
			}
			toRun.add(ro);
			ro.state = STATE_WAITING;
			if (waiting > 0)
			{
				toRun.notify();					
			}
			else if (total < maxThreads)
			{
				(new RunnerThread()).start();
			}
		}		
	}
	
	public final void run(Runnable r, String name, UncaughtExceptionHandler ueh) {
		run(new RunnableObject(r,name,ueh), false);
	}

	public static final byte STATE_RUNNING = 0, STATE_WAITING = 1, STATE_FINISHED = 2;
	
	public class ThreadLocal<E> extends AbstractList<E>{
		@SuppressWarnings("unchecked")
		private E obj[] = (E[]) new Object[maxThreads];
		
		public int size()
		{
			return obj.length;
		}
		
		public E get(int i)
		{
			return obj[i];
		}
		
		public ThreadLocal() {}
		
		public final E get()
		{
			int id = getCurrentId();
			if (id > obj.length)
			{
				return null;
			}
			return obj[id];
		}
		
		public final void set(E value)
		{
			if (obj.length < maxThreads)
			{
				obj = Arrays.copyOf(obj, maxThreads);
			}
			obj[getCurrentId()] = value;
		}
	}
	
	public static class RunnableObject implements Runnable{
		private final Runnable runnable;
		private final UncaughtExceptionHandler ueh;
		private final String name;
		private boolean rerun = false;
		private byte state = STATE_FINISHED;
		
		 
		public RunnableObject(Runnable runnable, String name, UncaughtExceptionHandler ueh){
			this.runnable = runnable;
			this.ueh = ueh;
			this.name = name;
		}
		
		public RunnableObject(String name, UncaughtExceptionHandler ueh)
		{
			this.runnable = null;
			this.ueh = ueh;
			this.name = name;
		}
		
		public final byte getState()
		{
			return state;
		}
		
		@Override
		public void run()
		{
			runnable.run();
		}
	}
	
	private class ParallelRunnableObject extends RunnableObject
	{
		private final int begin;
		private final int end;
		private final AtomicInteger finished;
		private final ParallelRangeRunnable prr;
		
		public ParallelRunnableObject(String name, UncaughtExceptionHandler ueh, int begin, int end, AtomicInteger finished, ParallelRangeRunnable prr)
		{
			super(null, name, ueh);
			this.begin = begin;
			this.end = end;
			this.finished = finished;
			this.prr = prr;
		}
		
		public void run()
		{
			try
			{
				prr.run(begin, end);
			}catch (Exception e)
			{
				throw e;
			}finally
			{
				synchronized (prr)
				{
					if (finished.decrementAndGet() == 0)
					{
						prr.notifyAll();
						prr.finished();
					}
				}
			}
		}
	}
	
	public final void runParallel(final ParallelRangeRunnable prr, String name, UncaughtExceptionHandler ueh, int from, final int to, final int maxBlockSize)
	{
		if (from == to)
		{
			return;
		}
		final int numBlocks = (to - from + maxBlockSize - 1) / maxBlockSize;
		final AtomicInteger finished = new AtomicInteger(numBlocks);
		for (int i = from; i < to; i += maxBlockSize)
		{
			run(new ParallelRunnableObject(name, ueh, i, Math.min(i + maxBlockSize, to), finished, prr), true);
		}
	}
	
	public final void runParallelAndWait(final ParallelRangeRunnable prr, String name, UncaughtExceptionHandler ueh, int from, final int to, final int maxBlockSize)
	{
		if (from == to)
		{
			return;
		}
		final int numBlocks = (to - from + maxBlockSize - 1) / maxBlockSize;
		final AtomicInteger finished = new AtomicInteger(numBlocks);
		for (int i = from; i < to; i += maxBlockSize)
		{
			run(new ParallelRunnableObject(name, ueh, i, Math.min(i + maxBlockSize, to), finished, prr), true);
		}
		while(finished.get() != 0)
		{
			if (Thread.currentThread() instanceof RunnerThread)
			{
				RunnableObject ro = null;
				synchronized(toRun)
				{
					ro = toRun.pollFirst();
				}
				if (ro != null)
				{
					ro.state = STATE_RUNNING;
					try {
						ro.run();
					} catch (Exception e) {
						if (ro.ueh == null)
							logger.error("Error at executing " + ro.name, e);
						else
							ro.ueh.uncaughtException(Thread.currentThread(), e);
					}
					if (ro.rerun)
					{
						ro.state = STATE_WAITING;
						ro.rerun = false;
						synchronized(toRun)
						{
							toRun.add(ro);
						}
					}
					else
					{
						ro.state = STATE_FINISHED;
					}
				}
			}
			else
			{
				synchronized(prr)
				{
					try {
						prr.wait(1000);
					} catch (InterruptedException e) {
						logger.error("Error at waiting", e);
					}
				}
			}
		}
	}
	
	public static interface ParallelRangeRunnable
	{
		public abstract void run(int from, int to);
		
		public void finished();
	}
	
	private final class RunnerThread extends Thread {
		private final int id;
		
		private RunnerThread(){
			id = freeIds[total++];
		}
		
		@Override
		public final void run() {
			RunnableObject ro = null;
			while (true){
				synchronized(toRun){
					if (ro != null){
						if (ro.rerun)
						{
							ro.state = STATE_WAITING;
							ro.rerun = false;
							toRun.add(ro);
						}
						else
						{
							ro.state = STATE_FINISHED;
						}
					}
					ro = toRun.pollFirst();
					if (ro == null){
						setName("RunnableRunner-Idle");
						++waiting;
						long time = System.currentTimeMillis();
						try {
							toRun.wait(timeout);
						} catch (InterruptedException e) {
							logger.error("Thread interrupted", e);
						}
						--waiting;
						ro = toRun.pollFirst();
						if (ro == null){
							if (System.currentTimeMillis() - time < timeout)
								continue;
							freeIds[--total] = id;
							return;
						}
					}
				}
				if (ro.name == null)
					setName("RunnableRunner");
				else
					setName(ro.name);
				ro.state = STATE_RUNNING;
				try {
					ro.run();
				} catch (Throwable e) {
					if (ro.ueh == null)
						logger.error("Error at executing " + ro.name, e);
					else
						ro.ueh.uncaughtException(Thread.currentThread(), e);
				} 
			}
		}
	}
}