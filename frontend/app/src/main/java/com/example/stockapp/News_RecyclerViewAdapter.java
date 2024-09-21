package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class News_RecyclerViewAdapter extends RecyclerView.Adapter<News_RecyclerViewAdapter.MyViewHolder> {

    private static NewsRecyclerViewInterface newsRecyclerViewInterface = null;

    Context context;
    ArrayList<NewsItem> newsItems;
    public News_RecyclerViewAdapter(Context context, ArrayList<NewsItem> newsItems){

        this.context = context;
        this.newsItems = newsItems;

    }

    @NonNull
    @Override
    public News_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_row, parent, false);
        return new News_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull News_RecyclerViewAdapter.MyViewHolder holder, int position) {

        holder.tvHeadline.setText(newsItems.get(position).getHeadline());
//        holder.tvSummary.setText(newsItems.get(position).getSummary());
        holder.tvSource.setText(newsItems.get(position).getSource());
        holder.tvDatetime.setText(newsItems.get(position).getFormattedDate());

        int squareSize = 50;
        //using picasso to load and crop the image
        Picasso.get().load(newsItems.get(position).getImage())
                .fit()
                .centerCrop()
                .into(holder.tvImage);
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvHeadline, tvSummary, tvSource, tvDatetime;

        ImageView tvImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeadline = itemView.findViewById(R.id.news_title);
//            tvSummary = itemView.findViewById(R.id.news);
            tvDatetime = itemView.findViewById(R.id.news_datetime);
            tvSource = itemView.findViewById(R.id.news_source);
            tvImage = itemView.findViewById(R.id.news_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newsRecyclerViewInterface != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION){
                            newsRecyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}