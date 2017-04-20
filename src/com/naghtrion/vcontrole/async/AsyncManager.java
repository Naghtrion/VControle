package com.naghtrion.vcontrole.async;

public class AsyncManager
{

    private static AsyncManager instance = null;
    private final AsyncQueue queue;


    public static AsyncManager getInstance()
    {
        if (instance == null)
            instance = new AsyncManager();
        return instance;
    }


    private AsyncManager()
    {
        queue = new AsyncQueue();
    }


    public void start()
    {
        Thread t = new Thread(queue);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }


    public void stop()
    {
        queue.kill();
    }


    public void addQueue(Runnable r)
    {
        queue.addQueue(r);
    }
}
