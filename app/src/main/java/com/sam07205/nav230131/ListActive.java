package com.sam07205.nav230131;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ListActive extends AppCompatActivity implements View.OnClickListener {

    private RequestQueue requestQueue;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView textNowPage;

    private ThreadPoolExecutor imageLoadThreadExec;
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

        reqPage(1, null);

        Button btnLogout = findViewById(R.id.btnLogout);
        String getName = getIntent().getStringExtra("account");
        assert getName != null;
        getName = getName.length() > 8 ? getName.substring(0, 8) + ".." : getName;
        btnLogout.setText(getName);
        btnLogout.setOnClickListener(this);

        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnPrevPage.setOnClickListener(this);

        btnNextPage = findViewById(R.id.btnNextPage);
        btnNextPage.setOnClickListener(this);

        textNowPage = findViewById(R.id.textNowPage);

        findViewById(R.id.textNowPage).setOnClickListener(this);

        imageLoadThreadExec = new ThreadPoolExecutor(1, 20, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
    }

    private void reqPage(int pageIndex, Callable<Void> afterResponse) {

        mProgressBar.setVisibility(View.VISIBLE);

        String url = "https://test-youthtycg.edwardforce.tw/app/api/tag/lists?page=" + pageIndex;
        JsonObjectRequest mJsonArrReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                        if (afterResponse != null) {
                            try {
                                afterResponse.call();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
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
        JSONObject jsonObji;
        ArrayList<itemTag> items = new ArrayList<>();
        try {
            JSONArray dataBody = response.getJSONArray("data");
            for (int i = 0; i < dataBody.length(); i++) {
                jsonObji = dataBody.getJSONObject(i);
                itemTag itemtag = new itemTag(jsonObji);
                items.add(itemtag);
                Log.i("REQC", jsonObji.toString());
            }
            itemAdapter mAdapter = new itemAdapter(this, items);
            mListView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.INVISIBLE);
            TotalNum = response.getInt("total") / 10 + (response.getInt("total") % 10 != 0 ? 1 : 0);
            textNowPage.setText(nowPage + " / " + TotalNum);
            Log.i("REQC", String.valueOf(TotalNum));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public class itemAdapter extends ArrayAdapter<itemTag> {

        Context mContext;
        List<itemTag> tagList;

        public itemAdapter(@NonNull Context context, @NonNull List<itemTag> list) {
            super(context, 0, list);
            mContext = context;
            tagList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            itemTag currentTag = tagList.get(position);
            ImageView image = (ImageView) listItem.findViewById(R.id.itemTagImage);
            TextView textTagID = listItem.findViewById(R.id.itemTagID);
            textTagID.setText(String.valueOf(currentTag.tagtId));
            imageLoadThreadExec.execute(() -> {
                URL myUrl = null;
                try {
                    myUrl = new URL(currentTag.tagImage);
                    InputStream inputStream = (InputStream) myUrl.getContent();
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    ListActive.this.runOnUiThread(() -> {
                        image.setImageDrawable(drawable);
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            return listItem;
        }

    }

    public class itemTag {
        int tagtId;
        String tagName;
        int tagClick;
        String tagImage;

        public itemTag(JSONObject gJson) throws JSONException {
            tagtId = gJson.getInt("tagtId");
            tagName = gJson.getString("tagName");
            tagClick = gJson.getInt("tagClick");
            tagImage = gJson.getString("tagImage");
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNextPage || v.getId() == R.id.btnPrevPage) {
            handlePageButtonClick(v);
        } else if (v.getId() == R.id.btnLogout) {
            showPopupMenu(v);
        } else if (v.getId() == R.id.textNowPage) {
            showPageDialog();
        }
    }

    private void showPageDialog() {
        final FrameLayout mLayout = new FrameLayout(this);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(nowPage));
        input.setHint(R.string.list_Dialog_GoTo_inp);
        mLayout.addView(input);
        mLayout.setPadding(30, 0, 30, 0);

        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(ListActive.this)
                .setTitle(R.string.list_Dialog_GoTo_Title)
                .setView(mLayout)
                .setPositiveButton(R.string.list_Dialog_GoTo_BTN_OK, new DialogInterface.OnClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            btnPrevPage.setEnabled(false);
                            btnNextPage.setEnabled(false);

                            int inpPage = Integer.parseInt(String.valueOf(input.getText()));
                            if (inpPage == nowPage)
                                return;
                            if (inpPage <= TotalNum && inpPage >= 1) {
                                nowPage = inpPage;
                                reqPage(nowPage, () -> {
                                    btnPrevPage.setEnabled(nowPage != 1);
                                    btnNextPage.setEnabled(nowPage != TotalNum);
                                    return null;
                                });
                                return;
                            }
                        } catch (Exception ignored) {

                        }
                        Toast.makeText(ListActive.this, String.format("%s %d~%d", getString(R.string.list_Dialog_GoTo_Toast_ERR), 1, TotalNum), Toast.LENGTH_SHORT).show();


                    }
                })
                .setNegativeButton(R.string.list_Dialog_GoTo_BTN_Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialog.show();
    }

    private void handlePageButtonClick(View v) {
        if (v.getId() == R.id.btnNextPage) {
            nowPage = Math.min(TotalNum, nowPage + 1);
        } else if (v.getId() == R.id.btnPrevPage) {
            nowPage = Math.max(1, nowPage - 1);
        }


        btnPrevPage.setEnabled(false);
        btnNextPage.setEnabled(false);
        reqPage(nowPage, () -> {
            btnPrevPage.setEnabled(nowPage != 1);
            btnNextPage.setEnabled(nowPage != TotalNum);
            return null;
        });

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
