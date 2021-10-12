package com.gallery.photosgallery.videogallery.bestgallery.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FavouriteAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FolderAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FolderVideoAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Class.ConnectionDetector;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.CameraInterface;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.SortListner;
import com.gallery.photosgallery.videogallery.bestgallery.Model.BaseModel;
import com.gallery.photosgallery.videogallery.bestgallery.Prefrences.SharedPrefrences;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Service.GetFileList;
import com.gallery.photosgallery.videogallery.bestgallery.Util.NotificationUtils;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private AppUpdateManager appUpdateManager;
    private static final int FLEXIBLE_APP_UPDATE_REQ_CODE = 123;
    private InstallStateUpdatedListener installStateUpdatedListener;

    public static RecyclerView mFolderRec;
    FolderAdapter folderAdapter;
    FolderVideoAdapter folderVideoAdapter;
    ArrayList<BaseModel> mFolderList = new ArrayList<>();
    ArrayList<BaseModel> mVideoFolderList = new ArrayList<>();

    ImageView mSearch;
    public static ImageView mEmpty;
    public static Handler album_handler;
    CameraInterface anInterface;
    int VIDEO_REQUEST=193;
     EditText mSearchBar;
     RelativeLayout mSearchRL,mTopLayer;
     ImageView mPhotos,mVideos,mfavourites;
    public static Handler Video_Handler;
    ArrayList<String> mFavouriteImageList = new ArrayList<>();
    String json1;
    Uri imageUri;
    FavouriteAdapter favouriteAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    ConnectionDetector cd;
    AdView adView;
    boolean isInternetPresent=false;
    LinearLayout bannerContainer;

    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    private void checkUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void popupSnackBarForCompleteUpdate() {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), "New app is ready!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Install", view -> {
                    if (appUpdateManager != null) {
                        appUpdateManager.completeUpdate();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.purple_500))
                .show();
    }

    private void removeInstallStateUpdateListener() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }


    @Override
    public void permissionGranted(){
        intializehandler();
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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);
        mSearchRL.setVisibility(View.GONE);
        mTopLayer.setVisibility(View.VISIBLE);
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        checkUpdate();

        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                removeInstallStateUpdateListener();
            } else {
                Toasty.info(getApplicationContext(), "InstallStateUpdatedListener: state: " + state.installStatus(), Toast.LENGTH_LONG).show();
            }
        };

        hideKeyboard(MainActivity.this);
        mSearchBar.clearFocus();
        anInterface=new CameraInterface(){
            @Override
            public void onCameraClick(){
                if(Utils.VIEW_TYPE.equals("Photos")){
                    fireAnalytics("Image page", "Open camera");
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, 101);
                }else if(Utils.VIEW_TYPE.equals("Videos")){
                    fireAnalytics("Video page", "Open camera");
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(intent, VIDEO_REQUEST);

                }

            }
        };
        folderAdapter = new FolderAdapter(MainActivity.this,anInterface);
        folderVideoAdapter = new FolderVideoAdapter(MainActivity.this,anInterface);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils notiUtils = new NotificationUtils(this);
        }
        MainCaller();

        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(Utils.VIEW_TYPE.equals("Photos")) {
                    folderAdapter.getFilter().filter(s);
                }else if(Utils.VIEW_TYPE.equals("Videos")){
                    folderVideoAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                    adView = new AdView(MainActivity.this);
                    fireAnalytics("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad_home));
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

//        throw new RuntimeException("Test Crash"); // Force a crash

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(R.id.searchBar);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void MainCaller(){
        intializehandler();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    MainCaller();
//                    saveVideo(data);
                }
            }
        }
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                try {
                    String imageurl = getRealPathFromURI(imageUri);

                    MainCaller();
                }
                catch (Exception e){

                }
            }
        }
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE) {
            fireAnalytics("Main", "App update");
//            Log.e("Result code:", String.valueOf(resultCode));
            if (resultCode == RESULT_CANCELED) {
                Toasty.info(getApplicationContext(), "Update canceled by user! ", Toast.LENGTH_LONG).show();

            } else if (resultCode == RESULT_OK) {

                Toasty.success(getApplicationContext(),"Update success!  ", Toast.LENGTH_LONG).show();
            } else {
                Toasty.error(getApplicationContext(), "Update Failed!  ", Toast.LENGTH_LONG).show();
                checkUpdate();
            }
        }
    }


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void saveVideo(Intent data){
            try {

                File newfile;

                AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                FileInputStream in = videoAsset.createInputStream();

                String root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
                File myDir = new File(root, "DCIM");
                myDir.mkdirs();

                File myDir1 = new File(myDir, "Gallery");
                myDir1.mkdirs();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fname = "" + timeStamp + ".mp4";

                newfile = new File(myDir1, fname);

                if (newfile.exists()) newfile.delete();


                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

            Log.e("Video", newfile.getPath());
                MediaScannerConnection.scanFile(this, new String[]{newfile.toString()}, new String[]{newfile.getName()}, null);
                MainCaller();

            } catch (Exception e) {
//            Log.e("Video", "Copy file error.");
                e.printStackTrace();
            }

    }

    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        File myDir = new File(root, "DCIM");
        myDir.mkdirs();

        File myDir1 = new File(myDir, "Camera");
        myDir1.mkdirs();


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "" + timeStamp + ".png";

        File file = new File(myDir1, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.e("TAG", "saveImage: " + file.getAbsolutePath());

        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, new String[]{file.getName()}, null);
        MainCaller();

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(Utils.IsUpdate){
                    intializehandler();
                    Utils.IsUpdate = false;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Favourites_pref",MODE_PRIVATE);
        Gson gson = new Gson();
        mFavouriteImageList = new ArrayList<>();
        json1 = sharedPreferences.getString("Fav_Image","");
        Type type1 = new TypeToken<ArrayList<String>>(){        }.getType();
        mFavouriteImageList = gson.fromJson(json1,type1);
        mFolderRec.setLayoutManager(new GridLayoutManager(getBaseContext(),2));
        mFolderRec.setLayoutAnimation(null);
        if(mFavouriteImageList!=null && mFavouriteImageList.size() > 0){
//            for(int i = 0;i < mFavouriteImageList.size();i++){
//                Log.e("Fav Image:",mFavouriteImageList.get(i));
//            }
            favouriteAdapter = new FavouriteAdapter(MainActivity.this);

            favouriteAdapter.Addall(mFavouriteImageList);
            favouriteAdapter.notifyDataSetChanged();
        }

//        Log.e("View type",Utils.VIEW_TYPE);

        if(Utils.VIEW_TYPE.equals("Favourites")){
            Loadfavourite();
        }else if(Utils.VIEW_TYPE.equals("Photos")){
            LoadPhotos();
        }else if(Utils.VIEW_TYPE.equals("Videos")){
            LoadVideos();
        }
    }

    public void init(){

        bannerContainer=findViewById(R.id.banner_container);

        mPhotos=findViewById(R.id.mPhotos);
        mVideos=findViewById(R.id.mVideos);
        mfavourites=findViewById(R.id.mFavourites);
        mPhotos.setOnClickListener(this);
        mVideos.setOnClickListener(this);
        mfavourites.setOnClickListener(this);
        mEmpty=findViewById(R.id.empty);

        mTopLayer=findViewById(R.id.topLayer);
        mSearchRL=findViewById(R.id.searchRL);

        rl_progress=findViewById(R.id.rl_progress);
        avi=findViewById(R.id.avi);
        mSearchBar=findViewById(R.id.searchBar);
        mSearchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mFolderRec = findViewById(R.id.folder_grid);
        mSearch = findViewById(R.id.mSearch);
        mSearch.setOnClickListener(this);


    }

    public void intializehandler() {

        album_handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int code = message.what;
                if (code == 21){

                    try{
                        FolderAdapter.objects.clear();
                        FolderAdapter.filteredList.clear();
                        mFolderList = new ArrayList<>();
                        mFolderList = (ArrayList<BaseModel>)message.obj;
                        Utils.mFolderDialogList=new ArrayList<>();
                        BaseModel FirstModel1 = new BaseModel();
                        FirstModel1.setFolderName("Create_Album");
                        FirstModel1.setType(1);
                        Utils.mFolderDialogList.add(0,FirstModel1);
                        Utils.mFolderDialogList.addAll(mFolderList);
                        BaseModel FirstModel = new BaseModel();
                        FirstModel.setFolderName("Camera");
                        FirstModel.setBucketName("Camera");
                        FirstModel.setType(1);
                        mFolderList.add(0, FirstModel);

                        if(mFolderList != null && mFolderList.size() > 0){

//                            folderAdapter.Addall(mFolderList);
                            for (int i = 0; i < mFolderList.size(); i++) {
//                                Log.e("Folder name:",mFolderList.get(i).getBucketName());
                                folderAdapter.add(i,mFolderList.get(i));
                            }
                        }

                        if(Utils.VIEW_TYPE.equals("Favourites")){
                            Loadfavourite();
                        }else if(Utils.VIEW_TYPE.equals("Photos")){
                            LoadPhotos();
                        }else if(Utils.VIEW_TYPE.equals("Videos")){
                            LoadVideos();
                        }
                        stopAnim();

                    }catch(Exception e){
//                        Log.e("Error",e.getMessage());
                    }
                }
                return false;
            }
        });

        Video_Handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                try{
                    FolderVideoAdapter.objects.clear();
                    FolderVideoAdapter.filteredList.clear();
                    mVideoFolderList = new ArrayList<>();
                    mVideoFolderList = (ArrayList<BaseModel>)message.obj;
                    Utils.mFolderVideoDialogList=new ArrayList<>();
                    BaseModel FirstModel1 = new BaseModel();
                    FirstModel1.setFolderName("Create_Album");
                    FirstModel1.setType(1);
                    Utils.mFolderVideoDialogList.add(0,FirstModel1);
                    Utils.mFolderVideoDialogList.addAll(mVideoFolderList);
                    BaseModel FirstModel = new BaseModel();
                    FirstModel.setFolderName("Camera");
                    FirstModel.setBucketName("Camera");
                    FirstModel.setType(1);
                    mVideoFolderList.add(0, FirstModel);

                    if(mVideoFolderList != null && mVideoFolderList.size() > 0){
//                        folderVideoAdapter.Addall(mVideoFolderList);
                        for (int i = 0; i < mVideoFolderList.size(); i++) {
                            folderVideoAdapter.add(i,mVideoFolderList.get(i));
                        }
                    }




                    if(Utils.VIEW_TYPE.equals("Favourites")){
                        Loadfavourite();
                    }else if(Utils.VIEW_TYPE.equals("Photos")){
                        LoadPhotos();
                    }else if(Utils.VIEW_TYPE.equals("Videos")){
                        LoadVideos();
                    }
                    stopAnim();

                }catch(Exception e){
//                    Log.e("Error",e.getMessage());
                }

                return false;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnim();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(MainActivity.this, GetFileList.class).putExtra("action", "Photos"));
                } else {
                    startService(new Intent(MainActivity.this, GetFileList.class).putExtra("action", "Photos"));
                }

            }
        }, 100);
    }

    RelativeLayout rl_progress;
    AVLoadingIndicatorView avi;

    public  void startAnim() {
        rl_progress.setVisibility(View.VISIBLE);
        avi.show();
    }

    public  void stopAnim() {
        rl_progress.setVisibility(View.GONE);
        avi.hide();
    }

    public void Loadfavourite(){

        Utils.VIEW_TYPE="Favourites";
        mfavourites.setImageDrawable(getResources().getDrawable(R.drawable.ic_favourite_selected));
        mVideos.setImageDrawable(getResources().getDrawable(R.drawable.video_deselected));
        mPhotos.setImageDrawable(getResources().getDrawable(R.drawable.photo_deselected));
        if(mFavouriteImageList!=null && mFavouriteImageList.size() > 0) {
            mFolderRec.setAdapter(favouriteAdapter);
            favouriteAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(View.GONE);
            mFolderRec.setVisibility(View.VISIBLE);
        }else{
            mFolderRec.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void LoadPhotos(){
        Utils.VIEW_TYPE="Photos";
        mPhotos.setImageDrawable(getResources().getDrawable(R.drawable.photo_selected));
        mVideos.setImageDrawable(getResources().getDrawable(R.drawable.video_deselected));
        mfavourites.setImageDrawable(getResources().getDrawable(R.drawable.favourite_deselected));
        if(mFolderList!=null && mFolderList.size() > 0) {
            mFolderRec.setAdapter(folderAdapter);
            folderAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(View.GONE);
            mFolderRec.setVisibility(View.VISIBLE);
        }else{
            mFolderRec.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void LoadVideos(){
        Utils.VIEW_TYPE="Videos";
        mPhotos.setImageDrawable(getResources().getDrawable(R.drawable.photo_deselected));
        mfavourites.setImageDrawable(getResources().getDrawable(R.drawable.favourite_deselected));
        mVideos.setImageDrawable(getResources().getDrawable(R.drawable.video_selected));
        if(mVideoFolderList!=null && mVideoFolderList.size() > 0) {
            mFolderRec.setAdapter(folderVideoAdapter);
            folderVideoAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(View.GONE);
            mFolderRec.setVisibility(View.VISIBLE);
        }else{
            mFolderRec.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.mFavourites:{
               Loadfavourite();
                mSearchRL.setVisibility(View.GONE);
                mTopLayer.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.mPhotos:{
               LoadPhotos();
            }
            break;
            case R.id.mVideos:{
               LoadVideos();
            }
            break;
            case R.id.mSearch:{
                mSearchRL.setVisibility(View.VISIBLE);
                mTopLayer.setVisibility(View.GONE);
            }
            break;
        }
    }


}