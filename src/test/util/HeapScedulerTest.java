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
        hs.enqueue(new Runnable() {
            @Override
            public void run() {
                hs.checkHeapOrder();
                assertEquals(hs.toString(), counter.get(), 2);
                counter.incrementAndGet();
            }
        }, time + 300);
        hs.enqueue(new Runnable() {
            @Override
            public void run() {
                hs.checkHeapOrder();
                assertEquals(hs.toString(), counter.get(), 0);
                counter.incrementAndGet();
            }
        }, time + 100);
        hs.enqueue(new Runnable() {
            @Override
            public void run() {
                hs.checkHeapOrder();
                assertEquals(hs.toString(), counter.get(), 1);
                counter.incrementAndGet();
            }
        }, time + 200);
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
