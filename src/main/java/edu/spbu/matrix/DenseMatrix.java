package edu.spbu.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix {
    public double[][] data;
    /**
     * загружает матрицу из файла
     * @param fileName
     */
    public DenseMatrix(String fileName) {
        try {
            File f = new File(fileName);
            Scanner input = new Scanner(f);
            String[] line;
            ArrayList<Double[]> a = new ArrayList<>();
            Double[] tmp = {};
            while (input.hasNextLine()) {
                line = input.nextLine().split(" ");
                tmp = new Double[line.length];
                for (int i=0; i<tmp.length; i++) {
                    tmp[i] = Double.parseDouble(line[i]);
                }
                a.add(tmp);
            }
            double[][] result = new double[a.size()][tmp.length];
            for (int i=0; i<result.length; i++) {
                for (int j=0; j<result[0].length; j++) {
                    result[i][j] = a.get(i)[j];
                }
            }
            data = result;
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public DenseMatrix(double[][] r) {
        data = r;
    }
    /**
     * однопоточное умнджение матриц
     * должно поддерживаться для всех 4-х вариантов
     *
     * @param o
     * @return
     */
    public Matrix mul(Matrix o) {
        if (o instanceof DenseMatrix) {
            return mul((DenseMatrix) o);
        }
        if (o instanceof SparseMatrix) {
            return mul((SparseMatrix) o);
        } else return null;
    }

    public double[][] transpose() {
        double[][] dT = new double[data[0].length][data.length];
        for (int i=0; i<data[0].length; i++ ) {
            for (int j=0; j<data.length; j++ ) {
                dT[i][j] = data[j][i];
            }
        }
        return dT;
    }

    private DenseMatrix mul(DenseMatrix d) {
        double[][] dT = d.transpose();
        double[][] result = new double[data.length][dT.length];
        for (int i=0; i<data.length; i++) {
            result[i] = new double[dT.length];
        }

        for (int i=0; i<data.length; i++) {
            for (int j=0; j<dT.length; j++) {
                for (int k=0; k<dT[0].length; k++) {
                    result[i][j] += data[i][k]*dT[j][k];
                }
            }
        }
        return new DenseMatrix(result);
    }

    private DenseMatrix mul(SparseMatrix s) {
        SparseMatrix sT = s.transpose();
        double[][] result = new double[data.length][s.col];
        double sum = 0;
        for (int i = 0; i<data.length; i++){
            for (HashMap.Entry<Integer, HashMap<Integer, Double>> row2 : sT.map.entrySet()) {
                for (int k = 0; k<data[0].length; k++) {
                    if (row2.getValue().containsKey(k)) {
                        sum += data[i][k]*row2.getValue().get(k);
                    }
                }
                result[i][row2.getKey()] = sum;
                sum = 0;
            }
        }
        return new DenseMatrix(result);
    }

    public void toFile(String filename) {
        try {
            PrintWriter w = new PrintWriter(filename);
            for (int i = 0; i<data.length; i++) {
                for (int j = 0; j<data[0].length; j++) {
                    w.print(data[i][j]+" ");

                }
                w.println();
            }
            w.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
     public DenseMatrix dmul(Matrix o) {
         DenseMatrix d = (DenseMatrix)o;
         class Dispatcher {
             int value = 0;
             public int next() {
                 synchronized (this) {
                     return value++;
                 }
             }
         }

         double[][] result = new double[data.length][d.data[0].length];
         double[][] dT = d.transpose();
         Dispatcher dispatcher = new Dispatcher();

         class MultRow implements Runnable {
             Thread thread;

             public MultRow(String s) {
                 thread = new Thread(this, s);
                 thread.start();
             }
             public void run() {
                 int i = dispatcher.next();
                 for (int j=0; j<dT.length; j++) {
                     for (int k=0; k<dT[0].length; k++) {
                         result[i][j] += data[i][k]*dT[j][k];
                     }
                 }
             }
         }

         MultRow one = new MultRow("one");
         MultRow two = new MultRow("two");
         MultRow three = new MultRow("three");
         MultRow four = new MultRow("four");

         try {
             one.thread.join();
             two.thread.join();
             three.thread.join();
             four.thread.join();

         } catch (InterruptedException e){
             e.printStackTrace();
         }

         return new DenseMatrix(result);
     }

    /**
     * спавнивает с обоими вариантами
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        boolean y = true;
        if (o instanceof DenseMatrix) {
            DenseMatrix tmp = (DenseMatrix)o;
            if (data.length == tmp.data.length && data[0].length == tmp.data[0].length) {
                for (int i = 0; i<data.length; i++) {
                    for (int j=0; j<data[0].length; j++) {
                        if (data[i][j] != tmp.data[i][j]) {
                            y = false;
                        }
                    }
                }
            } else {
                y = false;
            }


        } else if (o instanceof SparseMatrix) {
            SparseMatrix tmp = (SparseMatrix)o;
            if (data.length == tmp.row && data[0].length == tmp.col) {
                for (int i = 0; i<data.length; i++) {
                    for (int j=0; j<data[0].length; j++) {
                        if (tmp.map.containsKey(i)) {
                            if (tmp.map.get(i).containsKey(j)) {
                                if (tmp.map.get(i).get(j) != data[i][j]) {
                                    y = false;
                                }
                            } else {
                                y = false;
                            }
                        } else {
                            y = false;
                        }
                    }
                }
            } else {
                y = false;
            }
        }
        return y;
    }

}
