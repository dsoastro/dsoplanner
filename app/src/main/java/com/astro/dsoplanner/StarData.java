package com.astro.dsoplanner;

import java.io.Serializable;

public class StarData implements Serializable {
    private static final String TAG = StarData.class.getSimpleName();
    public MyList[] list; //array with lists which contain numbers of stars from HR catalog belonging to the relevant quadrant
    public MyList[] listHip; //the same with hip


    public StarData() {

    }

    public static int ConvHrToRow(int hr) {//get row number in the database based on hr number
        int row = hr - 1;
        if (hr > 92) row--;
        if (hr > 95) row--;
        if (hr > 182) row--;
        if (hr > 1057) row--;
        if (hr > 1841) row--;
        if (hr > 2472) row--;
        if (hr > 2496) row--;
        if (hr > 3515) row--;
        if (hr > 3671) row--;
        if (hr > 6309) row--;
        if (hr > 6515) row--;
        if (hr > 7189) row--;
        if (hr > 7539) row--;
        if (hr > 8296) row--;
        return row;
    }
}
