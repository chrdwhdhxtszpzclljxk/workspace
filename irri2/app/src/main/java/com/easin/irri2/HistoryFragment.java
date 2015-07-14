package com.easin.irri2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
/*
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
*/

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment{
       // implements OnChartGestureListener, OnChartValueSelectedListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private int mdelay;

    private ViewPager mPager;
    private List<View> listViews;

    private LineChartView mChartZ,mChartQ;//,mChartG;

    private MsgListView mview;
    private MsgListViewAdapter madp;
    public Vector<STCDINFO> mdata;
    public ActMain mactmain;
    private TextView tvStnmZ,tvTmZ,tvStnmQ,tvTmQ;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2,ActMain _actmain) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        fragment.mactmain = _actmain;
        return fragment;
    }

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        mPager = (ViewPager) v.findViewById(R.id.viewPager);
        listViews = new ArrayList<View>();
        listViews.add(inflater.inflate(R.layout.fragment_history_listview, null));
        listViews.add(inflater.inflate(R.layout.fragment_history_chart, null));
        listViews.add(inflater.inflate(R.layout.fragment_history_chart, null));
        //listViews.add(inflater.inflate(R.layout.fragment_history_chart, null));
        mview = (MsgListView) listViews.get(0).findViewById(android.R.id.list);
        mChartZ = (LineChartView) listViews.get(1).findViewById(R.id.chart1);
        mChartQ = (LineChartView) listViews.get(2).findViewById(R.id.chart1);
        tvStnmZ = (TextView) listViews.get(1).findViewById(R.id.tvstnm);
        tvTmZ = (TextView) listViews.get(1).findViewById(R.id.tvtm);
        tvStnmQ = (TextView) listViews.get(2).findViewById(R.id.tvstnm);
        tvTmQ = (TextView) listViews.get(2).findViewById(R.id.tvtm);
        //mChartG = (LineChart) listViews.get(3).findViewById(R.id.chart1);
        //mmyView = (MsgListView) listViews.get(2).findViewById(android.R.id.list);
        mdata = new Vector<STCDINFO>();
        mview.setItemsCanFocus(false);
        mview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        madp = new MsgListViewAdapter(getActivity(), mdata);
        mview.setAdapter(madp);
        mview.setonRefreshListener(new MsgListView.OnRefreshListener() {
            public void onRefresh() {
                new AsyncTask<Object, Object, Object>() {
                    protected Object doInBackground(Object... params) {
                        HttpGet httpRequest = new HttpGet(mactmain.getserverurl() + "?cmd=gethistorybyname&stnm=" + mactmain.stnmh + "&sbegin=" + mactmain.sbegin + "&send=" + mactmain.send);
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpResponse httpResponse = httpClient.execute(httpRequest);
                            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                HttpEntity res = httpResponse.getEntity();
                                InputStream is = res.getContent();
                                return ActMain.inputStreamToString(is);
                            } else {
                                return "";
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        super.onPostExecute(result);
                        try {
                            //创建一个JSON对象
                            String jsonstr = result.toString();
                            if (jsonstr.isEmpty()) return;
                            JSONObject jsonObject = new JSONObject(jsonstr);//.getJSONObject("parent");
                            int jsoncmd = jsonObject.getInt("cmdstatus");
                            if (jsoncmd == 1) {
                                JSONArray jsonrows = jsonObject.getJSONObject("rd").getJSONArray("rows");
                                mdata.clear();
                                for (int i = 0; i < jsonrows.length(); i++) {
                                    JSONArray jsonObject2 = (JSONArray) jsonrows.opt(i);
                                    STCDINFO info = new STCDINFO();
                                    info.STCD = jsonObject2.getString(0).trim();
                                    info.STNM = jsonObject2.getString(1).trim();
                                    info.TM = jsonObject2.getString(2).trim();
                                    info.UPZ = jsonObject2.getString(3).trim();
                                    info.DWZ = jsonObject2.getString(4).trim();
                                    info.TGTQ = jsonObject2.getString(5).trim();
                                    info.GTOPHGT = jsonObject2.getString(6).trim();
                                    //int idx = Collections.binarySearch(mdata, info, new STCDINFO_CMP());
                                    //if (idx < 0) {
                                    mdata.add(info);
                                    Collections.sort(mdata, new STCDINFO_CMP());
                                    //} else if (idx < mdata.size()) {
                                    //    mdata.set(idx, info);
                                    // }
                                }
                                if(jsonrows.length()>0){
                                    setData(50, 200);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        madp.notifyDataSetChanged();
                        mview.onRefreshComplete();
                    }
                }.execute();
            }
        });
        mview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long id) {
                final int idx = position;
                new AlertDialog.Builder(getActivity()).setTitle(mdata.get(position - 1).STNM).setItems(ActMain.areas, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            int idx1 = Collections.binarySearch(ActMain.mmy, mdata.get(idx - 1).STCD);
                            if (idx1 < 0) {
                                ActMain.mmy.add(mdata.get(idx - 1).STCD);
                                Collections.sort(ActMain.mmy);
                            }
                        } else if (which == 1) {
                            mactmain.showquery(dialog, which, idx, mdata);
                        } else if (which == 2) {
                            mactmain.showprop(mdata.get(idx - 1).STCD);
                        }
                    }
                }).show();

                return true;
            }
        });

        mview.setcanrefresh(false);
        mview.OnAutoRefresh();
        mview.refreshListener.onRefresh();

        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        /*
        {
            mChartZ.setOnChartGestureListener(this);
            mChartZ.setOnChartValueSelectedListener(this);
            mChartZ.setDrawGridBackground(false);
            //mChartZ.setDrawBorders(true);
            mChartZ.setDescription("");
            mChartZ.setNoDataTextDescription("You need to provide data for the chart.");
            mChartZ.setHighlightEnabled(true);
            mChartZ.setTouchEnabled(true);
            mChartZ.setDragEnabled(true);
            mChartZ.setScaleEnabled(true);
            mChartZ.setPinchZoom(true);
            MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
            mChartZ.setMarkerView(mv);
            mChartZ.setHighlightEnabled(false);
            YAxis leftAxis = mChartZ.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaxValue(220f);
            leftAxis.setAxisMinValue(-50f);
            leftAxis.setStartAtZero(false);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawLimitLinesBehindData(true);
            leftAxis.setTextSize(12);
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.setDrawGridLines(true);
            mChartZ.getAxisRight().setEnabled(false);
            mChartZ.animateX(2500, Easing.EasingOption.EaseInOutQuart);

            XAxis xAxis = mChartZ.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setTextSize(8);
           // xAxis.setLabelsToSkip(2);
            mChartZ.invalidate();
            // get the legend (only possible after setting data)
            Legend l = mChartZ.getLegend();
            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            l.setForm(LegendForm.LINE);
        }

        {
            mChartQ.setOnChartGestureListener(this);
            mChartQ.setOnChartValueSelectedListener(this);
            mChartQ.setDrawGridBackground(false);
            //mChartQ.setDrawBorders(true);
            mChartQ.setDescription("");
            mChartQ.setNoDataTextDescription("You need to provide data for the chart.");
            mChartQ.setHighlightEnabled(true);
            mChartQ.setTouchEnabled(true);
            mChartQ.setDragEnabled(true);
            mChartQ.setScaleEnabled(true);
            mChartQ.setPinchZoom(true);
            MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
            mChartQ.setMarkerView(mv);
            mChartQ.setHighlightEnabled(false);
            YAxis leftAxis = mChartQ.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaxValue(220f);
            leftAxis.setAxisMinValue(-50f);
            leftAxis.setStartAtZero(false);
            leftAxis.setTextSize(12);
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawLimitLinesBehindData(true);
            mChartQ.getAxisRight().setEnabled(false);
            mChartQ.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            mChartQ.invalidate();
            mChartQ.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            //mChartQ.getXAxis().
            // get the legend (only possible after setting data)
            Legend l = mChartQ.getLegend();
            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            l.setForm(LegendForm.LINE);
        }
        */

        {
            /*
            mChartG.setOnChartGestureListener(this);
            mChartG.setOnChartValueSelectedListener(this);
            mChartG.setDrawGridBackground(false);
            mChartG.setDrawBorders(true);
            mChartG.setDescription("");
            mChartG.setNoDataTextDescription("You need to provide data for the chart.");
            mChartG.setHighlightEnabled(true);
            mChartG.setTouchEnabled(true);
            mChartG.setDragEnabled(true);
            mChartG.setScaleEnabled(true);
            mChartG.setPinchZoom(true);
            MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
            mChartG.setMarkerView(mv);
            mChartG.setHighlightEnabled(false);
            YAxis leftAxis = mChartG.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaxValue(220f);
            leftAxis.setAxisMinValue(-50f);
            leftAxis.setStartAtZero(false);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawLimitLinesBehindData(true);
            mChartG.getAxisRight().setEnabled(false);

            mChartG.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            mChartG.invalidate();
            mChartG.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            // get the legend (only possible after setting data)
            Legend l = mChartG.getLegend();
            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            l.setForm(LegendForm.LINE);
            */
        }
        //setData(45, 100);
        mactmain.mviewHis = mview;
        return v;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mactmain.mviewHis = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

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
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量
        int three = one * 3;

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            int bkColor = Color.parseColor("#999999");
            int bkFocus = Color.parseColor("#aaaaaa");
            //t1.setBackgroundColor(bkColor);
            //t2.setBackgroundColor(bkColor);
            //t3.setBackgroundColor(bkColor);
            //t4.setBackgroundColor(bkColor);
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    }else{
                        animation = new TranslateAnimation(three, 0, 0, 0);
                    }
                   // t1.setBackgroundColor(bkFocus);
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }else{
                        animation = new TranslateAnimation(three, one, 0, 0);
                    }
                    //t2.setBackgroundColor(bkFocus);
                    break;
                case 2:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    }else{
                        animation = new TranslateAnimation(one,three, 0, 0);
                    }
                   // t3.setBackgroundColor(bkFocus);
                    break;
                case 3:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    }else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }
                  //  t4.setBackgroundColor(bkFocus);
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

    public void setData(int count, float range) {
        ArrayList<AxisValue> xVals = new ArrayList<AxisValue>();
        ArrayList<PointValue> yValsQ = new ArrayList<PointValue>();
        ArrayList<PointValue> yValsZ = new ArrayList<PointValue>();
        ArrayList<PointValue> yValsG = new ArrayList<PointValue>();
        tvStnmQ.setText(mactmain.stnmh + " -- 过闸流量");
        tvStnmZ.setText(mactmain.stnmh + " -- 闸前水位");
        String strdate = mactmain.sbegin + " - " + mactmain.send;
        tvTmQ.setText(strdate);
        tvTmZ.setText(strdate);
        count = mdata.size();
        int i = 0;
        float fmin = 9999999.0f,fmax = -999999.0f,fval = 0.0f,fminz = fmin,fmaxz = fmax,fminq = fmin, fmaxq = fmax,fming = fmin,fmaxg = fmax;
        for(STCDINFO info:mdata){
            xVals.add(new AxisValue(i).setLabel(info.TM.substring(5,16)));//("6-12 12:00");//(info.TM);
            if(info.TGTQ!=null && info.TGTQ.length()!=0  && !info.TGTQ.equals("null")) {
                fval = Float.parseFloat(info.TGTQ);
            }else{
                fval=0;
            }
            if(fminq > fval) fminq = fval;
            if(fmaxq < fval) fmaxq = fval;
            yValsQ.add(new PointValue(i,fval));
            if(info.UPZ!=null && info.UPZ.length()!=0  && !info.UPZ.equals("null")) {
                fval = Float.parseFloat(info.UPZ);
            }else{
                fval=0;
            }
            if(fminz > fval) fminz = fval;
            if(fmaxz < fval) fmaxz = fval;
            yValsZ.add(new PointValue(i,fval));
            /*
            if(info.GTOPHGT!=null && info.GTOPHGT.length()!=0 && !info.GTOPHGT.equals("null")) {
                fval = Float.parseFloat(info.GTOPHGT);
            }else{
                fval=0;
            }
            if (fming > fval) fming = fval;
            if (fmaxg < fval) fmaxg = fval;
            yValsG.add(new PointValue(i, fval));*/
            i++;
        }

        {
            //In most cased you can call data model methods in builder-pattern-like manner.
            Line line = new Line(yValsQ).setColor(Color.BLUE);
            line.setCubic(false);
            line.setHasPoints(false);
            line.setStrokeWidth(2);
            List<Line> lines = new ArrayList<Line>();
            lines.add(line);
            LineChartData data = new LineChartData();
            data.setLines(lines);

            float ystop,ystar,ystep;
            ystop = fmaxq+fmaxq*0.01f;
            ystar = fminq-fminq*0.01f;
            if(ystar<=0){
                ystar=0f;
            }
            ystep = (ystop-ystar)*0.1f;
            //坐标轴
            Axis axisX = new Axis(); //X轴
            axisX.setHasTiltedLabels(true);
            axisX.setTextColor(Color.WHITE);
            axisX.setName("采集时间");
            axisX.setTextSize(10);
            axisX.setMaxLabelChars(9);
            axisX.setInside(true);
            axisX.setValues(xVals);
            axisX.setHasLines(true);
            Axis axisY = new Axis();  //Y轴
            //Axis axisY = Axis.generateAxisFromRange(ystar,ystop,0.05f);  //Y轴
            axisY.setMaxLabelChars(5); //默认是3，只能看最后三个数字
            axisY.setHasLines(true);
            axisY.setName("流量(m3/s)");
            data.setAxisYLeft(axisY);
            data.setAxisXBottom(axisX);

            mChartQ.setLineChartData(data);
            // Set selection mode to keep selected month column highlighted.
            mChartQ.setValueSelectionEnabled(true);
            mChartQ.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);

            Viewport viewport = mChartQ.getMaximumViewport();
            viewport.set(viewport.left, ystop, viewport.right, ystar);
            //mChartQ.setMaximumViewport(viewport);
            mChartQ.setCurrentViewport(viewport);
        }

        {
            //In most cased you can call data model methods in builder-pattern-like manner.
            Line line = new Line(yValsZ).setColor(Color.BLUE);
            line.setCubic(false);
            line.setHasPoints(false);
            line.setStrokeWidth(2);
            List<Line> lines = new ArrayList<Line>();
            lines.add(line);
            LineChartData data = new LineChartData();
            data.setLines(lines);

            float ystop,ystar,ystep;
            ystop = fmaxz+fmaxz*0.01f;
            ystar = fminz-fminz*0.01f;
            if(ystar<=0){
                ystar=0f;
            }
            ystep = (ystop-ystar)*0.1f;
            //坐标轴
            Axis axisX = new Axis(); //X轴
            axisX.setHasTiltedLabels(true);
            axisX.setTextColor(Color.WHITE);
            axisX.setName("采集时间");
            axisX.setTextSize(10);
            axisX.setMaxLabelChars(9);
            axisX.setInside(true);
            axisX.setValues(xVals);
            axisX.setHasLines(true);
            Axis axisY = new Axis();  //Y轴
            //Axis axisY = Axis.generateAxisFromRange(ystar,ystop,0.05f);  //Y轴
            axisY.setMaxLabelChars(5); //默认是3，只能看最后三个数字
            axisY.setHasLines(true);
            axisY.setName("水位(m)");
            data.setAxisYLeft(axisY);
            data.setAxisXBottom(axisX);

            mChartZ.setLineChartData(data);
            // Set selection mode to keep selected month column highlighted.
            mChartZ.setValueSelectionEnabled(true);
            mChartZ.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);

            Viewport viewport = mChartZ.getMaximumViewport();
            viewport.set(viewport.left, ystop, viewport.right, ystar);
            //mChartZ.setMaximumViewport(viewport);
            mChartZ.setCurrentViewport(viewport);
        }

        /*
        {
            YAxis leftAxis = mChartZ.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaxValue(fmaxz * 1.1f);
            leftAxis.setAxisMinValue(fminz * 0.9f);
        }
        {
            YAxis leftAxis = mChartQ.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaxValue(fmaxq * 1.1f);
            leftAxis.setAxisMinValue(fminq * 0.9f);
        }
        */
        {
            //YAxis leftAxis = mChartG.getAxisLeft();
           // leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
           // leftAxis.setAxisMaxValue(fmaxg * 1.1f);
           // leftAxis.setAxisMinValue(fming * 0.9f);
        }

        //LineDataSet set1 = new LineDataSet(yValsQ, "流量");
        //set1.enableDashedLine(10f, 5f, 0f);
        //set1.setColor(Color.RED);
        //set1.setCircleColor(Color.WHITE);
        //set1.setLineWidth(2f);
        //set1.setCircleSize(2f);
        //set1.setDrawCircleHole(false);
        //set1.setValueTextSize(12f);
        //set1.setFillAlpha(65);
        //set1.setDrawCubic(true);
        //set1.setFillColor(Color.BLACK);
        //set1.setValueTextColor(Color.WHITE);

/*
        {
            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set1);

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(12f);

            // set data
            mChartQ.setData(data);
        }



        LineDataSet set2 = new LineDataSet(yValsZ, "水位");
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setColor(Color.RED);
        //set2.setCircleColor(Color.WHITE);
        set2.setLineWidth(2f);
        //set2.setCircleSize(3f);
        //set2.setFillAlpha(65);
       // set2.setFillColor(Color.WHITE);
        //set2.setDrawCircleHole(false);
        //set2.setDrawCubic(true);
        //set2.setHighLightColor(Color.rgb(244, 117, 117));

        {
            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set2);

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);
            data.setDrawValues(false);

            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(12f);


            // set data
            mChartZ.setData(data);
        }
        */
/*
        LineDataSet setG = new LineDataSet(yValsG, "开度");
        setG.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setG.setColor(Color.RED);
        setG.setCircleColor(Color.WHITE);
        setG.setLineWidth(2f);
        setG.setCircleSize(3f);
        setG.setFillAlpha(65);
        setG.setFillColor(Color.RED);
        setG.setDrawCircleHole(false);
        //setG.setDrawCubic(true);
        setG.setHighLightColor(Color.rgb(244, 117, 117));
        */



        {
          //  ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
          //  dataSets.add(setG);

            // create a data object with the datasets
           // LineData data = new LineData(xVals, dataSets);
           // data.setValueTextColor(Color.WHITE);
           // data.setValueTextSize(12f);

            // set data
           // mChartG.setData(data);
        }
    }

    /*
    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        //Log.i("Entry selected", e.toString());
       // Log.i("", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
    */
}
