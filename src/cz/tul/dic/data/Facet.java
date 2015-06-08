/* Copyright (C) LENAM, s.r.o. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Petr Jecmen <petr.jecmen@tul.cz>, 2015
 */
package cz.tul.dic.data;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author Petr Jecmen
 */
public final class Facet implements Serializable {

    private final int[] data;
    private final double[] center;
    private final int size;

    private Facet(final int[] data, final double[] center, final int size) {
        this.data = data;
        this.center = center;
        this.size = size;
    }

    public static Facet createFacet(final int size, final int... topLeft) {
        if (topLeft.length < Coordinates.DIMENSION) {
            throw new IllegalArgumentException("Not enough coordinates for facet center (" + (Coordinates.DIMENSION - topLeft.length) + " more needed).");
        }

        final int halfSize = size / 2;
        final double centerX = topLeft[Coordinates.X] + halfSize;
        final double centerY = topLeft[Coordinates.Y] + halfSize;

        final int[] data = new int[size * size * Coordinates.DIMENSION];

        int index;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                index = (x * size + y) * Coordinates.DIMENSION;

                data[index] = topLeft[Coordinates.X] + x;
                data[index + 1] = topLeft[Coordinates.Y] + y;
            }
        }
        return new Facet(data, new double[]{centerX, centerY}, size);
    }

    public int[] getData() {
        return data;
    }

    public double[] getCenter() {
        return center;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Facet -- loc : ");
        sb.append(Arrays.toString(center));
        sb.append(", size : ");
        sb.append(size);
        return sb.toString();
    }

}
