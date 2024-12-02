package com.example.firebaselogin;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleviewAdapter extends RecyclerView.Adapter<RecycleviewAdapter.MyViewHolder> {

    Context context;
    ArrayList<Listing_notification> notificationArrayList;

    public RecycleviewAdapter(Context context, ArrayList<Listing_notification> notificationArrayList) {
        this.context = context;
        this.notificationArrayList = notificationArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View x = LayoutInflater.from(context).inflate(R.layout.item_reservation,parent,false);

       return new MyViewHolder(x);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            Listing_notification listing_notification = notificationArrayList.get(position);
            holder.name.setText(listing_notification.name);
            holder.phoneNumber.setText(listing_notification.phoneNumber);
            holder.title.setText(listing_notification.title);
    }

    @Override
    public int getItemCount() {
        return notificationArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name,phoneNumber,title;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phoneNumber = itemView.findViewById(R.id.email);
            title = itemView.findViewById(R.id.product_name_id);
        }
    }
}
