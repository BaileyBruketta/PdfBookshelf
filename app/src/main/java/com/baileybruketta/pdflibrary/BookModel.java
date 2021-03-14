package com.baileybruketta.pdflibrary;

public class BookModel {
    String Title;
    String Author;
    String Genre;
    String Path;
    Integer CurrentPage;
    String ImagePath;

    public String getTitle(){
        return Title;
    }
    public String getAuthor(){
        return Author;
    }
    public String getGenre(){
        return Genre;
    }
    public String getPath(){
        return Path;
    }
    public Integer getCurrentPage(){
        return CurrentPage;
    }
    public String getImagePath(){ return ImagePath;}

    public void setTitle(String title){
        this.Title = title;
    }
    public void setAuthor(String author){
        this.Author = author;
    }
    public void setGenre(String genre){
        this.Genre = genre;
    }
    public void setPath(String path){
        this.Path = path;
    }
    public void setCurrentPage(Integer page){
        this.CurrentPage = page;
    }
    public void setImagePath(String pa){this.ImagePath = pa;}
}
