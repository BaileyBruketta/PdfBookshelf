package com.baileybruketta.pdflibrary;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BookModelAdapter extends RecyclerView.Adapter<BookModelAdapter.ViewHolder> {
    ArrayList<BookModel> bookdata;
    int layourResourceId;
    Context context;
    private static final String TAG = "BookModelAdapter";
    MainActivity callingActivity;

    public BookModelAdapter(Context context, int layoutResourceId, ArrayList<BookModel> data, MainActivity callingAct){
        this.layourResourceId = layoutResourceId;
        this.context = context;
        this.bookdata = data;
        this.callingActivity = callingAct;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_bookshelf, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        Log.d(TAG, "onBindViewHolder: called");

        holder.title.setText(bookdata.get(position).getTitle());
        holder.author.setText(bookdata.get(position).getAuthor());
        holder.image.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               Log.e("bookmodeladapteronclick", bookdata.get(position).getTitle());
               callingActivity.RenderBookPreReadScreen(bookdata.get(position));
           }
        });


    }

    @Override
    public int getItemCount(){
        return bookdata.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView image;
        TextView author;

        public ViewHolder(View itemView){
            super(itemView);

            title = itemView.findViewById(R.id.titletext);
            author = itemView.findViewById(R.id.authortext);
            image = itemView.findViewById(R.id.bookimage);

        }
    }
}


