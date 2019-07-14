package com.yupa.stuffshare.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.mindorks.placeholderview.annotations.Animate;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.yupa.stuffshare.R;


@Animate(Animate.CARD_LEFT_IN_DESC)
@NonReusable
@Layout(R.layout.gallery_item)
public class GalleryItem {

    @View(R.id.ivPhoto)
    protected ImageView imageView;

    protected Bitmap mBitmap;

    public GalleryItem(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Resolve
    protected void onResolved() {
        imageView.setImageBitmap(mBitmap);
    }
}