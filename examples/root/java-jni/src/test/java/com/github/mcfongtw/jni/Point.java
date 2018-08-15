package com.github.mcfongtw.jni;

public class Point {
    private int _x;
    private int _y;

    public Point(int x, int y) {
        this._x = x;
        this._y = y;
    }

    public int getX() {
        return this._x;
    }

    public int getY() {
        return this._y;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == null) {
            return false;
        }

        if (obj == null) {
            return false;
        }

        if ((obj instanceof Point) == false) {
            return false;
        }

        Point that = (Point) obj;

        if (this._x != that._x) {
            return false;
        }

        if (this._y != that._y) {
            return false;
        }

        return true;
    }
}

