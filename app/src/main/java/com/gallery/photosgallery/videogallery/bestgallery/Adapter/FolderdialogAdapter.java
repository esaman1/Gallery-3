package com.gallery.photosgallery.videogallery.bestgallery.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gallery.photosgallery.videogallery.bestgallery.Activity.MainActivity;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FolderInterface;
import com.gallery.photosgallery.videogallery.bestgallery.Model.BaseModel;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Service.GetFileList;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;

import java.io.File;
import java.util.ArrayList;


public class FolderdialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    ArrayList<BaseModel> objects = new ArrayList<>();
    Activity activity;
    public static String action;
    FolderInterface folderInterface;
    CardView mCancel, mOk;
    TextView mDialogTitle;
    EditText mAddedText;
    String newAlbum;

    public FolderdialogAdapter(Activity activity,ArrayList<BaseModel> objects ,FolderInterface folderInterface){
        this.activity = activity;
        this.objects=objects;
        this.folderInterface=folderInterface;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case 0: {                
                itemView = LayoutInflater.from(activity).inflate(R.layout.folder_list_view,parent,false);
                viewHolder = new MyClassView1(itemView);                
            }
            break;
            case 1:{               
                itemView = LayoutInflater.from(activity).inflate(R.layout.create_album_list_view,parent,false);
                viewHolder = new FolderdialogAdapter.ViewHolderAlbum1(itemView);
            }
            break;
        }

        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holders, int position){

        switch (holders.getItemViewType()) {
            case 0: {
                FolderdialogAdapter.MyClassView1 holder = (FolderdialogAdapter.MyClassView1)holders;
                BaseModel item = objects.get(position);
                holder.mImage.setClipToOutline(true);

                if(item.getPathlist().size() > 0){
                    Uri uri = Uri.fromFile(new File(item.getPathlist().get(0)));
                    Glide.with(activity)
                            .load(uri)
                            .centerCrop()
                            .into(holder.mImage);
                }
                holder.mFavourite.setVisibility(View.GONE);
                holder.mAlbumName.setText(item.getBucketName());
                if(Utils.VIEW_TYPE.equals("Photos")) {
                    if(item.getPathlist().size()==1) {
                        holder.mCount.setText(item.getPathlist().size() + " Photo");
                    }else{
                        holder.mCount.setText(item.getPathlist().size() + " Photos");
                    }
                }else{
                    if(item.getPathlist().size()==1) {
                        holder.mCount.setText(item.getPathlist().size() + " Video");
                    }else{
                        holder.mCount.setText(item.getPathlist().size() + " Videos");
                    }
                }
                holder.rootRL.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                       folderInterface.OnSelectFolder(action,item.getBucketPath(),position);
                    }
                });
            }
            case 1: {
                try{
                    FolderdialogAdapter.ViewHolderAlbum1 viewHolderAlbum = (FolderdialogAdapter.ViewHolderAlbum1)holders;
                    viewHolderAlbum.rltrootview.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            CreateAlbum(position);
                        }
                    });

                }catch(Exception e){
                }
            }
            break;
        }
    }


    @Override
    public int getItemCount(){
        return objects.size();
    }

    @Override
    public int getItemViewType(int position) {
        int val = 0;
        try {
            if (objects != null && objects.size() > 0) {
                val = objects.get(position).getType();
            } else {
                val = 0;
            }
        } catch (Exception w) {
        }
        return val;
    }


    public class MyClassView1 extends RecyclerView.ViewHolder{

        //        List
        ImageView mImage;
        TextView mAlbumName, mCount;
        RelativeLayout rootRL;
        ImageView mFavourite;

        public MyClassView1(@NonNull View itemView){
            super(itemView);
            mImage = itemView.findViewById(R.id.mImage);
            mAlbumName = itemView.findViewById(R.id.mFolderName);
            mCount = itemView.findViewById(R.id.mCount);
            rootRL = itemView.findViewById(R.id.rootRL);
            mFavourite = itemView.findViewById(R.id.mFavourite);
        }
    }

    public static class ViewHolderAlbum1 extends RecyclerView.ViewHolder {
        public RelativeLayout rltrootview;
        public ImageView mImage;

        public ViewHolderAlbum1(View view) {
            super(view);
            rltrootview = (RelativeLayout) view.findViewById(R.id.rlRootview);
            mImage =  view.findViewById(R.id.mImage);
        }
    }

    public void CreateAlbum(int p){

        AlertDialog alertadd = new AlertDialog.Builder(activity).create();
        LayoutInflater factory = LayoutInflater.from(activity);
        final View view = factory.inflate(R.layout.text_dialog,null);
        alertadd.setView(view);
        alertadd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCancel = view.findViewById(R.id.mCancel);
        mOk = view.findViewById(R.id.mDone);
        mAddedText = view.findViewById(R.id.mEditText);
        mDialogTitle = view.findViewById(R.id.t1);
        mDialogTitle.setText("Create New Album");
        mCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                alertadd.dismiss();
            }
        });

        mOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                newAlbum=mAddedText.getText().toString();
                if(!newAlbum.equals("")) {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + newAlbum;
                    File file=new File(path);
                    if(!file.exists()){
                        file.mkdirs();
                    }

                    folderInterface.OnSelectFolder(action,path,p);
                }
                alertadd.dismiss();
            }
        });
        alertadd.show();
    }

}

