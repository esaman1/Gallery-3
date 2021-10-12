package com.gallery.photosgallery.videogallery.bestgallery.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class BaseModel implements Serializable{
    boolean isDirectory = false;
    private String id;
    private String did;
    private String name, folderName;
    private String path;
    private long size;   //byte
    private String bucketId;  //Directory ID
    private String bucketName,bucketPath;  //Directory Name
    private String date;
    public ArrayList<String> pathlist;
    public int type;

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public ArrayList<String> getPathlist(){
        return pathlist;
    }

    public void setPathlist(ArrayList<String> pathlist){
        this.pathlist = pathlist;
    }

    public String getBucketPath(){
        return bucketPath;
    }

    public void setBucketPath(String bucketPath){
        this.bucketPath = bucketPath;
    }

    public boolean isDirectory(){
        return isDirectory;
    }

    public void setDirectory(boolean directory){
        isDirectory = directory;
    }

    public String getDid(){
        return did;
    }

    public void setDid(String did){
        this.did = did;
    }

    public String getFolderName(){
        return folderName;
    }

    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public long getSize(){
        return size;
    }

    public void setSize(long size){
        this.size = size;
    }

    public String getBucketId(){
        return bucketId;
    }

    public void setBucketId(String bucketId){
        this.bucketId = bucketId;
    }

    public String getBucketName(){
        return bucketName;
    }

    public void setBucketName(String bucketName){
        this.bucketName = bucketName;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

}
