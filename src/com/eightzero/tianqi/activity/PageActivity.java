package com.eightzero.tianqi.activity;

import com.example.tianqi.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * 导航页面
 * @author lyp
 * 
 */
public class PageActivity extends Activity {

	private final int SPLASH_DISPLAY_LENGHT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_page_main);
	}
	
	private void toMain(){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setClass(PageActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.push_left_in,R.anim.fade_out);
			}
		}, SPLASH_DISPLAY_LENGHT);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
//		if (NetworkHelper.getNetworkType(PageActivity.this) != NetworkHelper.NONE) { // ��������ܷ��ʷ�����
//			
//		}else{
//			
//		}
		toMain();
	}
}
