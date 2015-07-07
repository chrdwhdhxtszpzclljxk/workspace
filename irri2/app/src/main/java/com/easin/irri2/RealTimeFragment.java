package com.easin.irri2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RealTimeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RealTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
     * @return A new instance of fragment RealTimeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RealTimeFragment newInstance(String param1, String param2,ActMain _actmain) {

        RealTimeFragment fragment = new RealTimeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.mactmain = _actmain;
        fragment.setArguments(args);
        return fragment;
    }

    public RealTimeFragment() {
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
        View v =inflater.inflate(R.layout.fragment_real_time, container, false);
        mview = (MsgListView) v.findViewById(android.R.id.list);
        mdata = new Vector<STCDINFO>();
        mview.setItemsCanFocus(false);
        mview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        madp = new MsgListViewAdapter(getActivity(),mdata);
        mview.setAdapter(madp);
        mview.setonRefreshListener(new MsgListView.OnRefreshListener() {
            public void onRefresh() {
                new AsyncTask<Object, Object, Object>() {
                    protected Object doInBackground(Object... params) {
                        HttpPost httpRequest = new HttpPost(mactmain.getserverurl());
                        List<NameValuePair> cmdlist = new ArrayList<NameValuePair>();
                        NameValuePair cmd = new BasicNameValuePair("cmd", "realtime");
                        cmdlist.add(cmd);
                        try {
                            HttpEntity httpEntity = new UrlEncodedFormEntity(cmdlist);
                            httpRequest.setEntity(httpEntity);
                            //httpRequest.addHeader("Content-type", "application/x-www-form-urlencoded");

                        } catch (UnsupportedEncodingException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
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
                                for (int i = 0; i < jsonrows.length(); i++) {
                                    JSONArray jsonObject2 = (JSONArray) jsonrows.opt(i);
                                    STCDINFO info = new STCDINFO();
                                    info.STCD = jsonObject2.getString(0).trim();
                                    info.STNM = jsonObject2.getString(1).trim();
                                    info.TM = jsonObject2.getString(2).trim();
                                    info.Z = jsonObject2.getString(3).trim();
                                    info.Q = jsonObject2.getString(4).trim();
                                    info.GTOPHGT = jsonObject2.getString(5).trim();
                                    int idx = Collections.binarySearch(mdata, info, new STCDINFO_CMP());
                                    if (idx < 0) {
                                        mdata.add(info);
                                        Collections.sort(mdata, new STCDINFO_CMP());
                                    } else if (idx < mdata.size()) {
                                        mdata.set(idx, info);
                                    }
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



}
