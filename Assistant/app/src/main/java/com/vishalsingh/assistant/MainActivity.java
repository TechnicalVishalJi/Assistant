package com.vishalsingh.assistant;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.speech.*;
import android.speech.tts.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import org.json.*;
import org.mariuszgromada.math.mxparser.*;
import pl.droidsonroids.gif.*;

import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;

public class MainActivity extends Activity
{
	LinearLayout linearLayout, calculator, horizontalScroll, mic, scroll;
	SpeechRecognizer recognizer;
	TextToSpeech tts;
	EditText searchBox;
	InputMethodManager imm;
	GifImageView listening;
	Intent intent;
	Handler handler = new Handler();
	RelativeLayout suggestArea;
	ListView listView;
	SharedPreferences historyFile;
	WebView webview;
	Boolean show = false, equalPressed = true, operatorClicked = false;
	CustomizedExceptionHandler log;
	String titleResult = null, answerResult = null, snippetResult = null, calcExpression = "";
	JSONArray imagesResult = null, listResult = null;
	TextView solutionText, resultText;
	String key1 = "f93b6b0875d0dfe6c31c582a2b2931da0131acb18b7eb43ac062004562738c23",
	key2 = "533cb7d8820f4b84a3aca495c719f8ccc42a6428281d776315e7c7a50667a495";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		log = new CustomizedExceptionHandler("Crash Reports", "App");
		Thread.setDefaultUncaughtExceptionHandler(log);
        setContentView(R.layout.main);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy);
	
		
		
		checkPermission(Manifest.permission.RECORD_AUDIO, 100);
		
		linearLayout = findViewById(R.id.showContent);
		searchBox = findViewById(R.id.edittext);
		mic = findViewById(R.id.mic);
		listening = findViewById(R.id.listen);
		suggestArea = findViewById(R.id.suggestionArea);
		listView = findViewById(R.id.mainListView);
		horizontalScroll = findViewById(R.id.mainHorizontalScrollView);
		historyFile = getSharedPreferences("vishalsingh.assistant.history", Context.MODE_PRIVATE);
		webview = findViewById(R.id.mainWebView);
		horizontalScroll.removeAllViews();
		scroll = findViewById(R.id.mainScrollView);
		addTextView(1, "Hello Vishal, how can I help?");

		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView webView, String url) {
					webView.loadUrl(url);
					return true;
				}
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);

				}
				@Override  
				public void onPageFinished(final WebView view, String url) {
					super.onPageFinished(view, url);
					view.evaluateJavascript("javascript:(function() { " + "document.getElementById('taw').style.display='none'; " + "document.getElementsByClassName('Fh5muf')[0].style.display='none'; " + "document.getElementsByClassName('hgKEIc')[0].style.display='none'; " + "})()", null);
					view.setVisibility(View.VISIBLE);
				}
			});



		mic.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					recognizer.startListening(intent);
				}
			});

		listening.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					recognizer.stopListening();
					mic.setVisibility(View.VISIBLE);
					listening.setVisibility(View.GONE);
				}
			});


			
		setSuggestions(0, "");

		
		searchBox.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// If the event is a key-down event on the "enter" button
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
						// Perform action on key press
						String query = searchBox.getText().toString();
						searchBox.clearFocus();
						searchBox.setVisibility(View.GONE);
						suggestArea.setVisibility(View.VISIBLE);
						linearLayout.removeAllViews();
						scroll.removeAllViews();
						if (query.equals("")){
							addTextView(1, "Please type something to search!");
						}else{
							addTextView(0, query);
							giveResult(query);
						}
				 		return true;
					}

					return false;
				}
			});



		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

		searchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View view, boolean hasFocus) {
					if (hasFocus) {
						imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
					} else {
						imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					}
				}
			});
		
		
		tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener(){
				@Override
				public void onInit(int i)
				{
					if(i==TextToSpeech.SUCCESS){
						tts.setLanguage(Locale.CANADA);
						tts.setSpeechRate(1.0f);
					}
				}
			});
			
			
			
		intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.vishalsingh.assistant");

		recognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
	
		RecognitionListener listener = new RecognitionListener() {
			
			@Override
			public void onResults(Bundle results) {
				ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				if (voiceResults != null) {
					for (String match : voiceResults) {
							linearLayout.removeAllViews();
							scroll.removeAllViews();
							addTextView(0, match);
							giveResult(match);
							break;
					}
				}
			}

			@Override
			public void onReadyForSpeech(Bundle params) {
				mic.setVisibility(View.GONE);
				listening.setVisibility(View.VISIBLE);
			}

			
			@Override
			public void onError(int error) {
				if(error == 8){
					addTextView(1, "Mic was busy? Please ask again");
				}
				else if(error == 9){
					addTextView(1, "Some permissions are denied! Please allow them");
					checkPermission(Manifest.permission.RECORD_AUDIO, 100);
				}
				//scrollDown();
				mic.setVisibility(View.VISIBLE);
				listening.setVisibility(View.GONE);
				/**
				 *  ERROR_NETWORK_TIMEOUT = 1;
				 *  ERROR_NETWORK = 2;
				 *  ERROR_AUDIO = 3;
				 *  ERROR_SERVER = 4;
				 *  ERROR_CLIENT = 5;
				 *  ERROR_SPEECH_TIMEOUT = 6;
				 *  ERROR_NO_MATCH = 7;
				 *  ERROR_RECOGNIZER_BUSY = 8;
				 *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
				 *
				 * @param error code is defined in SpeechRecognizer
				 */
				 
			}

			@Override
			public void onBeginningOfSpeech() {}

			@Override
			public void onBufferReceived(byte[] buffer) {}

			@Override
			public void onEndOfSpeech() {
				listening.setVisibility(View.GONE);
				mic.setVisibility(View.VISIBLE);
			}

			@Override
			public void onEvent(int eventType, Bundle params) {}

			@Override
			public void onPartialResults(Bundle partialResults) {}

			@Override
			public void onRmsChanged(float rmsdB) {}
		};
		recognizer.setRecognitionListener(listener);
		recognizer.startListening(intent);
		
		
		
	 }
	 
	
	 
	 
	 
	 /*public void scrollDown(){
		 scroll.post(new Runnable() {
				 @Override
				 public void run() {
					 scroll.fullScroll(ScrollView.FOCUS_DOWN);
				 }
		});
	 }*/
	 
	 
	 
	 
	public void goWeather(View v){
		Intent myIntent = new Intent(MainActivity.this, WeatherActivity.class);
		//myIntent.putExtra("key", value); //Optional parameters
		MainActivity.this.startActivity(myIntent);
	}
	
	 
	 
	 
	
	 public void setSuggestions(int where, String query){
		 if ( where == 0){
			 String history = historyFile.getString("history", "");
			 if(history.contains("##")){
				 String[] array = history.split("##");
				 int length = array.length;
				 for (int i = 0; i < length; i++){
					if (array[i].equals("")){break;}
				 	TextView textView = new TextView(MainActivity.this);
					textView.setText(array[i]);
					RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				 	textView.setTextSize((float) 17);
					params.leftMargin = 40;
					textView.setId(R.id.suggestText);
					textView.setLayoutParams(params);
					textView.setForeground(getResources().getDrawable(R.drawable.ripple));
					textView.setOnClickListener(new View.OnClickListener(){
							 @Override
							 public void onClick(View view)
							 {
								 TextView text = view.findViewById(R.id.suggestText);
								 String text1 = text.getText().toString();
								 linearLayout.removeAllViews();
								 scroll.removeAllViews();
								 addTextView(0, text1);
								 giveResult(text1);
							 }
					});
					textView.setTextColor(getResources().getColor(R.color.white));
					textView.setPadding(15, 10, 15, 10);
					textView.setBackgroundResource(R.drawable.suggestion_bg);
				 	horizontalScroll.addView(textView, 0);
					if(i==4){break;}
				 }
				 if(array.length >5){
					 history = "";
					 for (int i = array.length - 5; i <= array.length-1; i++)
					 {
						 history = history + array[i] + "##";
					 }
					 historyFile.edit().putString("history", history).apply();
				 }
			 }
		 }else if(where == 2){
			 TextView textView = new TextView(MainActivity.this);
			 textView.setText(query);
			 RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			 textView.setTextSize((float) 17);
			 params.leftMargin = 40;
			 textView.setLayoutParams(params);
			 textView.setTextColor(getResources().getColor(R.color.white));
			 textView.setPadding(15, 10, 15, 10);
			 textView.setId(R.id.suggestText);
			 textView.setForeground(getResources().getDrawable(R.drawable.ripple));
			 textView.setOnClickListener(new View.OnClickListener(){
					 @Override
					 public void onClick(View view)
					 {
						 TextView text = view.findViewById(R.id.suggestText);
						 String text1 = text.getText().toString();
						 linearLayout.removeAllViews();
						 scroll.removeAllViews();
						 addTextView(0, text1);
						 giveResult(text1);
					 }
				 });
			 textView.setBackgroundResource(R.drawable.suggestion_bg);
			 horizontalScroll.addView(textView, 0);
		 }else{
			 String history = historyFile.getString("history", null);
			 if(history.contains("##")){
				 String[] array = history.split("##");
				 int length = array.length;
				 for (int i = 0; i < length; i++){
					 if (array[i].equals("")){break;}
					 if(i==4){break;}
				 }
			 }
		 }
	}
	 
	 
	 
	 
	 public void addTextView(int side, String string){
		 final TextView text = new TextView(MainActivity.this);
		 text.setText(string);
		 LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		 text.setTextSize((float) 17);
		 if(side == 0){
			 params.rightMargin = 40;
			 params.topMargin  = 25;
			 params.leftMargin = 50;
			 params.bottomMargin= 25;
			 text.setElevation(15);
			 text.setTextColor(getResources().getColor(R.color.white));
			 text.setPadding(30, 15, 30, 15);
			 text.setBackgroundResource(R.drawable.speech_bg);
		 }
		 else{
			 params.leftMargin = 50;
			 params.topMargin  = 25;
			 params.rightMargin = 40;
			 params.bottomMargin= 25;
			 params.gravity = Gravity.LEFT;
			 text.setElevation(15);
			 text.setTextColor(getResources().getColor(R.color.black));
			 text.setPadding(30, 15, 30, 15);
			 text.setBackgroundResource(R.drawable.speech_bg_opposite);
		 }
		 text.setLayoutParams(params);
		 runOnUiThread(new Runnable(){@Override public void run(){
		 	linearLayout.addView(text);
		 }});
	 }
	 
	 
	 public void callKeyboard(View view){
		 if(searchBox.getVisibility() == View.GONE){
			 searchBox.setText("");
			 searchBox.setVisibility(View.VISIBLE);
			 suggestArea.setVisibility(view.GONE);
			 searchBox.requestFocus();
		}
		else{
			 searchBox.setVisibility(View.GONE);
			 suggestArea.setVisibility(View.VISIBLE);
			 searchBox.clearFocus();
		}
	 }
	 
	 
	 
	public static String[] getLinks(String key, int n) throws MalformedURLException, IOException {
		String url = "http://www.google.com/search?q=";
		String charset = "UTF-8";
		String query = String.format("%s", URLEncoder.encode(key, charset));
		URLConnection con = new URL(url + query).openConnection();
		con.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		String wholeThing = "";
		while ((inputLine = in.readLine()) != null) wholeThing += inputLine;
		in.close();

		List<String> strings = new ArrayList<String>();
		String search = "<h3 class=\"r\"><a href=\"/url?q=";
		int stringsFound = 0;
		int searchChar = search.length();
		while(stringsFound < n && searchChar <= wholeThing.length()) {
			if(wholeThing.substring(searchChar - search.length(), searchChar).equals(search)) {
				int endSearch = 0;
				while(!wholeThing.substring(searchChar + endSearch, searchChar + endSearch + 4).equals("&amp")) {
					endSearch++;
				}
				strings.add(wholeThing.substring(searchChar, searchChar + endSearch));
				stringsFound++;
			}
			searchChar++;
		}
		String[] out = new String[strings.size()];
		for(int i = 0; i < strings.size(); i++) {
			out[i] = strings.get(i);
		}
		return out;
	}
	
	
	
	
	public void checkPermission(String permission, int requestCode)
	{
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
		}
	}
	
	
	
	public void newTextView(final LinearLayout layout, String text, int size){
		final TextView textView = new TextView(this);
		LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		textView.setTextSize((float) size);
		textView.setTextColor(getResources().getColor(R.color.black));
		textView.setPadding(25, 0, 10, 25);
		textView.setLayoutParams(params);
		textView.setText(text);
		textView.setTextIsSelectable(true);
		runOnUiThread(new Runnable(){@Override public void run(){
			layout.addView(textView);
		}});
	}
	
	
	
	
	
	public ArrayList<Double> getLocation(){
        Location gps_loc = null;
        Location network_loc = null;
        Location final_loc = null;
        double longitude = 0.0;
        double latitude = 0.0;
		ArrayList<Double> locationArray = new ArrayList<Double>();
		
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,200);
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 300);
		checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,400);
			
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
			|| ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
					
				Toast.makeText(MainActivity.this, "Cannot get location, permission denied", Toast.LENGTH_SHORT).show();
				return locationArray;
		}else{
			try {
				gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (gps_loc != null) {
				final_loc = gps_loc;
				latitude = final_loc.getLatitude();
				longitude = final_loc.getLongitude();
			}
			else if (network_loc != null) {
				final_loc = network_loc;
				latitude = final_loc.getLatitude();
				longitude = final_loc.getLongitude();
			}
		}
		locationArray.add(latitude);
		locationArray.add(longitude);
		return locationArray;
	}
	
	
	
	
	
	
	
	
	
	public void addinfo(String info, ArrayList<String> imgSrcs, String data){
		try{
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.wiki_layout, null);
		TextView textview = view.findViewById(R.id.info);
		LinearLayout imgSection = view.findViewById(R.id.img_section);
		
		    int size = imagesResult.length();
		    for(int i = 0; i < size; i++){
			try{
			    ImageView imageView1 = new ImageView(this);
			    new DownloadImageTask(imageView1).execute(imagesResult.getString(i));
			    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,(int) getResources().getDimension(R.dimen.dp150));
			    imageView1.setBackgroundResource(R.drawable.wiki_info);
			    params.rightMargin = (int) getResources().getDimension(R.dimen.dp5);
			    imageView1.setClipToOutline(true);
			    imageView1.setLayoutParams(params);
			    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
			    imageView1.setAdjustViewBounds(true);
			    imgSection.addView(imageView1);
			}catch(final Exception e){
				runOnUiThread(new Runnable(){@Override public void run(){
					log.e(MainActivity.this, e);
				}});
			}
		    }
		
		String text = info +"\n\n" + data;
		textview.setText(text);
		
		runOnUiThread(new Runnable(){@Override public void run(){
			scroll.addView(view);
			scroll.setVisibility(View.VISIBLE);
		}});
		}catch (final Exception e){
			runOnUiThread(new Runnable(){@Override public void run(){
				Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
			}});
		}
	}
	
	
	
	
	
	public void addMoreResults(){
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.wiki_layout, null);
		TextView textview = view.findViewById(R.id.info);
		LinearLayout imgSection = view.findViewById(R.id.img_section);

		if(imagesResult != null){
			int size = imagesResult.length();
			for(int i = 0; i < size; i++){
			    try{
				ImageView imageView1 = new ImageView(this);
				new DownloadImageTask(imageView1).execute(imagesResult.getString(i));
				LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,(int) getResources().getDimension(R.dimen.dp150));
				imageView1.setBackgroundResource(R.drawable.wiki_info);
				params.rightMargin = (int) getResources().getDimension(R.dimen.dp5);
				imageView1.setClipToOutline(true);
				imageView1.setLayoutParams(params);
				imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView1.setAdjustViewBounds(true);
				imgSection.addView(imageView1);
			    }catch(final Exception e){
					runOnUiThread(new Runnable(){@Override public void run(){
						log.e(MainActivity.this, e);
					}});
				}
			}
		}
		
		if(snippetResult != null){
			textview.setText(snippetResult);
			textview.setVisibility(View.VISIBLE);
		}
		if(listResult != null){
			final LinearLayout main = view.findViewById(R.id.wiki_layoutLinearLayout);
			runOnUiThread(new Runnable(){@Override public void run(){
				for(int i = 0; i<listResult.length(); i++){
					newTextView(main, String.valueOf(i+1 + ". ") + listResult.optString(i), 15);
				}
			}});
		}
		runOnUiThread(new Runnable(){@Override public void run(){
			scroll.addView(view);
			if(linearLayout.getChildCount()>2){
		    	linearLayout.removeViewAt(2);
			}
			scroll.setVisibility(View.VISIBLE);
		}});
	}
	
	
	
	
	public void showCurrencyConverter(final Double fromPrice, String fromCurrency, final Double toPrice, String toCurrency, JSONArray chartArray, String dateToday){
	    try{
	    ArrayList<String> months = new ArrayList<>();
	    ArrayList<Integer> date = new ArrayList<>();
	    ArrayList<Double> price = new ArrayList<>();
	    int size = chartArray.length();
	    for(int i = 0; i<size; i++){
		JSONObject obj = chartArray.getJSONObject(i);
		price.add(obj.getDouble("price"));
		String[] dateTime =  obj.getString("time").split(" ")[0].split("-");
		date.add(Integer.parseInt(dateTime[2]));
		if(i == 0 || i == size -1){
		        String mon = dateTime[1];
			if(mon.equals("01")){months.add("Jan");}
			else if(mon.equals("02")){months.add("Feb");}
			else if(mon.equals("03")){months.add("Mar");}
			else if(mon.equals("04")){months.add("Apr");}
			else if(mon.equals("05")){months.add("May");}
			else if(mon.equals("06")){months.add("Jun");}
			else if(mon.equals("07")){months.add("Jul");}
			else if(mon.equals("08")){months.add("Aug");}
			else if(mon.equals("09")){months.add("Sep");}
			else if(mon.equals("10")){months.add("Oct");}
			else if(mon.equals("11")){months.add("Nov");}
			else if(mon.equals("12")){months.add("Dec");}
		}
	    }
	        
		String html = "<html lang='en'><head>" +
		"</head><body>"+
		"<canvas id='myChart'></canvas>"+
		    "<script type='text/javascript' src='chart.js'></script>"+
		    "<script type='text/javascript'>"+
		    "const dates = " + date.toString() +";"+
		    "const data = {"+
		    "labels: dates,"+
		    " datasets: [{"+
		    "label: 'Rate',"+
		    "data: " + price.toString() + ","+
		    "fill: true,"+
		    "backgroundColor: '#3C9AFA30',"+
		    "borderColor: '#3C9AFA',"+
		    "tension: .4"+
		    "}]"+
		    "};"+
		    "const config = {"+
		    "type: 'line',"+
		    "data: data,"+
		    "options: {"+
		    "plugins: {"+
		    "legend : {"+
		    "display: false"+
		    " }"+
		    "},"+
		    "radius: 0,"+
		    "hitRadius: 100,"+
		    "responsive: true,"+
		    "scales: {"+
		    "x: {"+
		    "title: {"+
		    "display: true,"+
		    "text: '" + months.get(0) + "-" + months.get(1) + " : Dates'"+
		    "},"+
		    "grid: {"+
		    "display: false"+
		    "},}}}};"+
		    "const myChart = new Chart("+
		    "document.getElementById('myChart'),"+
		    "config);"+
		    "</script>"+
		"</body></html>";
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout menu = (LinearLayout) inflater.inflate(R.layout.currency_layout, null);
		TextView heading = menu.findViewById(R.id.currency_layoutTextView1);
		TextView subHeading = menu.findViewById(R.id.currency_layoutTextView);
		WebView web = menu.findViewById(R.id.currency_layoutWebView);
		final EditText firstBox = menu.findViewById(R.id.currencylayoutEditText1);
		final EditText secondBox = menu.findViewById(R.id.currencylayoutEditText2);
		TextView fromText = menu.findViewById(R.id.currencylayoutTextView1);
		TextView toText = menu.findViewById(R.id.currencylayoutTextView2);
		TextView dateText = menu.findViewById(R.id.currency_layoutDate);
		
		heading.setText(fromPrice + " "+ fromCurrency + " equals" );
		subHeading.setText(toPrice + " " + toCurrency);
		web.getSettings().setJavaScriptEnabled(true);
		web.setWebViewClient(new WebViewClient(){
					@Override
					public boolean shouldOverrideUrlLoading(WebView webView, String url) {
						return true;
					}
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
					}
					@Override  
					public void onPageFinished(final WebView view, String url) {
						super.onPageFinished(view, url);
					}
				});
		web.loadDataWithBaseURL("file:///android_res/drawable/", html,"text/html", "utf-8", null);
	    firstBox.setText(String.valueOf(fromPrice));
		secondBox.setText(String.valueOf(toPrice));
		dateText.setText(dateToday);
		fromText.setText(fromCurrency);
		toText.setText(toCurrency);
		runOnUiThread(new Runnable(){@Override public void run(){
			linearLayout.addView(menu);
		}});
		
			firstBox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// If the event is a key-down event on the "enter" button
						if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
							// Perform action on key press
							Double oneUnit = toPrice/fromPrice;
							secondBox.setText(String.valueOf(Double.parseDouble(firstBox.getText().toString())*oneUnit));
						}
						return false;
					}
				});
				
			secondBox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// If the event is a key-down event on the "enter" button
						if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
							// Perform action on key press
							Double oneUnit = fromPrice/toPrice;
							firstBox.setText(String.valueOf(Double.parseDouble(secondBox.getText().toString())*oneUnit));
						}
						return false;
					}
				});
		
	    }catch(final Exception e){
			runOnUiThread(new Runnable(){@Override public void run(){
				log.e(MainActivity.this, e);
			}});
		}
	}
	
	
	
	
	
	public void calculate(View v){
		if(equalPressed){
			resultText.setText("");
			solutionText.setText("");
			calcExpression = "";
			equalPressed = false;
		}
		String btn = ((TextView)v).getText().toString();
		if(btn.equals("C")){
			try{
				String result = resultText.getText().toString();
				if(result.length() != 0 && calcExpression.length() != 0){
					resultText.setText(result.substring(0, result.length()-1));
					calcExpression = calcExpression.substring(0, calcExpression.length()-1);
					operatorClicked = false;
				}else{
					solutionText.setText("");
				}
			}catch(Exception e){Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();}
		}else if(btn.equals("=")){
			String query = resultText.getText().toString();
			if(!query.endsWith("+") & !query.endsWith("-") & !query.endsWith("×") & !query.endsWith("/")){
				solutionText.setText(query);
				String text = String.valueOf(new Expression(calcExpression).calculate());
				if(text.endsWith(".0")){
					text = text.replace(".0", "");
				}else if(text.equals("NaN") || resultText.getText().toString().equals(text)){
					return;
				}
				resultText.setText(text);
				equalPressed = true;
			}
			return;
		}else if(btn.equals("(")){
			resultText.setText(resultText.getText().toString() + btn);
			calcExpression+=btn;
			operatorClicked = false;
			return;
		}else if(btn.equals(")")){
			resultText.setText(resultText.getText().toString() + btn);
			calcExpression+=btn;
			operatorClicked = false;
			return;
		}else if(btn.equals("+") | btn.equals("-") | btn.equals("×") | btn.equals("/")){
			if(!operatorClicked){
				String query = resultText.getText().toString();
				if(!query.endsWith("+") & !query.endsWith("-") & !query.endsWith("×") & !query.endsWith("/")){
					resultText.setText(query + btn);
					calcExpression+=btn;
					operatorClicked = true;
				}
			}
			return;
		}else{
			resultText.setText(resultText.getText().toString() + btn);
			calcExpression+=btn;
			operatorClicked = false;
		}
		String text = String.valueOf(new Expression(calcExpression).calculate());
		if(text.endsWith(".0")){
			text = text.replace(".0", "");
		}else if(text.equals("NaN")){
			return;
		}
		solutionText.setText(text);
	}
	
	
	
	
	
	
	
	public void showCalculator(String result){
		LayoutInflater Inflater = LayoutInflater.from(this);
		calculator = (LinearLayout) Inflater.inflate(R.layout.calculator_layout, null);
		solutionText =  calculator.findViewById(R.id.solution);
		resultText = calculator.findViewById(R.id.result);
		resultText.setText("Ans. " + result);
		runOnUiThread(new Runnable(){@Override public void run(){
			linearLayout.addView(calculator);
		}});
	}
	
	
	
	public void addLoader(){
		LayoutInflater inflator = LayoutInflater.from(this);
		LinearLayout loading = (LinearLayout) inflator.inflate(R.layout.loader, null);
		linearLayout.addView(loading);
	}
	
	
	
	public void removeLoader(){
		runOnUiThread(new Runnable(){@Override public void run(){
			linearLayout.removeViewAt(1);
		}});
		
	}
	
	
	
	public void giveResult(final String query)
	{
		
		titleResult = null;
		answerResult = null;
		snippetResult = null;
		imagesResult = null;
		webview.setVisibility(View.GONE);
		
		addLoader();
		
		Thread thread = new Thread(new Runnable(){@Override public void run(){
			try
			{
			if (query.contains("meaning of "))
			{
				final String word1 = query.split("meaning of ")[1];
				
					URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + URLEncoder.encode(word1));
					BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
					String JSON_STRING = "";
					String n;
					while ((n = in.readLine()) != null)
					{
						JSON_STRING += n;
					}
					in.close();
					//oldresult = false;
				
				
				if (!JSON_STRING.isEmpty())
				{
					JSONObject json = new JSONArray(JSON_STRING).getJSONObject(0);
					String word = json.getString("word");
					JSONArray meanings = json.getJSONArray("meanings");
					LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
					final LinearLayout dictionaryLayout = (LinearLayout) inflater.inflate(R.layout.dictionary_layout, null);
					LinearLayout dictionaryBox = dictionaryLayout.findViewById(R.id.dictionaryBox);
					((TextView) dictionaryBox.findViewById(R.id.heading)).setText(word);
					String def1 = "";
					for (int i = 0; i < meanings.length(); i++)
					{
						JSONObject obj = meanings.getJSONObject(i);
						String partOfSpeech= obj.getString("partOfSpeech");
						JSONArray definitions = obj.getJSONArray("definitions");
						LinearLayout dictionaryBox1 = (LinearLayout) inflater.inflate(R.layout.dictionary_box1, null);
						((TextView) dictionaryBox1.findViewById(R.id.type)).setText(partOfSpeech);
						for (int j=0; j < definitions.length(); j++)
						{
							JSONObject definitionBox = definitions.getJSONObject(j);
							String definition = definitionBox.getString("definition");
							if (j==0){def1 = definition;}
							if (definitionBox.has("example"))
							{
								definition = definition + "\nExample: " + definitionBox.getString("example");
							}
							LinearLayout dictionaryBox2 = (LinearLayout) inflater.inflate(R.layout.dictionary_box2, null);
							((TextView)dictionaryBox2.findViewById(R.id.number)).setText(String.valueOf(j + 1) + ".");
							((TextView)dictionaryBox2.findViewById(R.id.example)).setText(definition);
							((LinearLayout) dictionaryBox1.findViewById(R.id.dictionary_box1)).addView(dictionaryBox2);
						}
						JSONArray synonyms = obj.getJSONArray("synonyms");
						String synonymsText = "";
						int ssize = synonyms.length();
						for (int j = 0; j < ssize; j++)
						{
							if (j != ssize - 1)
							{
								synonymsText += synonyms.getString(j) + ", ";
							}
							else
							{
								synonymsText += synonyms.getString(j);
							}
						}
						if (!synonymsText.isEmpty())
						{
							TextView synonymBox = dictionaryBox1.findViewById(R.id.synonyms);
							synonymBox.setText("Synonyms: " + synonymsText);
							synonymBox.setVisibility(View.VISIBLE);
						}
						JSONArray antonyms = obj.getJSONArray("antonyms");
						String antonymsText = "";
						int asize = antonyms.length();
						for (int j = 0; j < asize; j++)
						{
							if (j != asize - 1)
							{
								antonymsText += antonyms.getString(j) + ", ";
							}
							else
							{
								antonymsText += antonyms.getString(j);
							}
						}
						if (!antonymsText.isEmpty())
						{
							TextView antonymBox = dictionaryBox1.findViewById(R.id.antonyms);
							antonymBox.setText("Antonyms: " + synonymsText);
							antonymBox.setVisibility(View.VISIBLE);
						}
						dictionaryBox.addView(dictionaryBox1);
					}
					final String def2 = def1;
					runOnUiThread(new Runnable(){@Override public void run(){
						scroll.addView(dictionaryLayout);
						scroll.setVisibility(View.VISIBLE);
						tts.speak("Definition of " + word1 + " is " + def2, TextToSpeech.QUEUE_FLUSH, null);
					}});
				}
			}else if(query.contains("weather")){
				if(query.equals("weather"))
				{
					runOnUiThread(new Runnable(){@Override public void run(){
						webview.loadUrl("https://www.google.com/search?q=" + "weather+in+Chaubepur+Pankhan+Kanpur" + "&aqs=chrome.1.69i60j69i59j0i131i433i512j0i131i433i457i512j69i60l4j0i402j0i131i433i512.3657j0j9&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8");
							}});
				}else
				{
					runOnUiThread(new Runnable(){@Override public void run(){
						webview.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(query) + "&aqs=chrome.1.69i60j69i59j0i131i433i512j0i131i433i457i512j69i60l4j0i402j0i131i433i512.3657j0j9&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8");
							}});
				}
			}else if(query.equals("stop"))
			{
				removeLoader();
				addTextView(1, "Ok");
				return;
			}
			else
			{
					String jsonurl = "https://serpapi.com/search.json?engine=google&q=" + URLEncoder.encode(query) + "&location=Uttar+Pradesh%2C+India&google_domain=google.com&gl=in&hl=en&device=mobile&api_key=" + key1;
					URL url = new URL(jsonurl);
					BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
					String JSON_STRING ="";
					String n;
					while ((n = in.readLine()) != null)
					{
						JSON_STRING += n;
					}
					in.close();

				if (!JSON_STRING.isEmpty())
				{
					JSONObject json = new JSONObject(JSON_STRING);
					if (json != null)
					{
						if (json.has("answer_box"))
						{
							final JSONObject answerBox= json.getJSONObject("answer_box");
							String type = answerBox.getString("type");
							if (type.equals("organic_result"))
							{

								if (answerBox.has("answer"))
								{
									answerResult = answerBox.getString("answer");
								}
								if (answerResult != null)
								{
									
									runOnUiThread(new Runnable(){@Override public void run(){
										
										addTextView(1, answerResult);
									}});
								}

								if (answerBox.has("snippet"))
								{
									snippetResult = answerBox.getString("snippet");
								}
								if (answerBox.has("images"))
								{
									imagesResult = answerBox.getJSONArray("images");
								}
								if (answerBox.has("list"))
								{
									listResult = answerBox.getJSONArray("list");
								}
								if ((snippetResult != null || imagesResult != null)  &&  answerResult != null)
								{
									final TextView text = new TextView(MainActivity.this);
									text.setText("Show more?");
									LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
									text.setTextSize((float) 17);
									params.leftMargin = 50;
									params.topMargin  = 25;
									params.rightMargin = 40;
									params.bottomMargin = 25;
									params.gravity = Gravity.LEFT;
									text.setElevation(15);
									text.setTextColor(getResources().getColor(R.color.black));
									text.setPadding(30, 15, 30, 15);
									text.setBackgroundResource(R.drawable.speech_bg_opposite);
									text.setLayoutParams(params);
									text.setOnClickListener(new OnClickListener(){
										    @Override
										    public void onClick(View p1)
										    {
												addMoreResults();
										    }
										});
									runOnUiThread(new Runnable(){@Override public void run(){
										linearLayout.addView(text);
									}});
								}
								else if (snippetResult != null || imagesResult != null)
								{
									if (snippetResult != null && imagesResult != null)
									{
										runOnUiThread(new Runnable(){@Override public void run(){
											addMoreResults();
											
												}});
									}
									else
									{
										if (snippetResult != null)
										{
											runOnUiThread(new Runnable(){@Override public void run(){
										    	
												addTextView(1, snippetResult);
											}});
										}
										if (imagesResult != null)
										{
										    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
										    final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.wiki_layout, null);
										    final LinearLayout imgSection = view.findViewById(R.id.img_section);

										    int size = imagesResult.length();
										    for (int i = 0; i < size; i++)
											{
												try
												{
													final ImageView imageView1 = new ImageView(MainActivity.this);
													new DownloadImageTask(imageView1).execute(imagesResult.getString(i));
													LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(R.dimen.dp150));
													imageView1.setBackgroundResource(R.drawable.wiki_info);
													params.rightMargin = (int) getResources().getDimension(R.dimen.dp5);
													imageView1.setClipToOutline(true);
													imageView1.setLayoutParams(params);
													imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
													imageView1.setAdjustViewBounds(true);
													runOnUiThread(new Runnable(){@Override public void run(){
														imgSection.addView(imageView1);
													}});
												}
												catch (final Exception e)
												{
													runOnUiThread(new Runnable(){@Override public void run(){
														log.e(MainActivity.this, e);
													}});
												}
										    }
											if (listResult != null)
											{
												LinearLayout main = view.findViewById(R.id.wiki_layoutLinearLayout);
												for (int i = 0; i < listResult.length(); i++)
												{
													newTextView(main, String.valueOf(i + 1 + ". ") + listResult.optString(i), 15);
												}
											}
											runOnUiThread(new Runnable(){@Override public void run(){
										    	scroll.addView(view);
										    	scroll.setVisibility(View.VISIBLE);
											}});

										}
										
									}
								}
								if (answerResult == null && snippetResult == null && imagesResult == null)
								{
									runOnUiThread(new Runnable(){@Override public void run(){
										webview.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(query) + "&oq=ta&aqs=chrome.1.69i60j69i59j69i60l4j69i57j69i59l2j0i131i433i512.1353j0j4&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8&sbfbu=0");
									}});
								}
								if (snippetResult != null)
								{
									log.i(query + "?\n" + "Ans." + snippetResult);
								}

							}
							else if (type.equals("calculator_result"))
							{
								showCalculator(answerBox.getString("result"));
							}
							else if (type.equals("population_result"))
							{
								addTextView(1, answerBox.getString("population"));
								
								JSONArray others =  answerBox.getJSONArray("other");
								if (others != null)
								{
									String otherInfo = "";
									for (int i = 0; i < others.length(); i++)
									{
										if (i == others.length() - 1)
										{
										    otherInfo += String.valueOf(i + 1) + ". " + others.getJSONObject(i).getString("place") + " : " + others.getJSONObject(i).getString("population");
										}
										else
										{
										    otherInfo += String.valueOf(i + 1) + ". " + others.getJSONObject(i).getString("place") + " : " + others.getJSONObject(i).getString("population") + "\n";
										}
									}
									
									addTextView(1, "Some other Places:" + "\n" + otherInfo);
									
								}

							}
							else if (type.equals("currency_converter"))
							{
								JSONObject obj = answerBox.getJSONObject("currency_converter");
								final JSONObject from = obj.getJSONObject("from");
								final JSONObject to = obj.getJSONObject("to");
								final JSONArray chartArray = answerBox.getJSONArray("chart");
								final String date = answerBox.getString("date");
								runOnUiThread(new Runnable(){@Override public void run(){
											try
											{
												showCurrencyConverter(from.getDouble("price"), from.getString("currency"), to.getDouble("price"), to.getString("currency"), chartArray, date);
											}
											catch (JSONException e)
											{
												log.e(MainActivity.this, e);
											}
										}});
							}
							else if (type.equals("dictionary_results"))
							{
								addTextView(1, "If you want to search word meanings, then search like this \"meaning of <your word here>\"");
							}
							else
							{
								runOnUiThread(new Runnable(){@Override public void run(){
									webview.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(query) + "&oq=ta&aqs=chrome.1.69i60j69i59j69i60l4j69i57j69i59l2j0i131i433i512.1353j0j4&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8&sbfbu=0");
								}});
							}

						}
						else if (json.has("knowledge_graph"))
						{
							final JSONObject graph = json.getJSONObject("knowledge_graph");
							if (!graph.has("formula") & graph.has("description"))
							{
								LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
								final LinearLayout wikiLayout = (LinearLayout) inflater.inflate(R.layout.wiki_layout, null);
								TextView info = wikiLayout.findViewById(R.id.info);
								final LinearLayout imgSection = wikiLayout.findViewById(R.id.img_section);
								info.setVisibility(View.VISIBLE);
								JSONArray images = graph.optJSONArray("header_images");
								if (images != null)
								{
									ArrayList<String> imgSrcs = new ArrayList<>();
									for (int i =0; i < images.length(); i++)
									{
										String src= images.getJSONObject(i).getString("image");
										imgSrcs.add(src);
									}
									if (imgSrcs != null)
									{
										int size = imgSrcs.size();
										for (int i = 0; i < size; i++)
										{
											final ImageView imageView1 = new ImageView(MainActivity.this);
											new DownloadImageTask(imageView1).execute(imgSrcs.get(i));
											LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(R.dimen.dp150));
											imageView1.setBackgroundResource(R.drawable.wiki_info);
											params.rightMargin = (int) getResources().getDimension(R.dimen.dp5);
											imageView1.setClipToOutline(true);
											imageView1.setLayoutParams(params);
											imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
											imageView1.setAdjustViewBounds(true);
											runOnUiThread(new Runnable(){@Override public void run(){
												imgSection.addView(imageView1);
											}});
										}
									}
								}
								else
								{wikiLayout.findViewById(R.id.imageSectionLayout).setVisibility(View.GONE);}
								if (graph.has("type"))
								{
									info.setText(graph.getString("title") + " - " + graph.getString("type"));
								}
								else
								{
									info.setText(graph.getString("title"));
								}
								final LinearLayout wikiLinear = wikiLayout.findViewById(R.id.wiki_layoutLinearLayout);
								runOnUiThread(new Runnable(){@Override public void run(){
								try{
								newTextView(wikiLinear, graph.getString("description"), 15);
								if (graph.has("website"))
								{
									newTextView(wikiLinear , "About", 17);
									newTextView(wikiLinear, "Source: " + graph.getString("website"), 15);
								}

								JSONArray elements = graph.names();
								for (int i = 0; i < elements.length(); i++)
								{
									String element = elements.getString(i);
									String value = graph.optString(element);
									if (!value.isEmpty() & !value.startsWith("{") & !value.startsWith("[") & !value.endsWith("]") & !value.endsWith("}") & !element.equals("title") & !element.equals("type") & !element.equals("website") & !value.startsWith("https://") & !element.equals("description"))
									{
										newTextView(wikiLinear, element + ": " + value , 15);
									}
								}
								}catch(final Exception e){
									runOnUiThread(new Runnable(){@Override public void run(){
										log.e(MainActivity.this, e);
									}});
								}
								
									scroll.addView(wikiLayout);
									scroll.setVisibility(View.VISIBLE);
								}});
							}
							else
							{
								runOnUiThread(new Runnable(){@Override public void run(){
									webview.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(query) + "&oq=ta&aqs=chrome.1.69i60j69i59j69i60l4j69i57j69i59l2j0i131i433i512.1353j0j4&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8&sbfbu=0");
								}});
							}
						}
						else
						{
							runOnUiThread(new Runnable(){@Override public void run(){
								webview.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(query) + "&oq=ta&aqs=chrome.1.69i60j69i59j69i60l4j69i57j69i59l2j0i131i433i512.1353j0j4&client=ms-android-oppo&sourceid=chrome-mobile&ie=UTF-8&sbfbu=0");
							}});
						}
					}
				}
				else
				{
					addTextView(1, "Error finding results");
				}
			}
			String history = historyFile.getString("history", "");
			if (!history.contains(query))
			{
				historyFile.edit().putString("history", history + query + "##").apply();
				setSuggestions(2, query);
			}
			
			removeLoader();
		}
		catch (final Exception e)
		{
			runOnUiThread(new Runnable(){@Override public void run(){
				log.e(MainActivity.this, e);
			}});
		}
		
		}});
		thread.start();
		
	}
	 
}





class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try{
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			}catch(Exception e){}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap bitmap) {
		    bmImage.setImageBitmap(bitmap);
		}
}
