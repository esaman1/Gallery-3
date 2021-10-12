package com.gallery.photosgallery.videogallery.bestgallery.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.divyanshu.colorseekbar.ColorSeekBar;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FontStyleAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FrameAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Class.CallStickerView;
import com.gallery.photosgallery.videogallery.bestgallery.Class.DrawingView;
import com.gallery.photosgallery.videogallery.bestgallery.Class.SquaredFrameLayout;
import com.gallery.photosgallery.videogallery.bestgallery.Fragment.FilterListFragment;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FontStyleInterface;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FrameInterface;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Class.CustomViewPager;
import com.gallery.photosgallery.videogallery.bestgallery.Class.RotateTransformation;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class EditActivity extends AppCompatActivity implements View.OnClickListener,FilterListFragment.FiltersListFragmentListener{

    LinearLayout mFilter, mCrop, mText, mFrame,mDoodle;
    RelativeLayout mEditLayer;
    ImageView mClose, mDone,mYes,mNo;
    RelativeLayout mFilterRL,mStyleRL,mFrameRL;
    LinearLayout mTextRL, mCropRL,mDoodleRL;
    LinearLayout mBottomLayer;
    ImageView mEditCrop, mEditRotate, mEditVertical, mEditHorizontal;
    LinearLayout mAddTextLL, mTextStyleLL, mTextColorLL;
    LinearLayout mFree,mLine,mSquare,mCircle;
    CardView mCancel, mOk;
    TextView mDialogTitle,mText1;
    CustomViewPager mFilterPager;
    FilterListFragment filtersListFragment;
    GestureImageView mImage;
    Bitmap mFilterBitmap;
    EditText mAddedText;
    String newText="";
    CallStickerView callStickerView;
    protected StickerView stickerView;
    SquaredFrameLayout mMainFrame;
    String mStyleList[];
    int[] mFrameList;
    String[] mFrameNameList;
    int[] colors;
    RecyclerView mStyleRecycler,mFrameRecycler;
    ColorSeekBar mColorbar,mColorbarDoodle;
    FontStyleAdapter mFontAdapter;
    FrameAdapter mFrameAdapter;
    FontStyleInterface fontStyleInterface;
    FrameInterface frameInterface;
    Bitmap mBackupBitmap;
    LinearLayout mDrawingPad;
    DrawingView mDrawingView;
    Boolean IsDoodle=false;
    int shapeCount = 0;
    RelativeLayout mHolderRL;
    com.isseiaoki.simplecropview.CropImageView mCropImage;
    Boolean IsFrame=false;


    static
    {
        System.loadLibrary("NativeImageProcessor");
    }

    private FirebaseAnalytics mFirebaseAnalytics;
    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(EditActivity.this);
        InrializeFontArrayList("textfonts");
        InrializeFrameArrayList();
        IntializeColorArrayList();

        fontStyleInterface = new FontStyleInterface() {
            @Override
            public void Font(Typeface typeface) {
                fireAnalytics("Edit Image", "Font");
                Sticker sticker = callStickerView.GetStickerView();
                if (sticker != null) {
                    ((TextSticker) sticker).setTypeface(typeface);
                    callStickerView.UpdateStickerDetail(sticker);
                } else {
                    Toasty.info(getApplicationContext(),"Click on text to apply font", Toast.LENGTH_SHORT).show();
                }
            }
        };

        frameInterface = new FrameInterface() {
            @Override
            public void Frame(int p) {
                fireAnalytics("Edit Image", "Frame");
                switch (p){
                    case 0 : { mCropImage.setCropMode(com.isseiaoki.simplecropview.CropImageView.CropMode.FREE); } break;
                    case 1 : { mCropImage.setCustomRatio(1,1); } break;
                    case 2 : { mCropImage.setCustomRatio(4,5); } break;
                    case 3 : { mCropImage.setCustomRatio(9,16); } break;
                    case 4: { mCropImage.setCustomRatio(5,4); } break;
                    case 5 : { mCropImage.setCustomRatio(3,4); } break;
                    case 6 : { mCropImage.setCustomRatio(4,3); } break;
                    case 7 :
                    case 16 : { mCropImage.setCustomRatio(2,3); } break;
                    case 8 : { mCropImage.setCustomRatio(3,2); } break;
                    case 9 : { mCropImage.setCustomRatio(9,16); } break;
                    case 10 : { mCropImage.setCustomRatio(16,9); } break;
                    case 11 : { mCropImage.setCustomRatio(1,2); } break;
                    case 12 : { mCropImage.setCustomRatio(1,2); } break;
                    case 13 : { mCropImage.setCustomRatio(1,2); } break;
                    case 14 : { mCropImage.setCustomRatio(2,3); } break;
                    case 15 : { mCropImage.setCustomRatio(41,18); } break;
                    case 17 : { mCropImage.setCustomRatio(19,9); } break;
                    case 18 : { mCropImage.setCustomRatio(16,9); } break;
                    case 19 : { mCropImage.setCustomRatio(3,1); } break;
                }

            }
        };

        mFontAdapter = new FontStyleAdapter(EditActivity.this, mStyleList, fontStyleInterface);
        mFrameAdapter = new FrameAdapter(EditActivity.this, mFrameList,mFrameNameList, frameInterface);

        mColorbarDoodle.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                fireAnalytics("Edit Image", "Doodle color");
                Utils.mDoodleColor=i;

            }
        });

    }

    public Bitmap BitmapFromPath(String path){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(path,bmOptions);
    }

    public void init(){
        mImage=findViewById(R.id.mImage);
        mCropImage=findViewById(R.id.cropImageView);

        mMainFrame=findViewById(R.id.mMainframe);

        mEditLayer = findViewById(R.id.mEditLayer);
        mStyleRL = findViewById(R.id.mStyleRL);
        mFrameRL = findViewById(R.id.mFrameRL);

        mBottomLayer = findViewById(R.id.mBottomLayer);
        mFilterRL = findViewById(R.id.mFilterRL);
        mCropRL = findViewById(R.id.mCropRL);
        mTextRL = findViewById(R.id.mTextRL);
        mDoodleRL = findViewById(R.id.mDoodleRL);

        mClose = findViewById(R.id.mClose);
        mDone = findViewById(R.id.mDone);
        mClose.setOnClickListener(this);
        mDone.setOnClickListener(this);
        mYes = findViewById(R.id.mYes);
        mNo = findViewById(R.id.mNo);
        mYes.setOnClickListener(this);
        mNo.setOnClickListener(this);

        mFilter = findViewById(R.id.mFilter);
        mCrop = findViewById(R.id.mCrop);
        mText = findViewById(R.id.mText);
        mFrame = findViewById(R.id.mFrame);
        mDoodle = findViewById(R.id.mDoodle);


        mFilter.setOnClickListener(this);
        mCrop.setOnClickListener(this);
        mText.setOnClickListener(this);
        mFrame.setOnClickListener(this);
        mDoodle.setOnClickListener(this);

        mEditCrop = findViewById(R.id.mEditCrop);
        mEditCrop.setOnClickListener(this);
        mEditRotate = findViewById(R.id.mEditRotate);
        mEditRotate.setOnClickListener(this);
        mEditVertical = findViewById(R.id.mEditVertical);
        mEditVertical.setOnClickListener(this);
        mEditHorizontal = findViewById(R.id.mEditHorizontal);
        mEditHorizontal.setOnClickListener(this);

        mAddTextLL = findViewById(R.id.mAddText);
        mAddTextLL.setOnClickListener(this);
        mTextStyleLL = findViewById(R.id.mTextStyle);
        mTextStyleLL.setOnClickListener(this);
        mTextColorLL = findViewById(R.id.mTextColor);
        mTextColorLL.setOnClickListener(this);

        mFilterPager=findViewById(R.id.mFilterPager);

        stickerView = (StickerView) findViewById(R.id.sticker_view);
        stickerView.setOnClickListener(this);

        callStickerView = new CallStickerView(this, stickerView);
        callStickerView.IntializeStickerView();
        callStickerView.IntializeStickerEvent();
        callStickerView.ShowBorder();

        mStyleRecycler=findViewById(R.id.mStyleRec);
        mFrameRecycler=findViewById(R.id.mFrameRec);
        mColorbar=findViewById(R.id.color_bar);
        mStyleRL.setVisibility(View.GONE);
        mFrameRL.setVisibility(View.GONE);
        mColorbar.setVisibility(View.GONE);
        mStyleRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mFrameRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mText1=findViewById(R.id.text1);

        mFree=findViewById(R.id.mFree);
        mLine=findViewById(R.id.mLine);
        mSquare=findViewById(R.id.mSquare);
        mCircle=findViewById(R.id.mCircle);
        mFree.setOnClickListener(this);
        mLine.setOnClickListener(this);
        mSquare.setOnClickListener(this);
        mCircle.setOnClickListener(this);

        mDrawingPad=(LinearLayout)findViewById(R.id.view_drawing_pad);

        mColorbarDoodle=findViewById(R.id.color_bar_doodle);
        mHolderRL=findViewById(R.id.mHolder);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Utils.IsFramed){
            Uri uri=Utils.mEditedURI;
            try {
                Utils.mEditedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mFilterBitmap=Utils.mEditedBitmap;
                Glide.with(EditActivity.this)
                        .load(Utils.mEditedBitmap)
                        .into(mImage);
                Utils.IsFramed=false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            if(!Utils.IsCropped) {
                String mEditFile = Utils.mEditpath;
                Utils.mOriginalFile = new File(mEditFile);
                Utils.mOriginalBitmap = BitmapFromPath(Utils.mOriginalFile.getAbsolutePath());

                Glide.with(EditActivity.this)
                        .load(Utils.mOriginalFile.getPath())
                        .into(mImage);

                Utils.mEditedBitmap = Utils.mOriginalBitmap;
                mFilterBitmap = Utils.mOriginalBitmap;
            }
            if(!Utils.IsFramed) {
                Glide.with(EditActivity.this)
                        .load(Utils.mEditedBitmap)
                        .into(mImage);
                mFilterBitmap = Utils.mEditedBitmap;
            }
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.back: {
                onBackPressed();
            }
            break;
            case R.id.mFilter: {
                fireAnalytics("Edit Image", "set Filter");
                mBackupBitmap=Utils.mEditedBitmap;
                mEditLayer.setVisibility(View.VISIBLE);
                mText1.setText("Filter");
                mFilterRL.setVisibility(View.VISIBLE);
                mCropRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.GONE);
                mBottomLayer.setVisibility(View.GONE);
                mDoodleRL.setVisibility(View.GONE);
                callStickerView.HideBorder();
                mClose.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                setupViewPager();
            }
            break;
            case R.id.mCrop: {
                fireAnalytics("Edit Image", "set crop");
                mBackupBitmap=Utils.mEditedBitmap;
                mEditLayer.setVisibility(View.VISIBLE);
                mText1.setText("Crop");
                mCropRL.setVisibility(View.VISIBLE);
                mFilterRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.GONE);
                mBottomLayer.setVisibility(View.GONE);
                mDoodleRL.setVisibility(View.GONE);
                mClose.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                callStickerView.HideBorder();

            }
            break;
            case R.id.mDoodle: {
                fireAnalytics("Edit Image", "set doodle");
                mBackupBitmap=Utils.mEditedBitmap;
                mEditLayer.setVisibility(View.VISIBLE);
                mDoodleRL.setVisibility(View.VISIBLE);
                mText1.setText("Doodle");
                mCropRL.setVisibility(View.GONE);
                mFilterRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.GONE);
                mBottomLayer.setVisibility(View.GONE);
                mClose.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                shapeCount=0;
            }
            break;
            case R.id.mText: {
                fireAnalytics("Edit Image", "set text");
                mBackupBitmap=Utils.mEditedBitmap;
                mEditLayer.setVisibility(View.VISIBLE);
                mText1.setText("Text");
                mCropRL.setVisibility(View.GONE);
                mFilterRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.VISIBLE);
                mBottomLayer.setVisibility(View.GONE);
                mDoodleRL.setVisibility(View.GONE);
                mClose.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                AddText();
            }
            break;
            case R.id.mFrame: {
                fireAnalytics("Edit Image", "set frame");
                mCropRL.setVisibility(View.GONE);
                mFilterRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.GONE);
                mBottomLayer.setVisibility(View.GONE);
                mDoodleRL.setVisibility(View.GONE);
                mFrameRL.setVisibility(View.VISIBLE);
                mClose.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);
                mYes.setVisibility(View.VISIBLE);
                mNo.setVisibility(View.VISIBLE);
                callStickerView.HideBorder();
                mText1.setText("Frames");
                mFrameRecycler.setVisibility(View.VISIBLE);
                mFrameRecycler.setAdapter(mFrameAdapter);
                mFrameRecycler.setItemAnimator(new DefaultItemAnimator());
                mMainFrame.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(mMainFrame.getDrawingCache());
                mMainFrame.setDrawingCacheEnabled(false);
                IsFrame=true;
                mBackupBitmap=bitmap;
                mMainFrame.setVisibility(View.GONE);
                mCropImage.setVisibility(View.VISIBLE);
                mCropImage.setImageBitmap(bitmap);

            }
            break;
            case R.id.mClose: {
                mText1.setText("");
                if(mEditLayer.getVisibility()==View.VISIBLE){
                    showSaveDialog();
                }else {
                    OriginalView();
                }
            }
            break;
            case R.id.mDone: {
                fireAnalytics("Edit Image", "Done");
                mText1.setText("");
                OriginalView();
                new AsynchSaveImage().execute((Void[]) null);
            }
            break;
            case R.id.mEditCrop: {
                fireAnalytics("Edit Image", "set crop");
                Utils.IsCropped=true;
                callStickerView.HideBorder();
                startCropImageActivity(getImageUri(EditActivity.this,Utils.mEditedBitmap));
            }
            break;
            case R.id.mEditRotate: {
                fireAnalytics("Edit Image", "Rotate");
                callStickerView.HideBorder();
                new AsynchRotateImage().execute((Void[]) null);
            }
            break;
            case R.id.mEditVertical: {
                fireAnalytics("Edit Image", "Rotate vertical");
                callStickerView.HideBorder();
                Utils.mEditedBitmap=flipImage(Utils.mEditedBitmap,3);
                mFilterBitmap=Utils.mEditedBitmap;
                mImage.setImageBitmap(Utils.mEditedBitmap);
            }
            break;
            case R.id.mEditHorizontal: {
                fireAnalytics("Edit Image", "Rotate horizontal");
                callStickerView.HideBorder();
                Utils.mEditedBitmap=flipImage(Utils.mEditedBitmap,4);
                mFilterBitmap=Utils.mEditedBitmap;
                mImage.setImageBitmap(Utils.mEditedBitmap);
            }
            break;
            case R.id.mAddText:{
                fireAnalytics("Edit Image", "Add text");
                mStyleRL.setVisibility(View.GONE);
                AddText();
            }
            break;
            case R.id.mTextStyle:{
                fireAnalytics("Edit Image", "Text style");
                mTextRL.setVisibility(View.GONE);
                mStyleRL.setVisibility(View.VISIBLE);
                mStyleRecycler.setVisibility(View.VISIBLE);
                mColorbar.setVisibility(View.GONE);
                mStyleRecycler.setAdapter(mFontAdapter);
                mStyleRecycler.setItemAnimator(new DefaultItemAnimator());
            }
            break;
            case R.id.mTextColor:{
                fireAnalytics("Edit Image", "Text color");
                mTextRL.setVisibility(View.GONE);
                mStyleRL.setVisibility(View.VISIBLE);
                mStyleRecycler.setVisibility(View.GONE);
                mFrameRecycler.setVisibility(View.GONE);
                mColorbar.setVisibility(View.VISIBLE);
                mColorbar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
                    @Override
                    public void onColorChangeListener(int i) {
                        Sticker sticker = callStickerView.GetStickerView();
                        if (sticker != null) {
                            ((TextSticker) sticker).setTextColor(i);
                            callStickerView.UpdateStickerDetail(sticker);
                        } else {
                            Toasty.info(getApplicationContext(), "Click on text to fill color", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            break;
            case R.id.sticker_view:{
                fireAnalytics("Edit Image", "Add sticker");
                mBottomLayer.setVisibility(View.GONE);
                mEditLayer.setVisibility(View.VISIBLE);
                mTextRL.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.mYes:{

                DisableDrawingPad();
                mClose.setVisibility(View.VISIBLE);
                mDone.setVisibility(View.VISIBLE);
                mYes.setVisibility(View.GONE);
                mNo.setVisibility(View.GONE);
                mText1.setText("");
                mCropRL.setVisibility(View.GONE);
                mFilterRL.setVisibility(View.GONE);
                mTextRL.setVisibility(View.GONE);
                mStyleRL.setVisibility(View.GONE);
                mFrameRL.setVisibility(View.GONE);
                mBottomLayer.setVisibility(View.VISIBLE);
                mDoodleRL.setVisibility(View.GONE);
                mMainFrame.setVisibility(View.VISIBLE);
                mCropImage.setVisibility(View.GONE);
                if(IsFrame) {
                    Bitmap bitmap = mCropImage.getCroppedBitmap();
                    mImage.setImageBitmap(bitmap);
                    Utils.mEditedBitmap=bitmap;
                    mFilterBitmap=bitmap;
                    IsFrame = false;
                }
            }
            break;
            case R.id.mNo:{
                IsDoodle=false;
                if(IsFrame) {
                    mMainFrame.setVisibility(View.VISIBLE);
                    mCropImage.setVisibility(View.GONE);
                    IsFrame = false;
                }

                mDrawingPad.removeView(mDrawingView);
                Utils.mEditedBitmap=mBackupBitmap;
                mFilterBitmap=mBackupBitmap;
                mImage.setImageBitmap(Utils.mEditedBitmap);
                if(mBottomLayer.getVisibility()==View.GONE) {
                    mClose.setVisibility(View.VISIBLE);
                    mDone.setVisibility(View.VISIBLE);
                    mYes.setVisibility(View.GONE);
                    mNo.setVisibility(View.GONE);
                    mText1.setText("");
                    mCropRL.setVisibility(View.GONE);
                    mFilterRL.setVisibility(View.GONE);
                    mTextRL.setVisibility(View.GONE);
                    mStyleRL.setVisibility(View.GONE);
                    mFrameRL.setVisibility(View.GONE);
                    mBottomLayer.setVisibility(View.VISIBLE);
                    mDoodleRL.setVisibility(View.GONE);
                }
            }
            break;
            case R.id.mFree:{
                fireAnalytics("Edit Image", "Doodle free");
                IsDoodle=true;
                mFree.setEnabled(false);
                mDrawingView=new DrawingView(this);
                mDrawingPad.addView(mDrawingView);
            }
            break;
            case R.id.mLine:{
                fireAnalytics("Edit Image", "Doodle line");
                DisableDrawingPad();
                SetView(0);
            }
            break;
            case R.id.mSquare:{
                fireAnalytics("Edit Image", "Doodle square");
                DisableDrawingPad();
                SetView(1);
            }
            break;
            case R.id.mCircle:{
                fireAnalytics("Edit Image", "Doodle circle");
                DisableDrawingPad();
                SetView(2);
            }
            break;
        }
    }

    private void showSaveDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_save_image));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                mText1.setText("");
                OriginalView();
                new AsynchSaveImage().execute((Void[]) null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Discard",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                onBackPressed();
            }
        });
        builder.create().show();
    }

    public void DisableDrawingPad(){
        if(IsDoodle){
            mFree.setEnabled(true);
            callStickerView.HideBorder();
            mMainFrame.setDrawingCacheEnabled(true);
            Utils.mEditedBitmap = Bitmap.createBitmap(mMainFrame.getDrawingCache());
            mMainFrame.setDrawingCacheEnabled(false);
            mFilterBitmap=Utils.mEditedBitmap;
            mImage.setImageBitmap(Utils.mEditedBitmap);
            IsDoodle=false;
            mDrawingPad.removeView(mDrawingView);
            callStickerView.ShowBorder();
        }
    }

    public void SetView(int shape){

        shapeCount++;
        Drawable drawable=getResources().getDrawable(R.drawable.ic_line);;
        switch (shape){
            case 0:{                drawable =getResources().getDrawable(R.drawable.ic_line);             }            break;
            case 1:{                drawable =getResources().getDrawable(R.drawable.ic_square);             }            break;
            case 2:{                drawable =getResources().getDrawable(R.drawable.ic_circle);             }            break;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, Utils.mDoodleColor);
        callStickerView.AdImageSticker( drawable);
        callStickerView.ShowBorder();
    }


    private void InrializeFrameArrayList() {

        mFrameList = new int[]{R.drawable.f_free,R.drawable.f_i_1_1,R.drawable.f_i_4_5,
                                R.drawable.f_i_story,R.drawable.f_5_4,R.drawable.f_3_4,
                                R.drawable.f_4_3,R.drawable.f_2_3,R.drawable.f_3_2,
                                R.drawable.f_9_16,R.drawable.f_16_9,R.drawable.f_1_2,
                                R.drawable.f_a4,R.drawable.f_a5,
                                R.drawable.f_fb_post,R.drawable.f_fb_cover,
                                R.drawable.f_p_post,R.drawable.f_y_cover,
                                R.drawable.f_t_post,R.drawable.f_t_header};

        mFrameNameList=new String[]{"Free","Ins 1:1","Ins 4:5",
                                    "Ins Story","5:4","3:4",
                                    "4:3","2:3","3:2",
                                    "9:16","16:9","1:2",
                                    "A4","A5",
                                    "Post","Cover",
                                    "Post","Cover",
                                    "Post","Header"};

    }

    private void InrializeFontArrayList(String dirFrom) {
        Resources res = getResources(); //if you are in an activity
        AssetManager am = res.getAssets();
        mStyleList = new String[0];
        try {
            mStyleList = am.list(dirFrom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void IntializeColorArrayList() {
        TypedArray ta = getResources().obtainTypedArray(R.array.rainbow);
        colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
    }

    public Bitmap flipImage(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        // if vertical
        if(type == 3) {

            matrix.preScale(1.0f,-1.0f);
        }
        // if horizonal
        else if(type == 4) {
            // x = x * -1
                matrix.preScale(-1.0f, 1.0f);
            // unknown type
        } else {
            return null;
        }

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    int CurrentAngle_Postition = -1;
    Integer Angle[] = new Integer[]{90, 180, -90, 0};
    public void rotate() {
        try {

                CurrentAngle_Postition--;
                if (CurrentAngle_Postition == -2) {
                    CurrentAngle_Postition = 2;
                }
                if (CurrentAngle_Postition < 0) {
                    CurrentAngle_Postition = 3;
                }


            Glide.with(EditActivity.this)
                    .asBitmap()
                    .load(Utils.mEditedBitmap)
                    .transform(new RotateTransformation(EditActivity.this, Angle[CurrentAngle_Postition]))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,@Nullable Transition<? super Bitmap> transition) {
                            mImage.setImageBitmap(resource);
                            Utils.mEditedBitmap=resource;
                            mFilterBitmap=Utils.mEditedBitmap;
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        } catch (Exception e) {
//            Log.e("Error:",e.getMessage());
        }
    }

    public class AsynchRotateImage extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = new ProgressDialog(EditActivity.this);
                progressDialog.setMessage("Wait..");
                progressDialog.show();
            } catch (Exception e) {
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            rotate();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (progressDialog != null) {

                    if (!EditActivity.this.isFinishing() && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }catch (Exception e){}
        }
    }

    public Uri getImageUri(Context inContext,Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void startCropImageActivity(Uri imageUri) {
//        Log.e("uri",imageUri.toString());
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setAllowRotation(false)
                .start(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // handle result of CropImageActivity
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Bitmap bitmap = null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                    mImage.setImageBitmap(bitmap);
                    Utils.mEditedBitmap=bitmap;
                    mFilterBitmap=bitmap;

                }catch(IOException e){
                   Toasty.error(EditActivity.this,"Problem in cropping image!!!",Toast.LENGTH_SHORT).show();
                }

            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toasty.error(this,"Cropping failed: " + result.getError(),Toast.LENGTH_LONG).show();
            }
        }
    }

    public void OriginalView(){
        mDrawingPad.removeView(mDrawingView);
        mEditLayer.setVisibility(View.VISIBLE);
        mBottomLayer.setVisibility(View.VISIBLE);
        mFilterRL.setVisibility(View.GONE);
        mCropRL.setVisibility(View.GONE);
        mTextRL.setVisibility(View.GONE);
        mStyleRL.setVisibility(View.GONE);
        mFrameRL.setVisibility(View.GONE);
        mDoodleRL.setVisibility(View.GONE);
        mClose.setVisibility(View.VISIBLE);
        mDone.setVisibility(View.VISIBLE);
        mYes.setVisibility(View.GONE);
        mNo.setVisibility(View.GONE);
    }

    public class AsynchSaveImage extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                callStickerView.HideBorder();
                progressDialog = new ProgressDialog(EditActivity.this);
                progressDialog.setMessage("Wait..");
                progressDialog.show();
            } catch (Exception e) {
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mMainFrame.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mMainFrame.getDrawingCache());
            mMainFrame.setDrawingCacheEnabled(false);
//            Uri uri=getImageUri(EditActivity.this,bitmap);
            Utils.CaptureImage(bitmap,EditActivity.this);
            Utils.IsUpdate=true;
            fireAnalytics("Edit Image", "save image");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (progressDialog != null) {

                    if (!EditActivity.this.isFinishing() && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        onBackPressed();
                    }
                }

            }catch (Exception e){}
        }
    }


    private void setupViewPager() {
        mFilterPager.setVisibility(View.VISIBLE);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filtersListFragment = new FilterListFragment();
        filtersListFragment.setListener(this);

        adapter.addFragment(filtersListFragment, "Filters");

        mFilterPager.setAdapter(adapter);
    }



    @Override
    public void onBackPressed(){
        if(mStyleRL.getVisibility()==View.VISIBLE){
            mStyleRL.setVisibility(View.GONE);
            mTextRL.setVisibility(View.VISIBLE);
        }else  if(mFilterRL.getVisibility()==View.VISIBLE || mCropRL.getVisibility()==View.VISIBLE
                || mTextRL.getVisibility()==View.VISIBLE || mStyleRL.getVisibility()==View.VISIBLE
                || mDoodleRL.getVisibility()==View.VISIBLE || mFrameRL.getVisibility()==View.VISIBLE){
//            Log.e("back on","OriginalView");
            mText1.setText("");
            OriginalView();
        }else{
//            Log.e("back on","backpress super");
            mText1.setText("");
            Utils.IsCropped=false;
            super.onBackPressed();
        }
    }

    public void AddText(){


        AlertDialog alertadd = new AlertDialog.Builder(this).create();
        LayoutInflater factory = LayoutInflater.from(EditActivity.this);
        final View view = factory.inflate(R.layout.text_dialog,null);
        alertadd.setView(view);
        alertadd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCancel = view.findViewById(R.id.mCancel);
        mOk = view.findViewById(R.id.mDone);
        mAddedText = view.findViewById(R.id.mEditText);
        mDialogTitle = view.findViewById(R.id.t1);
        mDialogTitle.setText("Add text");
        mCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                alertadd.dismiss();
            }
        });

        mOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                newText=mAddedText.getText().toString();
                if(!newText.equals("")) {
                    callStickerView.AdTextViewSticker(newText, null);
                    callStickerView.ShowBorder();
                }
                alertadd.dismiss();
            }
        });
        alertadd.show();
    }

    @Override
    public void onFilterSelected(Filter filter){
       Bitmap bitmap=mFilterBitmap;
        final int maxSize = 960;
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight =bitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap,outWidth,outHeight,true);

        Bitmap bitmap1 = filter.processFilter(bitmap);
        mImage.setImageBitmap(bitmap1);
        Utils.mEditedBitmap=bitmap1;
        fireAnalytics("Edit Image", "Filter selection");
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }
    }
}