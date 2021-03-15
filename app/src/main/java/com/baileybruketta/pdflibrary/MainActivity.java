package com.baileybruketta.pdflibrary;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private static final int REQUEST_CODE = 1;

    Integer pageNumber = 0;
    String pdfFileName;
    Uri currentlyReading;
    Uri mCropImageUri;
    ImageView cropped_image_preview;

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
        //make db if must
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
        //create table if not exists (ie first instance of app) - this should probably be moved somewhere else and checked with stored device preferences
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS library(path VARCHAR,title VARCHAR,author VARCHAR, genre VARCHAR, currentpage INT,imagepath VARCHAR);");
        mydatabase.close();

        Button AddPdfButton = this.findViewById(R.id.addPdfbutton);
        AddPdfButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
               AddPdfButtonClicked();
           }
        });

        Button searchButton = this.findViewById(R.id.SearchButton);
        EditText searchin = this.findViewById(R.id.searchinput);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SearchButtonClicked(searchin.getText().toString());
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
            }
        });

        Button cancelsearchbutton = this.findViewById(R.id.cancelsearchbutton);
        cancelsearchbutton.setVisibility(View.INVISIBLE);
        cancelsearchbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cancelbuttonclicked();
            }

        });

        SetupCards();

    }
    public void emptySearchBar(){
        EditText searchin = this.findViewById(R.id.searchinput);
        searchin.getText().clear();
        searchin.clearFocus();
    }
    public void cancelbuttonclicked(){
        Button cancelsearchbutton = this.findViewById(R.id.cancelsearchbutton);
        cancelsearchbutton.setVisibility(View.INVISIBLE);
        SetupCards();
    }
    public void SearchButtonClicked(String term){

        ArrayList<BookModel> books = getBooksByTerm(term);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_bookshelf);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new PaddingItemDecoration(5));
        BookModelAdapter adapter = new BookModelAdapter(this, R.layout.listview_bookshelf, books, this);
        recyclerView.setAdapter(adapter);
        Button cancelsearchbutton = this.findViewById(R.id.cancelsearchbutton);
        cancelsearchbutton.setVisibility(View.VISIBLE);
        emptySearchBar();
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

        if (requestCode != CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            if (result != null){
            Uri data = result.getData();
            String pdfPath = data.getPath();
            Log.e("onActivityResult", pdfPath);
            AddPdfToIndex(data);
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, result);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        //handle cropimageactivity result
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult data = CropImage.getActivityResult(result);
            if (resultCode == RESULT_OK){
                cropped_image_preview.setImageURI(data.getUri());
                Log.d("image uri", data.getUri().toString());
                mCropImageUri = data.getUri();
            }
        }

        //implement save pdf location - form popup for add title and author


        //TEST
        //displayPdfFromUri(data);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
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
        Button editbutton = popupView.findViewById(R.id.editbookbutton);
        ImageView previewbook = popupView.findViewById(R.id.book_preview_image);


        //set view
        titleview.setText(model.getTitle());
        authorview.setText(model.getAuthor());
        previewbook.setImageURI(Uri.parse(model.ImagePath));

        //edit book button
        editbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditBook(model);
            }
        });

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

    private void EditBook(BookModel model){
        mCropImageUri = Uri.parse(model.ImagePath);
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
        cropped_image_preview  = popupView.findViewById(R.id.imageInput);
        Button cancelbutton   = popupView.findViewById(R.id.cancelsavebutton);
        Button savebutton     = popupView.findViewById(R.id.savebutton);
        Button deletebutton = popupView.findViewById(R.id.deletebutton);
        deletebutton.setVisibility(View.VISIBLE);

        authorinput.setText(model.getAuthor());
        titleinput.setText(model.getTitle());
        genreinput.setText(model.getGenre());
        cropped_image_preview.setImageURI(mCropImageUri);

        deletebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DeleteBook(model, popupWindow);
            }
        });

        cropped_image_preview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onSelectImageClick(v);
            }
        });

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

                ContentValues cv = new ContentValues();
                cv.put("title", titleinput.getText().toString());
                cv.put("author", authorinput.getText().toString());
                cv.put("genre", genreinput.getText().toString());
                cv.put("imagepath", mCropImageUri.toString());
                //savepdfindatabase(uri, titleinput.getText().toString(), authorinput.getText().toString(), genreinput.getText().toString());

                SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
                mydatabase.update("library", cv, "path = ?", new String[] {model.getPath()});
                mydatabase.close();

                popupWindow.dismiss();
                SetupCards();
            }
        });
    }

    void DeleteBook(BookModel model, PopupWindow caller){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.check_before_delete_popup, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(this.findViewById(R.id.addPdfbutton), Gravity.CENTER, 0, 0);
        Button deletebutton = popupView.findViewById(R.id.yes_delete_button);
        Button cancelbutton = popupView.findViewById(R.id.no_delete_button);

        deletebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
                mydatabase.delete("library", "path = ?", new String[] {model.getPath()});
                popupWindow.dismiss();
                caller.dismiss();
                SetupCards();
            }
        });
        cancelbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
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
        cropped_image_preview  = popupView.findViewById(R.id.imageInput);
        Button cancelbutton   = popupView.findViewById(R.id.cancelsavebutton);
        Button savebutton     = popupView.findViewById(R.id.savebutton);
        Button deletebutton   = popupView.findViewById(R.id.deletebutton);
        deletebutton.setVisibility(View.INVISIBLE);

        cropped_image_preview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onSelectImageClick(v);
            }
        });

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

    public void onSelectImageClick(View view){
        CropImage.startPickImageActivity(this);
    }

    private void savepdfindatabase(Uri uri, String title, String author, String genre){
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("path", uri.toString());
        values.put("title", title);
        values.put("author", author);
        values.put("genre", genre);
        values.put("currentpage", 0);
        if(mCropImageUri != null){
            values.put("imagepath", mCropImageUri.toString());
        }

        //create table if not exists (ie first instance of app) - this should probably be moved somewhere else and checked with stored device preferences
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS library(path VARCHAR,title VARCHAR,author VARCHAR, genre VARCHAR, currentpage INT,imagepath VARCHAR);");

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
                bookModel.setImagePath(cursor.getString(5));
                arrayList.add(bookModel);
            } while (cursor.moveToNext());
        }
        mydatabase.close();
        Log.e("getAllBooks result", arrayList.toString());
        return arrayList;
    }

    public ArrayList<BookModel> getBooksByTerm(String term){
        ArrayList<BookModel> arrayList = new ArrayList();
        ArrayList<BookModel> allbooks = getAllBooks();
        for (int i = 0; i < allbooks.size(); i++){
            if (allbooks.get(i).getTitle().toLowerCase().contains(term.toLowerCase())){
                arrayList.add(allbooks.get(i));
            }else if(allbooks.get(i).getAuthor().toLowerCase().contains(term.toLowerCase())){
                arrayList.add(allbooks.get(i));
            }

        }
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
        mydatabase.close();
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
        mydatabase.close();
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
}

