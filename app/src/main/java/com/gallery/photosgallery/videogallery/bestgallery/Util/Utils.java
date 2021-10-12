package com.gallery.photosgallery.videogallery.bestgallery.Util;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;


import com.gallery.photosgallery.videogallery.bestgallery.Activity.ViewImageActivity;
import com.gallery.photosgallery.videogallery.bestgallery.Model.BaseModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class Utils{

    public static ArrayList<BaseModel> mFolderDialogList = new ArrayList<>();
    public static ArrayList<BaseModel> mFolderVideoDialogList = new ArrayList<>();
    public static String VIEW_TYPE="Photos";
    public static int DELETE_REQUEST=100;
    public static int RENAME_REQUEST=101;

    public static boolean IsUpdate=false;
    public static File mOriginalFile;
    public static String mEditpath;
    public static Bitmap mOriginalBitmap;
    public static Bitmap mEditedBitmap;
    public static Uri mEditedURI;
    public static boolean IsFramed=false;
    public static boolean IsCropped=false;
    public static Context c;
    public static int mDoodleColor= Color.BLACK;
    public static final String LOGIN_PREFERENCE ="Gallery_3" ;

    public static int GetRandomNumber(){
        int flag=0;
        flag=new Random().nextInt(1000);
        return flag;
    }

    public static String getSize(long size) {
        long kilo = 1024;
        long mega = kilo * kilo;
        long giga = mega * kilo;
        long tera = giga * kilo;
        String s = "";
        double kb = (double)size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;
        if(size < kilo) {
            s = size + " Bytes";
        } else if(size >= kilo && size < mega) {
            s =  String.format("%.2f", kb) + " KB";
        } else if(size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if(size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if(size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }

    public static String convertTimeFromUnixTimeStamp(String date) {

        try {
            DateFormat inputFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss zz yyy");
            Date d = null;
            try {
                d = inputFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // DateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy");

            Locale loc = new Locale("en", "US");
            DateFormat outputFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
            return outputFormat.format(d);
        }catch (Exception e){
            return "not vaialble";
        }
    }

    public static boolean delete(final Context context,final File file) {

        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{
                file.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        contentResolver.delete(filesUri, where, selectionArgs);
//        contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);

        if (file.exists()) {
            contentResolver.delete(filesUri, where, selectionArgs);
        }
        return !file.exists();
    }



    public static String getCurrentDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }
    public static long getFilePathToMediaID(String songPath, Context context)
    {
        long id = 0;
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Images.Media.DATA;
        String[] selectionArgs = {songPath};
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
        }

        return id;
    }

    public static File CaptureImage(Bitmap bitmap,Context context) {
        c=context;

        File f = null;
        try {

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            //----------------dsestination path--------
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/"+"Editer");
            if(!myDir.exists())
                myDir.mkdirs();
//            String ParentPath= Utils.mOriginalFile.getParentFile().getPath();
////            Log.e("Parent path:",ParentPath + "*");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Uri destination = Uri.fromFile(new File(myDir.getPath() + "/" + timeStamp + ".png"));
            String NewImagePath=destination.getPath();
//            Log.e("New path:",NewImagePath + "*");
            //-----------------------------------------
            if (NewImagePath != null) {
                f = new File(NewImagePath);
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //------------insert into media list----
                File newfilee = new File(destination.getPath());
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, destination.getPath());
                values.put(MediaStore.Images.Media.DATE_TAKEN, newfilee.lastModified());
                scanPhoto(newfilee.getPath());
                Toasty.success(c, "Image saved successfully!!!", Toast.LENGTH_LONG).show();
                Uri uri1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri1 = FileProvider.getUriForFile(c.getApplicationContext(), c.getPackageName() + ".provider", newfilee);
                } else {
                    uri1 = Uri.fromFile(newfilee);
                }
                c.getContentResolver().notifyChange(uri1, null);
                Utils.IsUpdate=true;
                ViewImageActivity.list.add(0,NewImagePath);

            }
        } catch (Exception e) {
        }
        return f;

    }

    public static MediaScannerConnection msConn;
    public static void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(c, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
//                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
//                Log.i("msClient obj", "scan completed");
            }
        });
        msConn.connect();
    }

}
