package com.gallery.photosgallery.videogallery.bestgallery.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.gallery.photosgallery.videogallery.bestgallery.Activity.ViewVideoActivity;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.google.android.material.shape.CornerFamily;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    Activity activity;
    ArrayList<String> objects = new ArrayList<>();
    private ArrayList<String> mArrayList = new ArrayList<>();
    ArrayList<String> mFavouriteImageList = new ArrayList<>();
    String json1;
    Boolean exist = false;
    int pos;

    public VideoAdapter(Activity activity){
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView;
        RecyclerView.ViewHolder viewHolder = null;

            itemView = LayoutInflater.from(activity).inflate(R.layout.folder_grid_view,parent,false);
            viewHolder = new MyClassView(itemView);
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params != null) {
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth();
                params.height = width / 2;
            }

        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holders, int position){
        RequestOptions options = new RequestOptions();
        File file = new File(objects.get(position));
        float radius = activity.getResources().getDimension(R.dimen.my_value);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("Favourites_pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        mFavouriteImageList = new ArrayList<>();
        json1 = sharedPreferences.getString("Fav_Image","");
        Type type1 = new TypeToken<ArrayList<String>>(){
        }.getType();
        mFavouriteImageList = gson.fromJson(json1,type1);

        if(mFavouriteImageList == null){
            mFavouriteImageList = new ArrayList<>();
        }

            MyClassView holder = (MyClassView) holders;
            if(mFavouriteImageList.size() > 0){
                for(int i = 0;i < mFavouriteImageList.size();i++){
                    if(mFavouriteImageList.get(i).equals(file.getPath())){
                        holder.mFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_full_heart));
                    }
                }
            }
            holder.mPlay.setVisibility(View.VISIBLE);
            holder.mImage.setClipToOutline(true);
            holder.mAlbumName.setVisibility(View.GONE);
            holder.mCount.setVisibility(View.GONE);

            if (file.exists()) {
                holder.mImage.setClipToOutline(true);
                holder.mImage.setAdjustViewBounds(true);
                Glide.with(activity)
                        .load(file.getPath())
                        .apply(options.centerCrop()
                                .skipMemoryCache(true)
                                .priority(Priority.LOW)
                                .format(DecodeFormat.PREFER_ARGB_8888))
                        .into(holder.mImage);
//                holder.mImage.setShapeAppearanceModel(holder.mImage.getShapeAppearanceModel()
//                        .toBuilder()
//                        .setTopRightCorner(CornerFamily.ROUNDED,radius)
//                        .setTopLeftCorner(CornerFamily.ROUNDED,radius)
//                        .setBottomLeftCorner(CornerFamily.ROUNDED,radius)
//                        .setBottomRightCorner(CornerFamily.ROUNDED,radius)
//                        .build());
            }
            holder.mImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewVideoActivity slideShowActivity = new ViewVideoActivity();
                            slideShowActivity.SetList(objects, position);
                            Intent in = new Intent(activity,ViewVideoActivity.class);
                            in.putExtra("From","Album");
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(in);
                        }
                    });

                }
            });

            holder.mFavourite.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    json1 = sharedPreferences.getString("Fav_Image","");
                    Type type1 = new TypeToken<ArrayList<String>>(){
                    }.getType();
                    mFavouriteImageList = gson.fromJson(json1,type1);

                    if(mFavouriteImageList == null){
                        mFavouriteImageList = new ArrayList<>();
                    }

                    if(mFavouriteImageList.size() > 0){
                        for(int i = 0;i < mFavouriteImageList.size();i++){
                            if(mFavouriteImageList.get(i).equals(file.getPath())){
                                exist = true;
                                pos = i;
                                break;
                            }else{
                                exist = false;
                            }
                        }
                    }
                    if(!exist){
                        mFavouriteImageList.add(file.getPath());
                        holder.mFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_full_heart));
                    }else{
                        mFavouriteImageList.remove(pos);
                        holder.mFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_empty_heart));
                        exist = false;
                    }

                    json1 = gson.toJson(mFavouriteImageList);
                    editor.putString("Fav_Image",json1);
                    editor.apply();
                }
            });

    }

    @Override
    public int getItemCount(){
        return objects.size();
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }


    public class MyClassView extends RecyclerView.ViewHolder{

        ImageView mImage;
        TextView mAlbumName, mCount;
        ImageView mFavourite,mPlay;

        public MyClassView(@NonNull View itemView){
            super(itemView);
            mImage = itemView.findViewById(R.id.mImage);
            mPlay = itemView.findViewById(R.id.mPlay);
            mAlbumName = itemView.findViewById(R.id.albumName);
            mCount = itemView.findViewById(R.id.count);
            mFavourite = itemView.findViewById(R.id.mFavourite);
        }
    }



    public void Addall(ArrayList<String> list) {

        mArrayList = new ArrayList<>();
        objects = list; //--reference
        mArrayList.addAll(list); //--backup for filter
        notifyDataSetChanged();
    }

}

