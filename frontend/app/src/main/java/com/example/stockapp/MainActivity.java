package com.example.stockapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView; // Add import for TextView

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;

    private double moneyInWallet;

    private TextView TVWalletMoney;

    private TextView TVNetWorth;

    ArrayList<WatchModel> watchModels = new ArrayList<>();
    ArrayList<PortfolioItemModel> portfolioItemModels = new ArrayList<>();

    final double[] stockTotalValue = {0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fetchWalletData();



        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ImageButton searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                onSearchRequested();
            }
        });




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the TextView
        final TextView textView = findViewById(R.id.text);

        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = "http://192.168.56.1:8000/company-profile";
        queue = Volley.newRequestQueue(this);
        String url = "https://kas25-25.wl.r.appspot.com/company-profile?symbol=AAPL";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: " + response.substring(0, Math.min(response.length(), 500)));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
                Log.e("Volley Error", "Error: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

//        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);

        setUpWatchModels();
//        setUpPortModels();

        Date now = new Date();
        DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedTime = formatter.format(now);

        TextView curDateText = findViewById(R.id.textView4);
        curDateText.setText(formattedTime);

        TextView finhubFooterText = findViewById(R.id.textView3);
        finhubFooterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "https://finnhub.io/";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onSearchRequested() {
        // Trigger the system to display the search dialog
        return super.onSearchRequested();
    }

    interface QuoteCallback {
        void onQuoteFetched(double closePrice);
    }

    private void fetchCompanyQuote(String query, QuoteCallback callback) {
        Log.d("SearchableActivity","IN FETCH COMP QUOTE "+query);

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://kas26-422108.wl.r.appspot.com/company-quote/"+query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("SearchableActivity", "in TRY quoteDataArray - ");

                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject quoteData = jsonObject.getJSONObject("quoteData");
                            double closePrice = Double.parseDouble(quoteData.getString("c"));

                            // Call the callback with fetched closePrice
                            callback.onQuoteFetched(closePrice);
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


    private void fetchWalletData(){
        RequestQueue queue = Volley.newRequestQueue(this);
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
                            TVWalletMoney = findViewById(R.id.textViewWalletMoney);
                            TVWalletMoney.setText("Cash Balance: " + moneyInWallet);

                            calcNetWorth();
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

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setUpWatchModels() {

        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/getwatchdata";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String id = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                String ticker = jsonObject.getString("ticker");

                                String c = jsonObject.getString("c");
                                String d = jsonObject.getString("d");
                                String dp = jsonObject.getString("dp");
                                // Parse other fields as needed
                                Log.d("tag2", "id: " + id + ", name: " + name + ", ticker: " + ticker+", c: " + c);

                                watchModels.add(new WatchModel(ticker, name, c, d, dp));
                            }
                            updateRecyclerView();
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

    private void updateRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);
        Watch_RecyclerViewAdapter adapter = new Watch_RecyclerViewAdapter(this, watchModels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Handle swipe action here, for example, delete item from the list
                int position = viewHolder.getAdapterPosition();
                watchModels.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

// Create an ItemTouchHelper and attach it to your RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void calcNetWorth(){

        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/getportdata";



        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String ticker = jsonObject.getString("ticker");
                                String quantity = jsonObject.getString("quantityData");
                                String totalCost = jsonObject.getString("totalCostData");

                                fetchCompanyQuote(ticker, new QuoteCallback() {
                                    @Override
                                    public void onQuoteFetched(double closePrice) {
                                        // Use the fetched closePrice here
                                        Log.d("SearchableActivity", "Close Price: " + closePrice);
                                        stockTotalValue[0] = stockTotalValue[0] + closePrice * Double.parseDouble(quantity);
                                        Log.d("SearchableActivity", "stockTotalValue[0]: " + stockTotalValue[0]);

                                    }
                                });


                            }


                            TVNetWorth = findViewById(R.id.textViewNetWorth);
                            double netWorth = moneyInWallet + stockTotalValue[0];
                            Log.d("SearchableActivity", "Net Worth moneyInWallet: " + moneyInWallet);
                            Log.d("SearchableActivity", "Net Worth stockTotalValue[0]: " + stockTotalValue[0]);
                            Log.d("SearchableActivity", "Net Worth net worth: " + netWorth);

                            TVNetWorth.setText("Net Worth: " + netWorth);

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

    private void setUpPortModels() {

        String url = "https://kas25-25.wl.r.appspot.com/api/todoapp/getportdata";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String id = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                String ticker = jsonObject.getString("ticker");
                                String quantity = jsonObject.getString("quantityData");
                                String totalCost = jsonObject.getString("totalCostData");

                                // Parse other fields as needed
                                Log.d("tag3", "id: " + id + ", name: " + name + ", ticker: " + ticker+", quantity: " + quantity);

                                portfolioItemModels.add(new PortfolioItemModel(ticker, quantity, totalCost, totalCost, totalCost));
                            }
                            updatePRecyclerView();
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

    private void updatePRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.pRecyclerView);
        Port_RecyclerViewAdapter adapter = new Port_RecyclerViewAdapter(this, portfolioItemModels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}