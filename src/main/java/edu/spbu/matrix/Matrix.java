package edu.spbu.matrix;

import java.util.Objects;

/**
 *
 */
public interface Matrix
{
    /**
     * однопоточное умнджение матриц
     * должно поддерживаться для всех 4-х вариантов
     * @param o
     * @return
     */
    Matrix mul(Matrix o);
    boolean equals(Object o);

    /**
     * многопоточное умножение матриц
     * @param o
     * @return
     */
    Matrix dmul(Matrix o);

}
