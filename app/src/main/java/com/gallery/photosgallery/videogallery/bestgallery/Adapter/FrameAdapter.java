package com.gallery.photosgallery.videogallery.bestgallery.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gallery.photosgallery.videogallery.bestgallery.Interface.FontStyleInterface;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FrameInterface;
import com.gallery.photosgallery.videogallery.bestgallery.R;

public class FrameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Activity context;
    int[] data;
    String[] dataname;
    FrameInterface fontStyleInterface;
    float x,y;

    public FrameAdapter(Activity context, int[] data,String[] name, FrameInterface fontStyleInterface) {
        this.context = context;
        this.data=data;
        this.dataname=name;
        this.fontStyleInterface=fontStyleInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_view, null);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        ViewHolder viewHolder= (ViewHolder) holder;
        viewHolder.button.setImageResource(data[position]);
        viewHolder.mName.setText(dataname[position]);
        viewHolder.mFrameRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fontStyleInterface.Frame(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.length;
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView button;
        TextView mName;
        RelativeLayout mFrameRl;
        public ViewHolder(View itemView) {
            super(itemView);
            button=itemView.findViewById(R.id.mFrame);
            mName=itemView.findViewById(R.id.mName);
            mFrameRl=itemView.findViewById(R.id.mFrameRl);

        }
    }
}

