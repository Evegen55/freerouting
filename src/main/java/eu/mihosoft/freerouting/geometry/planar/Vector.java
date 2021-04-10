/*
 *   Copyright (C) 2014  Alfons Wirtz
 *   website www.freerouting.net
 *
 *   Copyright (C) 2017 Michael Hoffer <info@michaelhoffer.de>
 *   Website www.freerouting.mihosoft.eu
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/>
 *   for more details.
 *
 * Vector.java
 *
 * Created on 1. Februar 2003, 14:28
 */

package eu.mihosoft.freerouting.geometry.planar;

import java.io.Serializable;
import java.math.BigInteger;

import eu.mihosoft.freerouting.datastructures.Signum;

/**
 * Abstract class describing functionality of Vectors. Vectors are used for translating Points in the plane.
 *
 * @author Alfons Wirtz
 */

public abstract class Vector implements Serializable {

    /**
     * returns true, if this vector is equal to the zero vector.
     */
    public abstract boolean is_zero();

    /**
     * returns the Vector such that this plus this.negate() is zero
     */
    public abstract Vector negate();

    /**
     * adds vector to this vector
     */
    public abstract Vector add(Vector vector);

    /**
     * Let L be the line from the Zero Vector to vector. The function returns Side.ON_THE_LEFT, if this Vector is on the
     * left of L Side.ON_THE_RIGHT, if this Vector is on the right of L and Side.COLLINEAR, if this Vector is collinear
     * with L.
     */
    public abstract Side sideOf(Vector vector);

    /**
     * returns true, if the vector is horizontal or vertical
     */
    public abstract boolean isOrthogonal();

    /**
     * returns true, if the vector is diagonal
     */
    public abstract boolean isDiagonal();

    /**
     * Returns true, if the vector is orthogonal or diagonal
     */
    public boolean isMultipleOf45Degree() {
        return isOrthogonal() || isDiagonal();
    }

    /**
     * The function returns Signum.POSITIVE, if the scalar product of this vector and vector {@literal >} 0,
     * Signum.NEGATIVE, if the scalar product Vector is {@literal <} 0, and Signum.ZERO, if the scalar product is equal
     * 0.
     */
    public abstract Signum projection(Vector vector);

    /**
     * Returns an approximation of the scalar product of this vector with vector by a double.
     */
    public abstract double scalarProduct(Vector vector);

    /**
     * approximates the coordinates of this vector by float coordinates
     */
    public abstract FloatPoint toFloat();

    /**
     * Turns this vector by factor times 90 degree.
     */
    public abstract Vector turn90Degree(int factor);

    /**
     * Mirrors this vector at the x axis.
     */
    public abstract Vector mirrorAtXAxis();

    /**
     * Mirrors this vector at the y axis.
     */
    public abstract Vector mirrorAtYAxis();

    /**
     * Standard implementation of the zero vector .
     */
    public static final IntVector ZERO = new IntVector(0, 0); //TODO

    /**
     * Creates a Vector (x, y) in the plane.
     */
    public static Vector getInstance(int x, int y) {
        IntVector result = new IntVector(x, y);
        if (Math.abs(x) > Limits.CRIT_INT ||
            Math.abs(y) > Limits.CRIT_INT) {
            return new RationalVector(result);
        }
        return result;
    }

    /**
     * Creates a 2-dimensinal Vector from the 3 input values. If z != 0 it correspondents to the Vector in the plane
     * with rational number coordinates (x / z, y / z).
     */
    public static Vector getInstance(
            BigInteger x,
            BigInteger y,
            BigInteger z
    ) {
        if (z.signum() < 0) {
            // the dominator z of a RationalVector is expected to be positive
            x = x.negate();
            y = y.negate();
            z = z.negate();

        }
        if ((x.mod(z)).signum() == 0 && (x.mod(z)).signum() == 0) { //TODO possible bug maybe second pard is y.mod(z)
            // x and y can be divided by z
            x = x.divide(z);
            y = y.divide(z);
            z = BigInteger.ONE;
        }
        if (z.equals(BigInteger.ONE)) {
            if ((x.abs()).compareTo(Limits.CRIT_INT_BIG) <= 0 &&
                (y.abs()).compareTo(Limits.CRIT_INT_BIG) <= 0) {
                // the Point fits into an IntPoint
                return new IntVector(x.intValue(), y.intValue());
            }
        }
        return new RationalVector(x, y, z);
    }

    /**
     * returns an approximation of the euclidian length of this vector
     */
    public double lengthApprox() {
        return toFloat().size();
    }


    /**
     * Returns an approximation of the cosinus of the angle between this vector and  vector by a double.
     */
    public double cosAngle(Vector vector) {
        double result = scalarProduct(vector);
        result /= toFloat().size() * vector.toFloat().size();
        return result;
    }

    /**
     * Returns an approximation of the signed angle between this vector and vector.
     */
    public double angleApprox(Vector vector) {
        double result = Math.acos(cosAngle(vector));
        if (this.sideOf(vector) == Side.ON_THE_LEFT) {
            result = -result;
        }
        return result;
    }

    /**
     * Returns an approximation of the signed angle between this vector and the x axis.
     */
    public double angleApprox() {
        Vector other = new IntVector(1, 0);
        return other.angleApprox(this);
    }

    /**
     * Returns an approximation vector of this vector with the same direction and length p_length.
     */
    public abstract Vector changeLengthApprox(double lenght);

    abstract Direction toNormalizedDirection();


    // auxiliary functions needed because the virtual function mechanism
    // does not work in parameter position

    abstract Vector add(IntVector p_other);

    abstract Vector add(RationalVector p_other);

    abstract Point addTo(IntPoint p_point);

    abstract Point addTo(RationalPoint p_point);

    abstract Side sideOf(IntVector p_other);

    abstract Side sideOf(RationalVector p_other);

    abstract Signum projection(IntVector p_other);

    abstract Signum projection(RationalVector p_other);

    abstract double scalarProduct(IntVector p_other);

    abstract double scalarProduct(RationalVector p_other);


}