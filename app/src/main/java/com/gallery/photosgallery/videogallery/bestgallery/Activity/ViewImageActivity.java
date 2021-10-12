package com.gallery.photosgallery.videogallery.bestgallery.Activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.FolderdialogAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Adapter.ShowImageAdapter;
import com.gallery.photosgallery.videogallery.bestgallery.Class.ConnectionDetector;
import com.gallery.photosgallery.videogallery.bestgallery.Interface.FolderInterface;
import com.gallery.photosgallery.videogallery.bestgallery.Prefrences.SharedPrefrences;
import com.gallery.photosgallery.videogallery.bestgallery.R;
import com.gallery.photosgallery.videogallery.bestgallery.Service.GetFileList;
import com.gallery.photosgallery.videogallery.bestgallery.Class.CustomViewPager;
import com.gallery.photosgallery.videogallery.bestgallery.Util.LoginPreferenceManager;
import com.gallery.photosgallery.videogallery.bestgallery.Util.Utils;
import com.gallery.photosgallery.videogallery.bestgallery.animation.CubePageTransformer;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

import static com.gallery.photosgallery.videogallery.bestgallery.Util.Utils.DELETE_REQUEST;
import static com.gallery.photosgallery.videogallery.bestgallery.Util.Utils.RENAME_REQUEST;

public class ViewImageActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    ImageView mBack, mRotate, mFavourite, mMore;
    ImageView mSlideshow, mEdit, mDelete, mShare;
    public static RelativeLayout mRotateRL, mOptionRL;
    RelativeLayout mTopLayer;
    LinearLayout mBottomLayer;
    CustomViewPager mImagePager;
    TextView mRotateRight, mRightLeft, mRotateBottom;
    TextView mCopy, mMove, mRename, mPrint, mWallpaper, mInfo;
    public static ArrayList<String> list;
    public static int currenpositionToDisplay = 0;
    ShowImageAdapter imageAdapter;
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
    File to;
    int createAlbumPosition = 0;
    String destinationPath;
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;

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
        setContentView(R.layout.activity_view_image);

        init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(ViewImageActivity.this);
        initview();
        From = getIntent().getStringExtra("From");

        if (From.equals("Favs")) {
            mImagePager.setPagingEnabled(false);
            fireAnalytics("ViewImageActivity", "From favourites");
            mCopy.setVisibility(View.GONE);
            mMove.setVisibility(View.GONE);
            mRename.setVisibility(View.GONE);

        } else {
            mImagePager.setPagingEnabled(true);
            fireAnalytics("ViewImageActivity", "From Folder");
            mCopy.setVisibility(View.VISIBLE);
            mMove.setVisibility(View.VISIBLE);
            mRename.setVisibility(View.VISIBLE);
        }

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(ViewImageActivity.this);
                    fireAnalytics("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad_img));
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
        mRotate = findViewById(R.id.mRotate);
        mFavourite = findViewById(R.id.mFavourite);
        mMore = findViewById(R.id.mMore);
        mSlideshow = findViewById(R.id.mSlideshow);
        mEdit = findViewById(R.id.mEdit);
        mDelete = findViewById(R.id.mDelete);
        mShare = findViewById(R.id.mShare);

        mBack.setOnClickListener(this);
        mRotate.setOnClickListener(this);
        mFavourite.setOnClickListener(this);
        mMore.setOnClickListener(this);
        mSlideshow.setOnClickListener(this);
        mEdit.setOnClickListener(this);
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
        mPrint = findViewById(R.id.mPrint);
        mWallpaper = findViewById(R.id.mWallpaper);
        mInfo = findViewById(R.id.mInfo);
        mCopy.setOnClickListener(this);
        mMove.setOnClickListener(this);
        mRename.setOnClickListener(this);
        mPrint.setOnClickListener(this);
        mWallpaper.setOnClickListener(this);
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
        imageAdapter = new ShowImageAdapter(this, list, mImagePager);
        mImagePager.setAdapter(imageAdapter);
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
            public void OnSelectFolder(String action, String path, int position) {
                createAlbumPosition = position;
                destinationPath = path;
                if (action.equals("copy")) {
                    CopyImage(path);
                } else if (action.equals("move")) {
                    File sampleFile = Environment.getExternalStorageDirectory();
                    File from = new File(list.get(mImagePager.getCurrentItem()));
                    File file1 = new File(path);
                    if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                        MoveImage(path);
                    } else {
                        if (!SharedPrefrences.getSharedPreference(ViewImageActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                            startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                        } else {
                            moveSDImage(from, path);
                        }
                    }
                }
                dial.dismiss();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_REQUEST) {
            if (resultCode == RESULT_OK) {

                int position = mImagePager.getCurrentItem();
                File file = new File(list.get(position));
//                Log.e("Position", String.valueOf(position));
                Toasty.success(ViewImageActivity.this, "Image Successfully deleted!!!", Toast.LENGTH_SHORT).show();
                list.remove(position);
                if (position == list.size() - 1) {
                    onBackPressed();
                } else {
                    imageAdapter.notifyDataSetChanged();
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
                Toasty.error(ViewImageActivity.this, "Deny by user!!!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RENAME_REQUEST) {
            if (resultCode == RESULT_OK) {
                int position = mImagePager.getCurrentItem();
                File from = new File(list.get(position));

                Log.e("From", String.valueOf(from.getPath()));
                Log.e("To", String.valueOf(to.getPath()));

                Log.e("Uri", String.valueOf(Uri_one));
                int row = 0;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, to.getPath());
                row = getContentResolver().update(Uri_one, values, null, null);
                Log.e("Updated row", String.valueOf(row));


                boolean IsRename = from.renameTo(to);
                Log.e("IsRename", String.valueOf(IsRename));
                if (row > 0) {
                    scanPhoto(to.toString());
                    Utils.IsUpdate = true;
                    list.remove(position);
                    list.add(position, to.getAbsolutePath());
                    Toasty.success(ViewImageActivity.this, "Rename Successfully!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toasty.error(ViewImageActivity.this, "Deny by user!!!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE) {
            if (resultCode == RESULT_OK) {

                File file1 = new File(list.get(mImagePager.getCurrentItem()));
                String[] parts = (file1.getAbsolutePath()).split("/");

                DocumentFile documentFile = DocumentFile.fromTreeUri(this, data.getData());
                documentFile = documentFile.findFile(parts[parts.length - 1]);

                if (documentFile == null) {
                    DocumentFile documentFile1 = DocumentFile.fromTreeUri(this, data.getData());
                    SharedPrefrences.setSharedPreferenceUri(ViewImageActivity.this, documentFile1.getUri());
                    SharedPrefrences.setSharedPreference(ViewImageActivity.this, file1.getParentFile().getAbsolutePath());
                    moveSDImage(file1, destinationPath);

                } else {
                    Log.e("Document ", String.valueOf(documentFile.getUri()));
                    SharedPrefrences.setSharedPreferenceUri(ViewImageActivity.this, documentFile.getUri());
                    SharedPrefrences.setSharedPreference(ViewImageActivity.this, file1.getParentFile().getAbsolutePath());
                    getData(file1);

//                    moveSDImage(file1, createAlbumPosition,destinationPath);
                }

            }
        }
    }

    private void moveSDImage(File sourceFile, String destinationPath) {

        File file;
        file = new File(destinationPath, sourceFile.getName());

//        Log.e("File",file.getPath());
        OutputStream fileOutupStream = null;
        File file2 = new File(file.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());

//        Log.e("File2",file2.getPath());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

//            ArrayList<String> favFileList = SharedPrefrences.getFavouriteFileList(ImageViewActivity.this);
//            if (favFileList.contains(sourceFile.getAbsolutePath())) {
//                favFileList.remove(sourceFile.getAbsolutePath());
//                favFileList.add(file.getAbsolutePath());
//                SharedPrefrance.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
//                SharedPrefrance.setFavouriteFileList(ImageViewActivity.this, favFileList);
//            }

            if (sourceFile.exists()) {
                boolean isDelete = Utils.delete(ViewImageActivity.this, sourceFile);
                Log.e("LLLL_Del: ", String.valueOf(isDelete));
            }

            scanPhoto(file.toString());


            if (mImagePager.getCurrentItem() == list.size() - 1) {
                list.remove(mImagePager.getCurrentItem());
                onBackPressed();
            } else {
                list.remove(mImagePager.getCurrentItem());
                imageAdapter.notifyDataSetChanged();
            }
            if (list.size() == 0)
                onBackPressed();

            Utils.IsUpdate = true;
            Toasty.success(ViewImageActivity.this, "Image moved suceesfully!!!", Toast.LENGTH_SHORT).show();
            fireAnalytics("ViewImageActivity", "Move Image");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }

        } catch (Exception e) {
            Toasty.error(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void getData(File file1) {

        File file2 = new File(file1.getParentFile().getParentFile().getAbsolutePath(), file1.getName());
        Log.e("File_2 :", file2.getPath());

        OutputStream fileOutupStream = null;
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            if (file1.exists()) {
                boolean isDelete = Utils.delete(ViewImageActivity.this, file1);
            }

            list.remove(mImagePager.getCurrentItem());
            imageAdapter.notifyDataSetChanged();
            if (list.isEmpty())
                onBackPressed();
            Utils.IsUpdate = true;
            Toasty.success(ViewImageActivity.this, "Image moved suceesfully!!!", Toast.LENGTH_SHORT).show();
            fireAnalytics("ViewImageActivity", "Move Image");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        Uri treeUri = Uri.parse(SharedPrefrences.getSharedPreferenceUri(ViewImageActivity.this));

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(ViewImageActivity.this, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public String getExtSdCardFolder(final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : getExternalFilesDirs("external")) {
            if (file != null && !file.equals(getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("TAG", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    boolean renameFile(Context context) {

        try {

            ContentResolver contentResolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
            contentResolver.update(Uri_one, contentValues, null, null);

            contentValues.clear();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, to.getName());
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation);
            contentValues.put(MediaStore.Images.Media.TITLE, to.getName());
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            contentResolver.update(Uri_one, contentValues, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    String relativeLocation = Environment.DIRECTORY_DOWNLOADS + File.separator + "AppFolder";

    Uri Uri_one, Uri_two;
    long mediaID_one, mediaID_two;

    public void ChangeName(String newName) {
        int position = mImagePager.getCurrentItem();
        File from = new File(list.get(position));


        String extension = from.getAbsolutePath().substring(from.getAbsolutePath().lastIndexOf("."));
        String FolderPath = from.getParentFile().getAbsolutePath();
        to = new File(FolderPath + "/" + newName + extension);

        mediaID_one = Utils.getFilePathToMediaID(from.getAbsolutePath(), getBaseContext());
        Uri_one = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaID_one);
        List<Uri> uris = new ArrayList<>();
        uris.add(Uri_one);

        boolean isRename = from.renameTo(to);
        if (!isRename) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PendingIntent pi = MediaStore.createWriteRequest(getContentResolver(), uris);
                try {
                    startIntentSenderForResult(pi.getIntentSender(), RENAME_REQUEST, null, 0, 0,
                            0);
                } catch (IntentSender.SendIntentException e) {

                }


//                renameFiles(getBaseContext(),from);
            }
        }
        if (isRename) {
            ContentResolver resolver = getApplicationContext().getContentResolver();
            resolver.delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA +
                            " =?", new String[]{from.getAbsolutePath()});
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(to));
            getApplicationContext().sendBroadcast(intent);
            scanPhoto(to.toString());

            Utils.IsUpdate = true;
            list.remove(position);
            list.add(position, to.getAbsolutePath());
            Toasty.success(ViewImageActivity.this, "Rename Successfully!", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }

        } else {
            Toasty.error(ViewImageActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.IsUpdate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (imageAdapter != null) {
                        imageAdapter.notifyDataSetChanged();
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
                            if (imageAdapter != null) {
                                imageAdapter.RefreshDeleteData(mImagePager.getCurrentItem());
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
            case R.id.mRotate: {
                mRotateRL.setVisibility(View.VISIBLE);
                mOptionRL.setVisibility(View.GONE);
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
            case R.id.mSlideshow: {
                mOptionRL.setVisibility(View.GONE);
                fireAnalytics("ViewImageActivity", "Slideshow");
                HideToolBar();
                AutoSliderTimerSet();
//                StartTimer();
            }
            break;
            case R.id.mEdit: {
                mOptionRL.setVisibility(View.GONE);
                File mEditFile = new File(list.get(mImagePager.getCurrentItem()));
                fireAnalytics("ViewImageActivity", "Edit Image");
                Intent in = new Intent(ViewImageActivity.this, EditActivity.class);
                Utils.mEditpath = mEditFile.getPath();
                startActivity(in);
            }
            break;
            case R.id.mDelete: {
                deleteDialog = new Dialog(ViewImageActivity.this, android.R.style.Theme_DeviceDefault);
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
                        imageAdapter.DeleteAction(position, From);
                        fireAnalytics("ViewImageActivity", "Delete Image");
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
                fireAnalytics("ViewImageActivity", "Share Image");
                String path;
                File file;
                Uri uri;

                path = list.get(mImagePager.getCurrentItem());
                file = new File(path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(ViewImageActivity.this, getPackageName() + ".provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share Via"));
            }
            break;
            case R.id.mRotateRight: {
                fireAnalytics("ViewImageActivity", "Rotate Image Right");
                mRotateRL.setVisibility(View.GONE);
                imageAdapter.rotate(1, mImagePager.getCurrentItem());
            }
            break;
            case R.id.mRotateLeft: {
                fireAnalytics("ViewImageActivity", "Rotate Image Left");
                mRotateRL.setVisibility(View.GONE);
                imageAdapter.rotate(2, mImagePager.getCurrentItem());
            }
            break;
            case R.id.mRotateBottom: {
                fireAnalytics("ViewImageActivity", "Rotate Image Bottom");
                mRotateRL.setVisibility(View.GONE);
                imageAdapter.rotate(3, mImagePager.getCurrentItem());
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
            case R.id.mPrint: {
                fireAnalytics("ViewImageActivity", "Print Image ");
                mOptionRL.setVisibility(View.GONE);
                PrintDialog(mImagePager.getCurrentItem());
            }
            break;
            case R.id.mWallpaper: {
                fireAnalytics("ViewImageActivity", "Set Image as wallpaper");
                mOptionRL.setVisibility(View.GONE);
                SetAsWallaper(mImagePager.getCurrentItem());
            }
            break;
            case R.id.mInfo: {
                fireAnalytics("ViewImageActivity", "Info Image");
                mOptionRL.setVisibility(View.GONE);
                FileInfo(mImagePager.getCurrentItem());
            }
            break;
        }
    }


    public void AutoSliderTimerSet() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(R.string.slideshowtimer)
                .items(R.array.slideshow_times)
                .itemsCallbackSingleChoice(getSelectedAutoSlideshowTimer(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        LoginPreferenceManager.SaveIntgData(getApplicationContext(), "slideshowtimer", which);
                        SlideShowDuration = getAutoSlideShowTime();
                        //-----start slide show
                        StartTimer();
                        HideToolBar();
                        builder.build().dismiss();
                        return true;
                    }
                })
                .show();
    }

    private int getSelectedAutoSlideshowTimer() {
        int effect = (int) LoginPreferenceManager.GetIntData(getApplicationContext(), "slideshowtimer");
        return effect;
    }

    public int getAutoSlideShowTime() {
        int effect = getSelectedAutoSlideshowTimer();
        Log.d("Effect", "EffectAfterSaved :- " + effect);
        int time = -1;
        switch (effect) {
            case 0:
                time = 1000;
                break;
            case 1:
                time = 2000;
                break;
            case 2:
                time = 3000;
                break;
            case 3:
                time = 4000;
                break;
            case 4:
                time = 5000;
                break;

            default:
                time = 1000;
                break;
        }
        return time;
    }

    int currentPage = 0;
    public int SlideShowDuration = 2000;

    private void StartTimer() {
        try {
            if (folderAdapter != null) {
                // System.out.println("==>>>>>>"+pager.getCurrentItem());
                currentPage = (mImagePager.getCurrentItem() + 1); //--if use scroll manully after sttart slideshow thrn start continously
            }
            //----animation show so delay whule slideshow
            if (mImagePager != null) {
                mImagePager.setDurationScroll(SlideShowDuration);
            }
            // Auto start of viewpager
            final Handler handler = new Handler();
            final Runnable Update = new Runnable() {
                public void run() {

                    if (list != null) {
//                        Log.e("List size:", String.valueOf(list.size()));
                        if (currentPage == list.size()) {
                            currentPage = 0;
                            CancelSlideShow();
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
            }, 0, SlideShowDuration);
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
                imageAdapter.notifyDataSetChanged();

            }
            mImagePager.setPageTransformer(false, null);
            mImagePager.setAnimation(null);
            fireAnalytics("ViewImageActivity", "Slideshow cancel");
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
        mImagePager.setPageTransformer(false, new CubePageTransformer());
        if (counter >= 7) {
            counter = 0;
        }
//        Log.e("Current page:" + String.valueOf(currentPage), "Counter :" + counter);
//
//        switch (counter){
//            case 0: {    mImagePager.setPageTransformer(false, new CubePageTransformer());  }   break;
//            case 1: {    mImagePager.setPageTransformer(false, new ZoomFadePageTransformer());  }   break;
//            case 2: {    mImagePager.setPageTransformer(false, new ZoomCenterPageTransformer());  }   break;
//            case 3: {    mImagePager.setPageTransformer(false, new ZoomPageTransformer());  }   break;
//            case 4: {    mImagePager.setPageTransformer(false, new InRightDownTransformer());  }   break;
//            case 5: {    mImagePager.setPageTransformer(false, new InRightUpTransformer());  }   break;
//            case 6: {    mImagePager.setPageTransformer(false, new SlowBackgroundTransformer());  }   break;
//        }
        counter++;
    }

    public void FileInfo(int position) {
        try {
            if (list != null && list.size() > 0) {
                File filePath = new File(list.get(position));

                final Dialog fileDetailsDialog = new Dialog(ViewImageActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
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
            }
        } catch (Exception e) {
        }
    }


    public void SetAsWallaper(int postion) {
        try {
            if (list.get(postion).startsWith("https")) {
                Toasty.error(ViewImageActivity.this, "A problem occur with this file!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                File file = new File(list.get(postion));
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(ViewImageActivity.this, getPackageName() + ".provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                // uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "image/jpeg");
                intent.putExtra("mimeType", "image/jpeg");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Set as:"));
            }
        } catch (Exception e) {
        }
    }

    public void PrintDialog(int position) {
        if (list != null && list.size() > position) {
            String ext = new File(list.get(position)).getName();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
            if (sub_ext.equalsIgnoreCase("jpg") || sub_ext.equalsIgnoreCase("jpeg") || sub_ext.equalsIgnoreCase("gif")
                    || sub_ext.equalsIgnoreCase("png")) {
                PrintHelper photoPrinter = new PrintHelper(ViewImageActivity.this);
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                Bitmap bitmap = BitmapFactory.decodeFile(list.get(position));
                photoPrinter.printBitmap(new File(list.get(position)).getName(), bitmap);
            } else {
                Toasty.error(ViewImageActivity.this, "Can't print file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void RenameDialog() {
        AlertDialog alertadd = new AlertDialog.Builder(this).create();
        LayoutInflater factory = LayoutInflater.from(ViewImageActivity.this);
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
                fireAnalytics("ViewImageActivity", "Rename Image");
                if (mNewName.getText() != null)
                    ChangeName(mNewName.getText().toString());
                alertadd.dismiss();
            }
        });
        alertadd.show();
    }


    public void FolderDialog(String action) {
        folderAdapter = new FolderdialogAdapter(ViewImageActivity.this, Utils.mFolderDialogList, folderInterface);
        dial = new Dialog(ViewImageActivity.this, android.R.style.Theme_DeviceDefault);
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
        fireAnalytics("ViewImageActivity", "Copy Image");
        Utils.IsUpdate = true;
        Toasty.success(ViewImageActivity.this, "Image copied suceesfully!!!", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
        } else {
            startService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
        }

    }

    public void MoveImage(String FolderPath) {
        int position = mImagePager.getCurrentItem();
        File from = new File(list.get(position));
        File to = new File(FolderPath + "/" + from.getName());

//        Log.e("Image From",from.getPath());
        Log.e("Image to",to.getPath());
//        boolean IsRename = from.renameTo(to);
//        Log.e("Image rename", String.valueOf(IsRename));
        Bitmap bitmap = BitmapFactory.decodeFile(from.getAbsolutePath());

        try {
            FileOutputStream out = new FileOutputStream(to);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Log.e("LL_Image: ", to.getAbsolutePath());

//            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(AlbumViewActivity.this);
//            favFileList.remove(sourceFile.getAbsolutePath());
//            favFileList.add(file.getAbsolutePath());
//            SharedPrefrance.setFavouriteFileList(AlbumViewActivity.this, new ArrayList<>());
//            SharedPrefrance.setFavouriteFileList(AlbumViewActivity.this, favFileList);

            if (from.exists()) {
                boolean isDelete = Utils.delete(ViewImageActivity.this, from);
            }

            scanPhoto(to.toString());

            list.remove(position);
            if (position == list.size() - 1) {
                onBackPressed();
            } else {
                imageAdapter = new ShowImageAdapter(this, list, mImagePager);
                mImagePager.setAdapter(imageAdapter);
                mImagePager.setCurrentItem(position + 1);
            }
            if (list.size() == 0)
                onBackPressed();
            scanPhoto(to.toString());
            Utils.IsUpdate = true;
            Toasty.success(ViewImageActivity.this, "Image moved suceesfully!!!", Toast.LENGTH_SHORT).show();
            fireAnalytics("ViewImageActivity", "Move Image");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            } else {
                startService(new Intent(ViewImageActivity.this, GetFileList.class).putExtra("action", "Photos"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.error(ViewImageActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
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
        File file = new File(list.get(mImagePager.getCurrentItem()));
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = getSharedPreferences("Favourites_pref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
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
        fireAnalytics("ViewImageActivity", "Add to Favourite");
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


    long id = 0;
    Uri uri = null;
    List<Uri> images = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void renameFiles(Context context, File dirSrc) {
        IntentSender result = null;
        Log.e("dirSrc", dirSrc.getPath());
//        1
        String externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();
//        File dirSrc=new File(externalDir,context.getString(R.string.app_name));
        if (dirSrc.exists() || dirSrc.listFiles() != null) {
//            2
            String[] projection = {MediaStore.Images.Media._ID};
            String selection = MediaStore.Images.Media.DATA;
            String[] selectionArgs = {dirSrc.getPath()};
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection + "=?", selectionArgs, null);

//            3
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    id = Long.parseLong(cursor.getString(idIndex));
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    images.add(uri);
                }
                cursor.close();
            }

//            4
            List<Uri> uris = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                if (context.checkUriPermission(images.get(i),
                        Binder.getCallingPid(), Binder.getCallingUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager
                        .PERMISSION_GRANTED) {
                    uris.add(images.get(i));
                }
            }

            Log.e("uris granted:", String.valueOf(uris.size()));

//            5

//            if (!uris.isEmpty()) {
//                result = MediaStore.createWriteRequest(context.getContentResolver(), uris).getIntentSender();
//            }
//
////              6
//
//            String relativeLocation = Environment.DIRECTORY_DCIM + File.separator + "AppFolder";
//            //7
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation);
//
//            try {
//                for (int i = 0; i < images.size(); i++) {
//                    cr.update(images.get(i), contentValues, null);
//                }
//            } catch (Exception e) {
//                Log.e("Error:", e.getMessage());
//            }

//*******************************************

            PendingIntent pi = MediaStore.createWriteRequest(getContentResolver(), uris);
            try {
                startIntentSenderForResult(pi.getIntentSender(), RENAME_REQUEST, null, 0, 0,
                        0);
            } catch (IntentSender.SendIntentException e) {
            }

        }

    }

}