package com.comix.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.comix.overwatch.HiveProgressView;

import java.util.Locale;

public class MainActivity extends Activity {

  private static final int INITIAL_SPACING_DP = 8;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final HiveProgressView progressView = findViewById(R.id.hive_progress);
    final TextView label = findViewById(R.id.spacing_label);
    SeekBar bar = findViewById(R.id.spacing_bar);

    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressView.setSpacingDp(progress);
        label.setText(String.format(Locale.US, "Spacing %ddp", progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    bar.setProgress(INITIAL_SPACING_DP);
    progressView.setSpacingDp(INITIAL_SPACING_DP);
    label.setText(String.format(Locale.US, "Spacing %ddp", INITIAL_SPACING_DP));
  }
}
