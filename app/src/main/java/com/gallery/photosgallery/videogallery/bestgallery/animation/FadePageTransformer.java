package com.gallery.photosgallery.videogallery.bestgallery.animation;

import android.view.View;


public class FadePageTransformer extends BasePageTransformer {

    @Override
    public void handleInvisiblePage(View view,float position) {
    }

    @Override
    public void handleLeftPage(View view,float position) {
        view.setTranslationX(-view.getWidth() * position);
        view.setAlpha(1 + position);
    }

    @Override
    public void handleRightPage(View view,float position) {
        view.setTranslationX(-view.getWidth() * position);
        view.setAlpha(1 - position);
    }

}