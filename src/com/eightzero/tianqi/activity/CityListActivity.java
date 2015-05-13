package com.eightzero.tianqi.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eightzero.tianqi.adapter.CityAdapter;
import com.eightzero.tianqi.model.CityModel;
import com.eightzero.tianqi.tool.Application;
import com.eightzero.tianqi.tool.CharacterParser;
import com.eightzero.tianqi.tool.MyToast;
import com.eightzero.tianqi.tool.PinyinComparator;
import com.eightzero.tianqi.tool.SharePreferenceUtil;
import com.eightzero.tianqi.view.ClearEditText;
import com.eightzero.tianqi.view.SideBar;
import com.eightzero.tianqi.view.SideBar.OnTouchingLetterChangedListener;
import com.example.tianqi.R;

public class CityListActivity extends Activity {
	private ImageView backIv;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private TextView cityName;
	private CityAdapter adapter;
	private ClearEditText mClearEditText;
	private Application mApplication;
	private SharePreferenceUtil sharePreferenceUtil;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<CityModel> SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_listview);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);//需要添加的语句 ,设置全屏显示
		initView();
		initOclickListener();
	}

	public void initView() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		mApplication = Application.getInstance();
		sharePreferenceUtil = mApplication.getSharePreferenceUtil();
		backIv = (ImageView) findViewById(R.id.iv_back);
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		SourceDateList = filledData(getResources().getStringArray(R.array.date));
		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new CityAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);
		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		cityName = (TextView)findViewById(R.id.tv_my_position);
		cityName.setText(sharePreferenceUtil.getCity());

	}
	
	private void initOclickListener(){
		//返回监听
		backIv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);	
			}
		});
		
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}
			}
		});
		
		//点击listView的item
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				MyToast.showShort(getApplication(), ((CityModel) adapter.getItem(position)).getName());
			}
		});
		
		
		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<CityModel> filledData(String[] date) {
		List<CityModel> mSortList = new ArrayList<CityModel>();
		for (int i = 0; i < date.length; i++) {
			CityModel sortModel = new CityModel();
			sortModel.setName(date[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<CityModel> filterDateList = new ArrayList<CityModel>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (CityModel sortModel : SourceDateList) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

}
