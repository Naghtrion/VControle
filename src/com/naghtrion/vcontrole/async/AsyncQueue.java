package com.naghtrion.vcontrole.async;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class AsyncQueue extends Thread
{

    private final ConcurrentLinkedQueue<Runnable> queue;
    private boolean stop = false;


    protected AsyncQueue()
    {
        queue = new ConcurrentLinkedQueue<>();
    }


    @Override
    public void run()
    {
        while (true)
        {
            synchronized (queue)
            {
                while (queue.isEmpty() && !stop)
                {
                    try
                    {
                        queue.wait();
                    }
                    catch (InterruptedException e)
                    {
                        Logger.getLogger(AsyncQueue.class.getName()).log(Level.SEVERE, null, e);
                        return;
                    }
                }
            }
            if (stop)
            {
                return;
            }
            try
            {
                Runnable r = queue.poll();
                r.run();
            }
            catch (Exception e)
            {
                Logger.getLogger(AsyncQueue.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }


    public void addQueue(Runnable r)
    {
        synchronized (queue)
        {
            queue.offer(r);
            queue.notify();
        }
    }


    public void kill()
    {
        stop = true;
        synchronized (queue)
        {
            queue.notify();
        }
    }

}
