package com.example.stockapp;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.Menu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class SearchableActivity extends AppCompatActivity {

    ArrayList<NewsItem> newsItems = new ArrayList<>();

    private RequestQueue queue;

    final Context context = this;
    private Button button;

    private TextView sharesOwnedTextView;

    private TextView changeTextView;

    private TextView marketValueTextView;

    private TextView avgCostTextView;

    private TextView totalCostTextView;

    private TextView companyNameTextView;
    private TextView companyIpoTextView;
    private TextView companyIndustryTextView;
    private TextView companyWebpageTextView;
    private TextView companyPeersTextView;

    private TextView lowPriceTextView;

    private TextView openPriceTextView;

    private TextView highPriceTextView;

    private TextView prevCloseTextView;
    private TextView cPriceTextView;
    private TextView dPriceTextView;
    private TextView dpPriceTextView;

    private TextView tickerPriceTextView;

    private boolean isStarFilled = false;

    private float closePrice = 0;

    private String query_global;

    private double moneyInWallet = 0;

    ArrayList<String> watchlistTickerArray = new ArrayList<>();

    private String curTicker;
    private WebView webView;
    private WebView webView3;
    JSONArray historicalDataArray;

    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query_global = intent.getStringExtra(SearchManager.QUERY);
        }

        fetchWalletData();

        button = (Button) findViewById(R.id.buttonShowCustomDialog);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.custom);
                dialog.setTitle("Trade Stocks");

                EditText editText = dialog.findViewById(R.id.editText);
                TextView tradeBalanceText = dialog.findViewById(R.id.tradeBalanceText);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        double value = Double.parseDouble(s.toString()) * closePrice;
                        tradeBalanceText.setText(String.format(Locale.getDefault(), "%.2f", value));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                Button buyButton = dialog.findViewById(R.id.buyButton);
                Button sellButton = dialog.findViewById(R.id.sellButton);

                // Set click listener for the BUY button
                buyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss the current dialog
                        dialog.dismiss();

                        String quantityString = editText.getText().toString();

                        // Check if the quantity entered is valid
                        if (quantityString.isEmpty() || Float.parseFloat(quantityString) <= 0) {
                            // Display toast message for invalid input
                            Toast.makeText(context, "Cannot buy non-positive shares", Toast.LENGTH_SHORT).show();
                            return; // Exit the method without proceeding further
                        }

                        float quantityData = Float.parseFloat(quantityString);
                        float totalCostData = quantityData * closePrice;

                        // Check if the user has enough money to buy
                        if (totalCostData > moneyInWallet) {
                            // Display toast message for insufficient funds
                            Toast.makeText(context, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addToPortfolio(query_global, editText, closePrice, "buy");
                        moneyInWallet = moneyInWallet - totalCostData;
                        updateWallet(moneyInWallet);

                        // Show the buy_success dialog
                        final Dialog successDialog = new Dialog(context);
                        successDialog.setContentView(R.layout.buy_success);

                        // Get reference to views in the buy_success dialog
                        TextView successMessage = successDialog.findViewById(R.id.buy_success_message);

                        // Get the number of shares from the EditText in the custom dialog
                        String shares = editText.getText().toString();

                        // Set the success message with the number of shares bought
                        successMessage.setText("Congratulations! You bought " + shares + " shares.");

                        // Show the buy_success dialog
                        successDialog.show();
                    }
                });

                // Set click listener for the SELL button
                sellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss the current dialog
                        dialog.dismiss();

                        String quantityString = editText.getText().toString();

                        // Check if the quantity entered is valid
                        if (quantityString.isEmpty() || Float.parseFloat(quantityString) <= 0) {
                            // Display toast message for invalid input
                            Toast.makeText(context, "Cannot sell non-positive shares", Toast.LENGTH_SHORT).show();
                            return; // Exit the method without proceeding further
                        }

                        float quantityData = Float.parseFloat(quantityString);
                        float totalCostData = quantityData * closePrice;

                        addToPortfolio(query_global, editText, closePrice, "sell");
                        moneyInWallet = moneyInWallet + totalCostData;
                        updateWallet(moneyInWallet);

                        // Show the buy_success dialog
                        final Dialog successDialog = new Dialog(context);
                        successDialog.setContentView(R.layout.sell_success);

                        // Get reference to views in the buy_success dialog
                        TextView successMessage = successDialog.findViewById(R.id.sell_success_message);

                        // Get the number of shares from the EditText in the custom dialog
                        String shares = editText.getText().toString();

                        // Set the success message with the number of shares bought
                        successMessage.setText("Congratulations! You sold " + shares + " shares.");

                        // Show the buy_success dialog
                        successDialog.show();
                    }
                });

                dialog.show();
            }
        });

        toolbar = findViewById(R.id.tickerToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sharesOwnedTextView = findViewById(R.id.sharesOwnedTextView);
        changeTextView = findViewById(R.id.changeTextView);
        avgCostTextView = findViewById(R.id.avgCostTextView);
        totalCostTextView = findViewById(R.id.totalCostTextView);
        marketValueTextView = findViewById(R.id.marketValueTextView);

        companyNameTextView = findViewById(R.id.companyNameTextView);
        companyIpoTextView = findViewById(R.id.companyIpoTextView);
        companyIndustryTextView = findViewById(R.id.companyIndustryTextView);
        companyWebpageTextView = findViewById(R.id.companyWebpageTextView);
        companyPeersTextView = findViewById(R.id.companyPeersTextView);

        lowPriceTextView = findViewById(R.id.lowPriceTextView);
        openPriceTextView = findViewById(R.id.openPriceTextView);
        highPriceTextView = findViewById(R.id.highPriceTextView);
        prevCloseTextView = findViewById(R.id.prevCloseTextView);

        cPriceTextView = findViewById(R.id.cPriceTextView);
        dPriceTextView = findViewById(R.id.dPriceTextView);
        dpPriceTextView = findViewById(R.id.dpPriceTextView);
        tickerPriceTextView = findViewById(R.id.tickerPriceTextView);

        Log.d("SearchableActivity","IN MAIN BEFORE FETCH COMP ");

        toolbar.setTitle(query_global);

//        if (query_global != null && !query_global.isEmpty()) {
//            toolbar.setTitle(query_global);
//        }
        fetchCompanyNews(query_global);

        toolbar.setTitle(query_global);

        fetchCompanyDetails(query_global);
        Log.d("SearchableActivity","IN MAIN AFTER FETCH COMP ");

        fetchCompanyQuote(query_global);

        fetchCompanyInsider(query_global);

        webView = findViewById(R.id.webView_2);

        getHistoricalChartData(query_global);

        fetchCompanyPeers(query_global);

    }

    private void addSentimentTable(double totalMSPR, double posMSPR, double negMSPR, double totalChange, double posChange, double negChange) {

        TableLayout tableLayout = findViewById(R.id.tableLayout);



        // Example data
        String[] rowData1 = {query_global, "MSRP", "Change"};
        String[] rowData2 = {"Total", String.valueOf(totalMSPR), String.valueOf(totalChange)};
        String[] rowData3 = {"Positive", String.valueOf(posMSPR), String.valueOf(posChange)};
        String[] rowData4 = {"Negative", String.valueOf(negMSPR), String.valueOf(negChange)};


        // Add rows to the table
        addRow(tableLayout, rowData1);
        addRow(tableLayout, rowData2);
        addRow(tableLayout, rowData3);
        addRow(tableLayout, rowData4);
    }

    private void addRow(TableLayout tableLayout, String[] rowData) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(layoutParams);

        for (String data : rowData) {
            TextView textView = new TextView(this);
            textView.setText(data);
            textView.setPadding(16, 16, 16, 16); // Adjust padding as needed
            tableRow.addView(textView);
        }

        tableLayout.addView(tableRow);
    }

    private void getHistoricalChartData(String tickerSymbol){
        Log.d("SearchableActivity","HERE IN GETHISTCHART");
        String url = "https://kas25-25.wl.r.appspot.com/company-profile-poly?symbol="+tickerSymbol;
        queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("SearchableActivity","HERE IN GETHISTCHART response "+response);
                            JSONArray highchartsDataArray = jsonObject.getJSONArray("highchartsData");

                            createHistoricalChart(highchartsDataArray);

                            fetchCompanyRec(query_global);
                            fetchCompanyEps(query_global);
                            fetchCompanyHourly(query_global);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // Display the first 500 characters of the response string.


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void createHistoricalChart(JSONArray dataArray){
        Log.d("SearchableActivity","Data for creating historical chart is " + dataArray);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

//        webView.loadUrl("file:///android_asset/hist_chart.html");

        String escapedQuery = query_global.replace("'", "\\'");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                webView.loadUrl("javascript:createHistoricalChart(" + dataArray + ", '" + escapedQuery + "')");
//                webView.loadUrl("javascript:createHistoricalChart(" + dataArray + "," + query_global+ ")");
            }
        });
    }

    private void createRecChart(JSONArray dataArray){
        Log.d("SearchableActivity","Data for creating rec chart is " + dataArray);

        WebView webView3 = findViewById(R.id.webView_3);

        WebSettings webSettings = webView3.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView3.loadUrl("file:///android_asset/rec_chart.html");

        webView3.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("SearchableActivity","HERE IN CREATERECCHART");
                webView3.loadUrl("javascript:createRecommendationsChart(" + dataArray + ")");
            }
        });
    }

    private void createEpsChart(JSONArray dataArray){
        Log.d("SearchableActivity","Data for creating eps chart is " + dataArray);

        WebView webView3 = findViewById(R.id.webView_4);

        WebSettings webSettings = webView3.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView3.loadUrl("file:///android_asset/eps_chart.html");

        webView3.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("SearchableActivity","HERE IN CREATE EPS CHART");

                webView3.loadUrl("javascript:createEarningsChart(" + dataArray + ")");
            }
        });
    }

    private void createHourlyChart(JSONArray dataArray){
        Log.d("SearchableActivity","Data for creating eps chart is " + dataArray);

        WebView webView3 = findViewById(R.id.webView_5);

        WebSettings webSettings = webView3.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView3.loadUrl("file:///android_asset/hourly_chart.html");

        String escapedQuery = query_global.replace("'", "\\'");

        webView3.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("SearchableActivity","HERE IN CREATE EPS CHART");
                webView3.loadUrl("javascript:createHourlyChart(" + dataArray + ", '" + escapedQuery + "')");
            }
        });
    }

    private void updateWallet(double updatedMoneyInWallet) {

        queue = Volley.newRequestQueue(this);
//        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/adddata";
        String url = "http://192.168.56.1:8000/api/todoapp/updatewalletdata";

        JSONObject params = new JSONObject();
        try {
            params.put("curWalletValue", updatedMoneyInWallet);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("SearchableActivity", "in updateWallet params: "+params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        Log.d("SearchableActivity", "Wallet data updated");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorMessage = new String(error.networkResponse.data);
                            Log.e("SearchableActivity", "Error updating wallet: " + errorMessage);
                        } else {
                            Log.e("SearchableActivity", "Error updating wallet: " + error.getMessage());
                        }
                    }
                });


        queue.add(request);


    }

    private void addToPortfolio(String queryGlobal, EditText editText, float closePrice, String buyOrSell) {

        RequestQueue queue_portfolio = Volley.newRequestQueue(this);
//        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/adddata";
        String url = "http://192.168.56.1:8000/api/todoapp/addportdata";

        String companyName = companyNameTextView.getText().toString();
        int colonIndex = companyName.indexOf(":");
        if (colonIndex != -1 && colonIndex < companyName.length() - 1) {
            // Extract the substring after the colon (including any leading or trailing spaces)
            companyName = companyName.substring(colonIndex + 1).trim();
        }

        String quantityString = editText.getText().toString();
        Log.d("SearchableActivity", "in addToPortfolio quantityString: "+quantityString);
        float quantityData = Float.parseFloat(quantityString);
        Log.d("SearchableActivity", "in addToPortfolio quantityData: "+quantityData);

        float totalCostData = quantityData * closePrice;

        Log.d("SearchableActivity", "in addToPortfolio query: "+queryGlobal);

        Log.d("SearchableActivity", "in addToPortfolio name: "+companyName);

        Log.d("SearchableActivity", "in addToPortfolio buyOrSell: "+buyOrSell);

        JSONObject params = new JSONObject();
        try {
            params.put("nameData", companyName);
            params.put("tickerData", queryGlobal);
            params.put("quantityData", quantityData);
            params.put("totalCostData", totalCostData);
            params.put("buyOrSellData", buyOrSell);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("SearchableActivity", "in addToPortfolio params: "+params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        Log.d("SearchableActivity", "Data added to portfolio");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorMessage = new String(error.networkResponse.data);
                            Log.e("SearchableActivity", "Error adding data to portfolio: " + errorMessage);
                        } else {
                            Log.e("SearchableActivity", "Error adding data to portfolio: " + error.getMessage());
                        }
                    }
                });


        queue_portfolio.add(request);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem starItem = menu.findItem(R.id.action_star);

        fetchWatchlistData();

        boolean isInWatchlist = checkIfInWatchlist(query_global);

        // Set the icon based on whether the query is in the watchlist
        if (isInWatchlist) {
            // Set the filled star icon
            starItem.setIcon(R.drawable.full_star);
        } else {
            // Set the empty star icon
            starItem.setIcon(R.drawable.star_border);
        }

        return true;

//        starItem.setIcon(R.drawable.star_border); // Set the empty star icon initially
//        return true;
    }

    public boolean checkIfInWatchlist(String query_global) {



        for (String ticker : watchlistTickerArray) {
            if (ticker.equalsIgnoreCase(query_global)) {
                return true; // Query matches a ticker in the watchlist
            }
        }
        return false;
    }

    private void fetchWatchlistData() {

        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/getwatchdata";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String ticker = jsonObject.getString("ticker");
                                watchlistTickerArray.add(ticker);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_star) {
            // Handle click on the star icon
            Log.d("SearchableActivity", "in onOptionsItemSelected action star");

            toggleStarIcon(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStarIcon(MenuItem item) {
        Log.d("SearchableActivity", "in toggleStarIcon begin");
        if (isStarFilled) {
            Log.d("SearchableActivity", "in toggleStarIcon borderstar");
            // If the star is filled, empty it
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.star_border));
            removeFromWatchlist();
        } else {
            // If the star is empty, fill it
            Log.d("SearchableActivity", "in toggleStarIcon fillstar");
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.full_star));
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            Log.d("SearchableActivity", "in toggleStarIcon fillstar query: "+query);
            String companyName = companyNameTextView.getText().toString();
            int colonIndex = companyName.indexOf(":");
            if (colonIndex != -1 && colonIndex < companyName.length() - 1) {
                // Extract the substring after the colon (including any leading or trailing spaces)
                companyName = companyName.substring(colonIndex + 1).trim();
            }
            Log.d("SearchableActivity", "in toggleStarIcon fillstar companyName: "+companyName);
            addToWatchlist(query, companyName);
        }
        // Update the star state
        isStarFilled = !isStarFilled;
    }

    private void addToWatchlist(String query, String name) {
        Toast.makeText(SearchableActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
        RequestQueue queue_watchlist = Volley.newRequestQueue(this);
//        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/adddata";
        String url = "http://192.168.56.1:8000/api/todoapp/adddata";

        Log.d("SearchableActivity", "in addToWatchlist query: "+query);

        Log.d("SearchableActivity", "in addToWatchlist name: "+name);

        JSONObject params = new JSONObject();
        try {
            params.put("nameData", name);
            params.put("tickerData", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("SearchableActivity", "in addToWatchlist params: "+params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        Log.d("SearchableActivity", "Data added to watchlist");


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorMessage = new String(error.networkResponse.data);
                            Log.e("SearchableActivity", "Error adding data to watchlist: " + errorMessage);
                        } else {
                            Log.e("SearchableActivity", "Error adding data to watchlist: " + error.getMessage());
                        }
                    }
                });


        queue_watchlist.add(request);

    }


    private void removeFromWatchlist() {
        // Get the query from the intent
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        // TODO: Send a DELETE request to the backend API to remove the query from the watchlist
        Toast.makeText(SearchableActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    private void fetchWalletData(){
        queue = Volley.newRequestQueue(this);
//        String url = "http://192.168.56.1:8000/company-profile";
        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/getwalletdata";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

//                        textView.setText("Response is: " + response.substring(0, Math.min(response.length(), 500)));
                        Log.d("SearchableActivity", "money in wallet response - "+response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0); // Get the first object from the array
                            moneyInWallet = jsonObject.getDouble("curWalletValue");

                            // Now you have the wallet money value in the moneyInWallet variable
                            Log.d("SearchableActivity", "Wallet Money Value: " + moneyInWallet);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        moneyInWallet = Float.parseFloat(response);
//                        Log.d("SearchableActivity", "money in wallet - "+moneyInWallet);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });

        queue.add(stringRequest);
    }


    private void fetchCompanyDetails(String query) {
        Log.d("SearchableActivity","IN FETCH COMP DETAILS "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SearchableActivity","BEFORE DISPLAY COMP DETAILS ");
                        displayCompanyDetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyNews(String query) {
        Log.d("SearchableActivity","IN FETCH COMP NEWS "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-news?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY newsDataArray - ");
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray newsDataArray = jsonResponse.getJSONArray("newsData");

                            Log.d("SearchableActivity", "newsDataArray - " + newsDataArray);

                            // Now you can pass this array to your method for further processing
                            setUpNewsItems(newsDataArray);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyRec(String query) {
        Log.d("SearchableActivity","IN FETCH COMP REC "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-rec?symbol="+query;
//        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY recDataArray - ");
//                            JSONObject jsonResponse = new JSONObject(response);
//                            JSONArray recDataArray = jsonResponse.getJSONArray("recData");
//
//                            Log.d("SearchableActivity", "recDataArray - " + recDataArray);
//
//                            // Now you can pass this array to your method for further processing
//                            createRecChart(recDataArray);

                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray recDataArray = jsonObject.getJSONArray("recData");
                            Log.d("SearchableActivity", "recDataArray - "+recDataArray);

                            createRecChart(recDataArray);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyHourly(String query) {
        String fromDate = "2024-05-01"; // Example date format
        String toDate = "2024-05-02"; // Example date format

        Log.d("SearchableActivity","IN FETCH COMP REC "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/hourly-chart?symbol=" + query + "&fromDate=" + fromDate + "&toDate=" + toDate;
//        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY recDataArray - ");
//                            JSONObject jsonResponse = new JSONObject(response);
//                            JSONArray recDataArray = jsonResponse.getJSONArray("recData");
//
//                            Log.d("SearchableActivity", "recDataArray - " + recDataArray);
//
//                            // Now you can pass this array to your method for further processing
//                            createRecChart(recDataArray);

                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray recDataArray = jsonObject.getJSONArray("hourlyChartData");
                            Log.d("SearchableActivity", "recDataArray - "+recDataArray);

                            createHourlyChart(recDataArray);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyEps(String query) {
        Log.d("SearchableActivity","IN FETCH COMP EPS "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-eps?symbol="+query;
//        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY epsDataArray - ");

                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray epsDataArray = jsonObject.getJSONArray("earningsData");
                            Log.d("SearchableActivity", "epsDataArray - "+epsDataArray);

                            createEpsChart(epsDataArray);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyInsider(String query) {
        Log.d("SearchableActivity","IN FETCH COMP INSIDER "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-insider?symbol="+query;
//        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY recDataArray - ");

                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject insiderData = jsonObject.getJSONObject("insiderData");
                            JSONArray insiderDataArray = insiderData.getJSONArray("data");
                            Log.d("SearchableActivity", "insiderDataArray - "+insiderDataArray);

                            double totalMSPR = 0;
                            double posMSPR = 0;
                            double negMSPR = 0;

                            double totalChange = 0;
                            double posChange = 0;
                            double negChange = 0;

                            for (int i = 0; i < insiderDataArray.length(); i++) {
                                JSONObject insiderDataInside = insiderDataArray.getJSONObject(i);
                                double mspr = insiderDataInside.getDouble("mspr");
                                double change = insiderDataInside.getDouble("change");

                                totalMSPR += mspr;
                                totalChange += change;

                                if (mspr >= 0) {
                                    posMSPR += mspr;
                                } else if (mspr < 0) {
                                    negMSPR += mspr;
                                }

                                if (change >= 0) {
                                    posChange += change;
                                } else if (change < 0) {
                                    negChange += change;
                                }
                            }

                            addSentimentTable(totalMSPR, posMSPR, negMSPR, totalChange, posChange, negChange);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyQuote(String query) {
        Log.d("SearchableActivity","IN FETCH COMP QUOTE "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-quote/"+query;
//        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol="+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY quoteDataArray - ");

                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject quoteData = jsonObject.getJSONObject("quoteData");
                            String lowPrice = quoteData.getString("l");
                            String openPrice = quoteData.getString("o");
                            String highPrice = quoteData.getString("h");
                            String prevClose = quoteData.getString("pc");
                            closePrice = Float.parseFloat(quoteData.getString("c"));
                            float dPrice = Float.parseFloat(quoteData.getString("d"));
                            float dpPrice = Float.parseFloat(quoteData.getString("dp"));

                            lowPriceTextView.setText("Low Price: " + lowPrice);
                            openPriceTextView.setText("Open Price: " + openPrice);
                            highPriceTextView.setText("High Price: " + highPrice);
                            prevCloseTextView.setText("Previous Close: " + prevClose);

                            cPriceTextView.setText("c: " + closePrice);
                            dPriceTextView.setText("d: " + dPrice);
                            dpPriceTextView.setText("dp: " + dpPrice);
                            tickerPriceTextView.setText("Ticker: " + query_global);


                            fetchPortfolioDetails(query_global);
                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void fetchCompanyPeers(String query) {
        Log.d("SearchableActivity","IN FETCH COMP PEERS "+query);

        queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-peers/"+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("SearchableActivity", "in TRY peersDataArray - ");

                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray peersData = jsonObject.getJSONArray("peersData");
                            // Convert the "peersData" array to a string for display
                            StringBuilder peersStringBuilder = new StringBuilder();
                            for (int i = 0; i < peersData.length(); i++) {
                                String peerName = peersData.getString(i);
                                peersStringBuilder.append(peerName).append("\n");
                            }
                            String peers = peersStringBuilder.toString();
//                            companyPeersTextView.setText("Company Peers: " + peers);

                            SpannableString spannableString = new SpannableString(peers);

                            // Apply clickable appearance to each peer
                            for (int i = 0; i < peersData.length(); i++) {
                                String peer = peersData.getString(i);
                                int startIndex = peers.indexOf(peer);
                                int endIndex = startIndex + peer.length();
                                spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            // Set the styled text to companyPeersTextView
                            companyPeersTextView.setText(spannableString);

                            companyPeersTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Start SearchableActivity with the clicked peer as the intent query
                                    Intent intent = new Intent(SearchableActivity.this, SearchableActivity.class);
                                    intent.setAction(Intent.ACTION_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, "QCOM");
                                    startActivity(intent);
                                }
                            });

                        } catch (JSONException e) {
                            Log.e("SearchableActivity", "Error parsing JSON response: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void displayCompanyDetails(String response) {
        Log.d("SearchableActivity","IN DISPLAY COMP DETAILS ");
        try {
            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(response);

            // Get the "companyProfile" object from the response
            JSONObject companyProfile = jsonObject.getJSONObject("companyProfile");

            // Extract the "name" and "ipo" values from the companyProfile object
            String companyName = companyProfile.getString("name");
            String ipoDate = companyProfile.getString("ipo");
            String industry = companyProfile.getString("finnhubIndustry");

            companyNameTextView.setText("Company Name: " + companyName);
            companyIpoTextView.setText("IPO Start Date: " + ipoDate);
            companyIndustryTextView.setText("Industry: " + industry);

            String webpage = companyProfile.getString("weburl");

            // Make the webpage URL clickable and navigate to the URL when clicked
            SpannableString webpageSpannableString = new SpannableString("Webpage: " + webpage);
            webpageSpannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    // Open the webpage URL in a browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpage));
                    startActivity(intent);
                }
            }, 9, webpageSpannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            companyWebpageTextView.setText(webpageSpannableString);
            companyWebpageTextView.setMovementMethod(LinkMovementMethod.getInstance());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpNewsItems(JSONArray newsDataArray) throws JSONException {

        for (int i = 0; i < newsDataArray.length(); i++) {
            JSONObject jsonObject = newsDataArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String headline = jsonObject.getString("headline");
            String source = jsonObject.getString("source");
            String summary = jsonObject.getString("summary");
            String url = jsonObject.getString("url");
            Uri image = Uri.parse(jsonObject.getString("image"));
            long datetime = Long.parseLong(jsonObject.getString("datetime"));

            Log.d("tag2", "id: " + id + ", headline: " + headline + ", source: " + source+", summary: " + summary);

//            newsItems.add(new NewsItem(headline, datetime, source));
            newsItems.add(new NewsItem(headline, summary, source, datetime, image, url));
        }
        updateNewsRecyclerView();

    }

    private void updateNewsRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);
        News_RecyclerViewAdapter adapter = new News_RecyclerViewAdapter(this, newsItems);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void fetchPortfolioDetails(String query) {

        queue = Volley.newRequestQueue(this);
        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/gettickerportdata/"+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        displayPortfolioDetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });

        queue.add(stringRequest);
    }

    private void displayPortfolioDetails(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            float quantityData = Float.parseFloat(jsonObject.getString("quantityData"));
            float totalCostData = Float.parseFloat(jsonObject.getString("totalCostData"));

            float avgCost = totalCostData / quantityData;

            float change = avgCost - closePrice;

            float marketValue = closePrice * quantityData;

            marketValueTextView.setText("Market Value: "+marketValue);
//            changeTextView.setText("Change: $"+change);
            totalCostTextView.setText("Total Cost: "+totalCostData);
            avgCostTextView.setText("Avg. Cost / Share: "+avgCost);
            sharesOwnedTextView.setText("Shares Owned: " + quantityData);

            SpannableString changeText = new SpannableString("Change: $" + change);

            // Determine the color based on whether change is positive or negative
            int colorResId;
            if (change > 0) {
                colorResId = R.color.green;
            } else if (change < 0) {
                colorResId = R.color.red;
            } else {
                colorResId = android.R.color.black; // Black color
            }

            // Apply the color to both the $ sign and the change number
            changeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, colorResId)), 7, changeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the SpannableString to the TextView
            changeTextView.setText(changeText);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}