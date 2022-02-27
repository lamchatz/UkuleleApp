package com.example.ukeleleapp;

public class CustomList  {

    private String content,link,url,artist;

    public CustomList(String art,String song,String l,String im){
        artist=art;
        content=artist + " >> " +song;
        link=l;
        url=im;
    }

    public String getArtist(){
        return artist;
    }
    public String getContent(){
        return content;
    }
    public String getLink(){
        return link;
    }

    public String getUrl() {
        return url;
    }
}
