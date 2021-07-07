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
 * Point.java
 *
 * Created on 1. Februar 2003, 11:38
 */

package eu.mihosoft.freerouting.geometry.planar;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Abstract class describing functionality for Points in the plane.
 *
 * @author Alfons Wirtz
 */

public abstract class Point implements Serializable {

    /**
     * returns the translation of this point by p_vector
     */
    public abstract Point translateBy(Vector p_vector);

    /**
     * returns the difference vector of this point and p_other
     */
    public abstract Vector differenceBy(Point p_other);


    /**
     * approximates the coordinates of this point by float coordinates
     */
    public abstract FloatPoint toFloat();

    /**
     * returns true, if this Point is a RationalPoint with denominator z = 0.
     */
    public abstract boolean isInfinite();

    /**
     * creates the smallest Box with integer coordinates  containing this point.
     */
    public abstract IntBox surroundingBox();


    /**
     * creates the smallest Octagon with integer coordinates  containing this point.
     */
    public abstract IntOctagon surroundingOctagon();

    /**
     * Returns true, if this point lies in the interiour or on the border of p_box.
     */
    public abstract boolean isContainedIn(IntBox p_box);


    public abstract Side sideOf(Line p_line);

    /**
     * returns the nearest point to this point on p_line
     */
    public abstract Point perpendicularProjection(Line p_line);

    /**
     * Standard implementation of the zero point .
     */
    public static final IntPoint ZERO = new IntPoint(0, 0);

    /**
     * creates an IntPoint from p_x and p_y. If p_x or p_y is to big for an IntPoint, a RationalPoint is created.
     */
    public static Point getInstance(int p_x, int p_y) {
        IntPoint result = new IntPoint(p_x, p_y);
        if (Math.abs(p_x) > Limits.CRIT_INT ||
            Math.abs(p_y) > Limits.CRIT_INT) {
            return new RationalPoint(result);
        }
        return result;
    }

    /**
     * factory method for creating a Point from 3 BigIntegers
     */
    public static Point getInstance(
            BigInteger x,
            BigInteger y,
            BigInteger z
    ) {
        if (z.signum() < 0) {
            // the dominator z of a RationalPoint is expected to be positive
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
                return new IntPoint(x.intValue(), y.intValue());
            }
        }
        return new RationalPoint(x, y, z);
    }

    /**
     * The function returns Side.ON_THE_LEFT, if this Point is on the left of the line from p_1 to p_2;
     * Side.ON_THE_RIGHT, if this Point is on the right of the line from p_1 to p_2; and Side.COLLINEAR, if this Point
     * is collinear with p_1 and p_2.
     */
    public Side sideOf(Point p_1, Point p_2) {
        Vector v1 = differenceBy(p_1);
        Vector v2 = p_2.differenceBy(p_1);
        return v1.sideOf(v2);
    }

    /**
     * Calculates the perpendicular direction froma this point to line. Returns Direction.NULL, if this point lies on
     * line.
     */
    public Direction perpendicularDirection(Line line) {
        Side side = this.sideOf(line);
        if (side == Side.COLLINEAR) {
            return Direction.NULL;
        }
        Direction result;
        if (side == Side.ON_THE_RIGHT) {
            result = line.direction().turn_45_degree(2);
        } else {
            result = line.direction().turn_45_degree(6);
        }
        return result;
    }

    /**
     * Returns 1, if this Point has a strict bigger x coordinate than p_other, 0, if the x cooordinates are equal, and
     * -1 otherwise.
     */
    public abstract int compareX(Point p_other);

    /**
     * Returns 1, if this Point has a strict bigger y coordinate than p_other, 0, if the y cooordinates are equal, and
     * -1 otherwise.
     */
    public abstract int compareY(Point p_other);

    /**
     * The function returns compare_x (point), if the result is not 0. Otherwise it returns compare_y (point).
     */
    public int compareXY(Point point) {
        int result = compareX(point);
        if (result == 0) {
            result = compareY(point);
        }
        return result;
    }

    /**
     * Turns this point by factor times 90 degree around point.
     */
    public Point turn90Degree(int factor, Point point) {
        Vector v = this.differenceBy(point);
        v = v.turn90Degree(factor);
        return point.translateBy(v);
    }

    /**
     * Mirrors this point at the vertical line through point.
     */
    public Point mirrorVertical(Point point) {
        Vector v = differenceBy(point);
        v = v.mirrorAtYAxis();
        return point.translateBy(v);
    }

    /**
     * Mirrors this point at the horizontal line through point.
     */
    public Point mirrorHorizontal(Point point) {
        Vector v = this.differenceBy(point);
        v = v.mirrorAtXAxis();
        return point.translateBy(v);
    }

    // auxiliary functions needed because the virtual function mechanism
    // does not work in parameter position

    abstract Point translateBy(IntVector p_vector);

    abstract Point translateBy(RationalVector p_vector);

    abstract Vector differenceBy(IntPoint p_other);

    abstract Vector differenceBy(RationalPoint p_other);

    abstract int compareX(IntPoint p_other);

    abstract int compareX(RationalPoint p_other);

    abstract int compareY(IntPoint p_other);

    abstract int compareY(RationalPoint p_other);
}