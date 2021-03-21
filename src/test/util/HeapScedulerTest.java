package test.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HeapSceduler;

public class HeapScedulerTest {
    HeapSceduler hs = new HeapSceduler();
    private Logger logger = LoggerFactory.getLogger(HeapSceduler.class);
    
    @Test
    public void testHeapSceduler(){
        long time = System.nanoTime();
        final AtomicInteger counter = new AtomicInteger();
        
        int order[] = new int[] {2,0,1};
        for (int i = 0; i < order.length; ++i)
        {
            final int current = order[i];
            hs.enqueue(new Runnable() {
                @Override
                public void run() {
                    hs.checkHeapOrder();
                    assertEquals(hs.toString(), counter.get(), current);
                    counter.incrementAndGet();
                }
            }, time + current);    
        }
        hs.start();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            logger.error("Unexpected interrupt", e);
        }
        hs.stop();
        assertEquals("Value was " + counter.get() + " times incremented. expected 3; Heap: " + hs, counter.get(), 3);
    }
}
