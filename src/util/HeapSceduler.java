package util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeapSceduler implements Runnable{
    private Thread th;
    private Runnable rHeap[] = new Runnable[0];
    private long tHeap[] = new long[0];
    private int length;
    private boolean stop;
    private static final Logger logger = LoggerFactory.getLogger(HeapSceduler.class);
    
    public synchronized void enqueue(Runnable r, long nanoTime)
    {
        if (length == rHeap.length){
            rHeap = Arrays.copyOf(rHeap, Math.max(rHeap.length + 1, rHeap.length * 2));
            tHeap = Arrays.copyOf(tHeap, Math.max(tHeap.length + 1, tHeap.length * 2));
        }
        rHeap[length] = r;
        tHeap[length] = nanoTime;
        shiftUp(length);
        ++length;
    }
    
    private void shiftUp(int position)
    {
        long time = tHeap[position];
        Runnable r = rHeap[position];
        while(position != 0)
        {
            int parent = (position - 1) / 2;
            if (tHeap[parent] < time) {
                tHeap[position] = time;
                rHeap[position] = r;
                return;
            }
            tHeap[position] = tHeap[parent];
            rHeap[position] = rHeap[parent];
            position = parent;
        }
        tHeap[position] = time;
        rHeap[position] = r;
        notify();
    }
    
    
    public synchronized boolean start()
    {
        if (th != null) {return false;}
        stop = false;
        th = new Thread(this, "HeapSceduler");
        th.start();
        return true;
    }

    public synchronized void stop()
    {
        stop = true;
        this.notifyAll();
    }

    private void shiftDown(int index)
    {
        long time = tHeap[index];
        Runnable r= rHeap[index];
        while (true)
        {
           int next = index * 2 + (index * 2 + 2 < length && tHeap[index * 2 + 2] < tHeap[index * 2 + 1] ? 2 : 1);

           if (next >= length || time >= tHeap[next])
           {
              break;
           }
           rHeap[index] = rHeap[next];
           tHeap[index] = tHeap[index];
           index = next;
        }
        rHeap[index] = r;
        tHeap[index] = time;
    }

    @Override
    public void run() {
        while(!stop)
        {
            Runnable r;
            synchronized(this) {
                while(true)
                {
                    long currentTime = System.nanoTime();
                    try {
                        if (length == 0)
                        {
                            this.wait();
                        }
                        else if (tHeap[0] > currentTime)
                        {
                            long waittime = (tHeap[0] - currentTime) / 1000000l;
                            this.wait(waittime);
                        }
                        else
                        {
                            break;
                        }
                    } catch (InterruptedException e) {}
                }
                r = rHeap[0];
                --length;
                rHeap[0] = rHeap[length];
                tHeap[0] = tHeap[length];
                rHeap[length] = null;
                shiftDown(0);
            }
            try{
                r.run();
            }
            catch(Exception e)
            {
                logger.error("Exception in running sceduled event", e);
            }
        }
        th = null;
    }
}
