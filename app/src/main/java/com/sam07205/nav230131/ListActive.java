package com.sam07205.nav230131;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActive extends AppCompatActivity implements View.OnClickListener {

    private RequestQueue requestQueue;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView textNowPage;

    private int TotalNum = 1;
    private int nowPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_list);

        mListView = findViewById(R.id.mListView);
        mProgressBar = findViewById(R.id.progressBar);

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        reqPage(1);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setText(getIntent().getStringExtra("account"));
        btnLogout.setOnClickListener(this);

        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnPrevPage.setOnClickListener(this);

        btnNextPage = findViewById(R.id.btnNextPage);
        btnNextPage.setOnClickListener(this);

        textNowPage = findViewById(R.id.textNowPage);

        findViewById(R.id.textNowPage).setOnClickListener(this);
    }

    private void reqPage(int pageIndex) {
        mProgressBar.setVisibility(View.VISIBLE);
        String url = "https://test-youthtycg.edwardforce.tw/app/api/tag/lists?page=" + pageIndex;
        JsonObjectRequest mJsonArrReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("REQC", error.toString());
                    }
                });

        requestQueue.add(mJsonArrReq);
    }

    private void handleResponse(JSONObject response) {
        JSONObject jsonobji;
        ArrayList<String> items = new ArrayList<>();
        try {
            JSONArray dataBody = response.getJSONArray("data");
            for (int i = 0; i < dataBody.length(); i++) {
                jsonobji = dataBody.getJSONObject(i);
                items.add(jsonobji.getString("tagName"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    ListActive.this,
                    android.R.layout.simple_list_item_1, items);
            mListView.setAdapter(adapter);
            mProgressBar.setVisibility(View.INVISIBLE);
            TotalNum = response.getInt("total");
            textNowPage.setText(nowPage + " / " + TotalNum);
            Log.i("REQC", String.valueOf(TotalNum));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNextPage || v.getId() == R.id.btnPrevPage) {
            handlePageButtonClick(v);
        } else if (v.getId() == R.id.btnLogout) {
            showPopupMenu(v);
        } else if (v.getId() == R.id.textNowPage) {
            final Dialog dialog = new Dialog(ListActive.this);
            dialog.setContentView(R.layout.list_page_goto_dialog);
            dialog.setTitle("前往頁數");
            
            TextInputLayout text = (TextInputLayout) dialog.findViewById(R.id.inputPage);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.20);
            dialog.getWindow().setLayout(width, height);
            dialog.show();

        }
    }

    private void handlePageButtonClick(View v) {
        if (v.getId() == R.id.btnNextPage) {
            nowPage = Math.min(TotalNum, nowPage + 1);
        } else if (v.getId() == R.id.btnPrevPage) {
            nowPage = Math.max(1, nowPage - 1);
        }

        reqPage(nowPage);
        btnPrevPage.setEnabled(nowPage != 1);
        btnNextPage.setEnabled(nowPage != TotalNum);
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuLogout) {
                    Intent intent = new Intent(ListActive.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }
}
