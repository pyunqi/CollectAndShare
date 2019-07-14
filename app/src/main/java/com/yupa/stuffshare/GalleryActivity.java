package com.yupa.stuffshare;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mindorks.placeholderview.PlaceHolderView;
import com.yupa.stuffshare.db.Stuff;
import com.yupa.stuffshare.fragments.AboutCASFragment;
import com.yupa.stuffshare.stuff.StuffManagement;
import com.yupa.stuffshare.utils.GalleryItem;
import com.yupa.stuffshare.utils.ImageHelper;

public class GalleryActivity extends AppCompatActivity implements AboutCASFragment.OnFragmentInteractionListener {

    private PlaceHolderView mGalleryView;
    private Handler handler = new Handler();
    AboutCASFragment casFragment  = new AboutCASFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

//        //add tool bar
        Toolbar myToolbar = findViewById(R.id.gallery_toolbar);
        myToolbar.setTitle("C & S Stuff Gallery");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:

                if (getSupportFragmentManager().findFragmentById(R.id.g_about) == null) {
                    getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .add(R.id.g_about, casFragment, "About").commit();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread loadGallery = new Thread(new LoadGallery());
        loadGallery.start();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * load pics
     */
    private class LoadGallery implements Runnable {

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryView = findViewById(R.id.galleryView);
                    for (Stuff s : StuffManagement.getStuffs(GalleryActivity.this)) {
                        mGalleryView.addView(new GalleryItem(ImageHelper.decodeSampledBitmapFromFile(s.get_picture(), 200, 350)));

                    }
                }
            });

        }
    }
}
