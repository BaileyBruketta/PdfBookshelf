package com.baileybruketta.pdflibrary;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class PaddingItemDecoration extends RecyclerView.ItemDecoration {
    private final int size;

    public PaddingItemDecoration(int size){
        this.size = size;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        super.getItemOffsets(outRect, view, parent, state);

        //if(parent.getChildAdapterPosition(view))
        outRect.right += size;
    }
}
