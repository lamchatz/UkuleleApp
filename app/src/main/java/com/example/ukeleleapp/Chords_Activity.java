package com.example.ukeleleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SymbolTable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ukuleleApp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * The main activity,content base.It displays the song and chords and has navigationDrawer which
 * includes transpose,darkMode and the saved tabs.
 */
public class Chords_Activity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private TextView chordsView;
    private String address,song,songName,artist;
    private String transpose,path,transposeInt,filepath,songPath;
    private Document doc;
    protected int tr;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    protected FloatingActionButton floatingActionButton;
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected Spinner spinner;
    protected MenuItem saveB,loadB,requestSongButton,addSongButton,openWebsiteButton,darkMode,keepPhoneOn;
    protected int counter=0;
    protected ArrayList<String> listPdfs;
    protected boolean toLoad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chords_);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toLoad=getIntent().getBooleanExtra("ToLoad",false);
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        initStrings();
        initNavigationDrawer();
        initMenuItems();
        listPdfs();
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        floatingActionButton=findViewById(R.id.saveOfflineButton);
        chordsView=findViewById(R.id.Chords);
        chordsView.setMovementMethod(new ScrollingMovementMethod());

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n=0;
                if (!transposeInt.equals("")){
                    n=Integer.parseInt(transposeInt);
                }
                if (n!=0){
                    songPath= songName.replace(transposeInt,"") +transposeInt + "_by_" + artist ;
                }else {
                    songPath= songName + "_by_" + artist  ;
                }

                if (listPdfs.contains(songPath)){
                    if (deleteFile(songPath)){
                        floatingActionButton.setImageResource(R.drawable.baseline_favorite_black_24);
                    }
                }else{
                    floatingActionButton.setImageResource(R.drawable.baseline_favorite_border_black_24);
                    createMyPdf();
                }
            }
        });

        if (toLoad){
            loadPDF(getIntent().getStringExtra("Song"));

        }else{
            new doit().execute();

        }

        songPath=songName.replace(transposeInt,"") +transposeInt + "_by_" + artist ;
        if (listPdfs.contains(songPath)){
            floatingActionButton.setImageResource(R.drawable.baseline_favorite_border_black_24);

        }
        else{
            floatingActionButton.setImageResource(R.drawable.baseline_favorite_black_24);
        }
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        print();
    }

    /**
     *     Define actions for navigationDrawer's components
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==spinner.getId()){
            spinner.performClick();
        } else if (item.getItemId()==(loadB.getItemId())){
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
        }else if (item.getItemId()==darkMode.getItemId()){
            //doNothing;
        }else if (item.getItemId()==keepPhoneOn.getItemId()){
            //doNothing;
        }
        else{
            String t=item.getTitle().toString();;
            loadPDF(t);
        }
        return true;
    }

/**
 * Makes the transpose options appear and if a number is selected,the activity is restarted
 * based on that number
 */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (++counter>1){
            String s=parent.getItemAtPosition(position).toString();
            if (!s.equals("Pick Half a Step")){
               if (connectedToInternet()){

                   Toast toast;
                   tr=Integer.parseInt(s);

                   transpose="?transpose=";
                   songName=songName.replace(transposeInt,"");
                   if (tr>0){
                       transpose+="+" + tr + "#point";
                       toast=Toast.makeText(this, "Transposing " +songName + " to +" + tr, Toast.LENGTH_SHORT);
                   }
                   else{
                       transpose=transpose + tr + "#point";
                       toast=Toast.makeText(this, "Transposing " +songName + " to " + tr, Toast.LENGTH_SHORT);
                   }

                   address=address+transpose;
                   Intent intent=getIntent();
                   finish();
                   intent.putExtra("Address",address);
                   intent.putExtra("Transpose",transpose);
                   intent.putExtra("Artist",artist);
                   intent.putExtra("SongName",songName);
                   intent.putExtra("ToLoad",false);

                   songPath= songName + "_by_" + artist;
                   if (listPdfs.contains(songPath)){
                       floatingActionButton.setImageResource(R.drawable.baseline_favorite_border_black_24);

                   }
                   else{
                       floatingActionButton.setImageResource(R.drawable.baseline_favorite_black_24);
                   }


                   toast.show();

                   startActivity(intent);
               }
               else{
                  Toast toast=Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_SHORT);
                  toast.show();
               }
            }
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     *     Get the html string from the given address and
     * split it into lines in order to change color every other line(highlighting the chords)
     * and then putting them all on a string which is passed to the TextView.
     */

    public class doit extends AsyncTask<Void, Void, Void> {
        Elements el;

        @Override
        protected Void doInBackground(Void... voids) {

            doc = null;
            try {
                doc = Jsoup.connect(address).get();
                el=doc.select("pre");
               // el = doc.select("pre");
                song = el.text();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (doc!=null){
                String finalText=mod(song);
                chordsView.setText(Html.fromHtml(finalText));
            }
            else{
                Intent intent=new Intent(Chords_Activity.this,MainActivity.class);
                Toast toast=Toast.makeText(getApplicationContext(),"Could not find your requested song\nCheck your spelling",Toast.LENGTH_LONG);
                toast.show();
                startActivity(intent);

            }

        }

    }

    /**
     * Creates a list of the available pdfs in the /PDFs directory
     * and adds it at the navigationDrawer menu.
     */
    public void listPdfs(){
        File directory = new File(path);
        File[] files = directory.listFiles();
        Menu menu=navigationView.getMenu().findItem(R.id.Load).getSubMenu();
        int fileLength=files.length;

        for (int i = 0; i < fileLength; i++)
        {
            menu.add(R.id.Load,(i+1),i,files[i].getName().replace(".pdf",""));
            listPdfs.add(files[i].getName().replace(".pdf",""));

        }
    }

    /**
     * Called whenever the user saves a tab.
     * Updates the list with available pdfs
     */
    public void update(){
        int n=0;
        String nm;
        if (!transposeInt.equals("")){
            n=Integer.parseInt(transposeInt);
        }
        if (n!=0){
            nm=songName.replace(transposeInt,"") + transposeInt + "_by_" + artist ;
        }else {
            nm=songName + "_by_" + artist ;

        }


       Menu menu=navigationView.getMenu().findItem(R.id.Load).getSubMenu();
        menu.add(R.id.Load,menu.size()+1,0,nm);
       Toast toast= Toast.makeText(this, nm +" was added for offline use.", Toast.LENGTH_SHORT);
       toast.show();
        listPdfs.add(nm);
    }


    /**
     *  Mods the string in order to highlight the chords and Verses,etc.
     *  returns the modded string
     */
    public String mod(String old) {
        old = old.replace(" ", "&nbsp;&nbsp;");
        String[] lines = old.split("\\n");
        String t = "";
        String br = "<br>";
        boolean flag = true;

        for (int i = 0; i < lines.length; i++) {
            String s="";
            t = lines[i];
            if (t.equals("&nbsp;&nbsp;\r") || t.equals("\r")){
                //doNothing
                s=br;
                lines[i]=s;
                continue;
            }

           if (t.contains("--")){
               s = br + t;
               lines[i] = s;
           }
           else{
               if (t.contains("Verse") || t.contains("Intro") || t.contains("Chorus") || t.contains("Bridge") || t.contains("Interlude")) {
                    s= br + "<b>" + t + "</b>";
                   lines[i] = s;
                   flag = true;
               } else {
                   if (flag) {
                       s = br + "<span style='color:#FFD732'>" + t + "</span>";
                       lines[i] = s;
                       flag = !flag;
                   } else {
                       flag = !flag;
                       s = br + t;
                       lines[i] = s;
                   }
               }
           }

        }
        String finalText = "";
        for (int i = 0; i < lines.length; i++) {
            finalText = finalText.concat(lines[i]);
        }
        finalText+="<br> <br> <br><br><br>";
        return finalText;
    }

    /**
     * creates the pdf file of the selected song in a similar fashion as the mod function.
     */
    protected void createMyPdf(){
        String[] songLines=song.split("\\n");

        PdfDocument pdf=new PdfDocument();
        PdfDocument.PageInfo pf=new PdfDocument.PageInfo.Builder(300,songLines.length*21,1).create();
        PdfDocument.Page page=pdf.startPage(pf);

        Paint paint=new Paint();

        boolean flag=false;
        int y=50;
        for (int i=0;i<songLines.length;i++){
            String t=songLines[i];
            if (t.contains("Verse") || t.contains("Intro") || t.contains("Chorus") || t.contains("Bridge")) {
                page.getCanvas().drawText(t,24,y,paint);
                y+=paint.descent()-paint.ascent();
                page.getCanvas().drawText("",24,y,paint);
                y+=paint.descent()-paint.ascent();
                flag = true;
            } else {
                if (flag) {
                    page.getCanvas().drawText(t,24,y,paint);
                    y+=10;
                    flag = !flag;
                } else {
                    page.getCanvas().drawText(t,24,y,paint);
                    y+=paint.descent()-paint.ascent();
                    page.getCanvas().drawText("",24,y,paint);
                    y+=paint.descent()-paint.ascent();
                    flag=!flag;
                }
            }

        }
        pdf.finishPage(page);
        File file=new File(filepath);


        if (file.exists()){
            //do nothing; It is basically unnecessary because if the file exists,the function won't be called
            // but it's a failsafe,if something bugs :).
        }
        else {
            try{
                pdf.writeTo(new FileOutputStream(file));
            }catch (Exception e){
                e.printStackTrace();
            }
            update();
        }

        pdf.close();

    }

    /**
     * Loads on screen the selected pdf file.First it parses the whole text,then
     * uses the mod function for the end result.
     *
     * @param name name of the selected song
     */
    public void loadPDF(String name){
        String  fp= path + name + ".pdf" ;
        //fp=path+ "take_me_to_church+3_by_hozier.pdf" for example.
        String parsedText="";
        try {

            PdfReader reader = new PdfReader(fp);
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(reader, i+1).trim()+"\n"; //Extracting the content from the different pages
            }
            reader.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        reloadStrings(name);
        song=parsedText;
        if (!toLoad){
            Toast.makeText(this, "Loading "+songName, Toast.LENGTH_SHORT).show();
        }

        chordsView.setText(Html.fromHtml(mod(parsedText)));
        drawerLayout.closeDrawers();
    }

    /**
     * Deletes the pdf file.
     * @param name name of the file to be deleted
     * @return true if file was deleted,false if not.
     */
    public boolean deleteFile(String name){
        String  fp= path+name;

        Iterator itr = listPdfs.iterator();
        while (itr.hasNext())
        {
            String toDel= (String) itr.next();
            if (toDel.equals(name  )) {
                itr.remove();
                break;
            }
        }

        File file=new File(fp+".pdf");
        if (file.exists()){
            file.delete();
            Toast toast=Toast.makeText(this, name+" was deleted", Toast.LENGTH_SHORT);
            toast.show();
            Menu menu=navigationView.getMenu().findItem(R.id.Load).getSubMenu();

            for (int i=0;i<menu.size();i++){
                String m=menu.getItem(i).getTitle().toString();
                if (m.equals(name)){
                    menu.removeItem(menu.getItem(i).getItemId());
                    break;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Initialize all the important strings that will be used later.
     */
    protected void initStrings(){
        path = getExternalFilesDir(null) + "/PDFs/";


       if (!toLoad){
           songName=getIntent().getStringExtra("SongName");
           transpose=getIntent().getStringExtra("Transpose");
           address=getIntent().getStringExtra("Address");
           artist=getIntent().getStringExtra("Artist");

           path = getExternalFilesDir(null) + "/PDFs/";

           songName=songName.replace("-","_");
           songName=songName.replace(" ", "");

           String temp[]={"",""};
           if (!transpose.equals("")){
               temp=transpose.split("=");
               temp=temp[1].split("#");
           }
           transposeInt=temp[0];

           filepath=songName;

           int n=0;
           if (!transposeInt.equals("")){
               n=Integer.parseInt(transposeInt);
           }

           if (n!=0){
               filepath=filepath.replace(transposeInt,"");
               filepath=filepath+transposeInt;
           }
           filepath=path + filepath + "_by_" + artist + ".pdf";


           String[] tempAd=address.split("\\?");
           address=tempAd[0];

           address+=transpose;

       }/*else{
           songName="";
           transpose="";
           address="";
           artist="";
       } */


        listPdfs=new ArrayList<>();
    }

    /**
     * Initialize the navigationDrawer and its components.
     */
    protected void initNavigationDrawer(){
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView=findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout=findViewById(R.id.drawer);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,(R.string.open),  (R.string.close));

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

    }

    /**
     * Initialize navigationDrawer's menu items.
     */
    protected void initMenuItems(){
        darkMode=findViewById(R.id.DarkMode);
        keepPhoneOn=findViewById(R.id.keepPhoneOn);
        requestSongButton=navigationView.getMenu().findItem(R.id.requestButton);
        addSongButton=navigationView.getMenu().findItem(R.id.addSongButton);
        openWebsiteButton=navigationView.getMenu().findItem(R.id.openWebsite);
        loadB=navigationView.getMenu().findItem(R.id.Load);
        spinner = (Spinner) navigationView.getMenu().findItem(R.id.spinner).getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transpose_buttons, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    /**
     * called when the user selects to load a song.First it sets the correct
     * values to artist,transposeInt and songName by deconstructing the newSongPath parameter.
     * Then the address.
     * @param newSongPath The name of the loaded song (i.e "take_me_to_church+3_by_hozier.pdf")
     */
    protected void reloadStrings(String newSongPath){
      //songName,artist,transposeInt.
        String[] temp=newSongPath.split("_by_");
        String t="";
        artist=temp[1].replace(".pdf","");
        for (int i=0;i<2;i++){
            t=t+temp[0].charAt(temp[0].length()-(i+1));
        }
        char c=t.charAt(0);
        if (Character.isDigit(c)){
            int n=Integer.parseInt(c + "");
            if (n==0){
                temp[0]=temp[0].replace(c+"","");
                songName=temp[0];
                transposeInt="";
                transpose="";

            }else{
                transposeInt="" +t.charAt(1) + t.charAt(0);
                transpose="?transpose=" + transposeInt +"#point";
                songName=temp[0];

            }
        }else{
            transposeInt="";
            songName=temp[0];
        }



        artist=artist.replace(' ','-');
        filepath=songName;
        songName=songName.replace("_", "-");
        address="https://ukutabs.com/" + artist.charAt(0) + "/" + artist + "/" + songName.replace(transposeInt,"") + "/" +transpose;



        int n=0;
        if (!transposeInt.equals("")){
            n=Integer.parseInt(transposeInt);
        }

        if (n!=0){
            filepath=filepath.replace(transposeInt,"");
            filepath=filepath+transposeInt;
        }
        filepath=path + filepath + "_by_" + artist + ".pdf";

        floatingActionButton.setImageResource(R.drawable.baseline_favorite_border_black_24);

    }

    protected void restart(String ad,String transp,String name){
        Intent intent=getIntent();
        finish();
        intent.putExtra("Address",ad);
        intent.putExtra("Transpose",transp);
        intent.putExtra("Restarted",true);
        intent.putExtra("SongName",name);

        startActivity(intent);
    }

    protected boolean connectedToInternet(){
        boolean connected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else{
            connected = false;
        }

        return connected;
    }

    public void print(){
        System.out.println("~~~~~~~~~~~~~~~~~~~");
        System.out.println("  songName = " + songName);
        System.out.println("  address = " + address);
        System.out.println("  artist = " + artist);
        System.out.println("  transposeInt = " + transposeInt);
        System.out.println("  filepath = " + filepath);
        System.out.println("  path = " + path);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

}



/*

 */