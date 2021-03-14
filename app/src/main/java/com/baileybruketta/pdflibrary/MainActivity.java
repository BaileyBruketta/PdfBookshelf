package com.baileybruketta.pdflibrary;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private static final int REQUEST_CODE = 1;

    Integer pageNumber = 0;
    String pdfFileName;

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
        displayPdfFromUri(data);

    }

    private void AddPdfToIndex(Uri uri){

    }

    private void savepdfindatabase(Uri uri, String title, String authot){
        SQLiteDatabase mydatabase = openOrCreateDatabase("bookindex",MODE_PRIVATE,null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS library(path VARCHAR,title VARCHAR,author VARCHAR);");
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

        PDFView pdfView = findViewById(R.id.pdfView);
        PDFView pdfView2 = popupView.findViewById(R.id.pdfView);
        pdfView2.fromUri(uri)
                .defaultPage(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();


        //popupView.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        popupWindow.dismiss();
        //        return true;
        //    }
        //});
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

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
}