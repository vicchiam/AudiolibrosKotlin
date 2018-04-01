package com.example.audiolibros

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.jetbrains.anko.startActivity


class SplashScreen : AppCompatActivity(), Animator.AnimatorListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val anim = AnimatorInflater.loadAnimator(this, R.animator.entrada) as AnimatorSet
        anim.addListener(this@SplashScreen)
        anim.setTarget(splash_text)
        anim.start()

    }


    override fun onAnimationStart(animator: Animator) {

    }

    override fun onAnimationEnd(animator: Animator) {
        startActivity<MainActivity>()
    }

    override fun onAnimationCancel(animator: Animator) {

    }

    override fun onAnimationRepeat(animator: Animator) {

    }
}
