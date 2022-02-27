package com.example.ukeleleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ukuleleApp.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SongSearchActivity extends AppCompatActivity {

    public static ArrayList<String> links,images;
    private String song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_search_activity);
        song="";
        song=getIntent().getExtras().getString("Song");
        initLists();

    }

    protected void initLists(){
       new connect().execute();
    }

    public class connect extends AsyncTask<Void, Void, Void>{
        public Document doc;
        @Override
        protected Void doInBackground(Void... voids) {
            String address="https://ukutabs.com/?s=";

            song=song.replace(" ","+");

            address=address+song;
            links=new ArrayList<>();
            images=new ArrayList<>();
            Document doc= null;
            boolean flag=false;
            try {
                doc = Jsoup.connect(address).get();
                Elements hrefList=doc.select("ul.archivelist").first().select("a[href]");
                Elements imagesList=doc.select("ul.archivelist").first().select("img");

                for (Element aref : hrefList){
                    flag=false;
                    String[] temp=aref.toString().split(" title");
                    String[] temp2=temp[0].split("=\"");
                    temp2=temp2[1].split("\"");

                    if (temp[1].contains("<img")){
                        flag=true;
                    }

                    if (!links.contains(temp2[0])){
                        if (!flag){
                            links.add(temp2[0]+ " ! !");
                        }else{
                            links.add(temp2[0]);
                        }
                    }
                }
                int c=0;
                while (c<imagesList.size()){
                    Element image=imagesList.get(c);
                    String[] temp = image.toString().split("src=\"");
                    String[] temp2 = temp[1].split("\"");
                    if (links.get(c).contains(" ! !")){
                        images.add("white");
                        links.set(c,links.get(c).replace(" ! !",""));
                        continue;
                    }else{
                        images.add(temp2[0]);
                        c++;
                    }
                }
                for (String string : images){
                    System.out.println(string);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ListView main_list_view=findViewById(R.id.main_list);

            final ArrayList<CustomList> list=new ArrayList<>();

            for (int i=0;i<links.size();i++){
                String temp[]=links.get(i).split("/");
                String art=temp[4].replace("-"," ");
                String title=temp[5].replace("-"," ");

                list.add(new CustomList(art,title,links.get(i),images.get(i)));
            }

            CustomArrayAdapter arrayAdapter=new CustomArrayAdapter(getApplicationContext(),0,list);

            main_list_view.setAdapter(arrayAdapter);
            main_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (connectedToInternet()){
                        startChordsActivity(position,list);
                    }else{
                        Toast.makeText(SongSearchActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        protected void startChordsActivity(int position,ArrayList<CustomList> list){
            String l=list.get(position).getLink();
            Intent intent=new Intent(getApplicationContext(), Chords_Activity.class);
            intent.putExtra("Address",list.get(position).getLink());
            String t="";
            intent.putExtra("Transpose",t);    //set as "" as default,the user decides later
            song=song.replace("+","_");
            intent.putExtra("SongName",song);
            intent.putExtra("Artist",list.get(position).getArtist());
            intent.putExtra("Restarted",false);
            startActivity(intent);
        }
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

}