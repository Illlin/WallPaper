package com.example.wallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView promptBox;
    private TextView negPromptBox;
    private Switch onOff;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Handler looper;
    Runnable looperTask = new Runnable(){
        @Override
        public void run(){
            ApiImageGen gen = new ApiImageGen(
                    promptBox.getText().toString(),
                    negPromptBox.getText().toString(),
                    "",
                    getApplicationContext()
            );
            gen.start();
            looper.postDelayed(looperTask, 1000*60*2);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup storage
        sharedpreferences = getSharedPreferences("Prompt", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        promptBox =  (TextView) findViewById(R.id.genPrompt);
        negPromptBox =  (TextView) findViewById(R.id.genNegPrompt);
        onOff = (Switch) findViewById(R.id.onOff);

        this.promptBox.setText(sharedpreferences.getString("pos", "Phone background"));
        this.negPromptBox.setText(sharedpreferences.getString("neg", "Bad, ugly"));
        this.onOff.setChecked(sharedpreferences.getBoolean("onOff", false));

        looper = new Handler();

        if (onOff.isChecked()){
            this.shutDown();
            this.startUp();
        }

    }

    public void startUp(){
        looperTask.run();
    }

    public void shutDown(){
        looper.removeCallbacks(looperTask);
    }

    public void toggle(View v){
        editor.putBoolean("onOff", onOff.isChecked());
        editor.commit();
        if (onOff.isChecked()){
            startUp();
        } else {
            shutDown();
        }
    }

    public void onClickBtn(View v) throws JSONException, IOException {
        editor.putString("pos", promptBox.getText().toString());
        editor.putString("neg", negPromptBox.getText().toString());
        editor.commit();
    }

    public void saveText(String file){

    }
}