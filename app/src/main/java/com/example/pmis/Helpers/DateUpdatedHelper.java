package com.example.pmis.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUpdatedHelper {
    String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    String dateUpdated = currentDate + ' ' + currentTime;
    public String getDateUpdated(){
        return dateUpdated;
    }
}
