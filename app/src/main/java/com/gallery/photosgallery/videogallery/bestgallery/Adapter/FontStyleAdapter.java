package com.gallery.photosgallery.videogallery.bestgallery.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.gallery.photosgallery.videogallery.bestgallery.Interface.FontStyleInterface;
import com.gallery.photosgallery.videogallery.bestgallery.R;

public class FontStyleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Activity context;
    String[] data;
    Typeface myTypeface;
    FontStyleInterface fontStyleInterface;


    public FontStyleAdapter(Activity context, String[] data, FontStyleInterface fontStyleInterface) {
        this.context = context;
        this.data=data;
        this.fontStyleInterface=fontStyleInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.style_view, null);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        ViewHolder viewHolder= (ViewHolder) holder;
        myTypeface = Typeface.createFromAsset(context.getAssets(), "textfonts/" + data[position]);
        viewHolder.button.setTypeface(myTypeface);
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myTypeface = Typeface.createFromAsset(context.getAssets(), "textfonts/" + data[position]);
                fontStyleInterface.Font(myTypeface);
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.length;
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        Button button;
        public ViewHolder(View itemView) {
            super(itemView);
            button=(Button)itemView.findViewById(R.id.btn_style);

        }
    }
}

