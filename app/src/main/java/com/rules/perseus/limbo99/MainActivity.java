package com.rules.perseus.limbo99;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "Mainactivity";

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private SpeechRecognizer speechRecognizer;
    private SharedPreferences mPrefs;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    WordsDataSource datasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnSpeak) {

                    startListenerIntent(getLanguagePref());

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

        String seachQuery = "shit";
        Boolean res = datasource.checkIfWordInTable(inputLanguage, seachQuery);

        if (res == true) {
            Log.i(TAG, "Word " + seachQuery + " found");
        } else {
            Log.i(TAG, "Word " + seachQuery + " NOT found");
        }

    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
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
                Toast.makeText(getApplicationContext(),
                        getString(R.string.error_speech_timeout),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.error),
                        Toast.LENGTH_SHORT).show();
            }

            startListenerIntent(getLanguagePref());
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
//                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }

            try {
                String textToDisplay = (String) txtSpeechInput.getText();
                if (textToDisplay.isEmpty()) {
                    textToDisplay = (String) data.get(0);
                } else {
                    textToDisplay = textToDisplay + " - " + (String) data.get(0);
                }
                txtSpeechInput.setText(textToDisplay);
            } catch (Exception e) {

            }

            // Immediately start a new intent:
            startListenerIntent(getLanguagePref());

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

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, inputLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
    }

    private String getLanguagePref() {
        String inputLanguage = mPrefs.getString("input_language", "");
        if (inputLanguage.equals("")) {
            inputLanguage = "en";
        }
        return inputLanguage;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click

        switch (item.getItemId()) {
            case R.id.action_settings:
                // Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_gauge:
                // Intent i2 = new Intent(MainActivity.this, GaugeActivity.class);
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

}
