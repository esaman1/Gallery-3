package com.gallery.photosgallery.videogallery.bestgallery.animation;

import android.view.View;


public class StackPageTransformer extends BasePageTransformer {

    @Override
    public void handleInvisiblePage(View view,float position) {
    }

    @Override
    public void handleLeftPage(View view,float position) {
    }

    @Override
    public void handleRightPage(View view,float position) {
        view.setTranslationX(-view.getWidth() * position);
    }

}