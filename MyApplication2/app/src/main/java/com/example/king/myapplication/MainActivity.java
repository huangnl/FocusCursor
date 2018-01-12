package com.example.king.myapplication;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        setContentView(view);
        FocusCursor focusCursor = new FocusCursor(view,ContextCompat.getDrawable(this,R.drawable.touchlayout_bg));
        focusCursor.startTrack();
    }
}
