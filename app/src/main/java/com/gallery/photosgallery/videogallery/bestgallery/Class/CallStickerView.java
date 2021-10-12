package com.gallery.photosgallery.videogallery.bestgallery.Class;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.DeleteIconEvent;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.FlipHorizontallyEvent;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.xiaopo.flying.sticker.ZoomIconEvent;

import java.util.ArrayList;
import java.util.Arrays;

import es.dmoral.toasty.Toasty;

/**
 * Created by Umiya Mataji on 4/18/2017.
 */

public class CallStickerView{

    private static final String TAG ="==>>" ;
    Activity activity;
    StickerView stickerView;
    TextSticker sticker;
    ArrayList<Sticker> arraylkist;
    int counter=0;
    public Sticker ChangesSticker;

    public CallStickerView(Activity activity,StickerView stickerView) {
        this.activity = activity;
        this.stickerView=stickerView;
        arraylkist=new ArrayList<>();
    }

    public void IntializeStickerView(){

        //currently you can config your own icons and icon event
        //the event you can custom
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(activity, com.xiaopo.flying.sticker.R.drawable.sticker_ic_close_white_18dp), BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(activity, com.xiaopo.flying.sticker.R.drawable.sticker_ic_scale_white_18dp), BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(activity, com.xiaopo.flying.sticker.R.drawable.sticker_ic_flip_white_18dp), BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

//        BitmapStickerIcon heartIcon = new BitmapStickerIcon(ContextCompat.getDrawable(activity, R.drawable.ic_favorite_white_24dp), BitmapStickerIcon.LEFT_BOTTOM);
//        heartIcon.setIconEvent(new HelloIconEvent());

      //  stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon, heartIcon));
        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon));

        //default icon layout
        //stickerView.configDefaultIcons();

        stickerView.setBackgroundColor(Color.TRANSPARENT);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);

    }


    public void IntializeStickerEvent(){

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerClicked(Sticker sticker) {
                //stickerView.removeAllSticker();
                if (sticker instanceof TextSticker) {
                   // ((TextSticker) sticker).setTextColor(Color.WHITE);


                    //Toast.makeText(activity,""+((TextSticker) sticker).getId(),Toast.LENGTH_LONG).show();
                    ChangesSticker=sticker;
                    stickerView.replace(sticker);
                    stickerView.invalidate();
                }
//                Log.d(TAG, "onStickerClicked");
            }

            @Override
            public void onStickerDeleted(Sticker sticker) {
//                Log.d(TAG, "onStickerDeleted");
                ChangesSticker=null;
            }

            @Override
            public void onStickerDragFinished(Sticker sticker) {
//                Log.d(TAG, "onStickerDragFinished");
                ChangesSticker=sticker;
            }

            @Override
            public void onStickerZoomFinished(Sticker sticker) {
//                Log.d(TAG, "onStickerZoomFinished");
                ChangesSticker=sticker;
            }

            @Override
            public void onStickerFlipped(Sticker sticker) {
//                Log.d(TAG, "onStickerFlipped");
                ChangesSticker=sticker;

            }

            @Override
            public void onStickerDoubleTapped(Sticker sticker) {
//                Log.d(TAG, "onDoubleTapped: double tap will be with two click");
                ChangesSticker=sticker;
            }
        });
    }


    public void AdTextViewSticker(String text,Drawable drawable){

        sticker = new TextSticker(activity);
        if(drawable!=null) {
            sticker.setDrawable(drawable);
        }

        sticker.setText(text);
        sticker.setTextColor(Color.GRAY);
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        sticker.resizeText();

        sticker.setId(counter);
        stickerView.addSticker(sticker);

         ChangesSticker=sticker; //--store sticke object
        //arraylkist.add(counter,sticker); //---object
        //counter++;
    }

    public void UpdateStickerDetail(Sticker sticker){

        stickerView.replace(sticker);
        stickerView.invalidate();
    }

    public Sticker GetStickerView(){

        return ChangesSticker;
    }



    public void reset() {
        stickerView.removeAllStickers();

    }

    public void testRemove(View view) {
        if (stickerView.removeCurrentSticker()) {
            Toasty.success(activity, "Remove current Sticker successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toasty.error(activity, "Remove current Sticker failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void AdImageSticker(Drawable drawable){

        //Drawable drawable1 = ContextCompat.getDrawable(activity, R.drawable.haizewang_23);
        stickerView.addSticker(new DrawableSticker(drawable));
    }


    public void HideBorder(){
        stickerView.showIcons=false;
        stickerView.showBorder=false;

    }

    public void ShowBorder(){
        stickerView.showIcons=true;
        stickerView.showBorder=true;
    }
}
