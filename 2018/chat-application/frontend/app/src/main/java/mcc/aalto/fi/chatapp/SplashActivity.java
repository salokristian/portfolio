package mcc.aalto.fi.chatapp;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import mcc.aalto.fi.chatapp.AccountActivity.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        startLoadingAnimation(handler);

        handler.postDelayed(this::startNextActivity, getResources().getInteger(R.integer.splash_refer_delay));
    }

    /**
     * Start the next activity. MainActivity if the user is logged in, otherwise
     */
    private void startNextActivity() {
        Class<?> activity;
        if (isLoggedIn()) {
           activity = MainActivity.class;
        } else {
            activity = LoginActivity.class;
        }

        startActivity(new Intent(this, activity));
        finish();
    }

    /**
     * Check if the user is logged in.
     * @return True if the user is logged in, otherwise false.
     */
    private boolean isLoggedIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Starts the loading animation and repeats it after a set delay.
     */
    private void startLoadingAnimation(Handler handler) {
        ImageView view = findViewById(R.id.logo);
        AnimatedVectorDrawable animatedLogo = (AnimatedVectorDrawable) view.getDrawable();
        animatedLogo.start();

        animatedLogo.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                handler.postDelayed(animatedLogo::start, getResources().getInteger(R.integer.bounce_repeat_delay));
            }
        });
    }
}
