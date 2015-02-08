package com.rules.perseus.limbo99;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TextView txtSpeechInput;
    private ImageButton micButton;
    private SpeechRecognizer speechRecognizer;
    private SharedPreferences mPrefs;
    private boolean isRunning;
    WordsDataSource datasource;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        isRunning = false;

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        micButton = (ImageButton) findViewById(R.id.micButton);

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.micButton) {
                    if (!isRunning) {
                        startListenerIntent(getLanguagePref());
                        changeRecordingState();
                    } else {
                        changeRecordingState();
                    }
                }
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new listener());

        DatabaseHandler dbHelper = DatabaseHandler.getInstance(this);

        try {
            dbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dbHelper.openDataBase();
        }catch(SQLException sqle){
            try {
                throw sqle;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        datasource = new WordsDataSource(this);
        datasource.open();

        String inputLanguage = mPrefs.getString("input_language", "");
        if (inputLanguage.equals("")) {
            inputLanguage = "en";
        }

//        DatabaseLookupTask task = new DatabaseLookupTask();
//        ArrayList<String> arrayList= new ArrayList<String>();
//        arrayList.add("abc");
//        arrayList.add("abcdec");
//        arrayList.add("shit");
//        arrayList.add("abc");
//        task.execute(arrayList);

//        if (res) {
//            Log.i(TAG, "Word " + seachQuery + " found");
//        } else {
//            Log.i(TAG, "Word " + seachQuery + " NOT found");
//        }
    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
            unmuteAudio();
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.v(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);

            if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                // TODO: do we need to handle this?
            } else {
                // TODO: do we need to handle this?
            }

            startListenerIntent(getLanguagePref());
        }
        public void onResults(Bundle results)
        {

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            DatabaseLookupTask task = new DatabaseLookupTask();
            task.execute(data);

            try {
                String textToDisplay = (String) txtSpeechInput.getText();
                if (textToDisplay.isEmpty()) {
                    textToDisplay = (String) data.get(0);
                } else {
                    textToDisplay = textToDisplay + " - " + data.get(0);
                }
                txtSpeechInput.setText(textToDisplay);
            } catch (Exception e) {
                // TODO
            }

            if (isRunning == true) {
                // Immediately start a new intent:
                startListenerIntent(getLanguagePref());
            }

        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    private void startListenerIntent(String inputLanguage) {

        muteAudio();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, inputLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizer.startListening(intent);
    }

    private String getLanguagePref() {
        String inputLanguage = mPrefs.getString("input_language", "");
        if (inputLanguage.equals("")) {
            inputLanguage = "en";
        }
        return inputLanguage;
    }

    // Mute audio that we don't here the beep sound when the recording starts:
    private void muteAudio() {
        Log.i(TAG, "Muting audio");
        AudioManager audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    private void unmuteAudio() {
        Log.i(TAG, "Unmuting audio");
        AudioManager audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        audioManager.setStreamMute(AudioManager.STREAM_RING, false);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }
    
    private void changeRecordingState() {
        if (isRunning == false) {
            // Start recording
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            micButton.startAnimation(pulse);
            isRunning = true;
        } else {
            // Stop recording
            micButton.clearAnimation();
            isRunning = false;
            Log.i(TAG, "Recording stopped");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_gauge:
                startActivity(new Intent(MainActivity.this, GaugeActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    private class DatabaseLookupTask extends AsyncTask<ArrayList, Void, Double> {
        @Override
        protected Double doInBackground(ArrayList... inArrayList) {

            datasource = new WordsDataSource(context);
            datasource.open();

            String inputLanguage = mPrefs.getString("input_language", "");
            if (inputLanguage.equals("")) {
                inputLanguage = "en";
            }

            int wordsFoundInDB = 0;
            int totalWords = 0;

            for (ArrayList<String> arrayList : inArrayList) {

                totalWords = arrayList.size();
                for (String str : arrayList) {
                    Boolean res = datasource.checkIfWordInTable(inputLanguage, str);

                    if (res) {
                        wordsFoundInDB++;
                        Log.i(TAG, "Word " + str + " found");
                    }
                }
            }
            Double hitRate = wordsFoundInDB/( (double) totalWords);

            Log.i(TAG, "hitRate " + hitRate);

            return hitRate;
        }

        @Override
        protected void onPostExecute(Double result) {

        }
    }


}
