package com.astro.dsoplanner.pushcamera;

public class P2i {
    int x, y;

    public P2i(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "P2i [x=" + x + ", y=" + y + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        P2i other = (P2i) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

}
