package com.example.fetchtest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView DisplayedData;
    private RequestQueue requestqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayedData = findViewById(R.id.text_view_result);
        requestqueue = Volley.newRequestQueue(this);

        jsonParse();
    }

    private void jsonParse() {
        String url = "https://fetch-hiring.s3.amazonaws.com/hiring.json";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<JSONObject> validItems = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject item = response.getJSONObject(i);
                                String name = item.optString("name");

                                if (name != "null" && !name.isEmpty()) {
                                    validItems.add(item);
                                }
                            }

                            Collections.sort(validItems, new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject item1, JSONObject item2) {
                                    int listId1 = item1.optInt("listId");
                                    int listId2 = item2.optInt("listId");
                                    String name1 = item1.optString("name");
                                    String name2 = item2.optString("name");

                                    if (listId1 != listId2) {
                                        return Integer.compare(listId1, listId2);
                                    } else {
                                        int nameAsInt1 = parseNumber(name1);
                                        int nameAsInt2 = parseNumber(name2);
                                        return Integer.compare(nameAsInt1, nameAsInt2);
                                    }
                                }

                                private int parseNumber(String name) {
                                    String[] parts = name.split(" ");
                                    if (parts.length > 1) {
                                        try {
                                            return Integer.parseInt(parts[1]);
                                        } catch (NumberFormatException e) {
                                        }
                                    }
                                    return 0;
                                }
                            });


                            Map<Integer, List<JSONObject>> listidGroups = new HashMap<>();
                            for (JSONObject item : validItems) {
                                int listId = item.optInt("listId");
                                if (!listidGroups.containsKey(listId)) {
                                    listidGroups.put(listId, new ArrayList<>());
                                }
                                listidGroups.get(listId).add(item);
                            }

                            StringBuilder resultBuilder = new StringBuilder();
                            for (int listId : listidGroups.keySet()) {
                                resultBuilder.append("List ID: ").append(listId).append("\n");
                                for (JSONObject item : listidGroups.get(listId)) {
                                    resultBuilder.append("  - ").append(item.optString("name")).append("\n");
                                }
                            }

                            DisplayedData.setText(resultBuilder.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestqueue.add(request);
    }
}
