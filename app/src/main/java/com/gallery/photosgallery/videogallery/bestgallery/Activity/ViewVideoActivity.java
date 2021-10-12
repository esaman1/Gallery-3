package com.gallery.photosgallery.videogallery.bestgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FolderdialogAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.ShowImageAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.ShowVideoAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Class.ConnectionDetector;
import com.gallery.photosgallery.videogallery.bestgallery.Class.CustomViewPager;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FolderInterface;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Service.GetFileList;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;
import com.gallery.photosgallery.videogallery.bestgallery.animation.AccordionPageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.AlphaPageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.CubePageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.FadePageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.FlipPageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.RotatePageTransformer;
import com.gallery.photosgallery.videogallery.bestgallery.animation.ZoomCenterPageTransformer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

import static com.gallery.photosgallery.videogallery.bestgallery.Util.Utils.DELETE_REQUEST;

public class ViewVideoActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    ImageView mBack, mFavourite, mMore;
    ImageView mDelete, mShare, mInfo;
    public static RelativeLayout mRotateRL, mOptionRL;
    RelativeLayout mTopLayer;
    LinearLayout mBottomLayer;
    CustomViewPager mImagePager;
    TextView mRotateRight, mRightLeft, mRotateBottom;
    TextView mCopy, mMove, mRename;
    public static ArrayList<String> list;
    public static int currenpositionToDisplay = 0;
    ShowVideoAdapter videoAdapter;
    TextView mImageName;
    ArrayList<String> mFavouriteImageList = new ArrayList<>();
    String json1;
    Boolean exist = false;
    int pos;
    FolderdialogAdapter folderAdapter;
    RecyclerView mFolderRec;
    FolderInterface folderInterface;
    MediaScannerConnection msConn;
    Dialog dial, deleteDialog;
    CardView mCancel, mOk;
    TextView mDialogTitle;
    EditText mNewName;
    Timer swipeTimer;
    TextView mDeleteFile;
    CardView mDialogCancel, mDialogDelete;
    String From = "";
    ConnectionDetector cd;
    AdView adView;
    boolean isInternetPresent = false;
    LinearLayout bannerContainer;
    private FirebaseAnalytics mFirebaseAnalytics;
    int createAlbumPosition=0;

    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = AdSize.BANNER;
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(ViewVideoActivity.this);
        initview();
        From = getIntent().getStringExtra("From");
        if (From.equals("Favs")) {
            String From1 = getIntent().getStringExtra("From1");
            if (From1.equals("FavsVideo")) {
                mImagePager.setPagingEnabled(false);
                fireAnalytics("ViewVideoActivity", "From favourite");
                mMore.setVisibility(View.GONE);
            } else {
                mImagePager.setPagingEnabled(true);
                fireAnalytics("ViewVideoActivity", "From folder");
                mMore.setVisibility(View.VISIBLE);
            }
        }

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(ViewVideoActivity.this);
                    fireAnalytics("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad_vid));
                    bannerContainer.addView(adView);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            fireAnalytics("admob_banner", "loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (loadAdError.getMessage() != null)
                                fireAnalytics("admob_banner_Error", loadAdError.getMessage());
                        }
                    });
                    loadBanner();
                }
            }, 2000);
        }
    }

    public void init() {
        bannerContainer = findViewById(R.id.banner_container);

        mImagePager = findViewById(R.id.mImagePager);

        mTopLayer = findViewById(R.id.mTopLayer);
        mTopLayer.setOnTouchListener(this);
        mBottomLayer = findViewById(R.id.mBottomLayer);
        mBottomLayer.setOnTouchListener(this);

        mBack = findViewById(R.id.back);
        mFavourite = findViewById(R.id.mFavourite);
        mMore = findViewById(R.id.mMore);

        mDelete = findViewById(R.id.mDelete);
        mShare = findViewById(R.id.mShare);

        mBack.setOnClickListener(this);

        mFavourite.setOnClickListener(this);
        mMore.setOnClickListener(this);

        mDelete.setOnClickListener(this);
        mShare.setOnClickListener(this);

        mRotateRL = findViewById(R.id.mRotateRL);
        mOptionRL = findViewById(R.id.mOptionRL);

        mRotateRight = findViewById(R.id.mRotateRight);
        mRightLeft = findViewById(R.id.mRotateLeft);
        mRotateBottom = findViewById(R.id.mRotateBottom);
        mRotateRight.setOnClickListener(this);
        mRightLeft.setOnClickListener(this);
        mRotateBottom.setOnClickListener(this);

        mCopy = findViewById(R.id.mCopy);
        mMove = findViewById(R.id.mMove);
        mRename = findViewById(R.id.mRename);

        mInfo = findViewById(R.id.mInfo);
        mCopy.setOnClickListener(this);
        mMove.setOnClickListener(this);
        mRename.setOnClickListener(this);

        mInfo.setOnClickListener(this);

        mImageName = findViewById(R.id.mImageName);
    }

    public void initview() {

        SharedPreferences sharedPreferences = getSharedPreferences("Favourites_pref", MODE_PRIVATE);
        Gson gson = new Gson();

        mFavouriteImageList = new ArrayList<>();
        json1 = sharedPreferences.getString("Fav_Image", "");
        Type type1 = new TypeToken<ArrayList<String>>() {
        }.getType();
        mFavouriteImageList = gson.fromJson(json1, type1);

        if (mFavouriteImageList == null) {
            mFavouriteImageList = new ArrayList<>();
        }


        if (list != null) {
            File file = new File(list.get(currenpositionToDisplay));
            mImageName.setText((currenpositionToDisplay + 1) + " / " + (list.size()));
            if (mFavouriteImageList.size() > 0) {
                if (mFavouriteImageList.contains(file.getPath())) {
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_red_heart));
                } else {
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_black_heart));
                }
            }
        }
        System.gc();
        videoAdapter = new ShowVideoAdapter(this, list, mImagePager);
        mImagePager.setAdapter(videoAdapter);
        mImagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (list != null) {
                    File file = new File(list.get(position));
                    mImageName.setText((position + 1) + " / " + (list.size()));
                    if (mFavouriteImageList.size() > 0) {
                        if (mFavouriteImageList.contains(file.getPath())) {
                            mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_red_heart));
                        } else {
                            mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_black_heart));
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mImageName.setText((currenpositionToDisplay + 1) + " / " + (list.size()));
        mImagePager.setCurrentItem(currenpositionToDisplay);
        folderInterface = new FolderInterface() {
            @Override
            public void OnSelectFolder(String action, String path,int position) {
                createAlbumPosition=position;
                if (action.equals("copy")) {
                    CopyImage(path);
                } else if (action.equals("move")) {
                    MoveImage(path);
                }
                dial.dismiss();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.IsUpdate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (videoAdapter != null) {
                        videoAdapter.notifyDataSetChanged();
                    }
                    registerReceiver(DeleteCompletedBroadcast, new IntentFilter("DeleteCompleted"));
//
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (DeleteCompletedBroadcast != null) {
                unregisterReceiver(DeleteCompletedBroadcast);
            }
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver DeleteCompletedBroadcast = new BroadcastReceiver() {

        public ProgressDialog pr_dialog;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                if (intent != null) {
                    if (intent.getBooleanExtra("started", false)) {
                        try {
                            pr_dialog = ProgressDialog.show(context, "Deleting",
                                    "Deleting files...",
                                    true, false);
                        } catch (Exception e) {
                        }
                    }

                    if (intent.getBooleanExtra("completed", false)) {
                        try {
                            //-----------------data refresh-----
                            if (videoAdapter != null) {
                                videoAdapter.RefreshDeleteData(mImagePager.getCurrentItem());
                            }
                            //----------------------------
                        } catch (Exception e) {
                        }
                        if (pr_dialog != null)
                            pr_dialog.dismiss();
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: {
                onBackPressed();
            }
            break;

            case R.id.mFavourite: {
                SetAsFavourite();
            }
            break;
            case R.id.mMore: {
                mOptionRL.setVisibility(View.VISIBLE);
                mRotateRL.setVisibility(View.GONE);
            }
            break;
            case R.id.mDelete: {

                deleteDialog = new Dialog(ViewVideoActivity.this, android.R.style.Theme_DeviceDefault);
                deleteDialog.requestWindowFeature(1);
                deleteDialog.setContentView(R.layout.delete_dialog);
                mDeleteFile = (TextView) deleteDialog.findViewById(R.id.txt_filename);
                mDialogCancel = (CardView) deleteDialog.findViewById(R.id.mCancel);
                mDialogDelete = (CardView) deleteDialog.findViewById(R.id.mDelete);
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                deleteDialog.setCanceledOnTouchOutside(true);
                mOptionRL.setVisibility(View.GONE);
                Utils.IsUpdate = true;
                int position = mImagePager.getCurrentItem();
                deleteDialog.show();
                File file = new File(list.get(position));
                mDeleteFile.setText(file.getName());
                mDialogDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                        fireAnalytics("ViewVideoActivity", "Video delete");
                        videoAdapter.DeleteAction(position, From);
                    }
                });
                mDialogCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                    }
                });
            }
            break;
            case R.id.mShare: {
                mOptionRL.setVisibility(View.GONE);
                fireAnalytics("ViewVideoActivity", "Share video");
                String path;
                File file;
                Uri uri;

                path = list.get(mImagePager.getCurrentItem());
                file = new File(path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(ViewVideoActivity.this, getPackageName() + ".provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharingIntent.setType("video/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share Via"));
            }
            break;

            case R.id.mCopy: {
                mOptionRL.setVisibility(View.GONE);
                FolderDialog("copy");
            }
            break;
            case R.id.mMove: {
                mOptionRL.setVisibility(View.GONE);
                FolderDialog("move");
            }
            break;
            case R.id.mRename: {
                mOptionRL.setVisibility(View.GONE);
                RenameDialog();
            }
            break;
            case R.id.mInfo: {
                mOptionRL.setVisibility(View.GONE);
                FileInfo(mImagePager.getCurrentItem());
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_REQUEST) {
            if (resultCode == RESULT_OK) {

                int position = mImagePager.getCurrentItem();
                File file = new File(list.get(position));
                Log.e("Position", String.valueOf(position));
                Toasty.success(ViewVideoActivity.this, "Video Successfully deleted!!!", Toast.LENGTH_SHORT).show();
                list.remove(position);
                if (position == list.size() - 1) {
                    onBackPressed();
                } else {
                    videoAdapter.notifyDataSetChanged();
                }
                if (list.size() == 0)
                    onBackPressed();

                SharedPreferences sharedPreferences = getSharedPreferences("Favourites_pref", MODE_PRIVATE);
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                mFavouriteImageList = new ArrayList<>();
                json1 = sharedPreferences.getString("Fav_Image", "");
                Type type1 = new TypeToken<ArrayList<String>>() {
                }.getType();
                mFavouriteImageList = gson.fromJson(json1, type1);

                if (mFavouriteImageList == null) {
                    mFavouriteImageList = new ArrayList<>();
                }
                if (mFavouriteImageList.contains(file.getPath())) {

                    mFavouriteImageList.remove(file.getPath());
                    json1 = gson.toJson(mFavouriteImageList);
                    editor.putString("Fav_Image", json1);
                    editor.apply();
                }
            } else {
                Toasty.error(ViewVideoActivity.this, "Deny by user!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    int currentPage = 0;

    private void StartTimer() {
        try {
            if (folderAdapter != null) {
                // System.out.println("==>>>>>>"+pager.getCurrentItem());
                currentPage = (mImagePager.getCurrentItem() + 1); //--if use scroll manully after sttart slideshow thrn start continously
            }
            //----animation show so delay whule slideshow
            if (mImagePager != null) {
                mImagePager.setDurationScroll(3000);
            }
            currentPage = mImagePager.getCurrentItem();
            // Auto start of viewpager
            final Handler handler = new Handler();
            final Runnable Update = new Runnable() {
                public void run() {

                    if (list != null) {
                        if (currentPage == list.size()) {
                            currentPage = 0;
                            CancelSlideShow();
                            mImagePager.setCurrentItem(currentPage, true);
                        } else {

                            // SetAnimatePagerTransition();//--effect
                            mImagePager.setCurrentItem(currentPage++, true);
                            SetAnimatePagerTransition();//--effect
                        }
                    }

                }
            };
            swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, 0, 3000);
        } catch (Exception e) {
        }
    }

    public void CancelSlideShow() {

        try {
            if (swipeTimer != null) {
                swipeTimer.cancel();
                swipeTimer.purge();
                swipeTimer = null;
                mTopLayer.setVisibility(View.VISIBLE);
                mBottomLayer.setVisibility(View.VISIBLE);
            }
            //----animation stop for delay scrolling
            if (mImagePager != null) {
                mImagePager.setDurationScroll(500);
                if (counter > 0) {
                    mImagePager.setCurrentItem(currentPage--, false);
                } else {
                    mImagePager.setCurrentItem(0, false);
                }
                folderAdapter.notifyDataSetChanged();
                mImagePager.setPageTransformer(true, null);
                fireAnalytics("ViewVideoActivity", "Cancel slideshow");
            }
        } catch (Exception e) {
        }
    }

    public boolean IsSlideShowRunning() {
        if (swipeTimer != null) {
            return true;
        } else {
            return false;
        }
    }

    public void HideToolBar() {
        // toolbar.setVisibility(View.GONE);
        if (IsSlideShowRunning()) {
            mTopLayer.setVisibility(View.VISIBLE);
            mBottomLayer.setVisibility(View.VISIBLE);
        } else {
            mTopLayer.setVisibility(View.GONE);
            mBottomLayer.setVisibility(View.GONE);
        }
    }


    int counter = 0;

    private void SetAnimatePagerTransition() {
        if (counter >= 7) {
            counter = 0;
        }
        if (counter == 0) {
            mImagePager.setPageTransformer(true, new FadePageTransformer());
        } else if (counter == 1) {
            mImagePager.setPageTransformer(true, new CubePageTransformer());
        } else if (counter == 2) {
            mImagePager.setPageTransformer(true, new AccordionPageTransformer());
        } else if (counter == 3) {
            mImagePager.setPageTransformer(true, new RotatePageTransformer());
        } else if (counter == 4) {
            mImagePager.setPageTransformer(true, new ZoomCenterPageTransformer());
        } else if (counter == 5) {
            mImagePager.setPageTransformer(true, new AlphaPageTransformer());
        } else if (counter == 6) {
            mImagePager.setPageTransformer(true, new FlipPageTransformer());
        }
        counter++;
    }

    public void FileInfo(int position) {
        try {
            if (list != null && list.size() > 0) {
                File filePath = new File(list.get(position));

                final Dialog fileDetailsDialog = new Dialog(ViewVideoActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                fileDetailsDialog.setContentView(R.layout.custom_file_details_dialog);
                fileDetailsDialog.show();

                final TextView lblFileName = (TextView) fileDetailsDialog.findViewById(R.id.id_name);
                final TextView lblFilePath = (TextView) fileDetailsDialog.findViewById(R.id.id_path);
                final TextView lblSize = (TextView) fileDetailsDialog.findViewById(R.id.id_size);
                final TextView lblCreateAt = (TextView) fileDetailsDialog.findViewById(R.id.id_create_at);

                // File file = new File(filePath);
                lblFileName.setText("Name : " + filePath.getName());
                lblFilePath.setText("Path : " + filePath.getPath());
                lblSize.setText("Size : " + Utils.getSize(filePath.length()));

                Date lastModDate = null;
                try {
                    lastModDate = new Date(filePath.lastModified());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    lblCreateAt.setText("Created on : " + Utils.convertTimeFromUnixTimeStamp(lastModDate.toString()));
                } catch (Exception e) {
                }

                CardView btnOkay = (CardView) fileDetailsDialog.findViewById(R.id.btn_okay);
                btnOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lblFileName.setText("");
                        lblFilePath.setText("");
                        lblSize.setText("");
                        lblCreateAt.setText("");
                        fileDetailsDialog.dismiss();
                    }
                });

                fireAnalytics("ViewVideoActivity", "Info video");
            }
        } catch (Exception e) {
        }
    }

    public void RenameDialog() {
        AlertDialog alertadd = new AlertDialog.Builder(this).create();
        LayoutInflater factory = LayoutInflater.from(ViewVideoActivity.this);
        final View view = factory.inflate(R.layout.text_dialog, null);
        alertadd.setView(view);
        alertadd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCancel = view.findViewById(R.id.mCancel);
        mOk = view.findViewById(R.id.mDone);
        mDialogTitle = view.findViewById(R.id.t1);
        File from = new File(list.get(mImagePager.getCurrentItem()));
        mDialogTitle.setText("Rename " + from.getName());
        mNewName = view.findViewById(R.id.mEditText);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertadd.dismiss();
            }
        });

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireAnalytics("ViewVideoActivity", "Rename video");
                if (mNewName.getText() != null)
                    ChangeName(mNewName.getText().toString());
                alertadd.dismiss();
            }
        });
        alertadd.show();
    }

    public void ChangeName(String newName) {
        int position = mImagePager.getCurrentItem();
        File from = new File(list.get(position));
        String extension = from.getAbsolutePath().substring(from.getAbsolutePath().lastIndexOf("."));
        String FolderPath = from.getParentFile().getAbsolutePath();
        File to = new File(FolderPath + "/" + newName + extension);
        boolean isRename = from.renameTo(to);
//        Log.e("From path",from.getPath());
//        Log.e("To path",to.getPath());
        if (isRename) {
            ContentResolver resolver = getApplicationContext().getContentResolver();
            resolver.delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                            " =?", new String[]{from.getAbsolutePath()});
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(to));
            getApplicationContext().sendBroadcast(intent);
            scanPhoto(to.toString());

            Utils.IsUpdate = true;
            list.remove(position);
            list.add(position, to.getAbsolutePath());
            Toasty.success(ViewVideoActivity.this, "Rename Successfully!", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }

        } else {
            Toasty.error(ViewVideoActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    public void FolderDialog(String action) {
        folderAdapter = new FolderdialogAdapter(ViewVideoActivity.this, Utils.mFolderVideoDialogList, folderInterface);
        dial = new Dialog(ViewVideoActivity.this, android.R.style.Theme_DeviceDefault);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.folders_dialog);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);
        folderAdapter.action = action;
        mFolderRec = dial.findViewById(R.id.targetList);
        mFolderRec.setLayoutManager(new LinearLayoutManager(getBaseContext(), RecyclerView.VERTICAL, false));
        mFolderRec.setAdapter(folderAdapter);
        dial.show();
    }

    public void CopyImage(String FolderPath) {

        File from = new File(list.get(mImagePager.getCurrentItem()));
        String[] arr = from.getName().split("\\.");
        File to = new File(FolderPath + "/" + arr[0] + Utils.GetRandomNumber() + "." + arr[1]);
//        Log.e("From path",from.getPath());
//        Log.e("To path",to.getPath());
        try {
            InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
        }

        scanPhoto(to.toString());
        fireAnalytics("ViewVideoActivity", "Copy video");
        Utils.IsUpdate = true;
        Toasty.success(ViewVideoActivity.this, "Video coppied suceesfully!!!", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
        } else {
            startService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
        }

    }

    public void MoveImage(String FolderPath) {
        int position = mImagePager.getCurrentItem();
        File from = new File(list.get(position));
        File to = new File(FolderPath + "/" + from.getName());
        boolean IsRename = from.renameTo(to);
//        Log.e("Video From", from.getPath());
//        Log.e("Video to", to.getPath());
//        Log.e("Video rename", String.valueOf(IsRename));
        if (IsRename) {
            boolean del = Utils.delete(ViewVideoActivity.this, from);
            list.remove(position);
            if (position == list.size() - 1) {
                onBackPressed();
            } else {
                videoAdapter = new ShowVideoAdapter(this, list, mImagePager);
                mImagePager.setAdapter(videoAdapter);
            }

            if (list.size() == 0)
                onBackPressed();
            scanPhoto(to.toString());

            Utils.IsUpdate = true;
            Toasty.success(ViewVideoActivity.this, "Video moved suceesfully!!!", Toast.LENGTH_SHORT).show();
            fireAnalytics("ViewVideoActivity", "Move video");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewVideoActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }

        }
        else{
            Toasty.error(ViewVideoActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
//                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
//                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }

    public void SetAsFavourite() {
        fireAnalytics("ViewVideoActivity", "Add to favourite");
        File file = new File(list.get(mImagePager.getCurrentItem()));
        SharedPreferences sharedPreferences = getSharedPreferences("Favourites_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        json1 = sharedPreferences.getString("Fav_Image", "");
        Type type1 = new TypeToken<ArrayList<String>>() {
        }.getType();
        mFavouriteImageList = gson.fromJson(json1, type1);

        if (mFavouriteImageList == null) {
            mFavouriteImageList = new ArrayList<>();
        }

        if (mFavouriteImageList.size() > 0) {
            for (int i = 0; i < mFavouriteImageList.size(); i++) {
                if (mFavouriteImageList.get(i).equals(file.getPath())) {
                    exist = true;
                    pos = i;
                    break;
                } else {
                    exist = false;
                }
            }
        }
        if (!exist) {
            mFavouriteImageList.add(file.getPath());
            mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_red_heart));
        } else {
            mFavouriteImageList.remove(pos);
            mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.circle_black_heart));
            exist = false;
        }

        Utils.IsUpdate = true;
        json1 = gson.toJson(mFavouriteImageList);
        editor.putString("Fav_Image", json1);
        editor.apply();
    }

    @Override
    public void onBackPressed() {

        if (IsSlideShowRunning()) {
            CancelSlideShow();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.mTopLayer || v.getId() == R.id.mBottomLayer) {
            mRotateRL.setVisibility(View.GONE);
            mOptionRL.setVisibility(View.GONE);
        }
        return false;
    }

    public void SetList(ArrayList<String> pathlist, int position) {
        try {

            list = pathlist;
            currenpositionToDisplay = position;
        } catch (Exception e) {
//            Log.e("Error:",e.getMessage());
        }
    }

}