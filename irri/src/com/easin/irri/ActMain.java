package com.easin.irri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.easin.irri.MsgListView.OnRefreshListener;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;


public class ActMain extends ActionBarActivity {
	private String[] areas = new String[]{"加关注","查询","属性"};
	private String[] areasmy = new String[]{"取消关注","查询","属性"};
	private String stnmh,sbegin,send;
	private ViewPager mPager;
	private ChildViewPager mPagerHis;//页卡内容
    private List<View> listViews,listHis; // Tab页面列表
    //private ImageView cursor;// 动画图片
    //private TextView t1, t2, t3, t4;// 页卡头标
    private View t1,t2,t3,t4;
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度	
    private int mdelay;
    private MsgListView mrealtime; 
    private MyListAdapter mrealtimeadp;
    public Vector<Vector<STCDINFO>> mlistdata;
    
    private MsgListView mmyView; 
    private MyListAdapter mmyadp;
    
    private MsgListView mhistoryView; 
    private MyListAdapter mhistoryadp;    
    //public Vector<STCDINFO> mmydata;    

    private Map<String,String> msetup;
    private List<String> mmy;
    
    private TextView tvbegin, tvend,tvinfo; 
    //private LineChart lcHistory;
    //private View mHisData,mHisChart;
    
    @Override
    protected void onPause(){
    	super.onPause();
    	put(msetup);
    	mmyput(mmy);
    }
    private List<NameValuePair> PrepareCmd(int idx){
    	List<NameValuePair> ret = new ArrayList<NameValuePair>();
    	switch(idx){
    	case 0:{
    		NameValuePair cmd = new BasicNameValuePair("cmd", "realtime");
    		ret.add(cmd);
    	}
    		break;
    	case 1:
    		
    		break;
    	case 2:{
    		NameValuePair cmd = new BasicNameValuePair("cmd", "my");
    		ret.add(cmd);
    		StringBuilder sb = new StringBuilder();
    		for(String stcd : mmy){
    			sb.append(stcd).append(",");
    		}
    		if(sb.length() > 0){
    			sb.append("0");
    			NameValuePair stcds = new BasicNameValuePair("stcds", sb.toString());
    			ret.add(stcds);
    		}
    		
    	}
    		break;
    	case 3:
    		break;
    	}
    	return ret;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	stnmh = "10001001";
    	sbegin = "";
    	send = "";
    	msetup = get();
    	mmy = mmyget();
    	mlistdata = new Vector<Vector<STCDINFO>>();
    	mlistdata.setSize(5);
    	mlistdata.set(0, new Vector<STCDINFO>());
    	mlistdata.set(1, new Vector<STCDINFO>());
    	mlistdata.set(2, new Vector<STCDINFO>());
        setContentView(R.layout.act_main);
        mdelay = 50;
        InitTextView();
        InitViewPager();
        InitRealTime();
        InitMyView();
        InitHistory();
		new Timer().schedule(new TimerTask() {              
            @Override  
            public void run() {
            	if(currIndex == 0) 
            		mrealtime.refreshListener.onRefresh();
            	else if(currIndex == 1)
            		mhistoryView.refreshListener.onRefresh();
            	else if(currIndex == 2)
            		mmyView.refreshListener.onRefresh();
            }   
        }, 100, mdelay*1000);   		
    }
    
    private void InitViewPager() {    // 初始化ViewPager
        mPager = (ViewPager) findViewById(R.id.viewPager);
        listViews = new ArrayList<View>();
        listHis = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.realtime, null));
        listViews.add(mInflater.inflate(R.layout.history_tab, null));
        listViews.add(mInflater.inflate(R.layout.my, null));
        listViews.add(mInflater.inflate(R.layout.about, null));
        mrealtime = (MsgListView) listViews.get(0).findViewById(android.R.id.list);
        mmyView = (MsgListView) listViews.get(2).findViewById(android.R.id.list);
        //mHisData = mInflater.inflate(R.layout.history, null);
        listHis.add(mInflater.inflate(R.layout.history, null));
        listHis.add(mInflater.inflate(R.layout.history_chart, null));
        mhistoryView = (MsgListView) listHis.get(0).findViewById(android.R.id.list);
        tvinfo = (TextView)listHis.get(0).findViewById(R.id.tvinfo);
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
        mPagerHis = (ChildViewPager)listViews.get(1).findViewById(R.id.viewPager);
        mPagerHis.setAdapter(new MyPagerAdapter(listHis));
        mPagerHis.setCurrentItem(0);
        mPagerHis.setOnPageChangeListener(new MyOnPageChangeListener());

    }    

    private void InitTextView() {// 初始化头标
        t1 = (View) findViewById(R.id.textView1);
        t2 = (View) findViewById(R.id.textView2);
        t3 = (View) findViewById(R.id.textView3);
        t4 = (View) findViewById(R.id.textView4);

        t1.setOnClickListener(new MyOnClickListener(0));
        t2.setOnClickListener(new MyOnClickListener(1));
        t3.setOnClickListener(new MyOnClickListener(2));
        t4.setOnClickListener(new MyOnClickListener(3));
    }      
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // * 头标点击监听
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
            if(index == 0){
            	mrealtime.refreshListener.onRefresh();
            }
            else if(index == 1){
            	//mhistoryView.refreshListener.onRefresh();
            		
            }else if(index == 2){
            	mmyView.refreshListener.onRefresh();
            }
        }
    };    
    
    // * ViewPager适配器
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }    
    
    /**
     * 页卡切换监听
*/
    public class MyOnPageChangeListener implements OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量
        int three = one * 3;

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            int bkColor = Color.parseColor("#999999");
            int bkFocus = Color.parseColor("#aaaaaa");
            t1.setBackgroundColor(bkColor);
            t2.setBackgroundColor(bkColor);
            t3.setBackgroundColor(bkColor);
            t4.setBackgroundColor(bkColor);
            switch (arg0) {
            case 0:
                if (currIndex == 1) {
                    animation = new TranslateAnimation(one, 0, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, 0, 0, 0);
                }else{
                	animation = new TranslateAnimation(three, 0, 0, 0);
                }
                t1.setBackgroundColor(bkFocus);
                break;
            case 1:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, one, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, one, 0, 0);
                }else{
                	animation = new TranslateAnimation(three, one, 0, 0);
                }
                t2.setBackgroundColor(bkFocus);
                break;
            case 2:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, two, 0, 0);
                } else if (currIndex == 1) {
                    animation = new TranslateAnimation(one, two, 0, 0);
                }else{
                	animation = new TranslateAnimation(one,three, 0, 0);
                }
                t3.setBackgroundColor(bkFocus);
                break;
            case 3:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, two, 0, 0);
                } else if (currIndex == 1) {
                    animation = new TranslateAnimation(one, two, 0, 0);
                }else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, one, 0, 0);
                }         
                t4.setBackgroundColor(bkFocus);
            	break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            //cursor.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }   

    class STCDINFO{
    	public String STCD;
    	public String STNM;
    	public String TM;
    	public String Z;
    	public String Q;
    	public String GTOPHGT;
    }

	@SuppressWarnings("rawtypes")
	class STCDINFO_CMP implements Comparator{ // 实现Comparator，定义自己的比较方法
		public int compare(Object o1, Object o2) {
			STCDINFO e1=(STCDINFO)o1;
			STCDINFO e2=(STCDINFO)o2;
			return e1.STCD.compareTo(e2.STCD);
		}
	}    
    
	class MyListAdapter extends BaseAdapter {
		private int[] colors = new int[] { 0xff626569, 0xff4f5257 };
		private int midx = 0;
		public MyListAdapter(Context context,int _idx) {
			mContext = context;
			midx = _idx;
		}

		public int getCount() {
			return mlistdata.get(midx).size();// mListStr.length;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iamge = null;
			TextView title = null;
			TextView text = null;
			TextView tvgtophgt = null;
			TextView tvz = null;
			TextView tvq = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.realtime_item, null);
				//iamge = (ImageView) convertView.findViewById(R.id.color_image);
			}
			title = (TextView) convertView.findViewById(R.id.tvstnm);
			text = (TextView) convertView.findViewById(R.id.tvtm);
			tvz = (TextView) convertView.findViewById(R.id.tvz);
			tvq = (TextView) convertView.findViewById(R.id.tvq);
			tvgtophgt = (TextView) convertView.findViewById(R.id.tvgtophgt);
			
			int colorPos = position % colors.length;
			convertView.setBackgroundColor(colors[colorPos]);
			title.setText(mlistdata.get(midx).get(position).STNM);//("Hello");//
			text.setText(mlistdata.get(midx).get(position).TM);//("2015-06-21");//
			tvz.setText("水位：" + mlistdata.get(midx).get(position).Z);
			tvq.setText("流量：" + mlistdata.get(midx).get(position).Q);
			tvgtophgt.setText("并度：" + mlistdata.get(midx).get(position).GTOPHGT);
			//iamge.setImageResource(R.drawable.jay);
			return convertView;
		}

		private Context mContext;

	}
	// 向本地写入数据
	private void put(Map<String,String> list) {
		try {
			// 打开文件
			File f = new File(getFilesDir(),"irri_setup.dat");
			FileOutputStream fos = new FileOutputStream(f);

			// 将数据写入文件
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list);

			// 释放资源
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// * 从本地读取数据
	@SuppressWarnings("unchecked")
	private Map<String,String> get() {
		Map<String,String> list = new HashMap<String,String>();
		try {
			File f = new File(getFilesDir(),"irri_setup.dat");
			if (!f.exists()) {
				return list;
			}

			// 打开文件
			FileInputStream fis = new FileInputStream(f);

			// 读取文件
			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (Map<String,String>) ois.readObject();

			// 释放资源
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return list;
	}
	
	// * 向本地写入数据
	private void mmyput(List<String> list) {

		try {
			// 打开文件
			File f = new File(getFilesDir(),"irri_mmy.dat");
			FileOutputStream fos = new FileOutputStream(f);

			// 将数据写入文件
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list);

			// 释放资源
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// * 从本地读取数据
	@SuppressWarnings("unchecked")
	private List<String> mmyget() {
		List<String> list = new ArrayList<String>();
		try {
			File f = new File(getFilesDir(),"irri_mmy.dat");
			if (!f.exists()) {
				return list;
			}

			// 打开文件
			FileInputStream fis = new FileInputStream(f);

			// 读取文件
			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (List<String>) ois.readObject();
			Collections.sort(list);
			// 释放资源
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return list;
	}	
	
	// Fast Implementation
	private String inputStreamToString(InputStream is) throws IOException {
		String ret = "";
	    String line = "";
	    StringBuilder total = new StringBuilder();
	     
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	 
	    // Read response until the end
	    while ((line = rd.readLine()) != null) {
	        total.append(line);
	    }
	     
	    // Return full string
	    ret = total.toString();
	    return ret;
	}
	

    private void InitHistory(){
        //刷新监听，此处实现真正刷新
    	//final int idxlist = i;
		mhistoryView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Object, Object, Object>() {
					protected Object doInBackground(Object... params) {
				        HttpGet httpRequest = new HttpGet("http://192.168.18.106/realtime.ashx?cmd=gethistorybyname&stnm=" + stnmh + "&sbegin=" + sbegin + "&send=" + send);
				         try{
				            HttpClient httpClient = new DefaultHttpClient();
				            HttpResponse httpResponse = httpClient.execute(httpRequest);
				            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				            	HttpEntity res = httpResponse.getEntity();
				            	InputStream is = res.getContent();
				            	return inputStreamToString(is);
				            } else{
				                return null;
				            }
				         }catch(ClientProtocolException e){
				        	 e.printStackTrace();
				         }catch (IOException e) {
				            e.printStackTrace();
				         }catch(Exception e){
				        	 e.printStackTrace();
				         }
				         return null;
					}

					@Override
					protected void onPostExecute(Object result) {
						super.onPostExecute(result);
						try{
				            //创建一个JSON对象
				            JSONObject jsonObject = new JSONObject(result.toString());//.getJSONObject("parent");
				            int jsoncmd = jsonObject.getInt("cmdstatus");
				            if(jsoncmd == 1){
				            	mlistdata.get(1).clear();
				            	JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
				            	for(int i = 0; i < jsonrows.length(); i++){
				            		JSONArray jsonObject2 = (JSONArray)jsonrows.opt(i);
				            		STCDINFO info = new STCDINFO();
				            		info.STCD = jsonObject2.getString(0).trim();
				            		info.STNM = jsonObject2.getString(1).trim();
				            		info.TM = jsonObject2.getString(2).trim();
				            		info.Z = jsonObject2.getString(3).trim();
				            		info.Q = jsonObject2.getString(4).trim();
				            		info.GTOPHGT = jsonObject2.getString(5).trim();
				            		//int idx = Collections.binarySearch(mlistdata.get(1), info,new STCDINFO_CMP());
				            		//if(idx < 0){
				            			mlistdata.get(1).add(info);
				            			//Collections.sort(mlistdata.get(1),new STCDINFO_CMP());
				            		//}else if(idx < mlistdata.get(1).size()){
				            		//	mlistdata.get(1).set(idx, info);
				            		//}
				            	}
				            }
				         }
				         catch (JSONException e) {
				            e.printStackTrace();
				         }						
						mhistoryadp.notifyDataSetChanged();
						mhistoryView.onRefreshComplete();
					}
				}.execute();
			}
		});
		mhistoryView.setItemsCanFocus(false);
		mhistoryView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);        
        
		mhistoryadp = new MyListAdapter(this,1);
		mhistoryView.setAdapter(mhistoryadp);
		
		mhistoryView.setOnItemLongClickListener(new OnItemLongClickListener() {  
	        @Override  
	        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,  
	            long id) {  
	        	final int idx = position;
	        	   new AlertDialog.Builder(ActMain.this).setTitle(mlistdata.get(1).get(position-1).STNM).setItems(areas,new DialogInterface.OnClickListener(){  
	        		      public void onClick(DialogInterface dialog, int which){
	        		    	  
	        		       dialog.dismiss();
	        		       if(which == 0){
			            		int idx1 = Collections.binarySearch(mmy, mlistdata.get(1).get(idx-1).STCD);
			            		if(idx1 < 0){
			            			mmy.add(mlistdata.get(1).get(idx-1).STCD);
			            			Collections.sort(mmy);
			            		}       		    	   
	        		       }else if (which == 1){
	        		    	   showquery(dialog,which,idx,1);
	        		       }
	        		       else if(which == 2){
	        		    	   showprop(mlistdata.get(1).get(idx-1).STCD);
	        		       }
	        		      }  
	        		   }).show();  	        	
	        	
			return true;  
	        }  
	    });  		
		    	
    }
    	
	
    private void InitRealTime(){
        //刷新监听，此处实现真正刷新
    	//final int idxlist = i;
		mrealtime.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Object, Object, Object>() {
					protected Object doInBackground(Object... params) {
				        HttpPost httpRequest = new HttpPost("http://192.168.18.106/realtime.ashx");
				        List<NameValuePair> cmdlist = PrepareCmd(0);
				        try {
							HttpEntity httpEntity = new UrlEncodedFormEntity(cmdlist);
							httpRequest.setEntity(httpEntity);
							//httpRequest.addHeader("Content-type", "application/x-www-form-urlencoded");
							
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				         try{
				            HttpClient httpClient = new DefaultHttpClient();
				            HttpResponse httpResponse = httpClient.execute(httpRequest);
				            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				            	HttpEntity res = httpResponse.getEntity();
				            	InputStream is = res.getContent();
				            	return inputStreamToString(is);
				            } else{
				                return null;
				            }
				         }catch(ClientProtocolException e){
				        	 e.printStackTrace();
				         }catch (IOException e) {
				            e.printStackTrace();
				         }catch(Exception e){
				        	 e.printStackTrace();
				         }
				         return null;
					}

					@Override
					protected void onPostExecute(Object result) {
						super.onPostExecute(result);
						try{
				            //创建一个JSON对象
				            JSONObject jsonObject = new JSONObject(result.toString());//.getJSONObject("parent");
				            int jsoncmd = jsonObject.getInt("cmdstatus");
				            if(jsoncmd == 1){
				            	JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
				            	for(int i = 0; i < jsonrows.length(); i++){
				            		JSONArray jsonObject2 = (JSONArray)jsonrows.opt(i);
				            		STCDINFO info = new STCDINFO();
				            		info.STCD = jsonObject2.getString(0).trim();
				            		info.STNM = jsonObject2.getString(1).trim();
				            		info.TM = jsonObject2.getString(2).trim();
				            		info.Z = jsonObject2.getString(3).trim();
				            		info.Q = jsonObject2.getString(4).trim();
				            		info.GTOPHGT = jsonObject2.getString(5).trim();
				            		int idx = Collections.binarySearch(mlistdata.get(0), info,new STCDINFO_CMP());
				            		if(idx < 0){
				            			mlistdata.get(0).add(info);
				            			Collections.sort(mlistdata.get(0),new STCDINFO_CMP());
				            		}else if(idx < mlistdata.get(0).size()){
				            			mlistdata.get(0).set(idx, info);
				            		}
				            	}
				            }
				         }
				         catch (JSONException e) {
				            e.printStackTrace();
				         }						
						mrealtimeadp.notifyDataSetChanged();
						mrealtime.onRefreshComplete();
					}
				}.execute();
			}
		});
		mrealtime.setItemsCanFocus(false);
		mrealtime.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);        
        
		mrealtimeadp = new MyListAdapter(this,0);
		mrealtime.setAdapter(mrealtimeadp);
		
		mrealtime.setOnItemLongClickListener(new OnItemLongClickListener() {  
	        @Override  
	        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,  
	            long id) {  
	        	final int idx = position;
	        	   new AlertDialog.Builder(ActMain.this).setTitle(mlistdata.get(0).get(position-1).STNM).setItems(areas,new DialogInterface.OnClickListener(){  
	        		      public void onClick(DialogInterface dialog, int which){
	        		    	  
	        		       dialog.dismiss();
	        		       if(which == 0){
			            		int idx1 = Collections.binarySearch(mmy, mlistdata.get(0).get(idx-1).STCD);
			            		if(idx1 < 0){
			            			mmy.add(mlistdata.get(0).get(idx-1).STCD);
			            			Collections.sort(mmy);
			            		}       		    	   
	        		       }else if (which == 1){
	        		    	   showquery(dialog,which,idx,0);
	        		       }
	        		       else if(which == 2){
	        		    	   showprop(mlistdata.get(0).get(idx-1).STCD);
	        		       }
	        		      }  
	        		   }).show();  	        	
	        	
			return true;  
	        }  
	    });  		
		    	
    }
    
    private void InitMyView(){
		mmyView.setonRefreshListener(new OnRefreshListener() {        //刷新监听，此处实现真正刷新
			public void onRefresh() {
				new AsyncTask<Object, Object, Object>() {
					protected Object doInBackground(Object... params) {
				        HttpPost httpRequest = new HttpPost("http://192.168.18.106/realtime.ashx");
				        List<NameValuePair> cmdlist = PrepareCmd(2);
				        try {
							HttpEntity httpEntity = new UrlEncodedFormEntity(cmdlist);
							httpRequest.setEntity(httpEntity);
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
				         try{
				            HttpClient httpClient = new DefaultHttpClient();
				            HttpResponse httpResponse = httpClient.execute(httpRequest);
				            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				            	HttpEntity res = httpResponse.getEntity();
				            	InputStream is = res.getContent();
				            	return inputStreamToString(is);
				            } else{
				                return null;
				            }
				         }catch(ClientProtocolException e){
				        	 e.printStackTrace();
				         }catch (IOException e) {
				            e.printStackTrace();
				         }catch(Exception e){
				        	 e.printStackTrace();
				         }
				         return null;
					}

					@Override
					protected void onPostExecute(Object result) {
						super.onPostExecute(result);
						try{
				            //创建一个JSON对象
				            JSONObject jsonObject = new JSONObject(result.toString());//.getJSONObject("parent");
				            int jsoncmd = jsonObject.getInt("cmdstatus");
				            if(jsoncmd == 1){
				            	JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
				            	for(int i = 0; i < jsonrows.length(); i++){
				            		JSONArray jsonObject2 = (JSONArray)jsonrows.opt(i);
				            		STCDINFO info = new STCDINFO();
				            		info.STCD = jsonObject2.getString(0).trim();
				            		info.STNM = jsonObject2.getString(1).trim();
				            		info.TM = jsonObject2.getString(2).trim();
				            		info.Z = jsonObject2.getString(3).trim();
				            		info.Q = jsonObject2.getString(4).trim();
				            		info.GTOPHGT = jsonObject2.getString(5).trim();
				            		int idx = Collections.binarySearch(mlistdata.get(2), info,new STCDINFO_CMP());
				            		if(idx < 0){
				            			mlistdata.get(2).add(info);
				            			Collections.sort(mlistdata.get(2),new STCDINFO_CMP());
				            		}else if(idx < mlistdata.get(2).size()){
				            			mlistdata.get(2).set(idx, info);
				            		}
				            	}
				            }
				         }
				         catch (JSONException e) {
				            e.printStackTrace();
				         }						
						mmyadp.notifyDataSetChanged();
						mmyView.onRefreshComplete();
					}
				}.execute();
			}
		});
		mmyView.setItemsCanFocus(false);
		mmyView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);        
        
		mmyadp = new MyListAdapter(this,2);
		mmyView.setAdapter(mmyadp);
		
		mmyView.setOnItemLongClickListener(new OnItemLongClickListener() {  
	        @Override  
	        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,  
	            long id) {  
	        	final int idx = position;
	        	   new AlertDialog.Builder(ActMain.this).setTitle(mlistdata.get(2).get(position-1).STNM).setItems(areasmy,new DialogInterface.OnClickListener(){  
	        		      public void onClick(DialogInterface dialog, int which){  
	        		       dialog.dismiss();
	        		       if(which == 0){
	        		    	   mmy.remove(mlistdata.get(2).get(idx-1).STCD);
	        		    	   mmyView.refreshListener.onRefresh();
	        		    	   mlistdata.get(2).remove(idx-1);
			            		//int idx1 = Collections.binarySearch(mmy, mlistdata.get(2).get(idx-1).STCD);
			            		//if(idx1 < 0){
			            		//	mmy.add(mlistdata.get(2).get(idx-1).STCD);
			            		//	Collections.sort(mmy);
			            		//}       		    	   
	        		       }else if(which == 2){
	        		    	   showprop(mlistdata.get(2).get(idx-1).STCD);
	        		       }
	        		      }  
	        		   }).show();  	        	
	        	
			return true;  
	        }  
	    });  		
		    	
    }    
    
    private void gethistorybyname(String stcd,String sbegin, String send){
        HttpGet httpRequest = new HttpGet("http://192.168.18.106/realtime.ashx?cmd=gethistorybyname&stnm=" + stcd + "&sbegin=" + sbegin + "&send=" + send);
        try{
           HttpClient httpClient = new DefaultHttpClient();
           HttpResponse httpResponse = httpClient.execute(httpRequest);
           if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
               String strResult = EntityUtils.toString(httpResponse.getEntity());
				try{
		            //创建一个JSON对象
		            JSONObject jsonObject = new JSONObject(strResult.toString());//.getJSONObject("parent");
		            int jsoncmd = jsonObject.getInt("cmdstatus");
		            if(jsoncmd == 1){
		            	JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
		            	for(int i = 0; i < jsonrows.length(); i++){
		            		JSONArray jsonObject2 = (JSONArray)jsonrows.opt(i);
		            		List<String> arl = new ArrayList<String>();
		            		for(int j = 0; j < jsonObject2.length(); j++){
		            			arl.add(jsonObject2.opt(j).toString().trim());
		            		}
		            		String [] strItems = (String [])arl.toArray(new String[arl.size()]);
		 	        	   new AlertDialog.Builder(ActMain.this).setTitle("属性").setItems(strItems,new DialogInterface.OnClickListener(){  
			        		      public void onClick(DialogInterface dialog, int which){  
			        		       dialog.dismiss();  
			        		      }  
			        		   }).show(); 		            		
		            		break;
		            	}
		            }
		         }
		         catch (JSONException e) {
		            e.printStackTrace();
		         }						               
           } else{
           }
        }catch(ClientProtocolException e){
       	 e.printStackTrace();
        }catch (IOException e) {
           e.printStackTrace();
        }
    }    
        
    private void showquery(DialogInterface dialog, int which,int idx,int dataid){
 	   LayoutInflater inflater = getLayoutInflater();
 	   View dialoglayout = inflater.inflate(R.layout.query, null);
 	   final EditText name = (EditText)dialoglayout.findViewById(R.id.etname);
 	   name.setText(mlistdata.get(dataid).get(idx-1).STNM);
 	   Calendar calendar=Calendar.getInstance();
 	   calendar.add(Calendar.MONTH, -1);
       int year=calendar.get(Calendar.YEAR);
       int monthOfYear=calendar.get(Calendar.MONTH) + 1;
       int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);	        		    	   
       tvbegin  = ((TextView)dialoglayout.findViewById(R.id.tvbegin));
       tvend  = ((TextView)dialoglayout.findViewById(R.id.tvend));
       tvbegin.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
       calendar.add(Calendar.MONTH, 1);
       tvend.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
 	   AlertDialog.Builder builder = new AlertDialog.Builder(ActMain.this);
 	   builder.setView(dialoglayout);
 	   builder.setPositiveButton("确定", new OnClickListener() {
 		   @Override
 		   public void onClick(DialogInterface dialog, int which) {
			   stnmh = name.getText().toString();//((EditText)dialoglayout.findViewById(R.id.etname)).getText().toString();
			   mPager.setCurrentItem(1);
			   mhistoryView.refreshListener.onRefresh(); 
			   tvinfo.setText(stnmh + sbegin + send);
 			   }
 		   });
 	   builder.setNegativeButton("取消",null);
 	   builder.show();    	
    }
    private void showprop(String _stcd){
    	final String stcd = _stcd;
    	
		new AsyncTask<Object, Object, Object>() {
			protected Object doInBackground(Object... params) {
				HttpGet httpRequest = new HttpGet("http://192.168.18.106/realtime.ashx?cmd=prop&stcd=" + stcd);
		         try{
		            HttpClient httpClient = new DefaultHttpClient();
		            HttpResponse httpResponse = httpClient.execute(httpRequest);
		            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
		            	HttpEntity res = httpResponse.getEntity();
		            	InputStream is = res.getContent();
		            	return inputStreamToString(is);
		            } else{
		                return null;
		            }
		         }catch(ClientProtocolException e){
		        	 e.printStackTrace();
		         }catch (IOException e) {
		            e.printStackTrace();
		         }catch(Exception e){
		        	 e.printStackTrace();
		         }
		         return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				super.onPostExecute(result);
				try{
		            //创建一个JSON对象
		            JSONObject jsonObject = new JSONObject(result.toString());//.getJSONObject("parent");
		            int jsoncmd = jsonObject.getInt("cmdstatus");
		            if(jsoncmd == 1){
		            	JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
		            	for(int i = 0; i < jsonrows.length(); i++){
		            		JSONArray jsonObject2 = (JSONArray)jsonrows.opt(i);
		            		List<String> arl = new ArrayList<String>();
		            		for(int j = 0; j < jsonObject2.length(); j++){
		            			arl.add(jsonObject2.opt(j).toString().trim());
		            		}
		            		String [] strItems = (String [])arl.toArray(new String[arl.size()]);
		 	        	   new AlertDialog.Builder(ActMain.this).setTitle("属性").setItems(strItems,new DialogInterface.OnClickListener(){  
			        		      public void onClick(DialogInterface dialog, int which){  
			        		       dialog.dismiss();  
			        		      }  
			        		   })
			        		   .setNegativeButton("确定", null)
			        		   .show(); 		            		
		            		break;
		            	}
		            }
		         }
		         catch (JSONException e) {
		            e.printStackTrace();
		         }	
			}
		}.execute();    	
   	}
    
    public void ondatebeginpickerclick(View v){ // 创建DatePickerDialog对象
    	Calendar calendar=Calendar.getInstance();
    	calendar.add(Calendar.DAY_OF_MONTH, -1);
    	int year=calendar.get(Calendar.YEAR);
    	int monthOfYear=calendar.get(Calendar.MONTH);
    	int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);	      	
        DatePickerDialog dpd=new DatePickerDialog(ActMain.this,Datelistenerbegin,year,monthOfYear,dayOfMonth);
        dpd.show();//显示DatePickerDialog组件    	
    }
    
    public void ondateendpickerclick(View v){ // 创建DatePickerDialog对象
    	Calendar calendar=Calendar.getInstance();
    	calendar.add(Calendar.DAY_OF_MONTH, -1);
    	int year=calendar.get(Calendar.YEAR);
    	int monthOfYear=calendar.get(Calendar.MONTH);
    	int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);	      	
        DatePickerDialog dpd=new DatePickerDialog(ActMain.this,Datelistenerend,year,monthOfYear,dayOfMonth);
        dpd.show();//显示DatePickerDialog组件    	
    }    
    
    private DatePickerDialog.OnDateSetListener Datelistenerbegin=new DatePickerDialog.OnDateSetListener() {
        /**params：view：该事件关联的组件
         * params：myyear：当前选择的年
         * params：monthOfYear：当前选择的月
         * params：dayOfMonth：当前选择的日
         */
        @Override
        public void onDateSet(DatePicker view, int myyear, int monthOfYear,int dayOfMonth) {
       		sbegin = myyear + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
       		tvbegin.setText(sbegin);
            //修改year、month、day的变量值，以便以后单击按钮时，DatePickerDialog上显示上一次修改后的值
            //year=myyear;
            //month=monthOfYear;
            //day=dayOfMonth;
            //更新日期
        	
            updateDate();
            
        }
        //当DatePickerDialog关闭时，更新日期显示
        private void updateDate() { // 在TextView上显示日期
            //showdate.setText("当前日期："+year+"-"+(month+1)+"-"+day);
        	
     	
        }
    };    
 	

    private DatePickerDialog.OnDateSetListener Datelistenerend=new DatePickerDialog.OnDateSetListener() {
        /**params：view：该事件关联的组件
         * params：myyear：当前选择的年
         * params：monthOfYear：当前选择的月
         * params：dayOfMonth：当前选择的日
         */
        @Override
        public void onDateSet(DatePicker view, int myyear, int monthOfYear,int dayOfMonth) {
       		send = myyear + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
       		tvend.setText(send);
            //修改year、month、day的变量值，以便以后单击按钮时，DatePickerDialog上显示上一次修改后的值
            //year=myyear;
            //month=monthOfYear;
            //day=dayOfMonth;
            //更新日期
        	
            updateDate();
            
        }
        //当DatePickerDialog关闭时，更新日期显示
        private void updateDate() { // 在TextView上显示日期
            //showdate.setText("当前日期："+year+"-"+(month+1)+"-"+day);
        	
     	
        }
    };        
    
}
