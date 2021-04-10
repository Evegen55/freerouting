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
 * IntPoint.java
 *
 * Created on 1. Februar 2003, 10:31
 */

package eu.mihosoft.freerouting.geometry.planar;

import eu.mihosoft.freerouting.logger.FRLogger;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Implementation of the abstract class Point as a tuple of integers.
 *
 * @author Alfons Wirtz
 */

public class IntPoint extends Point implements Serializable {

    /**
     * the x coordinate of this point
     */
    public final int x;

    /**
     * the y coordinate of this point
     */
    public final int y;

    /**
     * create an  IntPoint from two integer coordinates
     */
    public IntPoint(int x, int y) {
        if (Math.abs(x) > Limits.CRIT_INT || Math.abs(y) > Limits.CRIT_INT) {
            FRLogger.warn("Warning in IntPoint: x or y too big");
        }
        this.x = x;
        this.y = y;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final IntPoint intPoint = (IntPoint) o;
        return x == intPoint.x && y == intPoint.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public IntBox surroundingBox() {
        return new IntBox(this, this);
    }

    @Override
    public IntOctagon surroundingOctagon() {
        int tmp_1 = x - y;
        int tmp_2 = x + y;

        return new IntOctagon(x, y, x, y, tmp_1, tmp_1, tmp_2, tmp_2);
    }

    @Override
    public boolean isContainedIn(IntBox intBox) {
        return x >= intBox.ll.x &&
               y >= intBox.ll.y &&
               x <= intBox.ur.x &&
               y <= intBox.ur.y;
    }

    @Override
    public final Point translateBy(Vector vector) {
        if (vector.equals(Vector.ZERO)) {
            return this;
        }
        return vector.addTo(this);
    }

    @Override
    Point translateBy(IntVector intVector) {
        return (new IntPoint(x + intVector.x, y + intVector.y));
    }

    @Override
    Point translateBy(RationalVector rationalVector) {
        return rationalVector.addTo(this);
    }

    @Override
    public Vector differenceBy(Point point) {
        Vector tmp = point.differenceBy(this);
        return tmp.negate();
    }

    @Override
    Vector differenceBy(RationalPoint rationalPoint) {
        Vector tmp = rationalPoint.differenceBy(this);
        return tmp.negate();
    }

    @Override
    IntVector differenceBy(IntPoint intPoint) {
        return new IntVector(x - intPoint.x, y - intPoint.y);
    }

    @Override
    public Side sideOf(Line line) {
        Vector v1 = differenceBy(line.a);
        Vector v2 = line.b.differenceBy(line.a);
        return v1.sideOf(v2);
    }

    @Override
    public FloatPoint toFloat() {
        return new FloatPoint(x, y);
    }

    /**
     * returns the determinant of the vectors (x, y) and (p_other.x, p_other.y)
     */
    public final long determinant(IntPoint intPoint) {
        return (long) x * intPoint.y - (long) y * intPoint.x;
    }

    @Override
    public Point perpendicularProjection(Line line) {
        // this function is at the moment only implemented for lines
        // consisting of IntPoints.
        // The general implementation is still missing.
        IntVector v = (IntVector) line.b.differenceBy(line.a);
        BigInteger vxvx = BigInteger.valueOf((long) v.x * v.x);
        BigInteger vyvy = BigInteger.valueOf((long) v.y * v.y);
        BigInteger vxvy = BigInteger.valueOf((long) v.x * v.y);
        BigInteger denominator = vxvx.add(vyvy);
        BigInteger det = BigInteger.valueOf(((IntPoint) line.a).determinant((IntPoint) line.b));
        BigInteger point_x = BigInteger.valueOf(x);
        BigInteger point_y = BigInteger.valueOf(y);

        BigInteger tmp1 = vxvx.multiply(point_x);
        BigInteger tmp2 = vxvy.multiply(point_y);
        tmp1 = tmp1.add(tmp2);
        tmp2 = det.multiply(BigInteger.valueOf(v.y));
        BigInteger proj_x = tmp1.add(tmp2);

        tmp1 = vxvy.multiply(point_x);
        tmp2 = vyvy.multiply(point_y);
        tmp1 = tmp1.add(tmp2);
        tmp2 = det.multiply(BigInteger.valueOf(v.x));
        BigInteger proj_y = tmp1.subtract(tmp2);

        int signum = denominator.signum();
        if (signum != 0) {
            if (signum < 0) {
                denominator = denominator.negate();
                proj_x = proj_x.negate();
                proj_y = proj_y.negate();
            }
            if ((proj_x.mod(denominator)).signum() == 0 &&
                (proj_y.mod(denominator)).signum() == 0) {
                proj_x = proj_x.divide(denominator);
                proj_y = proj_y.divide(denominator);
                return new IntPoint(proj_x.intValue(), proj_y.intValue());
            }
        }
        return new RationalPoint(proj_x, proj_y, denominator);
    }

    /**
     * Returns the signed area of the parallelogramm spanned by the vectors p_2 - p_1 and this - p_1
     */
    public double signed_area(IntPoint p_1, IntPoint p_2) {
        IntVector d21 = p_2.differenceBy(p_1);
        IntVector d01 = this.differenceBy(p_1);
        return d21.determinant(d01);
    }

    /**
     * calculates the square of the distance between this point and p_to_point
     */
    public double distanceSquare(IntPoint p_to_point) {
        double dx = p_to_point.x - x;
        double dy = p_to_point.y - y;
        return dx * dx + dy * dy;
    }

    /**
     * calculates the distance between this point and toPoint
     */
    public double distance(IntPoint toPoint) {
        return Math.sqrt(distanceSquare(toPoint));
    }

    /**
     * Calculates the nearest point to this point on the horizontal or vertical line through intPoint (Snaps this point
     * to on ortogonal line through intPoint).
     */
    public IntPoint orthogonalProjection(IntPoint intPoint) {
        IntPoint result;
        int horizontalDistance = Math.abs(x - intPoint.x);
        int verticalDistance = Math.abs(y - intPoint.y);
        if (horizontalDistance <= verticalDistance) {
            // projection onto the vertical line through intPoint
            result = new IntPoint(intPoint.x, y);
        } else {
            // projection onto the horizontal line through intPoint
            result = new IntPoint(x, intPoint.y);
        }
        return result;
    }

    /**
     * Calculates the nearest point to this point on an orthogonal or diagonal line through intPoint (Snaps this point
     * to on 45 degree line through intPoint).
     */
    public IntPoint fortyfiveDegreeProjection(IntPoint intPoint) {
        int dx = x - intPoint.x;
        int dy = y - intPoint.y;
        double[] dist_arr = new double[4];
        dist_arr[0] = Math.abs(dx);
        dist_arr[1] = Math.abs(dy);
        double diagonal_1 = ((double) dy - (double) dx) / 2;
        double diagonal_2 = ((double) dy + (double) dx) / 2;
        dist_arr[2] = Math.abs(diagonal_1);
        dist_arr[3] = Math.abs(diagonal_2);
        double min_dist = dist_arr[0];
        for (int i = 1; i < 4; ++i) {
            if (dist_arr[i] < min_dist) {
                min_dist = dist_arr[i];
            }
        }

        if (min_dist == dist_arr[0]) {
            // projection onto the vertical line through intPoint
            return new IntPoint(intPoint.x, y);
        } else if (min_dist == dist_arr[1]) {
            // projection onto the horizontal line through intPoint
            return new IntPoint(x, intPoint.y);
        } else if (min_dist == dist_arr[2]) {
            // projection onto the right diagonal line through intPoint
            int diagonal_value = (int) diagonal_2;
            return new IntPoint(intPoint.x + diagonal_value,
                                intPoint.y + diagonal_value);
        } else {
            // projection onto the left diagonal line through intPoint
            int diagonal_value = (int) diagonal_1;
            return new IntPoint(intPoint.x - diagonal_value,
                                intPoint.y + diagonal_value);
        }
    }

    /**
     * Calculates a corner point p
     * <p>
     * so that the lines through this point and p
     * <p>
     * and
     * <p>
     * from p to toPoint are multiples of 45 degree,
     * <p>
     * and that the angle at p will be 45 degree.
     * <p>
     * If leftTurn, toPoint will be on the left of the line from this point to p,
     * <p>
     * else on the right.
     *
     * @return null, if the line from this point to toPoint is already amultiple of 45 degree.
     */
    public IntPoint fortyfiveDegreeCorner(IntPoint toPoint, boolean leftTurn) {
        int dx = toPoint.x - this.x;
        int dy = toPoint.y - this.y;
        IntPoint result;

        // handle the 8 sections between the 45 degree lines

        if (dy > 0 && dy < dx) {
            if (leftTurn) {
                result = new IntPoint(toPoint.x - dy, this.y);
            } else {
                result = new IntPoint(this.x + dy, toPoint.y);
            }
        } else if (dx > 0 && dy > dx) {
            if (leftTurn) {
                result = new IntPoint(toPoint.x, this.y + dx);
            } else {
                result = new IntPoint(this.x, toPoint.y - dx);
            }
        } else if (dx < 0 && dy > -dx) {
            if (leftTurn) {
                result = new IntPoint(this.x, toPoint.y + dx);
            } else {
                result = new IntPoint(toPoint.x, this.y - dx);
            }
        } else if (dy > 0 && dy < -dx) {
            if (leftTurn) {
                result = new IntPoint(this.x - dy, toPoint.y);
            } else {
                result = new IntPoint(toPoint.x + dy, this.y);
            }
        } else if (dy < 0 && dy > dx) {
            if (leftTurn) {
                result = new IntPoint(toPoint.x - dy, this.y);
            } else {
                result = new IntPoint(this.x + dy, toPoint.y);
            }
        } else if (dx < 0 && dy < dx) {
            if (leftTurn) {
                result = new IntPoint(toPoint.x, this.y + dx);
            } else {
                result = new IntPoint(this.x, toPoint.y - dx);
            }
        } else if (dx > 0 && dy < -dx) {
            if (leftTurn) {
                result = new IntPoint(this.x, toPoint.y + dx);
            } else {
                result = new IntPoint(toPoint.x, this.y - dx);
            }
        } else if (dy < 0 && dy > -dx) {
            if (leftTurn) {
                result = new IntPoint(this.x - dy, toPoint.y);
            } else {
                result = new IntPoint(toPoint.x + dy, this.y);
            }
        } else {
            // the line from this point to toPoint is already a multiple of 45 degree
            result = null;
        }
        return result;
    }

    /**
     * Calculates a corner point p
     * <p>
     * so that the lines through this point and p and from p to toPoint
     * <p>
     * are horizontal or vertical,
     * <p>
     * and
     * <p>
     * that the angle at p will be 90 degree.
     * <p>
     * If leftTurn, toPoint will be on the left of the line from this point to p, else on the right.
     *
     * @return null, if the line from this point to toPoint is already orthogonal.
     */
    public IntPoint ninetyDegreeCorner(IntPoint toPoint, boolean leftTurn) {
        int dx = toPoint.x - this.x;
        int dy = toPoint.y - this.y;
        IntPoint result;

        // handle the 4 quadrants

        if (dx > 0 && dy > 0 || dx < 0 && dy < 0) {
            if (leftTurn) {
                result = new IntPoint(toPoint.x, this.y);
            } else {
                result = new IntPoint(this.x, toPoint.y);
            }
        } else if (dx < 0 && dy > 0 || dx > 0 && dy < 0) {
            if (leftTurn) {
                result = new IntPoint(this.x, toPoint.y);
            } else {
                result = new IntPoint(toPoint.x, this.y);
            }
        } else {
            //the line from this point to toPoint is already orthogonal
            result = null;
        }
        return result;
    }

    @Override
    public int compareX(Point point) {
        return -point.compareX(this);
    }

    @Override
    public int compareY(Point point) {
        return -point.compareY(this);
    }

    @Override
    int compareX(IntPoint intPoint) {
        return Integer.compare(x, intPoint.x);
    }

    @Override
    int compareY(IntPoint intPoint) {
        return Integer.compare(y, intPoint.y);
    }

    @Override
    int compareX(RationalPoint rationalPoint) {
        return -rationalPoint.compareX(this);
    }

    @Override
    int compareY(RationalPoint rationalPoint) {
        return -rationalPoint.compareY(this);
    }
}

