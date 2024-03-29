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

    public synchronized boolean isEnqueued(Runnable r) {return ArrayUtil.linearSearch(rHeap, 0,length, r) >= 0;}

    public synchronized boolean enqueue(Runnable r, long nanoTime, boolean multipleAllowed)
    {
        if (!multipleAllowed && isEnqueued(r)) {
            return false;
        }
        if (length == rHeap.length){
            rHeap = Arrays.copyOf(rHeap, Math.max(rHeap.length + 1, rHeap.length * 2));
            tHeap = Arrays.copyOf(tHeap, Math.max(tHeap.length + 1, tHeap.length * 2));
        }
        rHeap[length] = r;
        tHeap[length] = nanoTime;
        shiftUp(length);
        ++length;
        return true;
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
           int next = index * 2 + 1;
           next += (next + 1 < length && tHeap[next] > tHeap[next + 1] ? 1 : 0);

           if (next >= length || time <= tHeap[next]){break;}
           rHeap[index] = rHeap[next];
           tHeap[index] = tHeap[next];
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
            long t;
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
                            this.wait((tHeap[0] - currentTime + 999999) / 1000000l);
                        }
                        else
                        {
                            break;
                        }
                    } catch (InterruptedException e) {}
                }
                t = tHeap[0];
                r = rHeap[0];
                --length;
                rHeap[0] = rHeap[length];
                tHeap[0] = tHeap[length];
                rHeap[length] = null;
                shiftDown(0);
            }
            try{
                long beginTime = System.nanoTime();
                if (beginTime - t > 100000000){logger.warn("Delayed method call (" + (beginTime - t) / 1000000 + "ms possibly high system load");}
                r.run();
                long finishTime = System.nanoTime();
                if (finishTime - beginTime > 10000000) {logger.warn("Method computation took very long (" + (finishTime - beginTime) / 1000000 + "ms consider using a seperate thread for computation if this happens frequently");}
            }
            catch(Throwable e)
            {
                logger.error("Exception in running sceduled event", e);
            }
        }
        th = null;
    }

    public synchronized void checkHeapOrder() {
        for (int i = 1; i < length; ++i)
        {
            if (tHeap[i] < tHeap[(i - 1) / 2])
            {
                throw new RuntimeException("Heap order is wrong [" + i + "]=" + tHeap[i] + "<["+ (i - 2)/2 + "]=" +tHeap[(i - 1) / 2]);
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(tHeap, length));
    }
}
