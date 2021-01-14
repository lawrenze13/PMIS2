package com.example.pmis.Helpers;

public class RangeHelper {
    private int low;
    private int high;

    public RangeHelper(int low, int high){
        this.low = low;
        this.high = high;
    }
    public boolean contains(int number){
        return(number >= low && number <= high);
    }
}
