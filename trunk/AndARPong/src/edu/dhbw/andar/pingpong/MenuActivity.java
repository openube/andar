package edu.dhbw.andar.pingpong;


import java.util.Vector;

import edu.dhbw.andarpong.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The main menu.
 * @author Tobi
 *
 */
public class MenuActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setListAdapter(new ArrayAdapter<String>(this, R.layout.main_menu_item, new String[]{"Let's play", "Instructions", "About"}));
		Vector<String> items = new Vector<String>();
		//TODO use string.xml
		items.add("Logo");
		items.add(getResources().getString(R.string.startgame));
		items.add(getResources().getString(R.string.instructions));
		items.add(getResources().getString(R.string.about));		
		setListAdapter(new MenuAdapter(items));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String str = (String) this.getListAdapter().getItem(position);
		if(str.equals(getResources().getString(R.string.startgame))) {
			//start the game activity
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
	
	class MenuAdapter extends BaseAdapter {
		
		private Vector<String> items;
		
		public MenuAdapter(Vector<String> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int i) {
			return items.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return position != 0;
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			String item = items.get(position);
            if (v == null) {
            	LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	if(position == 0) {
            		v = vi.inflate(R.layout.main_menu_logo, null);  
            	} else {
            		v = vi.inflate(R.layout.main_menu_item, null);  
            	}
            }   
            if(item != null) {
            	if(position == 0) {
            		ImageView icon = (ImageView) v;
            		icon.setImageResource(R.drawable.menu_logo);
            	} else {
            		TextView textView = (TextView) v;
            		textView.setText(item);
            	}
        		
            }
			return v;
		}
		
	}
}
