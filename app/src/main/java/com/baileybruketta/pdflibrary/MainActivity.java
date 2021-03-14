package com.baileybruketta.pdflibrary;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.util.ArrayList;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private static final int REQUEST_CODE = 1;

    Integer pageNumber = 0;
    String pdfFileName;
    Uri currentlyReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //remove title bar
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //make translucent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //StatusBarUtil.setTranslucent(this);

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_main);



        Init();

    }

    public void SetupCards(){
        ArrayList<BookModel> books = getAllBooks();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_bookshelf);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new PaddingItemDecoration(5));

        BookModelAdapter adapter = new BookModelAdapter(this, R.layout.listview_bookshelf, books, this);
        recyclerView.setAdapter(adapter);
    }

    public void Init(){
        Button AddPdfButton = this.findViewById(R.id.addPdfbutton);
        AddPdfButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
               AddPdfButtonClicked();
           }
        });
        Button searchButton = this.findViewById(R.id.SearchButton);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        SetupCards();

    }

    public static void SearchButtonClicked(){

    }
    public void AddPdfButtonClicked(){

        String devManu = Build.MANUFACTURER;

        if (devManu.toLowerCase() == "samsung") {
            Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            intent.putExtra("CONTENT_TYPE", "*/*");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent result){
        Log.e("onActivityResult", "called");
        Toast.makeText(getApplicationContext(), "end file select", Toast.LENGTH_LONG);
        //if (requestCode == RESULT_OK){
        Uri data = result.getData();
           // if (data.getLastPathSegment().endsWith("pdf")){
        String pdfPath = data.getPath();
         //       Toast.makeText(getApplicationContext(), pdfPath, Toast.LENGTH_SHORT);
        Log.e("onActivityResult", pdfPath);
         //   }
       // }
        //implement save pdf location - form popup for add title and author


        //TEST
        //displayPdfFromUri(data);
        AddPdfToIndex(data);

    }

    public void RenderBookPreReadScreen(BookModel model){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.book_onclicked_form_popup, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(this.findViewById(R.id.addPdfbutton), Gravity.CENTER, 0, 0);

        TextView titleview = popupView.findViewById(R.id.title_afterclick);
        TextView authorview = popupView.findViewById(R.id.author_afterclick);
        Button readtextbutton = popupView.findViewById(R.id.readbutton_afterclick);
        Button cancelbutton = popupView.findViewById(R.id.cancelbutton_afterclick);

        //set view
        titleview.setText(model.getTitle());
        authorview.setText(model.getAuthor());

        //read text button
        readtextbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                displayPdfFromUri(Uri.parse(model.getPath()));
            }
        });

        //cancel clicked
        cancelbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

    }

    private void AddPdfToIndex(Uri uri){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pdf_save_form_popup, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(this.findViewById(R.id.addPdfbutton), Gravity.CENTER, 0, 0);

        EditText authorinput  = popupView.findViewById(R.id.authorinput);
        EditText titleinput   = popupView.findViewById(R.id.titleinput);
        EditText genreinput   = popupView.findViewById(R.id.genreinput);
        Button cancelbutton   = popupView.findViewById(R.id.cancelsavebutton);
        Button savebutton     = popupView.findViewById(R.id.savebutton);


        //cancel clicked
        cancelbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        //save clicked
        savebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                savepdfindatabase(uri, titleinput.getText().toString(), authorinput.getText().toString(), genreinput.getText().toString());
                popupWindow.dismiss();
                SetupCards();
            }
        });
    }

    private void savepdfindatabase(Uri uri, String title, String author, String genre){
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("path", uri.toString());
        values.put("title", title);
        values.put("author", author);
        values.put("genre", genre);
        values.put("currentpage", 0);

        //create table if not exists (ie first instance of app) - this should probably be moved somewhere else and checked with stored device preferences
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS library(path VARCHAR,title VARCHAR,author VARCHAR, genre VARCHAR, currentpage INT);");

        // see if document is already exist
        ArrayList<BookModel> bookArray = getAllBooks();
        if (SeeIfBookExists(bookArray, uri) == false){
            mydatabase.insert("library", null, values);
            Log.e("savepdfindatabase", "added book");
        }

        // to do - move file to a folder for documents

        //close db
        mydatabase.close();
    }

    public boolean SeeIfBookExists(ArrayList<BookModel> books, Uri documentpath){
        Log.e("SeeIfBookExists uri", documentpath.toString());
        boolean retval = false;
        for (int i = 0; i < books.size(); i++){
            Log.e("seeIfBookExists loop", books.get(i).getPath());

            if (books.get(i).getPath().equals(documentpath.toString())){
                Log.e("seeIfBookExists", "identical path found");
                retval = true;
            }
        }
        return retval;
    }

    public ArrayList<BookModel> getAllBooks(){
        ArrayList<BookModel> arrayList = new ArrayList<>();
        String select_query = "SELECT * FROM library";
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex", MODE_PRIVATE, null);
        Cursor cursor = mydatabase.rawQuery(select_query, null);
        if(cursor.moveToFirst()){
            do{ //path,title,author,genrecurrentpage
                BookModel bookModel = new BookModel();
                bookModel.setPath(cursor.getString(0));
                bookModel.setTitle(cursor.getString(1));
                bookModel.setAuthor(cursor.getString(2));
                bookModel.setGenre(cursor.getString(3));
                bookModel.setCurrentPage(cursor.getInt(4));
                arrayList.add(bookModel);
            } while (cursor.moveToNext());
        }
        mydatabase.close();
        Log.e("getAllBooks result", arrayList.toString());
        return arrayList;
    }


    private void displayPdfFromUri(Uri uri){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pdf_viewer_popup, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(this.findViewById(R.id.addPdfbutton), Gravity.CENTER, 0, 0);

        //get current page
        int pagetogo = 0;
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex", MODE_PRIVATE, null);
        Cursor cursor = mydatabase.rawQuery("SELECT * from library where path='"+uri.toString()+"'",null);
        if (cursor.moveToFirst()){
            pagetogo = cursor.getInt(4);
        }
        Log.e("currentpageforload", String.valueOf(pagetogo));

        PDFView pdfView2 = popupView.findViewById(R.id.pdfView);
        pdfView2.fromUri(uri)
                .defaultPage(pagetogo)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();

        currentlyReading = uri;
        Button ExitButton = popupView.findViewById(R.id.exit_from_pdfviewer);
        ExitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //get current page
                pdfView2.getCurrentPage();
                popupWindow.dismiss();
            }
        });

        //popupView.setOnTouchListener(new View.OnTouchListener() {
       //     @Override
       //     public boolean onTouch(View v, MotionEvent event) {
      //          Log.d("pdfviewontouch", "called");
       //         popupWindow.dismiss();
       //         return true;
       //     }
       // });

    }

    //pdf viewer overide functions


    //part of initialization
    public static void setWindowFlag(Activity activity, final int bits, boolean on){
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on){
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        Log.d("onpagechanged", "called");

        //save the changed page
        ContentValues cv = new ContentValues();
        cv.put("currentpage", page);
        Log.e("pagechanged", String.valueOf(page));
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
        mydatabase.update("library", cv, "path = ?", new String[] {currentlyReading.toString()});

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
}

