package com.rules.perseus.limbo99;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private RadioGroup radioLanguage;
    private RadioButton radioLanguageButton;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        radioLanguage = (RadioGroup) findViewById(R.id.radioLanguage);

        /*
        Select the right radio button (of the currently selected language) when creating
        the radio buttons:
        */
        final String emptyString = "empty";
        String inputLanguage = mPrefs.getString("input_language", emptyString);
        if (inputLanguage.equals(emptyString)) {
            inputLanguage = "en";
        }
        if (inputLanguage.equals("en")) {
            radioLanguage.check(R.id.radioEnglish);
        } else if (inputLanguage.equals("de")) {
            radioLanguage.check(R.id.radioGerman);
        } else {
            radioLanguage.check(R.id.radioEnglish);
        }

            radioLanguage.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.radioGerman) {
                    // Write to shared preferences:
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("input_language", "de");
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "german",
                            Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.radioEnglish) {
                    Toast.makeText(getApplicationContext(), "english",
                            Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("input_language", "en");
                    editor.commit();
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



/*    public void addListenerOnButton() {

        radioLanguage = (RadioGroup) findViewById(R.id.radioLanguage);

        radioLanguageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = radioLanguage.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioLanguageButton = (RadioButton) findViewById(selectedId);

                Toast.makeText(SettingsActivity.this,
                        radioLanguageButton.getText(), Toast.LENGTH_SHORT).show();

            }

        });
    }*/
}
