package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Port_RecyclerViewAdapter extends RecyclerView.Adapter<Port_RecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<PortfolioItemModel> portfolioItemModels;
    public Port_RecyclerViewAdapter(Context context, ArrayList<PortfolioItemModel> portfolioItemModels){

        this.context = context;
        this.portfolioItemModels = portfolioItemModels;
    }

    @NonNull
    @Override
    public Port_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new Port_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Port_RecyclerViewAdapter.MyViewHolder holder, int position) {

        holder.tvShares.setText(portfolioItemModels.get(position).getCompanyShares());
        holder.tvTicker.setText(portfolioItemModels.get(position).getCompanyTicker());
        holder.tvChange.setText(portfolioItemModels.get(position).getCompanyChange());
        holder.tvChangePer.setText(portfolioItemModels.get(position).getCompanyChangePercent());
        holder.tvMV.setText(portfolioItemModels.get(position).getCompanyMarketValue());


    }

    @Override
    public int getItemCount() {
        return portfolioItemModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvShares, tvTicker, tvChange, tvChangePer, tvMV;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicker = itemView.findViewById(R.id.textViewTickerPort);
            tvShares = itemView.findViewById(R.id.textViewShares);
            tvMV = itemView.findViewById(R.id.textViewMarketV);
            tvChange = itemView.findViewById(R.id.textViewChangeP);
            tvChangePer = itemView.findViewById(R.id.textViewChangePerP);
        }
    }
}