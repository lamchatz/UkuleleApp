package com.example.ukeleleapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ukuleleApp.R;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


     private EditText text,artTex;
     private String song,artist,address;
     protected ArrayList<String> listPdfs;

    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected MenuItem loadB,requestSongButton,addSongButton,openWebsiteButton,darkModeButton,keepPhoneOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        listPdfs=new ArrayList<>();

        initDir();
        initNavigationDrawer();
        initMenuItems();


        final Button searchButton=findViewById(R.id.SearchButton);
        text=findViewById(R.id.SongName);
        artTex=findViewById(R.id.ArtistText);

        text.setInputType(InputType.TYPE_CLASS_TEXT);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAction();
            }
        });
    }

    /**
     * Open Chords_Activity,where the chords and lyrics of the selected song will appear
     * @param ad Ukutabs' website for the selected song
     */
    private void openChords_Activity(String ad){
        Intent intent=new Intent(this, Chords_Activity.class);
        intent.putExtra("Address",ad);
        String t="";
        intent.putExtra("Transpose",t);    //set as "" as default,the user decides later
        intent.putExtra("SongName",song);
        intent.putExtra("Artist",artist);
        intent.putExtra("Restarted",false);
        startActivity(intent);

    }

    /**
        calls "isApplicationSentToBackground" function to check whether the app was sent to
        background or not(pressed home). If not,it resets editexts' fields.
     */
    @Override
    public void onPause() {
        super.onPause();

        if (!isApplicationSentToBackground(this)){
            text.setText("");
            artTex.setText("");
            drawerLayout.closeDrawers();
            listPdfs();
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        listPdfs();

    }

    /**
     * Check if the app is sent to background,i.e. home button was pressed
    */
    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize the directory /PDFs to store the saved songs,if it doesn't exist
     */
    protected void initDir(){
        String folder_main = "PDFs";
        String path = getExternalFilesDir(null).toString()  ;
        File f = new File(path, folder_main);
        if (!f.exists()){
            f.mkdirs();
        }
    }

    /**
     * Checks internet connection and the input of EditText song,artist.If there is connection,we
     * proceed with check the input.If there is a blank field,a Toast message appears and we don't continue to
     * choords activity. Same thing if there is no internet connection.
     */
    protected void checkInternetAndInput(){
        boolean connected = false;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Check Internet Connection~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else{
            connected = false;
        }
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~C

        if (connected){
            if((!song.equals("")) && (!artist.equals(""))){
                //All fields are completed,continue.
                openChords_Activity(address);
            }else{
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Show Toast messages based on the fields~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (song.equals("") && (!artist.equals(""))){
                    Toast toast=Toast.makeText(getApplicationContext(),"Please input the song's name",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (artist.equals("") && (!song.equals(""))){
                    openSongSearchActivity();
                }else if (song.equals("") && artist.equals("")){
                    Toast toast=Toast.makeText(getApplicationContext(),"Please input the fields",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }else{
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Show Toast message if not connected on the interner~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            Toast toast=Toast.makeText(getApplicationContext(),"Check Internet Connection",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Determines what happens when the search button is clicked.
     */
    protected void clickAction(){
        song=text.getText().toString();
        artist=artTex.getText().toString();

        if((!song.equals("")) && (!artist.equals(""))){
            artist=artist.replace(' ','-');
            song=song.replace(' ','-');
            song=song.replace("'","");
            address= "https://ukutabs.com/" + artist.charAt(0) + "/"+artist+"/"+song +"/";
        }

        checkInternetAndInput();
    }

    public void listPdfs(){
        String path = getExternalFilesDir(null) + "/PDFs/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        Menu menu=navigationView.getMenu().findItem(R.id.Load2).getSubMenu();
        menu.clear();
        listPdfs.clear();
        int fileLength=files.length;

        for (int i = 0; i < fileLength; i++)
        {
            menu.add(R.id.Load2,(i+1),i,files[i].getName().replace(".pdf",""));
            listPdfs.add(files[i].getName().replace(".pdf",""));
        }
    }

    protected void initNavigationDrawer(){
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView=findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout=findViewById(R.id.drawer2);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,(R.string.open),  (R.string.close));

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

    }

    /**
     * Initialize navigationDrawer's menu items.
     */
    protected void initMenuItems(){
        requestSongButton=navigationView.getMenu().findItem(R.id.requestButton_main);
        addSongButton=navigationView.getMenu().findItem(R.id.addSongButton_main);
        openWebsiteButton=navigationView.getMenu().findItem(R.id.openWebsite_main);
        loadB=navigationView.getMenu().findItem(R.id.Load2);
        keepPhoneOn=navigationView.getMenu().findItem(R.id.keepPhoneOn_main);
        darkModeButton=navigationView.getMenu().findItem(R.id.DarkMode_main);

        //listPdfs();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==(loadB.getItemId())){
            //  DoNothing.
        }else if(item.getItemId()==openWebsiteButton.getItemId()){
            Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://ukutabs.com/"));
            startActivity(browserIntent);
        }else if(item.getItemId()==requestSongButton.getItemId()){
            Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://ukutabs.com/request-songs/"));
            startActivity(browserIntent);
        }
        else if (item.getItemId()==addSongButton.getItemId()){
            Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://ukutabs.com/submit-songs/"));
            startActivity(browserIntent);
        }else if (item.getItemId()==darkModeButton.getItemId()){
            //doNothing; PROS TO PARON
        }else if (item.getItemId()==keepPhoneOn.getItemId()){
            //doNothing PROS TO PARON
        }
        else{
            String t=item.getTitle().toString();
            Intent intent=new Intent(this, Chords_Activity.class);
            intent.putExtra("ToLoad",true);
            intent.putExtra("Song",t);

            Toast toast=Toast.makeText(this,"Loading " + t,Toast.LENGTH_SHORT);
            toast.show();
            startActivity(intent);
        }

        return true;
    }

    private void openSongSearchActivity(){
        Intent intent=new Intent(this,SongSearchActivity.class);
        System.out.println("~~`~~~~~`````  " + song);
        intent.putExtra("Song",song);
        startActivity(intent);
    }
}
//Obligatory field