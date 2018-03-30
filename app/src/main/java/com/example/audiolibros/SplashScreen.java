package com.example.audiolibros;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class SplashScreen extends AppCompatActivity implements Animator.AnimatorListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView text=(TextView) findViewById(R.id.splash_text);
        AnimatorSet anim=(AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.entrada);
        anim.addListener(SplashScreen.this);
        anim.setTarget(text);
        anim.start();

    }


    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        Intent i=new Intent(this,MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
