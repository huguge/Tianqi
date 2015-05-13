package com.eightzero.tianqi.adapter;

import java.util.List;
import java.util.Map;

import com.example.tianqi.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherGridViewAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<Map<String, String>> weatherListDate;
	private Context context;
	
	public WeatherGridViewAdapter(Context context, List<Map<String, String>> weatherListDate) {
		this.weatherListDate = weatherListDate;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if(weatherListDate == null)
			return 0;
		return weatherListDate.size();
	}

	@Override
	public Object getItem(int position) {
		return weatherListDate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class ViewHolder{
		private LinearLayout listItem;
		private TextView week;//星期
		private TextView AQI;//AQI指数
		private ImageView weatherImg;//天气图标
		private TextView temperature;//天气气温
		private TextView weatherStatus;//天气状态（如：晴）
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_gridview, null);
			holder = new ViewHolder();
			holder.listItem = (LinearLayout) convertView.findViewById(R.id.weather_item_layout);
			holder.week = (TextView) convertView.findViewById(R.id.weather_week);
			holder.AQI = (TextView) convertView.findViewById(R.id.weather_aqi);
			holder.weatherImg = (ImageView) convertView.findViewById(R.id.weather_img);
			holder.temperature = (TextView) convertView.findViewById(R.id.weather_temperature);
			holder.weatherStatus = (TextView) convertView.findViewById(R.id.weather_status);
			convertView.setPadding(8, 8, 8, 8); // 每格的间距
		}else{
			holder= (ViewHolder)convertView.getTag();
		}
		return convertView;
	}
}
