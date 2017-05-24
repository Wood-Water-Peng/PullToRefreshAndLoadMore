package com.example.jackypeng.testforloadmore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import assit.PtlDefaultHeadHandler;
import ui.ClassicRefreshAndLoadMoreLayout;
import ui.LoadmoreLayout;
import ui.PtlFootHandler;
import ui.PtlHeaderHandler;

public class MainActivity extends AppCompatActivity {

    private ListView list;
    private ArrayList<String> items = new ArrayList();
    private ClassicRefreshAndLoadMoreLayout loadmorelayout;
    private int count = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.list);
        loadmorelayout = (ClassicRefreshAndLoadMoreLayout) findViewById(R.id.classic_loadmore_layout);
        for (int i = 0; i < count; i++) {
            items.add("item_" + i);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        list.setAdapter(arrayAdapter);
        loadmorelayout.setPtlFootHandler(new PtlFootHandler() {
            @Override
            public void onLoadMoreBegin(LoadmoreLayout loadmoreLayout) {
                loadmoreLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadmorelayout.loadMoreComplete();
                        items.add("item_" + count);
                        count++;
                        arrayAdapter.notifyDataSetChanged();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMoreCompleted(LoadmoreLayout loadmoreLayout) {

            }
        });

        loadmorelayout.setPtlHeadHandler(new PtlHeaderHandler() {
            @Override
            public void onRefreshBegin(LoadmoreLayout frame) {
                loadmorelayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadmorelayout.refreshCompleted();
                    }
                }, 1000);
            }

            @Override
            public boolean checkCanDoRefresh(LoadmoreLayout frame, View content, View header) {
                return PtlDefaultHeadHandler.checkContentCanBePullDown(frame, content, header);
            }
        });


    }
}
