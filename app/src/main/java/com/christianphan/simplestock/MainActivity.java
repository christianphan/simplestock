package com.christianphan.simplestock;




import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;


import yahoofinance.YahooFinance;



public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {


    DataBaseHelper myDB;
    CSVDatabase mySEARCH;
    private String index;
    private ArrayList<Stock> arrayList;
    private custom_adapter adapter;
    private data datalist = new data();
    private Stock[] items = {};
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean deleteStock = false;
    int stockposition = 0;
    String values[] = new String[4];
    String dates[] = new String[4];
    SearchView mSearchView;
    YahooAPI yahooAPI;
    YahooAPIRefesh yahooAPIRefesh;

    String[] item = null;
    SimpleCursorAdapter simpleAdapter;
    ListView suggestionslist;
    Context thisContext;
    MenuItem searchItem;
    SharedPreferences pref;
    SharedPreferences pref2;
    SharedPreferences pref3;
    SharedPreferences pref4;



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

         if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String uri = intent.getDataString();
            index = uri;
             yahooAPI = new YahooAPI();
             yahooAPI.execute();
             mSearchView.setQuery("", false);
             mSearchView.clearFocus();
             searchItem.collapseActionView();
        }
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        index = query.toUpperCase();
        yahooAPI = new YahooAPI();
        yahooAPI.execute();
        mSearchView.setQuery("", false);
        mSearchView.clearFocus();
        searchItem.collapseActionView();

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // User changed the text
        return false;
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);


        searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSearchableInfo(manager.getSearchableInfo(
                new ComponentName(this, MainActivity.class)));



        return true;


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
            case R.id.menu_settings:
                aboutdialog();
                return true;
            case R.id.menu_search:
                return true;
            case R.id.menu_options:
                Intent j = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(j, 2);
                return true;
            case R.id.portfolio:
                Intent k = new Intent(MainActivity.this, Portfolio.class);
                startActivityForResult(k, 3);
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }


    }


    public void aboutdialog()
    {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final AlertDialog OptionDialog = builder.create();
            OptionDialog.setTitle("About" + "  (Version:" + version + ")");
            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.about_info, null);
            OptionDialog.setView(dialoglayout);
            OptionDialog.show();
            Button buttonrate = (Button) dialoglayout.findViewById(R.id.rateappbutton);

            buttonrate.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {


                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }

                }

            });
        }
        catch (Exception io)
        {

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // code for result
                deleteStock = true;
            }
            if (resultCode == RESULT_CANCELED && data != null) {

                String amountfromfrag = data.getStringExtra("amount");
                String valuefromfrag = data.getStringExtra("value");

                Stock updatedStock = arrayList.get(stockposition);
                updatedStock.value = datalist.getValueFromArray(stockposition);
                updatedStock.percent = datalist.getPercentFromArray(stockposition);
                updatedStock.change = datalist.getChangeFromArray(stockposition);
                updatedStock.open = datalist.getOpenFromArray(stockposition);
                updatedStock.high = datalist.getHighFromArray(stockposition);
                updatedStock.low = datalist.getLowFromArray(stockposition);
                updatedStock.volume = datalist.getVolumeFromArray(stockposition);
                updatedStock.annual = datalist.getAnnualFromArray(stockposition);
                updatedStock.time = datalist.getTimeFromArray(stockposition);
                updatedStock.valueofShares = valuefromfrag;
                updatedStock.amountofShares = amountfromfrag;
                arrayList.set(stockposition, updatedStock);
                String id = Integer.toString(updatedStock.primaryid);
                myDB.updateData(id, updatedStock.index, updatedStock.name, updatedStock.value,
                        updatedStock.percent, Integer.toString(updatedStock.color), updatedStock.change, updatedStock.open,
                        updatedStock.high, updatedStock.low, updatedStock.volume, updatedStock.annual, updatedStock.time, updatedStock.amountofShares, updatedStock.valueofShares);





            }
        }
    }


    public void onResume(){
        super.onResume();
        if (deleteStock == true){
            deleteStock = false;
            deleteItem(stockposition);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //sets up preferences
        pref = this.getSharedPreferences("com.christianphan.simplestock", Context.MODE_PRIVATE);
        pref2 = this.getSharedPreferences("indexes", Context.MODE_PRIVATE);
        pref3 = this.getSharedPreferences("AlarmActivated", Context.MODE_PRIVATE);
        pref4 = this.getSharedPreferences("Low_High", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        final ListView listview = (ListView) findViewById(R.id.listView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout. setColorSchemeResources(R.color.colorPrimary);
        arrayList = new ArrayList<Stock>(Arrays.asList(items));
        adapter = new custom_adapter(this, arrayList);
        listview.setAdapter(adapter);
        myDB = new DataBaseHelper(this);
        mySEARCH = new CSVDatabase(this);
        getList();
        //loadSearch();
        thisContext =  getApplicationContext();


        //sets Default Repeating Value to
        if(pref3.getInt("Repeating",-1) == -1 )
        {
            pref3.edit().putInt("Repeating", 2).commit();
        }

        try
        {
            refresh();
        }
        catch (Exception e)
        {

        }


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent i = new Intent(MainActivity.this, AdditonalInfo.class);

                stockposition = position;

                String testValue = arrayList.get(stockposition).value;
                String testPercent = arrayList.get(stockposition).percent;
                String testName = arrayList.get(stockposition).name;
                String testColor = Integer.toString(arrayList.get(stockposition).color);
                String testIndex = arrayList.get(stockposition).index;
                String testVolume = arrayList.get(stockposition).volume;
                String testOpen = arrayList.get(stockposition).open;
                String testHigh = arrayList.get(stockposition).high;
                String testLow = arrayList.get(stockposition).low;
                String testChange = arrayList.get(stockposition).change;
                String testAnnual = arrayList.get(stockposition).annual;
                String testTime = arrayList.get(stockposition).time;
                String testValueofStock = arrayList.get(stockposition).valueofShares;
                String testAmountofStock = arrayList.get(stockposition).amountofShares;
                String testdate1 = dates[0];
                String testdate2 = dates[1];
                String testdate3 = dates[2];
                String testdate4 = dates[3];
                String testprice1 = values[0];
                String testprice2 = values[1];
                String testprice3 = values[2];
                String testprice4 = values[3];
                String testprice5 = arrayList.get(stockposition).value;
                int intchange = datalist.IsPositive(stockposition);
                int idcode = arrayList.get(stockposition).primaryid;

                try {

                    intchange = Integer.parseInt(datalist.getChangeFromArray(stockposition).trim());
                } catch (NumberFormatException nfe) {

                }


                Bundle b = new Bundle();


                b.putString("Value", testValue);
                b.putString("Percent", testPercent);
                b.putString("Name", testName);
                b.putString("Color", testColor);
                b.putString("Index", testIndex);
                b.putString("Volume", testVolume);
                b.putString("Open", testOpen);
                b.putString("High", testHigh);
                b.putString("Low", testLow);
                b.putString("Change", testChange);
                b.putString("Annual", testAnnual);
                b.putString("Time", testTime);
                b.putInt("Test", intchange);
                b.putString("Date1", testdate1);
                b.putString("Date2", testdate2);
                b.putString("Date3", testdate3);
                b.putString("Date4", testdate4);
                b.putString("Price1", testprice1);
                b.putString("Price2", testprice2);
                b.putString("Price3", testprice3);
                b.putString("Price4", testprice4);
                b.putString("Price5", testprice5);
                b.putString("Amount", testAmountofStock);
                b.putString("ValueOfShares", testValueofStock);
                b.putInt("position", stockposition);
                b.putInt("ID", idcode);
                i.putExtras(b);


                startActivityForResult(i,1);


            }

        });


        listview.setLongClickable(true);

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                @Override
                                                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                                                               final int pos, long id) {


                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    final AlertDialog OptionDialog = builder.create();
                                                    OptionDialog.setTitle("Delete (" + arrayList.get(pos).index + ")" );
                                                    LayoutInflater inflater = getLayoutInflater();
                                                    View dialoglayout = inflater.inflate(R.layout.stockdelete, null);
                                                    OptionDialog.setView(dialoglayout);
                                                    OptionDialog.show();


                                                    Button submit = (Button) dialoglayout.findViewById(R.id.deleteButton);
                                                    Button cancel = (Button) dialoglayout.findViewById(R.id.noDeleteButton);

                                                    submit.setOnClickListener(new View.OnClickListener() {


                                                        @Override
                                                        public void onClick(View v) {

                                                            deleteItem(pos);

                                                            OptionDialog.cancel();

                                                        }


                                                    });


                                                    cancel.setOnClickListener(new View.OnClickListener() {

                                                        @Override
                                                        public void onClick(View v) {
                                                            OptionDialog.cancel();
                                                        }

                                                    });

                                                    return true;
                                                }



                                            });




        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {



            @Override
            public void onRefresh()
            {
                refresh();

            }




        });
        ;

    }


    public class YahooAPI extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... args) {

            try {

                yahoofinance.Stock stock = YahooFinance.get(index);




                datalist.setVaribles(stock.getName().toString(),stock.getQuote().getPrice().toString(), stock.getSymbol().toString(),
                        stock.getQuote().getChangeInPercent().toString(), stock.getQuote().getChange().toString(), stock.getQuote().getOpen().toString(),
                        stock.getQuote().getDayHigh().toString(), stock.getQuote().getDayLow().toString(),stock.getQuote().getVolume().toString(),
                        stock.getQuote().getYearHigh().toString(), stock.getQuote().getLastTradeDateStr(), "0", "0");

                ;

            } catch (MalformedURLException ex) {
                datalist.setStringName("error");
                return null;
            } catch (IOException ex) {
                datalist.setStringName("error");;
                return null;
            } catch (Exception e) {
                datalist.setStringName("Invalid Index");
                return null;
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);


            if (datalist.getStringName() != "error" && datalist.getStringName() != "Invalid Index" ) {


                Stock addedStock = new Stock(datalist.getStringName(), datalist.getStringValue(),
                        datalist.getStringIndex(), getApplicationContext(), datalist.getStringPercent(), 0, 0, datalist.getStringChange(),
                        datalist.getStringAnnual(), datalist.getStringHigh(),
                        datalist.getStringVolume(), datalist.getStringOpen(), datalist.getStringLow(), datalist.getStringTime(), datalist.getAmountofShares(), datalist.getValueofShares());


                datalist.setStringColor(Integer.toString(addedStock.color));

                myDB.insertData(datalist.getStringIndex(), datalist.getStringName(), datalist.getStringValue(),
                        datalist.getStringPercent(), datalist.getStringColor(), datalist.getStringChange(), datalist.getStringOpen(),datalist.getStringHigh(),
                        datalist.getStringLow(), datalist.getStringVolume(), datalist.getStringAnnual(), datalist.getStringTime(), datalist.getAmountofShares(), datalist.getValueofShares());


                //grabs stock id from myDB and sets the stock to have the same id
                Cursor res = myDB.getLastData();
                String StringID = res.getString(0);
                addedStock.setID(StringID);


                arrayList.add(addedStock);
                adapter.notifyDataSetChanged();
                datalist.AddToArrayList();

                Toast toast = Toast.makeText(getApplicationContext(), "(" + datalist.getStringIndex() + ")" + " added", Toast.LENGTH_SHORT);
                toast.show();



            } else if(datalist.getStringName() == "Invalid Index" )
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Sorry, (" +  index + ") is not availible", Toast.LENGTH_SHORT);
                toast.show();

            }
            else
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT);
                toast.show();

            }

        }


    }

    public void refresh() {

        yahooAPIRefesh = new YahooAPIRefesh();
        yahooAPIRefesh.execute();

    }


    public class YahooAPIRefesh extends AsyncTask<String, String, String> {

        private int count = 0;

        @Override
        protected String doInBackground(String... args) {

            if(arrayList.size() != 0) {
                try {

                    //gets all indexes into an array
                    String[] indexArray = new String[arrayList.size()];
                    for (int j = 0; j < arrayList.size(); j++) {
                        indexArray[j] = arrayList.get(j).index;
                    }


                    //single request
                    Map<String, yahoofinance.Stock> stock = YahooFinance.get(indexArray);


                    for (int i = 0; i < arrayList.size(); i++) {


                        //updates new datalist with information from new refresh
                        datalist.updateArrayList(i, stock.get(indexArray[i]).getQuote().getPrice().toString(),
                                stock.get(indexArray[i]).getQuote().getChangeInPercent().toString(), stock.get(indexArray[i]).getQuote().getChange().toString(),
                                stock.get(indexArray[i]).getQuote().getOpen().toString(), stock.get(indexArray[i]).getQuote().getDayHigh().toString(),
                                stock.get(indexArray[i]).getQuote().getDayLow().toString(), stock.get(indexArray[i]).getQuote().getVolume().toString(), stock.get(indexArray[i]).getQuote().getYearHigh().toString(),
                                stock.get(indexArray[i]).getQuote().getLastTradeDateStr());
                        count++;

                    }


                } catch (MalformedURLException ex) {
                    datalist.setStringName("BAD_INDEX");
                    return "error";
                } catch (IOException ex) {
                    datalist.setStringName("error");
                    return "error";
                } catch (Exception e) {
                    datalist.setStringName("error");
                    return "error";
                }

            }
            else
            {
                datalist.setStringName("No list");
                return null;
            }

            datalist.setStringName("Success");
            return "Success";


        }


        protected void onPostExecute(String avoid) {

            super.onPostExecute(avoid);
            if (datalist.getStringName() != "error" && datalist.getStringName() != "No list" && datalist.getStringName() != "BAD_INDEX") {

                for (int i = 0; i < arrayList.size(); i++) {


                    Stock updatedStock = arrayList.get(i);
                    updatedStock.value = datalist.getValueFromArray(i);
                    updatedStock.percent = datalist.getPercentFromArray(i);
                    updatedStock.change = datalist.getChangeFromArray(i);
                    updatedStock.open = datalist.getOpenFromArray(i);
                    updatedStock.high = datalist.getHighFromArray(i);
                    updatedStock.low = datalist.getLowFromArray(i);
                    updatedStock.volume = datalist.getVolumeFromArray(i);
                    updatedStock.annual = datalist.getAnnualFromArray(i);
                    updatedStock.time = datalist.getTimeFromArray(i);
                    updatedStock.valueofShares = arrayList.get(i).valueofShares;
                    updatedStock.amountofShares = arrayList.get(i).amountofShares;

                    arrayList.set(i, updatedStock);
                    String id = Integer.toString(updatedStock.primaryid);
                    myDB.updateData(id, updatedStock.index, updatedStock.name, updatedStock.value,
                            updatedStock.percent, Integer.toString(updatedStock.color), updatedStock.change, updatedStock.open,
                            updatedStock.high, updatedStock.low, updatedStock.volume, updatedStock.annual, updatedStock.time, updatedStock.amountofShares, updatedStock.valueofShares);


                }



                adapter.notifyDataSetChanged();

                Toast toast = Toast.makeText(getApplicationContext(), "Stock List Updated", Toast.LENGTH_SHORT);
                toast.show();
                mSwipeRefreshLayout.setRefreshing(false);

            }
            else if (datalist.getStringName() == "BAD_INDEX" && datalist.getStringName() != "No list")
            {
                String indexname = arrayList.get(count).index;
                Toast toast = Toast.makeText(getApplicationContext(), "Unable to update " + indexname + ". Please remove it.", Toast.LENGTH_SHORT);
                toast.show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
            else if(datalist.getStringName() != "No list"){
                Toast toast = Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT);
                toast.show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
            else if(datalist.getStringName() == "No list")
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }


        }

    }









    public boolean getList() {
        Cursor res = myDB.getAllData();


        if (res.getCount() == 0) {
            return false;
        }



        while (res.moveToNext()) {


            datalist.setVaribles(res.getString(2), res.getString(3), res.getString(1), res.getString(4), res.getString(6) ,res.getString(7), res.getString(8),
                    res.getString(9), res.getString(10), res.getString(11) ,res.getString(12), res.getString(13), res.getString(14));



            String StringID = res.getString(0);
            // String StringIndex =res.getString(1);
            //    StringName = res.getString(2);
            //    StringValue = res.getString(3);
            //    StringPercent = res.getString(4);
            String   StringColor = res.getString(5);
            //    StringChange = res.getString(6);


            Stock stockitem = new Stock(datalist.getStringName(), datalist.getStringValue(),
                    datalist.getStringIndex(), getApplicationContext(), datalist.getStringPercent(), Integer.parseInt(StringColor),Integer.parseInt(StringID),
                    datalist.getStringChange(), datalist.getStringAnnual(), datalist.getStringHigh(),
                    datalist.getStringVolume(), datalist.getStringOpen(), datalist.getStringLow(), datalist.getStringTime(), datalist.getAmountofShares(), datalist.getValueofShares());

            arrayList.add(stockitem);
            datalist.AddToArrayList();



        }

        return true;


    }

    public void deleteItem(int position)
    {

        try {
            String indexname = arrayList.get(position).index;

            Integer deletedRow = myDB.deleteData(Integer.toString(arrayList.get(position).primaryid));
            String prefcode = Integer.toString(arrayList.get(position).primaryid);
            arrayList.remove(position);
            adapter.notifyDataSetChanged();
            datalist.removeArrayList(position);
            int alarmID =  pref.getInt(prefcode, 0);

            if(alarmID != 0)
            {
                int notificationlist = pref3.getInt("Count", 0);
                pref.edit().remove(prefcode).apply();
                pref2.edit().remove(prefcode).apply();
                pref3.edit().putInt("Count", notificationlist -1).apply();
                String test = pref4.getString("HIGH" + prefcode, "0");
                pref4.edit().remove("LOW" + prefcode).apply();
                pref4.edit().remove("HIGH" + prefcode).apply();

                if(pref3.getInt("Count", 0) == 0)
                {
                    if(pref3.getString("alarm_ON_OR_OFF", "NEUTRAL").contains("TRUE"))
                    {
                        pref3.edit().putString("alarm_ON_OR_OFF", "NEUTRAL").apply();
                        Log.d("ALARM STATUS", pref3.getString("alarm_ON_OR_OFF", "NEUTRAL"));
                        cancelAlarm();
                    }
                }
            }
            Toast toast = Toast.makeText(getApplicationContext(), "(" + indexname + ")" + " Deleted", Toast.LENGTH_LONG);
            toast.show();
        }
        catch(Exception e)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Deletion Error", Toast.LENGTH_LONG);
            toast.show();

        }
    }

    public void cancelAlarm()
    {
        Calendar calendar = Calendar.getInstance();
        Intent alarm = new Intent(getApplicationContext(), AlarmReceiver.class);
        alarm.setAction("NEW_ALERT");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }





    //public void loadSearch()
   // {
    //    DataBaseReader myDbHelper = new DataBaseReader(getApplicationContext());
   //     myDbHelper = new DataBaseReader(this);

    //    try {

  //          myDbHelper.createDataBase();

  //      } catch (IOException ioe) {

  //          throw new Error("Unable to create database");

   //     }

  //      try {

    //        myDbHelper.openDataBase();

   //     }catch(SQLException sqle){

  //          throw sqle;

  //      }

  //  }




}
