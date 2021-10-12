package com.gallery.photosgallery.videogallery.bestgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gallery.photosgallery.videogallery.bestgallery.Adapter.ImagesAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.VideoAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Class.ConnectionDetector;
import com.gallery.photosgallery.videogallery.bestgallery.Model.BaseModel;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class ViewVideoAlbumActivity extends AppCompatActivity {

    RecyclerView mImageRec;
    VideoAdapter vidAdapter;
    public static ArrayList<String> pathlist;
    public static String FolderPath;
    public static String Bucket_Id;
    public static String Title = "Photos";
    TextView mTitle;
    ImageView mBack;
    ConnectionDetector cd;
    AdView adView;
    boolean isInternetPresent=false;
    LinearLayout bannerContainer;
    private FirebaseAnalytics mFirebaseAnalytics;
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
        setContentView(R.layout.activity_view_video_album);

        init();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(ViewVideoAlbumActivity.this);
        vidAdapter = new VideoAdapter(ViewVideoAlbumActivity.this);
        initView();
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(ViewVideoAlbumActivity.this);
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

    @Override
    protected void onResume(){
        super.onResume();
        if(Utils.IsUpdate){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    initView();
                }
            });
        }
    }

    public void init(){
        bannerContainer=findViewById(R.id.banner_container);
        mImageRec = findViewById(R.id.images_grid);

        mTitle = findViewById(R.id.mTitle);
        mBack = findViewById(R.id.back);
    }

    public void SaveList(BaseModel albumDetail) {

        pathlist = new ArrayList<>();
        pathlist = albumDetail.getPathlist();
        FolderPath = albumDetail.getBucketPath();
        Bucket_Id = albumDetail.getBucketId();
        Title = albumDetail.getBucketName();
    }

    private void initView() {
        mTitle.setText(Title);

        mImageRec.setLayoutManager(new GridLayoutManager(getBaseContext(),2));
        mImageRec.setLayoutAnimation(null);


        mImageRec.setAdapter(vidAdapter);

        if (pathlist != null && pathlist.size() > 0) {
            vidAdapter.Addall(pathlist);
            vidAdapter.notifyDataSetChanged();
        }
    }
}