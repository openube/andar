package edu.dhbw.andar.pingpong;

import edu.dhbw.andarpong.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		TextView t = (TextView)findViewById(R.id.about_text);
		t.setText("HEEEEELLLLLPPP MEE ... if you can!!!!");
	}
}
