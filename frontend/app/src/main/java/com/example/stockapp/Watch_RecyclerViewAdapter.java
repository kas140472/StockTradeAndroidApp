package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Watch_RecyclerViewAdapter extends RecyclerView.Adapter<Watch_RecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<WatchModel> watchModels;
    public Watch_RecyclerViewAdapter(Context context, ArrayList<WatchModel> watchModels){

        this.context = context;
        this.watchModels = watchModels;
    }

    @NonNull
    @Override
    public Watch_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new Watch_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Watch_RecyclerViewAdapter.MyViewHolder holder, int position) {

        holder.tvName.setText(watchModels.get(position).getCompanyName());
        holder.tvTicker.setText(watchModels.get(position).getCompanyTicker());
        holder.tvC.setText(watchModels.get(position).getCompanyC());
        holder.tvD.setText(watchModels.get(position).getCompanyD());
        holder.tvDP.setText(watchModels.get(position).getCompanyDP());


    }

    @Override
    public int getItemCount() {
        return watchModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvTicker, tvC, tvD, tvDP;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.textViewTickerPort);
            tvTicker = itemView.findViewById(R.id.textViewShares);
            tvC = itemView.findViewById(R.id.textViewMarketV);
            tvD = itemView.findViewById(R.id.textViewChangeP);
            tvDP = itemView.findViewById(R.id.textViewChangePerP);
        }
    }
}
