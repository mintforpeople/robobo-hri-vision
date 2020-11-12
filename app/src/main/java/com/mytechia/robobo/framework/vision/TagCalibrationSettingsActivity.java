package com.mytechia.robobo.framework.vision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TagCalibrationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_calibration_settings);

        Intent intent = getIntent();
        int squaresX = intent.getIntExtra("squaresX", 4);
        int squaresY = intent.getIntExtra("squaresY", 6);
        float squareLength = intent.getFloatExtra("squareLength", 5);
        float markerLength = intent.getFloatExtra("markerLength", 3);

        final EditText textSquaresX = findViewById(R.id.settings_squares_x);
        final EditText textSquaresY = findViewById(R.id.settings_squares_y);
        final EditText textSquaresLength = findViewById(R.id.settings_length_square);
        final EditText textMarkerLength = findViewById(R.id.settings_length_marker);

        textSquaresX.setText(String.valueOf(squaresX));
        textSquaresY.setText(String.valueOf(squaresY));
        textSquaresLength.setText(String.valueOf(squareLength));
        textMarkerLength.setText(String.valueOf(markerLength));

        Button doneButton = findViewById(R.id.done_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("squaresX", Integer.parseInt(textSquaresX.getText().toString()));
                returnIntent.putExtra("squaresY", Integer.parseInt(textSquaresY.getText().toString()));
                returnIntent.putExtra("squareLength", Float.parseFloat(textSquaresLength.getText().toString()));
                returnIntent.putExtra("markerLength", Float.parseFloat(textMarkerLength.getText().toString()));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }

}
