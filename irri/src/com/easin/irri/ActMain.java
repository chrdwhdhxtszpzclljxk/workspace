package com.easin.irri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ActMain extends ActionBarActivity {
	private String[] areas = new String[]{"加关注","查询","属性"};
	private ViewPager mPager;//页卡内容
    private List<View> listViews; // Tab页面列表
    //private ImageView cursor;// 动画图片
    //private TextView t1, t2, t3, t4;// 页卡头标
    private View t1,t2,t3,t4;
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度	
    private int mdelay;
    private MsgListView realtime; 
    private MyListAdapter mlistada;
    private Map<String,String> msetup;
    private List<String> mmy;
    public Vector<STCDINFO> mStcd;
    @Override
    protected void onPause(){
    	super.onPause();
    	put(msetup);
    	mmyput(mmy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	msetup = get();
    	mmy = mmyget();
    	mStcd = new Vector<STCDINFO>();
        setContentView(R.layout.act_main);
        mdelay = 5;
        InitTextView();
        InitViewPager();
        
        //刷新监听，此处实现真正刷新
		realtime.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Object, Object, Object>() {
					protected Object doInBackground(Object... params) {
				        HttpGet httpRequest = new HttpGet("http://test.gwgz.com/realtime.ashx?cmd=RealTime");
				         try{
				            HttpClient httpClient = new DefaultHttpClient();
				            HttpResponse httpResponse = httpClient.execute(httpRequest);
				            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				                String strResult = EntityUtils.toString(httpResponse.getEntity());
				                return strResult;
				            } else{
				                return null;
				            }
				         }catch(ClientProtocolException e){
				        	 e.printStackTrace();
				         }catch (IOException e) {
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
				            		int idx = Collections.binarySearch(mStcd, info,new STCDINFO_CMP());
				            		if(idx < 0){
				            			mStcd.add(info);
				            			Collections.sort(mStcd,new STCDINFO_CMP());
				            		}else if(idx < mStcd.size()){
				            			mStcd.set(idx, info);
				            		}
				            	}
				            	
				            	/*
					            StringBuilder builder = new StringBuilder();
					            for(int i = 0; i<jsonArray.length(); i++){
					                //新建一个JSON对象，该对象是某个数组里的其中一个对象
					                JSONObject jsonObject2 = (JSONObject)jsonArray.opt(i);
					                builder.append(jsonObject2.getString("id")); //获取数据
					                builder.append(jsonObject2.getString("title"));
					                builder.append(jsonObject2.getString("name"));
					            }
					            */
				            }
				            //myTextView.setText(builder.toString());
				         }
				         catch (JSONException e) {
				            e.printStackTrace();
				         }						
					
						mlistada.notifyDataSetChanged();
						// new MsgLoad().execute();//刷新监听中，真正执行刷新动作
						realtime.onRefreshComplete();
					}
				}.execute();
			}
		});
		realtime.setItemsCanFocus(false);
		realtime.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);        
        
		mlistada = new MyListAdapter(this);
		realtime.setAdapter(mlistada);
		
		realtime.setOnItemLongClickListener(new OnItemLongClickListener() {  
	        @Override  
	        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,  
	            long id) {  
	        //View v=adapterView.getChildAt(position);  
	        //v.setBackgroundColor(Color.RED);  
	        //Toast.makeText(ActMain.this,"您选择了" + Integer.toString(position), Toast.LENGTH_SHORT).show();
	        	final int idx = position;
	        	   new AlertDialog.Builder(ActMain.this).setTitle(mStcd.get(position-1).STNM).setItems(areas,new DialogInterface.OnClickListener(){  
	        		      public void onClick(DialogInterface dialog, int which){  
	        		       //Toast.makeText(ActMain.this, "您已经选择了: " + which + ":" + areas[which],Toast.LENGTH_LONG).show();  
	        		       dialog.dismiss();
	        		       if(which == 0){
			            		int idx1 = Collections.binarySearch(mmy, mStcd.get(idx-1).STCD);
			            		if(idx1 < 0){
			            			mmy.add(mStcd.get(idx-1).STCD);
			            			Collections.sort(mmy);
			            		}       		    	   
	        		       }else if(which == 2){
	        		    	   showprop(mStcd.get(idx-1).STCD);
	        		       }
	        		      }  
	        		   }).show();  	        	
	        	
			return true;  
	        }  
	    });  		
		
		
		new Timer().schedule(new TimerTask() {              
            @Override  
            public void run() {
            	if(currIndex == 0) 
            		realtime.refreshListener.onRefresh();
            }   
        }, 100, mdelay*1000);   		
        
    }
    
    private void showprop(String stcd){
        HttpGet httpRequest = new HttpGet("http://test.gwgz.com/realtime.ashx?cmd=prop&stcd=" + stcd);
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
		            	
		            	/*
			            StringBuilder builder = new StringBuilder();
			            for(int i = 0; i<jsonArray.length(); i++){
			                //新建一个JSON对象，该对象是某个数组里的其中一个对象
			                JSONObject jsonObject2 = (JSONObject)jsonArray.opt(i);
			                builder.append(jsonObject2.getString("id")); //获取数据
			                builder.append(jsonObject2.getString("title"));
			                builder.append(jsonObject2.getString("name"));
			            }
			            */
		            }
		            //myTextView.setText(builder.toString());
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

    
    /**
     * 初始化头标
*/
    private void InitTextView() {
        t1 = (View) findViewById(R.id.textView1);
        t2 = (View) findViewById(R.id.textView2);
        t3 = (View) findViewById(R.id.textView3);
        t4 = (View) findViewById(R.id.textView4);

        t1.setOnClickListener(new MyOnClickListener(0));
        t2.setOnClickListener(new MyOnClickListener(1));
        t3.setOnClickListener(new MyOnClickListener(2));
        t4.setOnClickListener(new MyOnClickListener(3));
        

    }    
    
    /**
     * 初始化ViewPager
*/
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.viewPager);
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.realtime, null));
        realtime = (MsgListView) listViews.get(0).findViewById(android.R.id.list);
        listViews.add(mInflater.inflate(R.layout.history, null));
        listViews.add(mInflater.inflate(R.layout.my, null));
        listViews.add(mInflater.inflate(R.layout.about, null));
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
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
    
    /**
     * 头标点击监听
*/
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    };    
    
    /**
     * ViewPager适配器
*/
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

		public MyListAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return mStcd.size();// mListStr.length;
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
			title.setText(mStcd.get(position).STNM);//("Hello");//
			text.setText(mStcd.get(position).TM);//("2015-06-21");//
			tvz.setText("水位：" + mStcd.get(position).Z);
			tvq.setText("流量：" + mStcd.get(position).Q);
			tvgtophgt.setText("并度：" + mStcd.get(position).GTOPHGT);
			//iamge.setImageResource(R.drawable.jay);
			return convertView;
		}

		private Context mContext;

	}

	/**
	 * 向本地写入数据
	 * */
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

	/**
	 * 从本地读取数据
	 * */
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
	
	/**
	 * 向本地写入数据
	 * */
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

	/**
	 * 从本地读取数据
	 * */
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
	
}
