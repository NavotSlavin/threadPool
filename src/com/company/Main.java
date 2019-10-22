package com.company;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        // Reading from System.in
        Scanner reader = new Scanner(System.in);
        int m_NumberOfMatrix;
        int m_DimOfMatrix;
        int m_NumberOfThreads = 0;
        long[][][] m_ArrOfMatrixcs;
        NumberOfTaskDone m_NumberOfTaskDone;
        MyQueue<long[][]> m_QueueOfFillingMatrix = new MyQueue<long[][]>();
        ThreadPool m_ThreadPool;

        while(m_NumberOfThreads < 2 || m_NumberOfThreads > 20) {
            System.out.println("Please enter the number of threads(between 2 to 20):");
             m_NumberOfThreads = reader.nextInt();
        }
        m_ThreadPool = new ThreadPool(m_NumberOfThreads);
        /*
         we created this object to count the number of task done in the queueTask in
         the thread pool and we wanted to do this with reference.
        */
        m_NumberOfTaskDone = new NumberOfTaskDone();

        while (true) {
            m_NumberOfMatrix = 0;
            m_DimOfMatrix = 0;
            m_NumberOfTaskDone.setValue(0);
            while (m_NumberOfMatrix < 2) {
                System.out.println("Please enter a number of square matrixes(above 2):");
                m_NumberOfMatrix = reader.nextInt();
            }

            System.out.println("Please enter the dimension of a square matrix:");
            m_DimOfMatrix = reader.nextInt();


            //----------------------- init variables with the relevant inputs ---------------------------//
            m_ArrOfMatrixcs = new long[m_NumberOfMatrix][m_DimOfMatrix][m_DimOfMatrix];


            //--------- adding task of filling matrix -------//
            for (int i = 0; i < m_NumberOfMatrix; i++) {
                m_ThreadPool.enquque(new fillMatrix(m_ArrOfMatrixcs[i], m_QueueOfFillingMatrix, m_NumberOfTaskDone));
            }

            //this condition signal the main thread if he can continue to the print part
            while (m_NumberOfTaskDone.getValue() != ((2 * m_NumberOfMatrix) - 1)) {
                synchronized (m_QueueOfFillingMatrix) {
                    if ((m_QueueOfFillingMatrix.getSize() >= 2)) {
                        //adding a mult task
                        m_ThreadPool.enquque(new multMatrix(m_QueueOfFillingMatrix.remove(), m_QueueOfFillingMatrix.remove(),
                                m_NumberOfTaskDone, m_QueueOfFillingMatrix));
                    }
                    m_QueueOfFillingMatrix.notify();

                }
            }

            //------------- printing the result matrix -----------//
            // pay attention here the thread use busy waiting
            while (m_QueueOfFillingMatrix.getSize() != 1) { }
            System.out.println("The matrix is:");
            printMatrix(m_QueueOfFillingMatrix.remove());
            System.out.println();
        }
    }


    public static void printMatrix(long[][] i_MatrixToPrint) {
        for (int i = 0; i < i_MatrixToPrint.length; i++) {
            for (int j = 0; j < i_MatrixToPrint.length; j++) {
                System.out.print(i_MatrixToPrint[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void printQueueToMatrix(MyQueue<long[][]> i_QueueToPrint) {
        long[][] matrixToPrint;
        for (int i = 0; i < i_QueueToPrint.getSize(); i++) {
            matrixToPrint = i_QueueToPrint.remove();
            printMatrix(matrixToPrint);
        }
        System.out.println();
    }

    //method returns int random value from min to max include min and max
    public static int getRandom(int i_Min, int i_Max) {
        return (int)(Math.random() * ((i_Max - i_Min) + 1)) + i_Min;
    }

    //this class is a task that filling matrix with random number in range 0 to 10 and add the matrix to the queue
    private static class fillMatrix implements Runnable {
        long[][] m_Matrix;
        NumberOfTaskDone m_NumberOfTaskDone;
        MyQueue<long[][]> m_QueueToAddTo;

        public fillMatrix(long[][] i_Matrix, MyQueue<long[][]> i_QueueToAddTo, NumberOfTaskDone i_NumberOfTaskDone) {
            m_Matrix = i_Matrix;
            m_NumberOfTaskDone = i_NumberOfTaskDone;
            m_QueueToAddTo = i_QueueToAddTo;
        }

        @Override
        public void run() {
            synchronized (m_Matrix) {
                for (int i = 0; i < m_Matrix.length; i++) {
                    for (int j = 0; j < m_Matrix.length; j++) {
                        long value = (long) getRandom(0,10);
                        m_Matrix[i][j] = value;
                    }
                }
                //critical section ++ opperation
                synchronized (m_NumberOfTaskDone) {
                    m_NumberOfTaskDone.setValue(m_NumberOfTaskDone.getValue() + 1);
                    m_NumberOfTaskDone.notify();
                }

                //add is synchronized
                m_QueueToAddTo.add(m_Matrix);
                m_Matrix.notify();
            }
        }
    }

    //this class is a task that get 2 matrix , create result matrix and mult them to result matrix and add to the queue
    private static class multMatrix implements Runnable {
        MyQueue<long[][]> m_QueueOfMatrix;
        long[][] m_Matrix1;
        long[][] m_Matrix2;
        long[][] m_ResultMatrix;
        MyQueue<long[][]> m_QueueToAddTo;
        NumberOfTaskDone m_NumberOfTaskDone;


        public multMatrix(long[][] i_Matrix1, long[][] i_Matrix2, NumberOfTaskDone i_NumberOfTaskDone, MyQueue<long[][]> i_QueueToAddTo) {
            m_Matrix1 = i_Matrix1;
            m_Matrix2 = i_Matrix2;
            m_ResultMatrix = new long[m_Matrix1.length][m_Matrix1.length];
            m_NumberOfTaskDone = i_NumberOfTaskDone;
            m_QueueToAddTo = i_QueueToAddTo;
        }

        @Override
        public void run() {

            int M1Rows = m_Matrix1.length;
            int M1Colums = m_Matrix1[0].length;
            int M2Rows = m_Matrix2.length;
            int M2Colums = m_Matrix2[0].length;

            if (M1Colums != M2Rows) {
                throw new IllegalArgumentException("M1:Rows: " + M1Colums + " did not match M2:Columns " + M2Rows + ".");
            }

            for (int i = 0; i < M1Rows; i++) { // aRow
                for (int j = 0; j < M2Colums; j++) { // bColumn
                    for (int k = 0; k < M1Colums; k++) { // aColumn
                        m_ResultMatrix[i][j] += m_Matrix1[i][k] * m_Matrix2[k][j];
                    }
                }
            }
            synchronized (m_NumberOfTaskDone) {
                m_NumberOfTaskDone.setValue(m_NumberOfTaskDone.getValue() + 1);
                m_NumberOfTaskDone.notify();
            }

            //adding the result matrix to the queue
            m_QueueToAddTo.add(m_ResultMatrix);

        }
    }

}


