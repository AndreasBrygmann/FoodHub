package com.example.firebaselogin;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder> implements Filterable {

    Context context;
    ArrayList<Listing> list;
    ArrayList<Listing> listFull;
    RecycleViewInterface listener;
    FirebaseStorage firebaseStorage;

    public MyRecycleViewAdapter(Context context, ArrayList<Listing> list, RecycleViewInterface listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.listFull = new ArrayList<>(list); // Initially, listFull is a copy of list
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_products, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Listing currentListing = list.get(position);
        holder.product_name.setText(currentListing.getTitle());
        holder.product_location.setText(currentListing.getCity());

        // Load the animation from your anim resource file
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        // Start the animation on the holder's itemView, which is the root view of each item
        holder.itemView.startAnimation(animation);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(currentListing.getDocumentID());

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.productImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(context).load(R.drawable.no_image).into(holder.productImage);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(list.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Listing> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Listing item : listFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) ||
                            item.getCity().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateListFull(ArrayList<Listing> newListFull) {
        listFull = new ArrayList<>(newListFull);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView product_name, product_location;
        ImageView productImage;
        View itemView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            product_name = itemView.findViewById(R.id.product_name_id);
            product_location = itemView.findViewById(R.id.product_location_id);
            productImage = itemView.findViewById(R.id.imageView_id);
            this.itemView = itemView;
        }
    }
}
