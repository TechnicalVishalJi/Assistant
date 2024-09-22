package com.vishalsingh.assistant;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.*;
import com.github.mikephil.charting.interfaces.datasets.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.json.*;
import com.github.mikephil.charting.utils.*;

public class WeatherActivity extends Activity
{
	String key1 = "x63TmpCJ475VClYvRIQsdTQS7qnMe0ms";
	String key2 = "dA3cHoWfA07f9yknhg6XFVPyyKAFqIGk";
	String key3 = "fGFKBTv5TwO2vRKfyjuhZYwtNQdnRP6A";
	String currentUrl = "http://dataservice.accuweather.com/currentconditions/v1/206679?apikey=" + key3 + "&details=true", 
	dailyUrl = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/206679?apikey=" + key3 + "&details=true&metric=true", 
	hourlyUrl = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/206679?apikey=" + key3 +  "&details=true&metric=true";
	//minuteUrl = "http://dataservice.accuweather.com/forecasts/v1/minute?q=26.4%2C80.5&apikey=fePFzDs9ANZomcIDdjfg8IGr2Z58YJDP";
	CustomizedExceptionHandler log;
	LineChart lineChart;
	LineChart lineChartToday;
	LinearLayout needle, hourlyBox, dailyBox, phaseBox;
	ImageView weatherIcon1;
	RelativeLayout sun1, moon1;
	TextView dateToday, temperature1, weatherText1, headline1, humidityText1, feelsLikeTemp1, windSpeed1, windSpeed2, uvIndex1, uvIndexText1,
		pressureMb, pressureAtm, moonriseTime, moonsetTime, sunriseTime, sunsetTime, rainMeasure;
	ProgressBar uvProgress;
	ArrayList<Entry> entries = new ArrayList<>();
	ArrayList<Entry> entries1 = new ArrayList<>();
	ArrayList<Entry> entries2 = new ArrayList<>();
	float lastHour = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		log = new CustomizedExceptionHandler("Crash Reports", "App");
		Thread.setDefaultUncaughtExceptionHandler(log);
        setContentView(R.layout.weather_layout);
		
		needle = findViewById(R.id.needle);
		lineChart = findViewById(R.id.line_chart);
		lineChartToday = findViewById(R.id.line_chart_today);
		sun1 = findViewById(R.id.sun_image);
		moon1 = findViewById(R.id.moon_image);
		dateToday = findViewById(R.id.date);
		weatherText1 = findViewById(R.id.weatherText);
		temperature1 = findViewById(R.id.temperature);
		weatherIcon1 = findViewById(R.id.weatherIcon);
		headline1 = findViewById(R.id.headline);
		humidityText1 = findViewById(R.id.humidityText);
		feelsLikeTemp1 = findViewById(R.id.feelsLikeTemp);
		windSpeed1 = findViewById(R.id.windSpeed);
		uvIndex1 = findViewById(R.id.uvIndex);
		uvIndexText1 = findViewById(R.id.uvText);
		uvProgress = findViewById(R.id.uvProgress);
		pressureMb = findViewById(R.id.pressureMb);
		pressureAtm = findViewById(R.id.pressureAtm);
		moonriseTime = findViewById(R.id.moonriseTime);
		moonsetTime = findViewById(R.id.moonsetTime);
		sunriseTime = findViewById(R.id.sunriseTime);
		sunsetTime = findViewById(R.id.sunsetTime);
		rainMeasure = findViewById(R.id.rainMeasure);
		hourlyBox = findViewById(R.id.hourlyWeatherBox);
		dailyBox = findViewById(R.id.dailyWeatherBox);
		phaseBox = findViewById(R.id.phaseBox);
		windSpeed2 = findViewById(R.id.windSpeed1);
		
		final LayoutInflater inflater = LayoutInflater.from(WeatherActivity.this);
		
		
		Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						
						URL currentUrl1 = new URL(currentUrl);
						URL dailyUrl1 = new URL(dailyUrl);
						URL hourlyUrl1 = new URL(hourlyUrl);
						//URL minuteUrl1 = new URL(minuteUrl);

						BufferedReader in = new BufferedReader(new InputStreamReader(currentUrl1.openStream()));
						String currentWeather = "";
						String n;
						while ((n = in.readLine()) != null){
							currentWeather += n;
						}
						in.close();

						if(!currentWeather.isEmpty()){
							JSONObject json = new JSONArray(currentWeather).getJSONObject(0);
							final String weatherText =json.getString("WeatherText");
							final int weatherIcon = json.getInt("WeatherIcon");
							
							final String temperature = json.getJSONObject("Temperature").getJSONObject("Metric").getString("Value");
							final JSONObject feelsLikeobj = json.getJSONObject("RealFeelTemperature").getJSONObject("Metric");
							final String feelsLike = feelsLikeobj.getString("Value");
							
							final String relativeHumidity = json.optString("RelativeHumidity");
							final JSONObject wind = json.getJSONObject("Wind");
							final String windSpeed = wind.getJSONObject("Speed").getJSONObject("Metric").getString("Value");
							final String windDegree = wind.getJSONObject("Direction").getString("Degrees");
							final String uvIndex = json.getString("UVIndex");
							final String uvIndexText = json.getString("UVIndexText");
							
							final String pressure = json.getJSONObject("Pressure").getJSONObject("Metric").getString("Value");
							final String rain = json.getJSONObject("PrecipitationSummary").getJSONObject("Past24Hours").getJSONObject("Metric").getString("Value");
							runOnUiThread(new Runnable() {
									@Override
									public void run() {
										weatherText1.setText(weatherText);
										if(weatherIcon == 1){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather_1));
										}else if(weatherIcon == 2 | weatherIcon == 3 | weatherIcon == 4 | weatherIcon == 5){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather2_5));
										}else if(weatherIcon == 6 | weatherIcon == 7 | weatherIcon == 8){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather6_8));
										}else if(weatherIcon == 11){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather11));
										}else if(weatherIcon == 12 | weatherIcon == 13){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather12_13));
										}else if(weatherIcon == 14){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather14));
										}else if(weatherIcon == 15 | weatherIcon == 16){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather15_16));
										}else if(weatherIcon == 17){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather17));
										}else if(weatherIcon == 18){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather18));
										}else if(weatherIcon == 32){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather32));
										}else if(weatherIcon == 33){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather33));
										}else if(weatherIcon == 34 | weatherIcon == 35 | weatherIcon == 36 | weatherIcon == 37 | weatherIcon == 38){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather34_38));
										}else if(weatherIcon == 39 | weatherIcon == 40){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather39_40));
										}else if(weatherIcon == 41 | weatherIcon == 42){
											weatherIcon1.setImageDrawable(getResources().getDrawable(R.drawable.weather41_42));
										}else{
											log.i("No Image found for Icon " + String.valueOf(weatherIcon));
										}
										temperature1.setText(temperature + "°");
										feelsLikeTemp1.setText(feelsLike + "°");
										humidityText1.setText(relativeHumidity + "%");
										windSpeed1.setText(windSpeed + "Km/h");
										windSpeed2.setText(windSpeed + "Km/h");
										needle.setRotation(Float.parseFloat(windDegree));
										uvIndex1.setText(uvIndex);
										uvIndexText1.setText(uvIndexText);
										uvProgress.setProgress(Integer.decode(uvIndex)*10, true);
										pressureMb.setText(pressure + " mb");
										pressureAtm.setText(String.format("%.4f", Float.parseFloat(pressure)/1013.25) + " atm");
										rainMeasure.setText(rain + "mm");
									}
								});
						}

						
						in = new BufferedReader(new InputStreamReader(dailyUrl1.openStream()));
						String dailyWeather = "";
						String n1;
						while ((n1 = in.readLine()) != null){
							dailyWeather += n1;
						}
						in.close();

						


						if(!dailyWeather.isEmpty()){
							JSONObject json1 = new JSONObject(dailyWeather);
							JSONArray json = json1.getJSONArray("DailyForecasts");
							final String headline = json1.getJSONObject("Headline").getString("Text");
							for (int i =0; i<json.length(); i++){
								JSONObject obj = json.getJSONObject(i);
								final String date = obj.getString("Date").split("T")[0];
								JSONObject sun = obj.getJSONObject("Sun");
								final String sunRise = sun.getString("Rise").split("T")[1].split("\\+")[0];
								final String sunSet = sun.getString("Set").split("T")[1].split("\\+")[0];
								JSONObject moon = obj.getJSONObject("Moon");
								final String moonRise = moon.getString("Rise").split("T")[1].split("\\+")[0];
								final String moonSet = moon.getString("Set").split("T")[1].split("\\+")[0];
								final String moonPhase = moon.getString("Phase");
								JSONObject temperature = obj.getJSONObject("Temperature");
								final String minimumTemp = temperature.getJSONObject("Minimum").getString("Value");
								final String maximumTemp = temperature.getJSONObject("Maximum").getString("Value");
								final int dayIcon = obj.getJSONObject("Day").getInt("Icon");
								final int nightIcon = obj.getJSONObject("Night").getInt("Icon");
								final int j = i;
								runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if(j==0){
												try
												{
													String start_dt = date;
													DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD"); 
													Date dateNew = formatter.parse(start_dt);
													SimpleDateFormat newFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");
													String finalDate = newFormat.format(dateNew);
													dateToday.setText(finalDate);
												}
												catch (ParseException e)
												{
													log.e(WeatherActivity.this, e);
												}
												
												headline1.setText(headline);
												String[] sunRiseUnits = sunRise.split(":"); //will break the string up into an array
												int sunRiseSeconds = Integer.parseInt(sunRiseUnits[0])*3600 + Integer.parseInt(sunRiseUnits[1])*60;
												String[] sunSetUnits = sunSet.split(":"); //will break the string up into an array
												int sunSetSeconds = Integer.parseInt(sunSetUnits[0])*3600 + Integer.parseInt(sunSetUnits[1])*60;
												int timeNow = LocalTime.now().toSecondOfDay();
												if(sunRiseSeconds<timeNow & sunSetSeconds>timeNow){
													int SunHours =  sunSetSeconds - sunRiseSeconds;
													int timeUntilNow = timeNow - sunRiseSeconds;
													double sunX = (timeUntilNow*90)/SunHours;
													double sunY = Math.sqrt(45*45 - Math.pow((sunX - 45),2));
													sun1.setTranslationX(px(sunX+(5*sunX)/90));
													sun1.setTranslationY(-px(sunY+5));
													
												}else{
													sun1.setVisibility(View.GONE);
												}

												String[] moonRiseUnits = moonRise.split(":"); //will break the string up into an array
												int moonRiseSeconds = Integer.parseInt(moonRiseUnits[0])*3600 + Integer.parseInt(moonRiseUnits[1])*60;
												String[] moonSetUnits = moonSet.split(":"); //will break the string up into an array
												int moonSetSeconds = Integer.parseInt(moonSetUnits[0])*3600 + Integer.parseInt(moonSetUnits[1])*60;
												int moonTimeNow = LocalTime.now().toSecondOfDay();
												if(moonRiseSeconds>moonSetSeconds){
													int moonHours = (24*3600 - moonRiseSeconds) + moonSetSeconds;
													int timeUntilNow = 0;
													if(moonTimeNow>moonRiseSeconds){
														timeUntilNow = moonTimeNow - moonRiseSeconds;
													}else{
														timeUntilNow = moonTimeNow + (24*3600-moonRiseSeconds);
													}
													if(timeUntilNow<moonHours){
														double moonX = (timeUntilNow*90)/moonHours;
														double moonY = Math.sqrt(45*45 - Math.pow((moonX - 45),2));
														moon1.setTranslationX(px(moonX+(5*moonX)/90));
														moon1.setTranslationY(-px(moonY+5));
														
													}else{
														moon1.setVisibility(View.GONE);
													}
												}else{
													if(moonRiseSeconds<moonTimeNow & moonSetSeconds>moonTimeNow){
														int moonHours =  moonSetSeconds - moonRiseSeconds;
														int timeUntilNow = moonTimeNow - moonRiseSeconds;
														double moonX = (timeUntilNow*90)/moonHours;
														double moonY = Math.sqrt(45*45 - Math.pow((moonX - 45),2));
														moon1.setTranslationX(px(moonX+(5*moonX)/90));
														moon1.setTranslationY(-px(moonY+5));
														
													}else{
														moon1.setVisibility(View.GONE);
													}
												}
												
												sunriseTime.setText(sunRiseUnits[0] + ":" + sunRiseUnits[1]);
												sunsetTime.setText(sunSetUnits[0] + ":" + sunSetUnits[1]);
												moonriseTime.setText(moonRiseUnits[0] + ":" + moonRiseUnits[1]);
												moonsetTime.setText(moonSetUnits[0] + ":" + moonSetUnits[1]);
											}
											LinearLayout component = (LinearLayout) inflater.inflate(R.layout.daily_weather_component, null);
											TextView dailyDate = component.findViewById(R.id.dailyDate);
											LinearLayout dayIcon1 = component.findViewById(R.id.dayIcon);
											TextView dayTemp = component.findViewById(R.id.dayTemp);
											TextView nightTemp = component.findViewById(R.id.nightTemp);
											LinearLayout nightIcon1 = component.findViewById(R.id.nightIcon);
											try
											{
												String start_dt = date;
												DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
												Date dateNew = formatter.parse(start_dt);
												SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM");
												String finalDate = newFormat.format(dateNew);
												dailyDate.setText(finalDate);
											}
											catch (ParseException e)
											{
												log.e(WeatherActivity.this, e);
											}
											if(dayIcon == 1){
												dayIcon1.setBackgroundResource(R.drawable.weather_1);
											}else if(dayIcon == 2 | dayIcon == 3 | dayIcon == 4 | dayIcon == 5){
												dayIcon1.setBackgroundResource(R.drawable.weather2_5);
											}else if(dayIcon == 6 | dayIcon == 7 | dayIcon == 8){
												dayIcon1.setBackgroundResource(R.drawable.weather6_8);
											}else if(dayIcon == 11){
												dayIcon1.setBackgroundResource(R.drawable.weather11);
											}else if(dayIcon == 12 | dayIcon == 13){
												dayIcon1.setBackgroundResource(R.drawable.weather12_13);
											}else if(dayIcon == 14){
												dayIcon1.setBackgroundResource(R.drawable.weather14);
											}else if(dayIcon == 15 | dayIcon == 16){
												dayIcon1.setBackgroundResource(R.drawable.weather15_16);
											}else if(dayIcon == 17){
												dayIcon1.setBackgroundResource(R.drawable.weather17);
											}else if(dayIcon == 18){
												dayIcon1.setBackgroundResource(R.drawable.weather18);
											}else{
												log.i("Image not found for dayIcon with name " + dayIcon);
											}
											
											if(nightIcon == 7 | nightIcon == 8){
												nightIcon1.setBackgroundResource(R.drawable.weather6_8);
											}else if(nightIcon == 11){
												nightIcon1.setBackgroundResource(R.drawable.weather11);
											}else if(nightIcon == 12){
												nightIcon1.setBackgroundResource(R.drawable.weather12_13);
											}else if(nightIcon == 15){
												nightIcon1.setBackgroundResource(R.drawable.weather15_16);
											}else if(nightIcon == 18){
												nightIcon1.setBackgroundResource(R.drawable.weather18);
											}else if(nightIcon == 32){
												nightIcon1.setBackgroundResource(R.drawable.weather32);
											}else if(nightIcon == 33){
												nightIcon1.setBackgroundResource(R.drawable.weather33);
											}else if(nightIcon == 34 | nightIcon == 35 | nightIcon == 36 | nightIcon == 37 | nightIcon == 38){
												nightIcon1.setBackgroundResource(R.drawable.weather34_38);
											}else if(nightIcon == 39 | nightIcon == 40){
												nightIcon1.setBackgroundResource(R.drawable.weather39_40);
											}else if(nightIcon == 41 | nightIcon == 42){
												nightIcon1.setBackgroundResource(R.drawable.weather41_42);
											}else{
												log.i("Image not found for nightIcon with name " + nightIcon);
											}
											
											dayTemp.setText(maximumTemp);
											nightTemp.setText(minimumTemp);
											
											LinearLayout phaseLayout = (LinearLayout)inflater.inflate(R.layout.phase_layout, null);
											try
											{
												String start_dt = date;
												DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
												Date dateNew = formatter.parse(start_dt);
												SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM");
												String finalDate = newFormat.format(dateNew);
												((TextView)phaseLayout.findViewById(R.id.phaseDate)).setText(finalDate);
											}
											catch (ParseException e)
											{
												log.e(WeatherActivity.this, e);
											}
											((TextView)phaseLayout.findViewById(R.id.moonRiseTime)).setText(moonRise.split(":")[0] + ":" + moonRise.split(":")[1]);
											((TextView)phaseLayout.findViewById(R.id.moonSetTime)).setText(moonSet.split(":")[0] + ":" + moonSet.split(":")[1]);
											
											if(moonPhase.equals("Full")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.full_moon);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Full\nMoon");
											}else if(moonPhase.equals("New")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundColor(Color.BLACK);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("New\nMoon");
											}else if(moonPhase.equals("WaningGibbous")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.waning_gibbous);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Waning\nGibbous");
											}else if(moonPhase.equals("WaxingGibbous")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.waxing_gibbous);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Waxing\nGibbous");
											}else if(moonPhase.equals("WaningCrescent")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.waning_crescent);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Waning\nCrescent");
											}else if(moonPhase.equals("WaxingCrescent")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.waxing_crescent);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Waxing\nCrescent");
											}else if(moonPhase.equals("First")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.first_quarter);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("First\nQuarter");
											}else if(moonPhase.equals("Third")){
												((LinearLayout)phaseLayout.findViewById(R.id.moonPhase)).setBackgroundResource(R.drawable.third_quarter);
												((TextView)phaseLayout.findViewById(R.id.PhaseName)).setText("Third\nQuarter");
											}else{log.i("No moon phase found for " + moonPhase);}
											
											String[] array = date.split("-");
											entries.add(new Entry(Integer.parseInt(array[2] + array[1]),Float.parseFloat(maximumTemp)));
											entries1.add(new Entry(Integer.parseInt(array[2] + array[1]),Float.parseFloat(minimumTemp)));
											
											dailyBox.addView(component);
											phaseBox.addView(phaseLayout);
											
										}
									});
							}
						}
						log.i("set daily");
						in = new BufferedReader(new InputStreamReader(hourlyUrl1.openStream()));
						String hourlyWeather = "";
						String n2;
						while ((n2 = in.readLine()) != null){
							hourlyWeather += n2;
						}
						in.close();

						

						if(!hourlyWeather.isEmpty()){
							JSONArray array = new JSONArray(hourlyWeather);
							for (int i =0; i<array.length(); i++){
								JSONObject obj = array.getJSONObject(i);
								String[] timeArray = obj.getString("DateTime").split("T")[1].split("\\+")[0].split(":");
								int icon = obj.getInt("WeatherIcon");
								String temp = obj.getJSONObject("Temperature").getString("Value");
								
								
								final LinearLayout view = (LinearLayout)inflater.inflate(R.layout.hourly_weather_component, null);
								((TextView) view.findViewById(R.id.hourlyTime)).setText(timeArray[0]+ ":" +  timeArray[1]);
								LinearLayout icon1 = view.findViewById(R.id.hourlyIcon);
								((TextView) view.findViewById(R.id.hourlyTemp)).setText(temp);
								
								if(icon == 1){
									icon1.setBackgroundResource(R.drawable.weather_1);
								}else if(icon == 2 | icon == 3 | icon == 4 | icon == 5){
									icon1.setBackgroundResource(R.drawable.weather2_5);
								}else if(icon == 6 | icon == 7 | icon == 8){
									icon1.setBackgroundResource(R.drawable.weather6_8);
								}else if(icon == 11){
									icon1.setBackgroundResource(R.drawable.weather11);
								}else if(icon == 12 | icon == 13){
									icon1.setBackgroundResource(R.drawable.weather12_13);
								}else if(icon == 14){
									icon1.setBackgroundResource(R.drawable.weather14);
								}else if(icon == 15 | icon == 16){
									icon1.setBackgroundResource(R.drawable.weather15_16);
								}else if(icon == 17){
									icon1.setBackgroundResource(R.drawable.weather17);
								}else if(icon == 18){
									icon1.setBackgroundResource(R.drawable.weather18);
								}else if(icon == 32){
									icon1.setBackgroundResource(R.drawable.weather32);
								}else if(icon == 33){
									icon1.setBackgroundResource(R.drawable.weather33);
								}else if(icon == 34 | icon == 35 | icon == 36 | icon == 37 | icon == 38){
									icon1.setBackgroundResource(R.drawable.weather34_38);
								}else if(icon == 39 | icon == 40){
									icon1.setBackgroundResource(R.drawable.weather39_40);
								}else if(icon == 41 | icon == 42){
									icon1.setBackgroundResource(R.drawable.weather41_42);
								}else{
									log.i("No Icon found for Icon " + String.valueOf(icon));
								}
								
								float time = Float.parseFloat(timeArray[0]);
								if(lastHour<=time){
									entries2.add(new Entry(time,Float.parseFloat(temp)));
									lastHour = time;
								}else{
									entries2.add(new Entry(lastHour +1,Float.parseFloat(temp)));
									lastHour ++;
								}
								
								runOnUiThread(new Runnable(){@Override public void run(){
											hourlyBox.addView(view);
								}});
								
							}
						}

						

						/*in = new BufferedReader(new InputStreamReader(minuteUrl1.openStream()));
						String minuteWeather = "";
						String n3;
						while ((n3 = in.readLine()) != null){
							minuteWeather += n3;
						}
						in.close();


						log.i("read minute");

						if(!minuteWeather.isEmpty()){
							String summary = new JSONObject(minuteWeather).getJSONObject("Summary").getString("Phrase");

						}

						log.i("set minute");
*/


						runOnUiThread(new Runnable(){@Override public void run(){
									lineChart.setTouchEnabled(true);
									lineChartToday.setTouchEnabled(true);
									lineChart.setPinchZoom(false);
									lineChartToday.setPinchZoom(false);
									lineChart.setDoubleTapToZoomEnabled(false);
									lineChart.setDoubleTapToZoomEnabled(false);
									
									
									LineDataSet lineDataSet1 = new LineDataSet(entries1, "Minimum Temerature");
									LineDataSet lineDataSet2 = new LineDataSet(entries, "Maximum Temperature");
									lineDataSet1.setLineWidth(4);
									lineDataSet2.setLineWidth(4);
									lineDataSet1.setColor(getResources().getColor(R.color.veryLightPurple));
									lineDataSet2.setColor(getResources().getColor(R.color.purple));
									lineDataSet1.setDrawCircles(false);
									lineDataSet2.setDrawCircles(false);
									lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
									lineDataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
									lineDataSet1.setDrawValues(false);
									lineDataSet2.setDrawValues(false);
									lineDataSet1.setDrawHorizontalHighlightIndicator(false);
									lineDataSet1.setDrawVerticalHighlightIndicator(false);
									lineDataSet2.setDrawHorizontalHighlightIndicator(false);
									lineDataSet2.setDrawVerticalHighlightIndicator(false);
									/*lineDataSet1.setDrawFilled(true);
									 lineDataSet1.setFillDrawable(getResources().getDrawable((R.drawable.chart_gradient)));
									 */
									ArrayList<ILineDataSet> dataSets = new ArrayList<>();
									dataSets.add(lineDataSet1);
									dataSets.add(lineDataSet2);

									LineData data = new LineData(dataSets);

									lineChart.getAxisLeft().setAxisMinimum(lineDataSet1.getYMin()-1);

									lineChart.setBackgroundColor(getResources().getColor(R.color.white));
									lineChart.setDrawBorders(false);
									lineChart.animateY(500);
									lineChart.setData(data);
									lineChart.getXAxis().setDrawGridLines(false);
									lineChart.getXAxis().setDrawAxisLine(false);
									lineChart.getXAxis().setDrawLabels(false);
									lineChart.getAxisLeft().setDrawGridLines(false);
									lineChart.getAxisLeft().setDrawAxisLine(false);
									lineChart.getAxisLeft().setDrawLabels(false);
									lineChart.getAxisRight().setDrawGridLines(false);
									lineChart.getAxisRight().setDrawAxisLine(false);
									lineChart.getAxisRight().setDrawLabels(false);
									lineChart.getDescription().setEnabled(false);
									CustomMarkerView mv = new CustomMarkerView (WeatherActivity.this, R.layout.tv_content_bg);
									mv.setChartView(lineChart);
									lineChart.setMarkerView(mv);
									lineChart.invalidate();

									LineDataSet lineDataSet3 = new LineDataSet(entries2, "Temperature");
									lineDataSet3.setLineWidth(4);
									lineDataSet3.setColor(getResources().getColor(R.color.purple));
									lineDataSet3.setDrawCircles(false);
									lineDataSet3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
									lineDataSet3.setDrawValues(false);
									lineDataSet3.setDrawHorizontalHighlightIndicator(false);
									lineDataSet3.setDrawVerticalHighlightIndicator(false);

									ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
									dataSets1.add(lineDataSet3);

									LineData data1 = new LineData(dataSets1);


									lineChartToday.getAxisLeft().setAxisMinimum(lineDataSet3.getYMin()-1);

									lineChartToday.setBackgroundColor(getResources().getColor(R.color.white));
									lineChartToday.setDrawBorders(false);
									lineChartToday.animateY(500);
									lineChartToday.getLegend().setEnabled(false);
									lineChartToday.setData(data1);
									lineChartToday.getXAxis().setDrawGridLines(false);
									lineChartToday.getXAxis().setDrawAxisLine(false);
									lineChartToday.getXAxis().setDrawLabels(false);
									lineChartToday.getAxisLeft().setDrawGridLines(false);
									lineChartToday.getAxisLeft().setDrawAxisLine(false);
									lineChartToday.getAxisLeft().setDrawLabels(false);
									lineChartToday.getAxisRight().setDrawGridLines(false);
									lineChartToday.getAxisRight().setDrawAxisLine(false);
									lineChartToday.getAxisRight().setDrawLabels(false);
									lineChartToday.getDescription().setEnabled(false);
									CustomMarkerView1 mv1 = new CustomMarkerView1(WeatherActivity.this, R.layout.tv_content_bg);
									mv1.setChartView(lineChartToday);
									lineChartToday.setMarkerView(mv1);
									lineChartToday.invalidate();
						}});

			/*		}catch(final UnknownHostException e){
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(WeatherActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
								}
							});
					}catch(final SocketException e){
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(WeatherActivity.this, "Internet Disconnected", Toast.LENGTH_SHORT).show();
								}
							});
					*/}catch (final Exception e) {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									log.e(WeatherActivity.this, e);
								}
							});
					}


				}
			});

		thread.start();
			
		ShapeDrawable.ShaderFactory shaderFactory3 = new ShapeDrawable.ShaderFactory() {
			@Override
			public Shader resize(int width, int height) {
				LinearGradient linearGradient = new LinearGradient(0, height, width, height,
																   new int[] {
																	   Color.parseColor("#FFFFFF"),
																	   Color.parseColor("#00000000"),//Color.parseColor("#5603ef"),
																	   Color.parseColor("#00000000"),
																	   getResources().getColor(R.color.white)},//Color.parseColor("#000a68")},
																   new float[] {
																	   0f, 0.2f, 0.8f, 1f},
																   Shader.TileMode.CLAMP);
				return linearGradient;
			}
		};

		PaintDrawable paint3 = new PaintDrawable();
		paint3.setShape(new RectShape());
		paint3.setShaderFactory(shaderFactory3);
		findViewById(R.id.line_chart).setForeground((Drawable)paint3);
		findViewById(R.id.line_chart_today).setForeground((Drawable)paint3);
		
		
		
		
	}
	
	public float px(double dp){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,(float)dp, getWindow().getContext().getResources().getDisplayMetrics());
		return px;
	}
	
	
	
	
	private class CustomMarkerView extends MarkerView {
		private TextView date;
		
		public CustomMarkerView(Context context, int layoutResource) {
			super(context, layoutResource);
			date = findViewById(R.id.date);
		}

		@Override
		public void refreshContent(Entry e, Highlight highlight) {
			String date1 = String.valueOf((int)e.getX());
			int len = date1.length();
			if(len==3){
				date1 = String.valueOf((int)e.getX()).substring(0,1) + " " + (new DateFormatSymbols().getMonths()[Integer.decode(String.valueOf((int)e.getX()).substring(1,3))-1]).substring(0,3);
				date.setText(date1 + ", " + e.getY()+ "°C");
			}else if(len==4){
				date1 = String.valueOf((int)e.getX()).substring(0,2) + " " + (new DateFormatSymbols().getMonths()[Integer.decode(String.valueOf((int)e.getX()).substring(2,4))-1]).substring(0,3);
				date.setText(date1 + ", " + e.getY()+ "°C");
			}else{
				date1 = "Error";
				log.i("" + e.getX());
			}
		}

		public int getXOffset() {
			return -(getWidth() / 2);
		}

		public int getYOffset() {
			return -getHeight();
		}
		
		@Override
		public void draw(Canvas canvas, float posX, float posY) {
			super.draw(canvas, posX, posY);
			getOffsetForDrawingAtPoint(posX, posY);
		}
	}
	
		
	private class CustomMarkerView1 extends MarkerView {
		private TextView date;

		public CustomMarkerView1(Context context, int layoutResource) {
			super(context, layoutResource);
			date = findViewById(R.id.date);
		}

		@Override
		public void refreshContent(Entry e, Highlight highlight) {
				int date1 = (int)e.getX();
				if(date1>23){
					date.setText(date1-24 + " Hrs, " + e.getY() + "°C");
				}else{
					date.setText(date1 + " Hrs, " + e.getY() + "°C");
				}
		}

		public int getXOffset() {
			return -(getWidth() / 2);
		}

		public int getYOffset() {
			return -getHeight();
		}

		@Override
		public void draw(Canvas canvas, float posX, float posY) {
			super.draw(canvas, posX, posY);
			getOffsetForDrawingAtPoint(posX, posY);
		}
	}
	
	
}
