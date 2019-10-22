package com.company;

public class NumberOfTaskDone {

    private Integer[] m_NumberOfTaskDone;

    public NumberOfTaskDone(){
        m_NumberOfTaskDone = new Integer[1];
        m_NumberOfTaskDone[0] = 0;
    }

    public int getValue(){
        return m_NumberOfTaskDone[0];
    }

    public void setValue(int value){
        m_NumberOfTaskDone[0] = value;
    }


}
