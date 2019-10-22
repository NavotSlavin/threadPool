package com.company;

public class MyQueue<T> {

    private static class QueueNode<T> {
        private T m_Data;
        private QueueNode<T> m_Next;

        public QueueNode(T m_Data) {
            this.m_Data = m_Data;

        }
    }

    private QueueNode<T> m_First; //the m_First that we will get out from the queue
    private QueueNode<T> m_Last; //the m_Last that we will get out from the queue
    private int m_Size = 0;

    //------------------- pay attention this is a blocking queue --------------//
    public synchronized void add(T item) {
        QueueNode<T> temp = new QueueNode<T>(item);
        //queue isn't emppty
        if(m_Last != null) {
            m_Last.m_Next = temp;
        }
        m_Last = temp;
        //queue is empty
        if(m_First == null) {
            m_First = m_Last;
        }
        m_Size++;
        notifyAll();
    }

    public synchronized T remove() {

        if (m_First == null) {
            System.out.println("the queue is empty, there isn't item to remove");
        }
        T m_DataToReturn = m_First.m_Data;
        m_First = m_First.m_Next;
        //if we remove the m_Last element
        if(m_First == null) {
            m_Last = null;
        }
        m_Size--;
        notifyAll();
        return m_DataToReturn;
    }

    public T peek(){
        //queue is empty
        if(m_First == null) {
            System.out.println("the queue is empty, there isn't item to remove");
        }
        return m_First.m_Data;
    }

    public boolean isEmpty(){
        return m_First == null;
    }

    public int getSize(){
        return this.m_Size;
    }

}

