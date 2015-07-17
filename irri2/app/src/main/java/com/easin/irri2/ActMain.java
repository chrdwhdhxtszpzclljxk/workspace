package com.easin.irri2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.easin.irri2.fragment.FragmentFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

// 主界面
public class ActMain extends Activity implements RadioGroup.OnCheckedChangeListener {
    static public String[] areas = new String[]{"加关注","查询","属性"};
    static public String[] areasmy = new String[]{"取消关注","查询","属性"};
    static public String[] querystr = new String[] {"所有","告警","2天","7天"};
    static public String serverurl = "http://1.85.44.234/realtime.ashx";
    private FragmentManager fragmentManager;
    static public List<String> mmy;
    static public Map<String, String> msetup;
    public String sbegin,send,stnmh;
    public TextView tvbegin,tvend;
    public EditText etServerUrl;
    static public MsgListView mviewHis;
    static public RealTimeFragment mRealTime;
    static public String mqueryauto="";
    public String getserverurl(){
        String ret = msetup.get("serverurl");
        if(ret == null) ret = serverurl;
        return ret;
    }
    @Override
    protected void onPause(){
        super.onPause();
        //put(msetup);
        setupsave(msetup,"irri_setup.dat");
        mmyput(mmy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity);
        msetup = setupload("irri_setup.dat");
        if(msetup.get("serverurl") == null){
            msetup.put("serverurl",serverurl);
        }
        mmy = mmyget();
        fragmentManager = getFragmentManager();
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
        radioGroup.setOnCheckedChangeListener(this);
        mviewHis = null;
        findViewById(R.id.rb_realtime).performClick();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //实例化需要的fragment
        Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId,this);
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    public  void bnQueryClick(View v){
        new AlertDialog.Builder(this).setTitle("").setItems(querystr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mqueryauto = querystr[which];
                mRealTime.filtermdata();
            }
        }).show();
    }

    public void bnHisQueryClick(View v){
        showquery(null, 0, -1, null);
    }

    public void showquery(DialogInterface dialog, int which,int idx,Vector<STCDINFO> _data){
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.query, null);
        final EditText name = (EditText)dialoglayout.findViewById(R.id.etname);
        if(idx > 0)  name.setText(_data.get(idx - 1).STNM);
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        int year=calendar.get(Calendar.YEAR);
        int monthOfYear=calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        TextView tvbegin  = ((TextView)dialoglayout.findViewById(R.id.tvbegin));
        TextView tvend  = ((TextView)dialoglayout.findViewById(R.id.tvend));
        tvbegin.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
        //calendar.add(Calendar.MONTH, 1);
        calendar=Calendar.getInstance();
        tvend.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
        sbegin = tvbegin.getText().toString();
        send = tvend.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(ActMain.this);
        builder.setView(dialoglayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stnmh = name.getText().toString();//((EditText)dialoglayout.findViewById(R.id.etname)).getText().toString();
                //mPager.setCurrentItem(1);
                //mhistoryView.refreshListener.onRefresh();
                //tvinfo.setText(stnmh + sbegin + send);
                if(mviewHis != null){
                    mviewHis.OnAutoRefresh();
                    mviewHis.refreshListener.onRefresh();
                }
                findViewById(R.id.rb_histroy).performClick();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    public void showprop(String _stcd){
        new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... params) {
                String stcd = params[0].toString();
                HttpGet httpRequest = new HttpGet(getserverurl()+"?cmd=prop&stcd=" + stcd);
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
        }.execute(_stcd);
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

    // * 向本地写入数据
    private void setupsave(Map<String, String> list,String file) {
        try {
            // 打开文件
            File f = new File(getFilesDir(),file);
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
    private Map<String, String> setupload(String file) {
        Map<String, String> list = new HashMap<String, String>();
        try {
            File f = new File(getFilesDir(),file);
            if (!f.exists()) {
                return list;
            }

            // 打开文件
            FileInputStream fis = new FileInputStream(f);

            // 读取文件
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (Map<String, String>) ois.readObject();
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



    public void ondatebeginpickerclick(View v){ // 创建DatePickerDialog对象
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int year=calendar.get(Calendar.YEAR);
        int monthOfYear=calendar.get(Calendar.MONTH);
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        tvbegin = (TextView)v;
        DatePickerDialog dpd=new DatePickerDialog(ActMain.this,Datelistenerbegin,year,monthOfYear,dayOfMonth);
        dpd.show();//显示DatePickerDialog组件
    }

    public void ondateendpickerclick(View v){ // 创建DatePickerDialog对象
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int year=calendar.get(Calendar.YEAR);
        int monthOfYear=calendar.get(Calendar.MONTH);
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        tvend = (TextView)v;
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

    // Fast Implementation
    static public String inputStreamToString(InputStream is) throws IOException {
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

}
