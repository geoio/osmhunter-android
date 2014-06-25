package com.geoio.osmhunter.app;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AttributeChangeActivity extends HunterActivity {
    List<JSONObject> attributes_list = new ArrayList<JSONObject>();
    LayoutInflater inflater;
    LinearLayout list_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_change);

        inflater = LayoutInflater.from(this);
        list_layout = (LinearLayout) this.findViewById(R.id.list);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        updateAttributesList(id);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.attribute_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void accountReady() {
    }

    private void updateAttributesList(String id) {
        Uri.Builder b = Uri.parse(getString(R.string.geoio_api_url)).buildUpon();
        AsyncHttpClient client = new AsyncHttpClient();

        // build url
        b.path("/buildings/");
        b.appendPath(id);
        b.appendPath("edit-form");
        String url = b.build().toString();

        // fire http
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // please do anything!
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray result = response.getJSONArray("result");

                    for(int i = 0; i < result.length(); i++) {
                        JSONObject attribute = result.getJSONObject(i);
                        String type = attribute.getString("type");

                        if(type.equals("text") || type.equals("url") || type.equals("phone")) {
                            View elem = inflater.inflate(R.layout.activity_attribute_change_item_text, null);
                            TextView label = (TextView) elem.findViewById(R.id.label);
                            EditText input = (EditText) elem.findViewById(R.id.input);

                            String labelText = attribute.getString("label");
                            String valueText = attribute.getString("value");

                            label.setText(labelText);
                            if(!valueText.equals("null")) {
                                input.setText(valueText);
                            }

                            if(type.equals("url")) {
                                input.setRawInputType(InputType.TYPE_TEXT_VARIATION_URI);
                            } else if(type.equals("phone")) {
                                input.setRawInputType(InputType.TYPE_CLASS_PHONE);
                            }

                            list_layout.addView(elem);
                        } else if(type.equals("select")) {
                            View elem = inflater.inflate(R.layout.activity_attribute_change_item_spinner, null);
                            TextView label = (TextView) elem.findViewById(R.id.label);
                            Spinner spinner = (Spinner) elem.findViewById(R.id.spinner);
                            JSONArray options = attribute.getJSONArray("options");

                            label.setText(attribute.getString("label"));

                            ArrayList<String> items = new ArrayList<String>();
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item);

                            /*if(attribute.getBoolean("allow_empty")) {
                                items.add("");  // empty
                            }*/

                            Integer selected = 0;
                            for(int a = 0; a < options.length(); a++) {
                                JSONObject option = options.getJSONObject(a);
                                if(option.getString("value").equals(attribute.getString("value"))) {
                                    selected = a;
                                }
                                items.add(option.getString("label"));
                            }

                            adapter.addAll(items);
                            spinner.setAdapter(adapter);
                            spinner.setSelection(selected);

                            list_layout.addView(elem);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
