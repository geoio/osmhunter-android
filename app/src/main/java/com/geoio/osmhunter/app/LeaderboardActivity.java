package com.geoio.osmhunter.app;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.geoio.osmhunter.app.SyncAdapter.HunterActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardActivity extends HunterActivity {
    private ListView listLeaderboard;
    private LeaderboardAdapter listLeaderboardAdapter;
    private List<JSONObject> leaderboard = new ArrayList<JSONObject>();
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_leaderboard);

        listLeaderboardAdapter = new LeaderboardAdapter(this, leaderboard);
        listLeaderboard = (ListView) findViewById(R.id.list_leaderboard);
        listLeaderboard.setAdapter(listLeaderboardAdapter);

        // swipe refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        refreshLayout.setColorScheme(R.color.geoio, R.color.swipe_refresh_1, R.color.geoio, R.color.swipe_refresh_2);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                leaderboardRefresh();
            }
        });
        refreshLayout.setRefreshing(true);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.leaderboard, menu);
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

    @Override
    public void accountReady() {
        leaderboardRefresh();
    }

    private void leaderboardRefresh() {
        if(!accountReady)
            return;

        Uri.Builder b = Uri.parse(getString(R.string.geoio_api_url)).buildUpon();
        AsyncHttpClient client = new AsyncHttpClient();

        // build url
        b.appendPath("user");
        b.appendPath("leaderboard");
        b.appendQueryParameter("apikey", user.getString(AccountManager.KEY_AUTHTOKEN));
        String url = b.build().toString();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // please do anything!
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (statusCode == 401) {
                        accountInvalidate();
                        return;
                    }

                    leaderboard.clear();

                    JSONArray result = response.getJSONArray("result");
                    for(int i = 0; i < result.length(); i++) {
                        leaderboard.add(result.getJSONObject(i));
                    }

                    refreshLayout.setRefreshing(false);
                    listLeaderboardAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class LeaderboardAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<JSONObject> mLeaderboard = new ArrayList<JSONObject>();
        private Context mContext;

        public LeaderboardAdapter(Context context, List<JSONObject> leaderboard) {
            mInflater = LayoutInflater.from(context);
            mLeaderboard = leaderboard;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mLeaderboard.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeaderboard.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = mInflater.inflate(R.layout.activity_leaderboard_list_item, null);
            }

            JSONObject user = mLeaderboard.get(i);

            ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
            TextView username = (TextView) view.findViewById(R.id.username);
            TextView points = (TextView) view.findViewById(R.id.points);

            try {
                Picasso.with(mContext).load(user.getString("image")).fit().into(avatar);
                username.setText(user.getString("username"));
                points.setText(user.getString("points"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }
    }

}