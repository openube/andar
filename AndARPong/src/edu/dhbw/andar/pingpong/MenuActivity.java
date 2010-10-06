package edu.dhbw.andar.pingpong;


import java.util.Vector;

import edu.dhbw.andarpong.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The main menu.
 * @author Tobi
 *
 */
public class MenuActivity extends Activity implements OnClickListener {
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_menu);
		
		TextView t = ((TextView)findViewById(R.id.main_menu_startgame));
		t.setOnClickListener(this);t.setText(R.string.startgame);
		t = ((TextView)findViewById(R.id.main_menu_intructions));
		t.setOnClickListener(this);t.setText(R.string.instructions);
		t = ((TextView)findViewById(R.id.main_menu_about));
		t.setOnClickListener(this);t.setText(R.string.about);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TextView t = ((TextView)findViewById(R.id.main_menu_startgame));
		t.setOnClickListener(this);t.setText(R.string.startgame);
	}

	@Override
	public void onClick(View v) {
		String str = ((TextView)v).getText().toString();
		if(str.equals(getResources().getString(R.string.startgame))) {
			//start the game activity
			((TextView)v).setClickable(false);
			((TextView)v).setText(R.string.starting);
			Intent intent = new Intent(MenuActivity.this, GameActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
		} else if(str.equals(getResources().getString(R.string.instructions))) {
			//show the instructions activity
			startActivity(new Intent(MenuActivity.this, InstructionsActivity.class));
		} else if(str.equals(getResources().getString(R.string.about))) {
			//start the about activity
			startActivity(new Intent(MenuActivity.this, AboutActivity.class));
		}
	}
	
	
}
