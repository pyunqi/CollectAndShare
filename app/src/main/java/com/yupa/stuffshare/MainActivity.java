package com.yupa.stuffshare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.yupa.stuffshare.entity.Stuff;
import com.yupa.stuffshare.fragments.AboutCASFragment;
import com.yupa.stuffshare.service.StuffLocalService;
import com.yupa.stuffshare.service.StuffWebservice;
import com.yupa.stuffshare.utils.ShowDialog;
import com.yupa.stuffshare.utils.ShowMessage;
import com.yupa.stuffshare.utils.StuffAdapter;

import java.io.File;

public class MainActivity extends AppCompatActivity implements AboutCASFragment.OnFragmentInteractionListener {

    private Handler handler = new Handler();
    AboutCASFragment casFragment = AboutCASFragment.newInstance();
    final static String imageBaseUrl = "http://yupa399.co.nf/showImage.php?pic=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.cas_toolbar);
        setSupportActionBar(myToolbar);

        //go add new stuff
        Button addStuff = findViewById(R.id.btnAdd);
        addStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    ShowDialog.show(MainActivity.this, "Network issue", "Please Connect to Internet first!");
                    return;
                }
                addStuff();
            }
        });

        //go show stuff pics
        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Fuzzy Searching by stuff name")
                        .setMessage("Input stuff name")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String stuffName = input.getText().toString();
                                SearchStuffsByNameFromServer searchStuffsByNameFromServer = new SearchStuffsByNameFromServer();
                                searchStuffsByNameFromServer.execute(MainActivity.this.getExternalFilesDir(null).getAbsolutePath(),stuffName);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                }).show();
            }
        });

        FloatingActionButton btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    ShowDialog.show(MainActivity.this, "Network issue", "Please Connect to Internet first!");
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Sync up with remote server!")
                        .setMessage("It may take a long time and flow，Are You Sure ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SyncServerData syncServerData = new SyncServerData();
                                syncServerData.execute(MainActivity.this.getExternalFilesDir(null).getAbsolutePath());
                                Thread listStuffs = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
                                listStuffs.start();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * load stuffs thread
     */
    class ShowStuffsList implements Runnable {

        ListView lv;
        String sName = "";

        //prepare for sorting or searching
        ShowStuffsList(ListView v, String stuffName) {
            lv = v;
            sName = stuffName;
        }

        ShowStuffsList(ListView v) {
            lv = v;
        }

        @Override
        public void run() {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Create stuff adapter
                    final StuffAdapter stuffsAdapter;
                    if (sName.isEmpty()) {
                        stuffsAdapter = new StuffAdapter(MainActivity.this, StuffLocalService.getStuffs(MainActivity.this));
                    } else {
                        stuffsAdapter = new StuffAdapter(MainActivity.this, StuffLocalService.getStuffsByName(MainActivity.this, sName));
                    }
                    // Set the adapter
                    if (!stuffsAdapter.isEmpty()) {
                        lv.setAdapter(stuffsAdapter);
                        //listen Click stuff event
                        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                                //Creating the instance of PopupMenu
                                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater()
                                        .inflate(R.menu.popup_menu, popup.getMenu());

                                //registering popup with OnMenuItemClickListener
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.share:
                                                File fileLocation = new File(stuffsAdapter.getStuff(position).get_picture());
                                                Uri path = FileProvider.getUriForFile(MainActivity.this, "com.yupa.fileprovider", fileLocation);
                                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                                emailIntent.setType("vnd.android.cursor.dir/email");
                                                String to[] = {""};
                                                emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                                                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I would like Share Stuff: " + stuffsAdapter.getStuff(position).get_name());
                                                emailIntent.putExtra(Intent.EXTRA_TEXT, stuffsAdapter.getStuff(position).get_description());
                                                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                                return true;
                                            case R.id.facebook:
                                                String picaName = stuffsAdapter.getStuff(position).get_picture();
                                                picaName = picaName.substring(picaName.lastIndexOf("/") + 1, picaName.length());
                                                ShareLinkContent content = new ShareLinkContent.Builder()
                                                        .setContentUrl(Uri.parse(imageBaseUrl + picaName))
                                                        .setQuote("My wonderful Stuff.")
                                                        .build();
                                                ShareDialog.show(MainActivity.this, content);
                                                return true;
                                            case R.id.update:
                                                Intent intent = new Intent(MainActivity.this, EditStuffActivity.class);
                                                intent.putExtra("stuff", stuffsAdapter.getStuff(position));
                                                startActivity(intent);
                                                return true;
                                            case R.id.del:
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle("Delete Stuff.")
                                                        .setMessage("Are you sure you want to delete this stuff?")
                                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                StuffLocalService.deleteStuff(MainActivity.this,
                                                                        stuffsAdapter.getStuff(position).get_id(),
                                                                        stuffsAdapter.getStuff(position).get_picture());
                                                                Thread listStuffs = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
                                                                listStuffs.start();
                                                            }
                                                        })
                                                        .setNegativeButton(android.R.string.no, null)
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .show();
                                                return true;
                                            case R.id.delSync:
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle("Delete Stuff and Synchronize.")
                                                        .setMessage("This Action Will Delete Server Side Data Too, Are you sure?")
                                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                SyncDeleteData syncDeleteData = new SyncDeleteData();
                                                                syncDeleteData.execute(stuffsAdapter.getStuff(position));
                                                                Thread listStuffs = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
                                                                listStuffs.start();
                                                            }
                                                        })
                                                        .setNegativeButton(android.R.string.no, null)
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .show();
                                                return true;
                                        }
                                        return true;
                                    }
                                });

                                popup.show(); //showing popup menu

                                return false;
                            }
                        });
                    }
                }
            });

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Thread listStuffs = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
        listStuffs.start();


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

                if (getSupportFragmentManager().findFragmentById(R.id.m_about) == null) {
                    getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .add(R.id.m_about, casFragment, "About").commit();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        // Do NOT call super.onBackPressed()
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    /**
     * go to take stuff pic step
     */
    private void addStuff() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Synchronize data task
     */
    private class SyncServerData extends AsyncTask<String, Integer, String> {

        private ProgressDialog progressDialog;

        public SyncServerData() {
            progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Start to synchronize server data ...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... parameters) {
            String res = StuffWebservice.syncServer(MainActivity.this, parameters[0]);
            return res;
        }

        @Override
        protected void onPostExecute(String res) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if ("200".equals(res)) {
                ShowMessage.showCenter(MainActivity.this, "Synchronizing finished!");
                Thread showStuffsList = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
                showStuffsList.start();
            } else {
                ShowMessage.showCenter(MainActivity.this, "Synchronizing failed!");
                return;
            }
        }
    }

    /**
     * Search stuff and Synchronize data
     */
    private class SearchStuffsByNameFromServer extends AsyncTask<String, Integer, String> {

        private ProgressDialog progressDialog;
        private String stuffName;

        public SearchStuffsByNameFromServer() {
            progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Searching stuffs from server ...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... parameters) {
            stuffName = StuffWebservice.searchStuffByName(MainActivity.this,parameters[0], parameters[1]);
            return parameters[1];
        }

        @Override
        protected void onPostExecute(String res) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (res !=null) {
                ShowMessage.showCenter(MainActivity.this, "Searching finished!");
                Thread showStuffsList = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView),stuffName));
                showStuffsList.start();
            } else {
                ShowMessage.showCenter(MainActivity.this, "something wrong!");
                return;
            }
        }
    }

    /**
     * delete data task
     */
    private class SyncDeleteData extends AsyncTask<Stuff, Integer, String> {

        private ProgressDialog progressDialog;

        public SyncDeleteData() {
            progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Start to Delete data forever...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Stuff... parameters) {
            Stuff stuff = parameters[0];
            String res = StuffWebservice.deleteStuff(stuff);
            if ("200".equals(res)) {
                StuffLocalService.deleteStuff(MainActivity.this, stuff.get_id(), stuff.get_picture());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String res) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if ("200".equals(res)) {
                ShowMessage.showCenter(MainActivity.this, "Stuff Deleted!");
                Thread showStuffsList = new Thread(new ShowStuffsList((ListView) findViewById(R.id.listView)));
                showStuffsList.start();
            } else {
                ShowMessage.showCenter(MainActivity.this, "Deleting failed!");
                return;
            }
        }
    }

}
