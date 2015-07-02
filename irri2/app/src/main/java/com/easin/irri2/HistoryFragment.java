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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements
        OnChartGestureListener, OnChartValueSelectedListener{
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

    private LineChart mChart;

    private MsgListView mview;
    private MsgListViewAdapter madp;
    public Vector<STCDINFO> mdata;
    public ActMain mactmain;


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
        mview = (MsgListView) listViews.get(0).findViewById(android.R.id.list);
        mChart = (LineChart) listViews.get(1).findViewById(R.id.chart1);
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
                        HttpGet httpRequest = new HttpGet(mactmain.serverurl+"?cmd=gethistorybyname&stnm=" + mactmain.stnmh + "&sbegin=" + mactmain.sbegin + "&send=" + mactmain.send);
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
                            if(jsonstr.isEmpty()) return;
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
                                    info.Z = jsonObject2.getString(3).trim();
                                    info.Q = jsonObject2.getString(4).trim();
                                    info.GTOPHGT = jsonObject2.getString(5).trim();
                                    //int idx = Collections.binarySearch(mdata, info, new STCDINFO_CMP());
                                    //if (idx < 0) {
                                        mdata.add(info);
                                        Collections.sort(mdata, new STCDINFO_CMP());
                                    //} else if (idx < mdata.size()) {
                                    //    mdata.set(idx, info);
                                   // }
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

        mview.OnAutoRefresh();
        mview.refreshListener.onRefresh();

        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);

        // set the marker to the chart
        mChart.setMarkerView(mv);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightEnabled(false);

        // x-axis limit line
//        LimitLine llXAxis = new LimitLine(10f, "Index 10");
//        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
//        llXAxis.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//        llXAxis.setTextSize(10f);
//
//        XAxis xAxis = mChart.getXAxis();
//        xAxis.addLimitLine(llXAxis);

        LimitLine ll1 = new LimitLine(130f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLabelPosition.POS_RIGHT);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLabelPosition.POS_RIGHT);
        ll2.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis.addLimitLine(ll1);
        //leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(220f);
        leftAxis.setAxisMinValue(-50f);
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        // add data
        setData(45, 100);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);


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
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        count = mdata.size();
        int i = 0;
        float fmin = 9999999.0f,fmax = -999999.0f,fval = 0.0f;
        for(STCDINFO info:mdata){
            xVals.add(info.TM);
            fval = Float.parseFloat(info.Q);
            if(fmin > fval) fmin = fval;
            if(fmax < fval) fmax = fval;
            yVals.add(new Entry(fval,i));
            fval = Float.parseFloat(info.Z);
            if(fmin > fval) fmin = fval;
            if(fmax < fval) fmax = fval;
            yVals2.add(new Entry(fval, i));
            i++;
        }

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis.addLimitLine(ll1);
        //leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(fmax * 1.1f);
        leftAxis.setAxisMinValue(fmin * 0.9f);

        LineDataSet set1 = new LineDataSet(yVals, "流量");
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setCircleSize(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);

        LineDataSet set2 = new LineDataSet(yVals2, "水位");
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setColor(Color.RED);
        set2.setCircleColor(Color.WHITE);
        set2.setLineWidth(2f);
        set2.setCircleSize(3f);
        set2.setFillAlpha(65);
        set2.setFillColor(Color.RED);
        set2.setDrawCircleHole(false);
        set2.setHighLightColor(Color.rgb(244, 117, 117));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set2);
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
    }

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
        Log.i("Entry selected", e.toString());
        Log.i("", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
