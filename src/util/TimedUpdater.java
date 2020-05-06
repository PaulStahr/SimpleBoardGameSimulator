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
package util;

import java.lang.ref.WeakReference;

public class TimedUpdater {
	private volatile boolean isRunning = false;
	private int sleepTime;
	@SuppressWarnings({"rawtypes" })
	private WeakReference updateHandler[] = new WeakReference[0];
	private int size = 0;
	private final Runnable runnable = new Runnable(){
		@Override
		public final void run(){
			long nextUpdate = System.nanoTime()/1000000;
			while (true){
            	final long lastUpdate = nextUpdate;
				final long waitTime = (nextUpdate+=sleepTime)-System.nanoTime()/1000000;
                try{
                	if (waitTime > 0)
                		Thread.sleep(waitTime);
                }catch (InterruptedException e){}

            	synchronized(updateHandler){
					if (size == 0){
						isRunning = false;
						return;
					}
					for (int i=0;i<size;i++){
						TimedUpdateHandler handler = (TimedUpdateHandler) updateHandler[i].get();
						if (handler == null){
							ArrayTools.remove(updateHandler, size--, i--);
						}else{
							try{
								final int updateInterval = handler.getUpdateInterval();
								for (long updateTime = ((lastUpdate + updateInterval - 1) / updateInterval) * updateInterval;updateTime < nextUpdate;updateTime += updateInterval)
									handler.update();
									
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	};
	
	public TimedUpdater(int sleepTime){
		this.sleepTime = sleepTime;
	}
	
	public void add(TimedUpdateHandler handler){
		synchronized(updateHandler){
			updateHandler = ArrayTools.add(updateHandler, size++, new WeakReference<TimedUpdateHandler>(handler));
			if (!isRunning){
				(new Thread(runnable, "Timed Updater")).start();
				isRunning = true;
			}
		}
	}
	
	public final boolean remove(TimedUpdateHandler handler){
		synchronized(updateHandler){
			size = ArrayTools.removeReferences(updateHandler, size, handler);
		}
		return false;
	}
}
