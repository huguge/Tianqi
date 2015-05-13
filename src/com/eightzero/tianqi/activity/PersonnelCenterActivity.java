package com.eightzero.tianqi.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.eightzero.tianqi.tool.Bimp;
import com.eightzero.tianqi.tool.CircularImage;
import com.eightzero.tianqi.tool.FileUtils;
import com.eightzero.tianqi.tool.PhotoActivity;
import com.example.tianqi.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PersonnelCenterActivity extends Activity{

	//页面控件
	private ImageView backIv;//返回
	private CircularImage userImage;//用户头像
	private RelativeLayout mobileVerified;//手机验证
	private RelativeLayout emailVerified;//邮箱验证
	private TextView tvPhoneNumber;//手机号码
	private TextView tvEmail;//邮箱
	
	public List<Bitmap> bmp = new ArrayList<Bitmap>();
	public List<String> drr = new ArrayList<String>();
	
	private float dp;
	
	private String userImageUrl;
	
	public static final String IMAGE_PATH = "formats";
	public static final File FILE_SDCARD = Environment.getExternalStorageDirectory();
	public static final File FILE_LOCAL = new File(FILE_SDCARD, IMAGE_PATH);
	public static final File FILE_PIC_SCREENSHOT = new File(FILE_LOCAL,"images/screenshots");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personnel_center);
		initView();
		initListener();
	}

	//初始化页面
	private void initView() {
		backIv = (ImageView)findViewById(R.id.iv_back);
		userImage = (CircularImage)findViewById(R.id.cir_user_image);
		mobileVerified = (RelativeLayout)findViewById(R.id.rlyt_mobile_to_verified);
		mobileVerified.setBackgroundResource(R.drawable.layout_background_color);//点击灰色效果
		emailVerified = (RelativeLayout)findViewById(R.id.rlyt_email_to_verified);
		emailVerified.setBackgroundResource(R.drawable.layout_background_color);//点击灰色效果
		tvPhoneNumber = (TextView)findViewById(R.id.tv_phone_number);
		tvEmail = (TextView)findViewById(R.id.tv_email); 
	}
	
	//初始化监听事件
	private void initListener() {
		
		//返回
		backIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
		
		//手机号验证
		mobileVerified.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(PersonnelCenterActivity.this, "抱歉，该功能尚未完成~~", Toast.LENGTH_SHORT).show();
			}
		});
		
		//邮箱验证
		emailVerified.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(PersonnelCenterActivity.this, "抱歉，该功能尚未完成~~", Toast.LENGTH_SHORT).show();
			}
		});
		
		//修改头像
		userImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new PopupWindows(PersonnelCenterActivity.this, userImage);//显示popwindow
			}
		});
		
	}
	
	
	//选择图片的 popupwindows
	public class PopupWindows extends PopupWindow {

		public PopupWindows(Context mContext, View parent) {

			View view = View.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
			// ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
			// R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(// 相册
							Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, RESULT_LOAD_IMAGE);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}
		
	private static final int TAKE_PICTURE = 0;
	private static final int RESULT_LOAD_IMAGE = 1;
	private static final int CUT_PHOTO_REQUEST_CODE = 2;
	private String path = "";
	private Uri photoUri;

	public void photo() {
		try {
			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			String sdcardState = Environment.getExternalStorageState();
			String sdcardPathDir = android.os.Environment
					.getExternalStorageDirectory().getPath() + "/tempImage/";
			File file = null;
			if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
				// 有sd卡，是否有myImage文件夹
				File fileDir = new File(sdcardPathDir);
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}
				// 是否有headImg文件
//				file = new File(sdcardPathDir + System.currentTimeMillis()+ ".JPEG");
				file = new File(sdcardPathDir + System.currentTimeMillis()+ ".png");
			}
			if (file != null) {
				path = file.getPath();
				photoUri = Uri.fromFile(file);
				openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

				startActivityForResult(openCameraIntent, TAKE_PICTURE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			if (drr.size() < 1 && resultCode == -1) {// 拍照
				startPhotoZoom(photoUri);
			}
			break;
		case RESULT_LOAD_IMAGE:
			if (drr.size() < 1 && resultCode == RESULT_OK && null != data) {// 相册返回
				Uri uri = data.getData();
				if (uri != null) {
					startPhotoZoom(uri);
				}
			}
			break;
		case CUT_PHOTO_REQUEST_CODE:
			if (resultCode == RESULT_OK && null != data) {// 裁剪返回
				Bitmap bitmap = Bimp.getLoacalBitmap(drr.get(drr.size() - 1));
				PhotoActivity.bitmap.add(bitmap);
				bitmap = Bimp.createFramedPhoto(480, 480, bitmap,(int) (dp * 1.6f));
				bmp.add(bitmap);
				
				if(bmp.size() > 0){
					userImage.setImageBitmap(bmp.get(0));
				}
			}
			break;
		}
	}
		
	private void startPhotoZoom(Uri uri) {
		try {
			// 获取系统时间 然后将裁剪后的图片保存至指定的文件夹
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
			String address = sDateFormat.format(new java.util.Date());
			if (!FileUtils.isFileExist("")) {
				FileUtils.createSDDir("");
			}
//			drr.add(FileUtils.SDPATH + address + ".JPEG");//JPEG 格式图片
			drr.add(FileUtils.SDPATH + address + ".png");//png 格式图片
//			Uri imageUri = Uri.parse("file:/sdcard/formats/" + address+ ".JPEG");
			Uri imageUri = Uri.parse("file:/sdcard/formats/" + address+ ".png");

			final Intent intent = new Intent("com.android.camera.action.CROP");

			// 照片URL地址
			intent.setDataAndType(uri, "image/*");

			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 480);
			intent.putExtra("outputY", 480);
			// 输出路径
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// 输出格式
//			intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("outputFormat",Bitmap.CompressFormat.PNG.toString());
			// 不启用人脸识别
			intent.putExtra("noFaceDetection", false);
			intent.putExtra("return-data", false);
			startActivityForResult(intent, CUT_PHOTO_REQUEST_CODE);
	
			userImageUrl = FILE_LOCAL +"/"+ address + ".png";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
