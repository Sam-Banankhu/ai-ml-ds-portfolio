package DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherDataDBHelper extends SQLiteOpenHelper {
    public static final String DB_name = "SENEGAL_WEATHER.db";
    public static final String TABLE_name = "weather_data";
    public static final String St_ID = "St_ID";
    public static final String Date = "Date";
    public static final String Day_Of_Year = "Day_Of_Year";
    public static final String Month = "Month";
    public static final String Year = "Year";
    public static final String Week_Of_Year = "Week_Of_Year";
    public static final String MIN_TEMP_2M = "MIN_TEMP_2M";
    public static final String AVG_Temp = "AVG_Temp";
    public static final String MAX_TEMP_2M = "MAX_TEMP_2M";
    public static final String HUM_2M = "HUM_2M";
    public static final String RAIN = "RAIN";

    public static final String DB_path = "/data/user/0/com.weatherfocus.wf/databases/";
    public Context context;
    SQLiteDatabase myDatabase;

    public WeatherDataDBHelper(Context context) {
        super(context, DB_name, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.execSQL("CREATE TABLE " + TABLE_name + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + St_ID + " INTEGER," + Date + " TEXT," + Day_Of_Year + " INTEGER," + Month + " INTEGER," + Year + " INTEGER," + Week_Of_Year + " INTEGER," + MIN_TEMP_2M + " REAL," + AVG_Temp + " REAL," + MAX_TEMP_2M + " REAL," + HUM_2M + " REAL," + RAIN + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_name);
        onCreate(db);
    }

    public boolean checkDataBase() {
        try {
            final String mPath = DB_name + DB_path;
            final File file = new File(mPath);
            return file.exists(); //TRUE or FALSE
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void copyDatabase() throws IOException {
        try {
            InputStream inputStream = context.getAssets().open(DB_name);
            String outputFileName = DB_path + DB_name;
            OutputStream outputStream = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDatabase() throws IOException {
        boolean databaseExist = checkDataBase();
        if (!databaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDatabase();
            } catch (IOException mIOException) {
                mIOException.printStackTrace();
                throw new Error("Error copying the database");
            } finally {
                this.close();
            }
        }
    }

  /*  @Override
    public synchronized void close() {
        if (myDatabase != null) {
            myDatabase.close();
            SQLiteDatabase.releaseMemory();
            super.close();
        }
    }*/

    public Cursor loadHandler() {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT Year FROM " + TABLE_name + " WHERE Week_Of_Year = ?", new String[]{"1"});
        //c.close();
        //db.close();
        return c;
    }

   /* public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data_list = db.rawQuery("SELECT Year FROM " + TABLE_name + " WHERE Week_Of_Year = ?", new String[]{"1"});
        return data_list;
    }*/

    public boolean insertDummy(int sid, int year, float rain_amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(St_ID, sid);
        cv.put(Year, year);
        cv.put(RAIN, rain_amount);
        long result = db.insert(TABLE_name, null, cv);
        return result != -1;
    }

    public List<Float> loadMinMaxStats(int startMonth, int startYear, int endYear, int endMonth, int St_ID) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Float> minMaxValues = new ArrayList<>(); //miniT, maxiT, miniR, maxiR, miniH, maxiH
        SQLiteDatabase db = this.getReadableDatabase();

        //TEMPERATURE QUERY
        List<Float> allMiniTemp = new ArrayList<>();
        List<Float> allMaxiTemp = new ArrayList<>();
        List<Float> allMiniRain = new ArrayList<>();
        List<Float> allMaxiRain = new ArrayList<>();
        List<Float> allMiniHum = new ArrayList<>();
        List<Float> allMaxiHum = new ArrayList<>();

        //TEMPERATURE QUERIES
        for (int y = startYear; y <= endYear; y++) {
            if (y == startYear) {
                Cursor temp = db.rawQuery("SELECT MIN(MIN_TEMP_2M), MAX(MAX_TEMP_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN ? AND 12", new String[]{"" + startYear, "" + St_ID, "" + startMonth});
                while (temp.moveToNext()) {
                    allMiniTemp.add(temp.getFloat(0));
                    allMaxiTemp.add(temp.getFloat(1));

                }
            }

            if (y == endYear) {
                Cursor temp2 = db.rawQuery("SELECT MIN(MIN_TEMP_2M), MAX(MAX_TEMP_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND ?", new String[]{"" + endYear, "" + St_ID, "" + endMonth});
                while (temp2.moveToNext()) {
                    allMiniTemp.add(temp2.getFloat(0));
                    allMaxiTemp.add(temp2.getFloat(1));
                }
            }
            //full years
            if (y != startYear && y != endYear) {
                Cursor tempFull = db.rawQuery("SELECT MIN(MIN_TEMP_2M), MAX(MAX_TEMP_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND 12", new String[]{"" + y, "" + St_ID});
                while (tempFull.moveToNext()) {
                    allMiniTemp.add(tempFull.getFloat(0));
                    allMaxiTemp.add(tempFull.getFloat(1));
                }
            }

        }
        minMaxValues.add(Collections.min(allMiniTemp));
        minMaxValues.add(Collections.max(allMaxiTemp));

        //RAINFALL QUERIES
        for (int y = startYear; y <= endYear; y++) {
            if (y == startYear) {
                Cursor rainQ = db.rawQuery("SELECT MIN(RAIN), MAX(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN ? AND 12", new String[]{"" + startYear, "" + St_ID, "" + startMonth});
                while (rainQ.moveToNext()) {
                    allMiniRain.add(rainQ.getFloat(0));
                    allMaxiRain.add(rainQ.getFloat(1));

                }
            }

            if (y == endYear) {
                Cursor rain2 = db.rawQuery("SELECT MIN(RAIN), MAX(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND ?", new String[]{"" + endYear, "" + St_ID, "" + endMonth});
                while (rain2.moveToNext()) {
                    allMiniRain.add(rain2.getFloat(0));
                    allMaxiRain.add(rain2.getFloat(1));
                }
            }
            //full years
            Cursor rainFull = db.rawQuery("SELECT MIN(RAIN), MAX(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND 12", new String[]{"" + y, "" + St_ID});
            while (rainFull.moveToNext()) {
                allMiniRain.add(rainFull.getFloat(0));
                allMaxiRain.add(rainFull.getFloat(1));
            }

        }
        minMaxValues.add(Collections.min(allMiniRain));
        minMaxValues.add(Collections.max(allMaxiRain));

        //HUMIDITY QUERIES
        for (int y = startYear; y <= endYear; y++) {
            if (y == startYear) {
                Cursor humidityQ = db.rawQuery("SELECT MIN(HUM_2M), MAX(HUM_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN ? AND 12", new String[]{"" + startYear, "" + St_ID, "" + startMonth});
                while (humidityQ.moveToNext()) {
                    allMiniHum.add(humidityQ.getFloat(0));
                    allMaxiHum.add(humidityQ.getFloat(1));

                }
            }

            if (y == endYear) {
                Cursor hum2 = db.rawQuery("SELECT MIN(HUM_2M), MAX(HUM_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND ?", new String[]{"" + endYear, "" + St_ID, "" + endMonth});
                while (hum2.moveToNext()) {
                    allMiniHum.add(hum2.getFloat(0));
                    allMaxiHum.add(hum2.getFloat(1));
                }
            }
            //full years
            Cursor humFull = db.rawQuery("SELECT MIN(HUM_2M), MAX(HUM_2M) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month BETWEEN 1 AND 12", new String[]{"" + y, "" + St_ID});
            while (humFull.moveToNext()) {
                allMiniHum.add(humFull.getFloat(0));
                allMaxiHum.add(humFull.getFloat(1));
            }

        }
        minMaxValues.add(Collections.min(allMiniHum));
        minMaxValues.add(Collections.max(allMaxiHum));
        // Cursor c = db.rawQuery("SELECT Year FROM " + TABLE_name + " WHERE Week_Of_Year = ?", new String[]{"1"});


        //c.close();
        //db.close();
        return minMaxValues;
    }

    public List<List<Float>> loadDailyGraphs(int startDay, int startMonth, int startYear, int endDay, int endMonth, int endYear, int St_ID) {//ORDER: minTempAvg, maxTempAvg, tempAvg, humidityAvg, months_period
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<List<Float>> daily_values = new ArrayList<>();

        List<Float> miniTempAvg = new ArrayList<>();
        List<Float> maxiTempAvg = new ArrayList<>();
        List<Float> tempAvg = new ArrayList<>();
        List<Float> humidityAvg = new ArrayList<>();
        List<Float> rainAccumulative = new ArrayList<>();
        List<Float> months_count = new ArrayList<>();
        List<Float> years_count = new ArrayList<>();
        List<Float> days_count = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        //int month = startMonth;
        // int day = startDay;
     /*   if (startYear != endYear) {

            for (int y = startYear; y <= endYear; y++) {
                if (y == startYear) {
                    for (int m = startMonth; m <= 12; m++) {
                        for (int d = startDay; d <= startDay + 31; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? AND Day_Of_Year = ?", new String[]{"" + startYear, "" + St_ID, "" + m, "" + d});
                            if (Q1.getColumnCount() > 0) {
                                while (Q1.moveToNext()) {
                                    miniTempAvg.add(Q1.getFloat(0));
                                    maxiTempAvg.add(Q1.getFloat(1));
                                    tempAvg.add(Q1.getFloat(2));
                                    humidityAvg.add(Q1.getFloat(3));
                                    rainAccumulative.add(Q1.getFloat(4));
                                    months_count.add((float) month);
                                    years_count.add((float) y);
                                    days_count.add((float) day);
                                    month++;
                                    day++;
                                }

                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                } else if (y == endYear) {
                    for (int m = 1; m <= endMonth; m++) {
                        for (int d = endDay; d <= endDay + 31; d++) {
                            Cursor Q2 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? AND Day_Of_Year = ?", new String[]{"" + startYear, "" + St_ID, "" + m, "" + d});
                            if (Q2.getColumnCount() > 0) {
                                while (Q2.moveToNext()) {
                                    miniTempAvg.add(Q2.getFloat(0));
                                    maxiTempAvg.add(Q2.getFloat(1));
                                    tempAvg.add(Q2.getFloat(2));
                                    humidityAvg.add(Q2.getFloat(3));
                                    rainAccumulative.add(Q2.getFloat(4));
                                    months_count.add((float) month);
                                    years_count.add((float) y);
                                    days_count.add((float) day);
                                    month++;
                                    day++;
                                }
                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                } else {
                    Cursor Q3 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ?", new String[]{"" + startYear, "" + St_ID});
                    while (Q3.moveToNext()) {
                        miniTempAvg.add(Q3.getFloat(0));
                        maxiTempAvg.add(Q3.getFloat(1));
                        tempAvg.add(Q3.getFloat(2));
                        humidityAvg.add(Q3.getFloat(3));
                        rainAccumulative.add(Q3.getFloat(4));
                        months_count.add((float) month);
                        years_count.add((float) y);
                        days_count.add((float) day);
                        month++;
                        day++;
                    }

                }
            }
            daily_values.add(miniTempAvg);
            daily_values.add(maxiTempAvg);
            daily_values.add(tempAvg);
            daily_values.add(humidityAvg);
            daily_values.add(rainAccumulative);
            daily_values.add(months_count);
            daily_values.add(years_count);
            daily_values.add(days_count);
        } else {
           if(startMonth!=endMonth){
              for(int m = startMonth; m<=endMonth; m++){
                  if(m==startMonth){
                      for(int d = startDay; d<=startDay+31; d++){
                          Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? AND Day_Of_Year = ?", new String[]{"" + startYear, "" + St_ID, "" + m, "" + d});
                          if (Q1.getColumnCount() > 0) {
                              while (Q1.moveToNext()) {
                                  miniTempAvg.add(Q1.getFloat(0));
                                  maxiTempAvg.add(Q1.getFloat(1));
                                  tempAvg.add(Q1.getFloat(2));
                                  humidityAvg.add(Q1.getFloat(3));
                                  rainAccumulative.add(Q1.getFloat(4));
                                  months_count.add((float) month);
                                  years_count.add((float) startYear);
                                  days_count.add((float) day);
                                  month++;
                                  day++;

                              }
                          }
                      }
                      daily_values.add(miniTempAvg);
                      daily_values.add(maxiTempAvg);
                      daily_values.add(tempAvg);
                      daily_values.add(humidityAvg);
                      daily_values.add(rainAccumulative);
                      daily_values.add(months_count);
                      daily_values.add(years_count);
                      daily_values.add(days_count);
                  }else if(m==endMonth){
                      for(int d = endDay-31; d<=endDay; d++){
                          Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? AND Day_Of_Year = ?", new String[]{"" + startYear, "" + St_ID, "" + m, "" + d});
                          if (Q1.getColumnCount() > 0) {
                              while (Q1.moveToNext()) {
                                  miniTempAvg.add(Q1.getFloat(0));
                                  maxiTempAvg.add(Q1.getFloat(1));
                                  tempAvg.add(Q1.getFloat(2));
                                  humidityAvg.add(Q1.getFloat(3));
                                  rainAccumulative.add(Q1.getFloat(4));
                                  months_count.add((float) month);
                                  years_count.add((float) startYear);
                                  days_count.add((float) day);
                                  month++;
                                  day++;

                              }
                          }
                      }
                      daily_values.add(miniTempAvg);
                      daily_values.add(maxiTempAvg);
                      daily_values.add(tempAvg);
                      daily_values.add(humidityAvg);
                      daily_values.add(rainAccumulative);
                      daily_values.add(months_count);
                      daily_values.add(years_count);
                      daily_values.add(days_count);
                  }else{
                      Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ?", new String[]{"" + startYear, "" + St_ID, "" + m});
                      while (Q1.moveToNext()) {
                          miniTempAvg.add(Q1.getFloat(0));
                          maxiTempAvg.add(Q1.getFloat(1));
                          tempAvg.add(Q1.getFloat(2));
                          humidityAvg.add(Q1.getFloat(3));
                          rainAccumulative.add(Q1.getFloat(4));
                          months_count.add((float) month);
                          years_count.add((float) startYear);
                          days_count.add((float) day);
                          month++;
                          day++;

                      }
                      daily_values.add(miniTempAvg);
                      daily_values.add(maxiTempAvg);
                      daily_values.add(tempAvg);
                      daily_values.add(humidityAvg);
                      daily_values.add(rainAccumulative);
                      daily_values.add(months_count);
                      daily_values.add(years_count);
                      daily_values.add(days_count);

                  }
              }

           }else{
               Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? AND Day_Of_Year BETWEEN ? AND ?", new String[]{"" + startYear, "" + St_ID, "" + startMonth, "" + startDay,""+endDay});
               while (Q1.moveToNext()) {
                   miniTempAvg.add(Q1.getFloat(0));
                   maxiTempAvg.add(Q1.getFloat(1));
                   tempAvg.add(Q1.getFloat(2));
                   humidityAvg.add(Q1.getFloat(3));
                   rainAccumulative.add(Q1.getFloat(4));
                   months_count.add((float) month);
                   years_count.add((float) startYear);
                   days_count.add((float) day);
                   month++;
                   day++;

               }
               daily_values.add(miniTempAvg);
               daily_values.add(maxiTempAvg);
               daily_values.add(tempAvg);
               daily_values.add(humidityAvg);
               daily_values.add(rainAccumulative);
               daily_values.add(months_count);
               daily_values.add(years_count);
               daily_values.add(days_count);

           }
        }
        //c.close();
        //db.close();
        */
        if (startYear != endYear) {
            for (int y = startYear; y <= endYear; y++) {
                if (y == startYear) {
                    for (int m = startMonth; m <= 12; m++) {

                        Cursor maxDayQ = db.rawQuery("SELECT DISTINCT " + Day_Of_Year + " FROM " + TABLE_name + " WHERE " + Year + " = ? AND " + Month + " = ?", new String[]{"" + y, "" + m});
                        int maxDayOfTheMonth = maxDayQ.getCount();

                        for (int d = startDay; d <= maxDayOfTheMonth; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + y, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                } else if (y == endYear) {//if year is the last year
                    for (int m = 1; m <= endMonth; m++) {
                        for (int d = 1; d <= endDay; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + y, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                } else {//for full years
                    for (int m = 1; m <= 12; m++) {
                        Cursor maxDayQ = db.rawQuery("SELECT DISTINCT " + Day_Of_Year + " FROM " + TABLE_name + " WHERE " + Year + " = ? AND " + Month + " = ?", new String[]{"" + y, "" + m});
                        int maxDayOfTheMonth = maxDayQ.getCount();
                        for (int d = 1; d <= maxDayOfTheMonth; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + y, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                }
            }
        } else {//start year same as end year
            if (startMonth != endMonth) {//if months are not the same
                for (int m = startMonth; m <= endMonth; m++) {
                    Cursor maxDayQ = db.rawQuery("SELECT DISTINCT " + Day_Of_Year + " FROM " + TABLE_name + " WHERE " + Year + " = ? AND " + Month + " = ?", new String[]{"" + startYear, "" + m});
                    int maxDayOfTheMonth = maxDayQ.getCount();
                    if (m == startMonth) {
                        for (int d = startDay; d <= maxDayOfTheMonth; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + startYear, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) startYear);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                        daily_values.add(miniTempAvg);
                        daily_values.add(maxiTempAvg);
                        daily_values.add(tempAvg);
                        daily_values.add(humidityAvg);
                        daily_values.add(rainAccumulative);
                        daily_values.add(months_count);
                        daily_values.add(years_count);
                        daily_values.add(days_count);
                    } else if (m == endMonth) {
                        for (int d = 1; d <= endDay; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + startYear, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) startYear);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                        daily_values.add(miniTempAvg);
                        daily_values.add(maxiTempAvg);
                        daily_values.add(tempAvg);
                        daily_values.add(humidityAvg);
                        daily_values.add(rainAccumulative);
                        daily_values.add(months_count);
                        daily_values.add(years_count);
                        daily_values.add(days_count);
                    } else {//for intermediary months

                        for (int d = 1; d <= maxDayOfTheMonth; d++) {
                            Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{m + "/" + d + "/" + startYear, "" + St_ID});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) startYear);
                                days_count.add((float) d);
                                // month++;
                                // day++;
                            }
                        }
                    }
                    daily_values.add(miniTempAvg);
                    daily_values.add(maxiTempAvg);
                    daily_values.add(tempAvg);
                    daily_values.add(humidityAvg);
                    daily_values.add(rainAccumulative);
                    daily_values.add(months_count);
                    daily_values.add(years_count);
                    daily_values.add(days_count);
                }

            } else { //months are the same and the years
                for (int d = startDay; d <= endDay; d++) {
                    Cursor Q1 = db.rawQuery("SELECT DISTINCT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN  FROM " + TABLE_name + " WHERE Date = ? AND St_ID = ?", new String[]{startMonth + "/" + d + "/" + startYear, "" + St_ID});
                    while (Q1.moveToNext()) {
                        miniTempAvg.add(Q1.getFloat(0));
                        maxiTempAvg.add(Q1.getFloat(1));
                        tempAvg.add(Q1.getFloat(2));
                        humidityAvg.add(Q1.getFloat(3));
                        rainAccumulative.add(Q1.getFloat(4));
                        months_count.add((float) startMonth);
                        years_count.add((float) startYear);
                        days_count.add((float) d);
                        // month++;
                        // day++;
                    }
                }
                daily_values.add(miniTempAvg);
                daily_values.add(maxiTempAvg);
                daily_values.add(tempAvg);
                daily_values.add(humidityAvg);
                daily_values.add(rainAccumulative);
                daily_values.add(months_count);
                daily_values.add(years_count);
                daily_values.add(days_count);
            }

        }

        return daily_values;
    }

    public List<List<Float>> loadWeeklyGraphs(int startWeek, int startMonth, int startYear, int endWeek, int endMonth, int endYear, int St_ID) {//ORDER: minTempAvg, maxTempAvg, tempAvg, humidityAvg, months_period
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<List<Float>> weekly_values = new ArrayList<>();

        List<Float> miniTempAvg = new ArrayList<>();
        List<Float> maxiTempAvg = new ArrayList<>();
        List<Float> tempAvg = new ArrayList<>();
        List<Float> humidityAvg = new ArrayList<>();
        List<Float> rainAccumulative = new ArrayList<>();
        List<Float> months_count = new ArrayList<>();
        List<Float> years_count = new ArrayList<>();
        List<Float> weeks_count = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        int week_select_start[] = {0, 7, 14, 21, 28};

       /* if (startYear != endYear) {
            for (int y = startYear; y <= endYear; y++) {
                if (y == startYear) {//first year
                    for (int m = startMonth; m <= 12; m++) {
                        if (m == startMonth) {
                            int week_number_tracker = 1;
                            Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                            while (Q1.moveToNext()) {
                                if (week_number_tracker >= startWeek) {
                                    miniTempAvg.add(Q1.getFloat(0));
                                    maxiTempAvg.add(Q1.getFloat(1));
                                    tempAvg.add(Q1.getFloat(2));
                                    humidityAvg.add(Q1.getFloat(3));
                                    rainAccumulative.add(Q1.getFloat(4));
                                    months_count.add((float) m);
                                    years_count.add((float) y);
                                    weeks_count.add((float) week_number_tracker);
                                }
                                week_number_tracker++;
                            }
                            weekly_values.add(miniTempAvg);
                            weekly_values.add(maxiTempAvg);
                            weekly_values.add(tempAvg);
                            weekly_values.add(humidityAvg);
                            weekly_values.add(rainAccumulative);
                            weekly_values.add(months_count);
                            weekly_values.add(years_count);
                            weekly_values.add(weeks_count);
                        } else {
                            int week_number_tracker = 1;
                            Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                weeks_count.add((float) week_number_tracker);
                                week_number_tracker++;
                            }
                        }
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else if (y == endYear) {//end year
                    for (int m = 1; m <= endMonth; m++) {
                        int week_number_tracker = 1;
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                        while (Q1.moveToNext()) {
                            if (week_number_tracker <= endWeek) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                weeks_count.add((float) week_number_tracker);
                            }
                            week_number_tracker++;
                        }
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else {//intermediary years
                    for (int m = 1; m <= 12; m++) {
                        int week_number_tracker = 1;
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                        while (Q1.moveToNext()) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) y);
                            weeks_count.add((float) week_number_tracker);

                            week_number_tracker++;
                        }
                    }
                }
            }
            weekly_values.add(miniTempAvg);
            weekly_values.add(maxiTempAvg);
            weekly_values.add(tempAvg);
            weekly_values.add(humidityAvg);
            weekly_values.add(rainAccumulative);
            weekly_values.add(months_count);
            weekly_values.add(years_count);
            weekly_values.add(weeks_count);
        } else {//years are equal
            for (int m = startMonth; m <= endMonth; m++) {
                if(m == startMonth){//startMonth
                    int week_number_tracker = 1;
                    Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                    while (Q1.moveToNext()) {
                        if (week_number_tracker >= startWeek) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);
                        }
                        week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                }else if(m ==endMonth){//end Month
                    int week_number_tracker = 1;
                    Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                    while (Q1.moveToNext()) {
                        if (week_number_tracker <= endWeek) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);
                        }
                        week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                }else{//intermediary months
                    int week_number_tracker = 1;
                    Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                    while (Q1.moveToNext()) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);
                            week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                }
            }

        }*/
        if (startYear != endYear) {
            for (int y = startYear; y <= endYear; y++) {
                if (y == startYear) {//first year
                    for (int m = startMonth; m <= 12; m++) {
                        if (m == startMonth) {
                            int week_number_tracker = startWeek; //==startWeek
                            // int month_days= getDaysOfAMonth(y, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                            // int days_limiter = month_days>=29? 28: 21;
                            for (int i = week_select_start[startWeek - 1]; i <= 28; i += 7) {
                                Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + y, "" + St_ID, "" + m});
                                if (Q1.getCount() > 0) {
                                    while (Q1.moveToNext()) {
                                        // if (week_number_tracker >= startWeek) {
                                        miniTempAvg.add(Q1.getFloat(0));
                                        maxiTempAvg.add(Q1.getFloat(1));
                                        tempAvg.add(Q1.getFloat(2));
                                        humidityAvg.add(Q1.getFloat(3));
                                        rainAccumulative.add(Q1.getFloat(4));
                                        months_count.add((float) m);
                                        years_count.add((float) y);
                                        weeks_count.add((float) week_number_tracker);
                                        //}
                                        week_number_tracker++;
                                    }
                                }
                            }
                            weekly_values.add(miniTempAvg);
                            weekly_values.add(maxiTempAvg);
                            weekly_values.add(tempAvg);
                            weekly_values.add(humidityAvg);
                            weekly_values.add(rainAccumulative);
                            weekly_values.add(months_count);
                            weekly_values.add(years_count);
                            weekly_values.add(weeks_count);
                        } else {
                            int week_number_tracker = 1;
                            // Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                            //int month_days= getDaysOfAMonth(y, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                            //int days_limiter = month_days>=29? 28: 21;
                            for (int i = 0; i <= 28; i += 7) {
                                Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + y, "" + St_ID, "" + m});
                                if (Q1.getCount() > 0) {
                                    while (Q1.moveToNext()) {
                                        miniTempAvg.add(Q1.getFloat(0));
                                        maxiTempAvg.add(Q1.getFloat(1));
                                        tempAvg.add(Q1.getFloat(2));
                                        humidityAvg.add(Q1.getFloat(3));
                                        rainAccumulative.add(Q1.getFloat(4));
                                        months_count.add((float) m);
                                        years_count.add((float) y);
                                        weeks_count.add((float) week_number_tracker);
                                        week_number_tracker++;
                                    }
                                }
                            }
                        }
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else if (y == endYear) {//end year
                    for (int m = 1; m <= endMonth; m++) {
                        if (m == endMonth) {
                            int week_number_tracker = 1;//restricted on week selection dropdown
                            for (int i = 0; i <= week_select_start[endWeek - 1]; i += 7) {
                                //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                                Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + y, "" + St_ID, "" + m});
                                if (Q1.getCount() > 0) {
                                    while (Q1.moveToNext()) {
                                        //if (week_number_tracker <= endWeek) {
                                        miniTempAvg.add(Q1.getFloat(0));
                                        maxiTempAvg.add(Q1.getFloat(1));
                                        tempAvg.add(Q1.getFloat(2));
                                        humidityAvg.add(Q1.getFloat(3));
                                        rainAccumulative.add(Q1.getFloat(4));
                                        months_count.add((float) m);
                                        years_count.add((float) y);
                                        weeks_count.add((float) week_number_tracker);
                                        //}

                                    }
                                    week_number_tracker++;
                                }
                            }
                        } else {
                            int week_number_tracker = 1;
                            // int month_days= getDaysOfAMonth(y, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                            //int days_limiter = month_days>=29? 28: 21;
                            for (int i = 0; i <= 28; i += 7) {
                                //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                                Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + y, "" + St_ID, "" + m});
                                if (Q1.getCount() > 0) {
                                    while (Q1.moveToNext()) {
                                        //if (week_number_tracker <= endWeek) {
                                        miniTempAvg.add(Q1.getFloat(0));
                                        maxiTempAvg.add(Q1.getFloat(1));
                                        tempAvg.add(Q1.getFloat(2));
                                        humidityAvg.add(Q1.getFloat(3));
                                        rainAccumulative.add(Q1.getFloat(4));
                                        months_count.add((float) m);
                                        years_count.add((float) y);
                                        weeks_count.add((float) week_number_tracker);
                                        //}

                                    }
                                    week_number_tracker++;
                                }
                            }
                        }
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else {//intermediary years

                    for (int m = 1; m <= 12; m++) {
                        int week_number_tracker = 1;
                        //int month_days= getDaysOfAMonth(y, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                        // int days_limiter = month_days>=29? 28: 21;
                        for (int i = 0; i <= 28; i += 7) {
                            // Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + y, "" + St_ID, "" + m});
                            Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + y, "" + St_ID, "" + m});
                            //if (Q1.getCount() > 0) {
                            while (Q1.moveToNext()) {
                                miniTempAvg.add(Q1.getFloat(0));
                                maxiTempAvg.add(Q1.getFloat(1));
                                tempAvg.add(Q1.getFloat(2));
                                humidityAvg.add(Q1.getFloat(3));
                                rainAccumulative.add(Q1.getFloat(4));
                                months_count.add((float) m);
                                years_count.add((float) y);
                                weeks_count.add((float) week_number_tracker);
                                week_number_tracker++;
                            }
                            //}
                        }
                    }
                }
            }
            weekly_values.add(miniTempAvg);
            weekly_values.add(maxiTempAvg);
            weekly_values.add(tempAvg);
            weekly_values.add(humidityAvg);
            weekly_values.add(rainAccumulative);
            weekly_values.add(months_count);
            weekly_values.add(years_count);
            weekly_values.add(weeks_count);
        } else {//years are equal
            for (int m = startMonth; m <= endMonth; m++) {
                if (m == startMonth) {//startMonth
                    int week_number_tracker = startWeek; //day_limiter taken care of on week dropdown
                    //int month_days= getDaysOfAMonth(startYear, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                    //int days_limiter = month_days>=29? 28: 21;
                    for (int i = week_select_start[startWeek - 1]; i <= 28; i += 7) {
                        //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + startYear, "" + St_ID, "" + m});

                        while (Q1.moveToNext()) {
                            //if (week_number_tracker >= startWeek) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);
                            //}

                        }
                        week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else if (m == endMonth) {//end Month
                    int week_number_tracker = 1;
                    for (int i = 0; i <= week_select_start[endWeek - 1]; i += 7) {// taken care of on week dropdown selection
                        //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + startYear, "" + St_ID, "" + m});
                        while (Q1.moveToNext()) {
                            //if (week_number_tracker <= endWeek) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);
                            //}

                        }
                        week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                } else {//intermediary months
                    int week_number_tracker = 1;
                    //int month_days= getDaysOfAMonth(startYear, m).size(); //IN ORDER TO TAKE CARE OF FEBRUARIES
                    //int days_limiter = month_days>=29? 28: 21;
                    for (int i = 0; i <= 28; i += 7) {
                        //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? GROUP BY " + Week_Of_Year, new String[]{"" + startYear, "" + St_ID, "" + m});
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + startYear, "" + St_ID, "" + m});
                        while (Q1.moveToNext()) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) m);
                            years_count.add((float) startYear);
                            weeks_count.add((float) week_number_tracker);

                        }
                        week_number_tracker++;
                    }
                    weekly_values.add(miniTempAvg);
                    weekly_values.add(maxiTempAvg);
                    weekly_values.add(tempAvg);
                    weekly_values.add(humidityAvg);
                    weekly_values.add(rainAccumulative);
                    weekly_values.add(months_count);
                    weekly_values.add(years_count);
                    weekly_values.add(weeks_count);
                }
            }

        }

        return weekly_values;
    }

    public List<List<Float>> loadMonthlyGraphs(int startMonth, int startYear, int endYear, int endMonth, int St_ID) {//ORDER: minTempAvg, maxTempAvg, tempAvg, humidityAvg, months_period
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<List<Float>> monthly_values = new ArrayList<>();

        List<Float> miniTempAvg = new ArrayList<>();
        List<Float> maxiTempAvg = new ArrayList<>();
        List<Float> tempAvg = new ArrayList<>();
        List<Float> humidityAvg = new ArrayList<>();
        List<Float> rainAccumulative = new ArrayList<>();
        List<Float> months_count = new ArrayList<>();
        List<Float> years_count = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //TEMPERATURE QUERIES\\
        if (startYear != endYear) {
            int month = startMonth;
            for (int y = startYear; y <= endYear; y++) {
                if (y == startYear) {
                    for (int m = startMonth; m <= 12; m++) {
                        Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ?", new String[]{"" + startYear, "" + St_ID, "" + m});
                        while (Q1.moveToNext()) {
                            miniTempAvg.add(Q1.getFloat(0));
                            maxiTempAvg.add(Q1.getFloat(1));
                            tempAvg.add(Q1.getFloat(2));
                            humidityAvg.add(Q1.getFloat(3));
                            rainAccumulative.add(Q1.getFloat(4));
                            months_count.add((float) month);
                            years_count.add((float) y);

                        }
                        month++;
                    }
                }

                if (y == endYear) {
                    for (int m = 1; m <= endMonth; m++) {
                        Cursor Q2 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ?", new String[]{"" + endYear, "" + St_ID, "" + m});
                        while (Q2.moveToNext()) {
                            miniTempAvg.add(Q2.getFloat(0));
                            maxiTempAvg.add(Q2.getFloat(1));
                            tempAvg.add(Q2.getFloat(2));
                            humidityAvg.add(Q2.getFloat(3));
                            rainAccumulative.add(Q2.getFloat(4));
                            months_count.add((float) month);
                            years_count.add((float) y);
                        }
                        month++;
                    }
                }
                //full years
                if (y != startYear && y != endYear) {
                    for (int m = 1; m <= 12; m++) {
                        Cursor Q3 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ?", new String[]{"" + y, "" + St_ID, "" + m});
                        while (Q3.moveToNext()) {
                            miniTempAvg.add(Q3.getFloat(0));
                            maxiTempAvg.add(Q3.getFloat(1));
                            tempAvg.add(Q3.getFloat(2));
                            humidityAvg.add(Q3.getFloat(3));
                            rainAccumulative.add(Q3.getFloat(4));
                            months_count.add((float) month);
                            years_count.add((float) y);
                        }
                        month++;
                    }
                }
            }
            monthly_values.add(miniTempAvg);
            monthly_values.add(maxiTempAvg);
            monthly_values.add(tempAvg);
            monthly_values.add(humidityAvg);
            monthly_values.add(rainAccumulative);
            monthly_values.add(months_count);
            monthly_values.add(years_count);
        } else { ///end month is equal to the start month
            for (int m = startMonth; m <= endMonth; m++) {
                Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ?", new String[]{"" + startYear, "" + St_ID, "" + m});
                while (Q1.moveToNext()) {
                    miniTempAvg.add(Q1.getFloat(0));
                    maxiTempAvg.add(Q1.getFloat(1));
                    tempAvg.add(Q1.getFloat(2));
                    humidityAvg.add(Q1.getFloat(3));
                    rainAccumulative.add(Q1.getFloat(4));
                    months_count.add((float) m);
                    years_count.add((float) startYear);

                }
            }

            monthly_values.add(miniTempAvg);
            monthly_values.add(maxiTempAvg);
            monthly_values.add(tempAvg);
            monthly_values.add(humidityAvg);
            monthly_values.add(rainAccumulative);
            monthly_values.add(months_count);
            monthly_values.add(years_count);
        }
        //c.close();
        //db.close();
        return monthly_values;
    }

    public List<List<Float>> loadAnnuallyGraphs(int startYear, int endYear, int St_ID) {//ORDER: minTempAvg, maxTempAvg, tempAvg, humidityAvg, months_period
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<List<Float>> annually_values = new ArrayList<>();

        List<Float> miniTempAvg = new ArrayList<>();
        List<Float> maxiTempAvg = new ArrayList<>();
        List<Float> tempAvg = new ArrayList<>();
        List<Float> humidityAvg = new ArrayList<>();
        List<Float> rainAccumulative = new ArrayList<>();
        List<Float> count = new ArrayList<>();
        List<Float> years_count = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //QUERY
        if (startYear != endYear) {
            int interval = 1;
            for (int y = startYear; y <= endYear; y++) {
                Cursor Q = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_TEMP), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ?", new String[]{"" + y, "" + St_ID});
                while (Q.moveToNext()) {
                    miniTempAvg.add(Q.getFloat(0));
                    maxiTempAvg.add(Q.getFloat(1));
                    tempAvg.add(Q.getFloat(2));
                    humidityAvg.add(Q.getFloat(3));
                    rainAccumulative.add(Q.getFloat(4));
                    count.add((float) interval);
                }
                years_count.add((float) y);
                interval++;
            }

            annually_values.add(miniTempAvg);
            annually_values.add(maxiTempAvg);
            annually_values.add(tempAvg);
            annually_values.add(humidityAvg);
            annually_values.add(rainAccumulative);
            annually_values.add(count);
            annually_values.add(years_count);

        } else { ///end year equal to end year
            Cursor Q = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_TEMP), AVG(HUM_2M), SUM(RAIN) FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ?", new String[]{"" + startYear, "" + St_ID});
            while (Q.moveToNext()) {
                miniTempAvg.add(Q.getFloat(0));
                maxiTempAvg.add(Q.getFloat(1));
                tempAvg.add(Q.getFloat(2));
                humidityAvg.add(Q.getFloat(3));
                rainAccumulative.add(Q.getFloat(4));
                count.add((float) 1);
                years_count.add((float) startYear);

            }

            annually_values.add(miniTempAvg);
            annually_values.add(maxiTempAvg);
            annually_values.add(tempAvg);
            annually_values.add(humidityAvg);
            annually_values.add(rainAccumulative);
            annually_values.add(count);
        }
        //c.close();
        //db.close();
        return annually_values;
    }

    public List<Integer> getDaysOfAMonth(int Year, int Month) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Integer> days = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor dQ = db.rawQuery("SELECT DISTINCT(Day_Of_Year) FROM " + TABLE_name + " WHERE Year = ? AND Month = ?", new String[]{"" + Year, "" + Month});
       /* while (dQ.moveToNext()) {
            days.add(dQ.getInt(0));
        }*/
        for (int i = 1; i <= dQ.getCount(); i++) {
            days.add(i);
        }


        //c.close();
        //db.close();
        return days;
    }

    public List<Integer> getRecommendedPeriod(int Year, int station, float mini_sow_rainfall) {

        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Integer> period = new ArrayList<>();//Month, Week, Day
        boolean period_already_found = false;
        SQLiteDatabase db = this.getReadableDatabase();

        for (int m = 5; m <= 10; m++) {
            int week_number = 1;
            for (int w = 0; w <= 28; w += 7) {

                //Cursor Q1 = db.rawQuery("SELECT AVG(MIN_TEMP_2M), AVG(MAX_TEMP_2M), AVG(AVG_Temp), AVG(HUM_2M), SUM(RAIN) FROM (SELECT MIN_TEMP_2M, MAX_TEMP_2M, AVG_Temp, HUM_2M, RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + i + ", 7)", new String[]{"" + Year, "" + St_ID, "" + m});
                Cursor Q1 = db.rawQuery("SELECT SUM(RAIN) FROM (SELECT RAIN FROM " + TABLE_name + " WHERE Year = ? AND St_ID = ? AND Month = ? LIMIT " + w + ", 7)", new String[]{"" + Year, "" + station, "" + m});
                while (Q1.moveToNext()) {
                    if (Q1.getFloat(0) >= mini_sow_rainfall && !period_already_found) {
                        period.add(week_number);
                        period.add(m);

                        period_already_found = true;
                    }

                }
                week_number++;
            }
        }


        return period;
    }




    public float getSowingRainFall(String crop_name) {

        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();
        float sowing_rainfall = 0;

        Cursor cursor = db.rawQuery("SELECT Rainfall_Sowing FROM plant_requirements WHERE Plant = ?", new String[]{crop_name});
        while (cursor.moveToNext()) {
            sowing_rainfall = cursor.getFloat(0);
        }
        return sowing_rainfall;
    }

    public String getExactSowingDate(int week_number, int month, int year, int St_ID) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String approxDate = "";
        int[] week_limiter_number = {0, 7, 14, 21, 28};
        int l = week_limiter_number[week_number - 1];
        Cursor cursor = db.rawQuery("SELECT date, max(RAIN) FROM (SELECT date, RAIN FROM Weather_data WHERE Year = ? AND Month = ? AND St_ID = ? LIMIT " + l + ", 7)", new String[]{"" + year, "" + month, "" + St_ID});
        while (cursor.moveToNext()) {
            approxDate = cursor.getString(0);
        }

        return approxDate;
    }

    public List<Integer> getMinMaxDays(String crop_name) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();

        List<Integer> min_max_days = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT Min_Days, Max_Days FROM plant_requirements WHERE Plant = ?", new String[]{crop_name});
        while (cursor.moveToNext()) {
            min_max_days.add(cursor.getInt(0));
            min_max_days.add(cursor.getInt(1));
        }
        return min_max_days;
    }

    public List<String> getExpectedHarvestTime(int plantingWeek, int plantingMonth, int plantingYear, int minDays, int maxDays, int St_ID) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();
        int[] week_limiter_number = {0, 7, 14, 21, 28};
        List<String> harvestPeriodRange = new ArrayList<>();
        int daysCount = 0;
        for (int m = plantingMonth; m <= 12; m++) {
            if (m == plantingMonth) {
                for (int w = plantingWeek; w <= 5; w++) {
                    Cursor mQuery = db.rawQuery("SELECT date FROM Weather_data WHERE Month = ? AND Year =? AND St_ID = ? LIMIT " + week_limiter_number[w - 1] + " , 7", new String[]{"" + plantingMonth, "" + plantingYear, "" + St_ID});
                    while (mQuery.moveToNext()) {
                        daysCount++;
                        if (daysCount == minDays) {
                            harvestPeriodRange.add(mQuery.getString(0));
                        }
                        if (daysCount == maxDays) {
                            harvestPeriodRange.add(mQuery.getString(0));
                        }
                    }
                }
            } else {
                Cursor mQuery = db.rawQuery("SELECT date FROM Weather_data WHERE Month = ? AND Year =? AND St_ID = ?", new String[]{"" + m, "" + plantingYear, "" + St_ID});
                while (mQuery.moveToNext()) {
                    daysCount++;
                    if (daysCount == minDays) {
                        harvestPeriodRange.add(mQuery.getString(0));
                    }
                    if (daysCount == maxDays) {
                        harvestPeriodRange.add(mQuery.getString(0));
                    }
                }
            }
        }
        return harvestPeriodRange; //minDate-maxDate
    }
    public List<Integer> getAdditionalSowingRequirements(String crop_name) {
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = this.getReadableDatabase();

        List<Integer> sowing_requirements = new ArrayList<>();//Rainfall_Sowing, Min_Rainfall, Max_Rainfall, Min_Temp, Max_Temp
        Cursor cursor = db.rawQuery("SELECT Rainfall_Sowing, Min_Temp, Max_Temp FROM plant_requirements WHERE Plant = ?", new String[]{crop_name});
        while (cursor.moveToNext()) {
            sowing_requirements.add(cursor.getInt(0));
            sowing_requirements.add(cursor.getInt(1));
            sowing_requirements.add(cursor.getInt(2));
        }
        return sowing_requirements;
    }
}
