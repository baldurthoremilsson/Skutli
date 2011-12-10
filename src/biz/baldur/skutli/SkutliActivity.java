package biz.baldur.skutli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import biz.baldur.skutli.R;
import biz.baldur.skutli.model.Route;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SkutliActivity extends ListActivity {
	DataStore dataStore;
	ProgressDialog dialog;
	Handler progressHandler;
	String baseUrl = "http://baldur.biz/bus/";
	ArrayAdapter<Route> arrayAdapter;

	public static final int PROGRESS_MAX = 1;
	public static final int PROGRESS_UPDATE = 2;
	
	private static final String FILENAME = "datastore";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		dataStore = loadDataStore();
        NextArrivalActivity.dataStore = dataStore;
        
		setContentView(R.layout.main);

		arrayAdapter = new ArrayAdapter<Route>(this, R.layout.choose_route_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = (TextView) super.getView(position, convertView, parent);
				Route route = getItem(position);
				textView.setText(route.getName());
				textView.setTextColor(route.getColor());
				return textView;
			}
		};
		setListAdapter(arrayAdapter);
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(SkutliActivity.this, NextArrivalActivity.class);
				intent.putExtra("route", ((TextView)view).getText());
				startActivity(intent);
			}
		});
		
		this.displayRoutes();
	}
	
	public void displayRoutes() {
		arrayAdapter.clear();
		for(Route route: dataStore.getRoutes()) {
			arrayAdapter.add(route);
		}
		arrayAdapter.notifyDataSetChanged();
	}
	
	public void fetchData(View view) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				saveDataStore(dataStore);
				displayRoutes();
			}
		};
		dialog = new ProgressDialog(this);
		dialog.setCancelable(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setMax(1);
		dialog.show();
		
	    progressHandler = new Handler() {
	        public void handleMessage(Message msg) {
	        	if(msg.what == PROGRESS_MAX) {
	        		dialog.setMax(msg.arg1);
	        	}
	        	else if(msg.what == PROGRESS_UPDATE) {
	        		dialog.setMessage("Sæki " + msg.obj);
	        		dialog.incrementProgressBy(1);
	        		if(dialog.getProgress() == dialog.getMax())
	        			dialog.dismiss();
	        	}
	        }
	    };
	    
	    Thread dataThread = new Thread(new Runnable() {
	    	public void run() {
	    		dataStore.populate(baseUrl, progressHandler, getResources());
	    		handler.sendEmptyMessage(0);
	    	}
	    });
	    
	    dataThread.start();
	}
	
	private void saveDataStore(DataStore dataStore) {
		FileOutputStream fos;
		try {
			fos = openFileOutput(FILENAME, MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(dataStore);
			os.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	private DataStore loadDataStore() {
		DataStore dataStore = null;
		FileInputStream fis;
		try {
			fis = openFileInput(FILENAME);
			ObjectInputStream is = new ObjectInputStream(fis);
			dataStore = (DataStore) is.readObject();
			is.close();
		} catch (FileNotFoundException e) {
		} catch (StreamCorruptedException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		if(dataStore == null)
			return new DataStore();
		return dataStore;
	}
}