package org.jbehave.eclipse.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbehave.eclipse.util.ProcessGroup;
import org.junit.Before;
import org.junit.Test;

public class ProcessGroupTest {

    private ProcessGroup<Integer> group;
    
    @Before
    public void setUp () {
        group = new ProcessGroup<Integer>(Executors.newFixedThreadPool(4));
    }
    
    @Test
    public void usecase () throws InterruptedException {
        final int NB = 100;
        final int NB_TASKS = 15;
        
        final AtomicInteger counter = new AtomicInteger();
        for(int i=0;i<NB;i++) {
            final int iRef = i;
            group.spawn(new Runnable () {
                @Override
                public void run() {
                    for(int i=0;i<NB_TASKS;i++) {
                        group.spawn(new Task(iRef+"-"+i, counter));
                    }
                }
            });
        }
        group.awaitTermination();
        assertThat(counter.get(), equalTo(NB*NB_TASKS));
    }
    
    private Random random = new Random(13L);
    
    class Task implements Runnable {
        final AtomicInteger counter;
        final String id;
        
        private Task(String id, AtomicInteger counter) {
            super();
            this.id = id;
            this.counter = counter;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(17));
                counter.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
