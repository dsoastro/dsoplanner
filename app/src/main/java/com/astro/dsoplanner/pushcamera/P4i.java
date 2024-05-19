package com.astro.dsoplanner.pushcamera;

public class P4i {
    int x, y, z, t;

    public P4i(int x, int y, int z, int t) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }

    @Override
    public String toString() {
        return "P4i [x=" + x + ", y=" + y + ", z=" + z + ", t=" + t + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + t;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
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
        P4i other = (P4i) obj;
        if (t != other.t)
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

}
