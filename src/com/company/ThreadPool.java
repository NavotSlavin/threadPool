package com.company;
public class ThreadPool{
    private MyQueue<Runnable> m_QueueTask;
    private threadWorker[] m_Threads;


    //constructor
    public ThreadPool(int i_NumberOfThreads) {
        m_QueueTask = new MyQueue<Runnable>();
        m_Threads = new threadWorker[i_NumberOfThreads];
        for(int i = 0; i < m_Threads.length; i++) {
            m_Threads[i] = new threadWorker();
            m_Threads[i].start();
        }
    }

    public void enquque(Runnable i_CurrentRunner) {
        synchronized (m_QueueTask){
            try {
                //add runner to the end of the queue
                m_QueueTask.add(i_CurrentRunner);
                //inform that the monitor is free and awake random thread
                m_QueueTask.notify();
            } catch(Exception err){
                Thread.currentThread().interrupt();
                // we are being interrupted so we should stop running
                return;
            }
        }

    }

    public MyQueue<Runnable> getQueueTask(){
        return m_QueueTask;
    }

    public threadWorker[] getThreadWorker(){
        return m_Threads;
    }


    public class threadWorker extends Thread {
        public void run() {
            Runnable runner;
            while (true) {
                synchronized (m_QueueTask) {
                    while (m_QueueTask.isEmpty()) {
                        try {
                            m_QueueTask.wait();
                        } catch (InterruptedException err) {
                            Thread.currentThread().interrupt();
                            // we are being interrupted so we should stop running
                            return;
                        }
                    }
                    //remove the first task to implement
                    runner = (Runnable) m_QueueTask.remove();
                }
                try {
                    runner.run();
                } catch (Exception err) {
                    throw err;
                }
            }
        }
    }
}

