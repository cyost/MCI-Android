package com.kynetx.mci.activities;

import com.kynetx.mci.R;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class GetPhotoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_photo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get_photo, menu);
		return true;
	}

}
