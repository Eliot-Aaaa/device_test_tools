package com.example.devicetest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.devicetest.common.Config;
import com.example.devicetest.common.Utill;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utill.checkPermission(this, Utill.REQUEST_PERMISSION);
        mContext = getApplicationContext();
        lv = findViewById(R.id.module_item_list);
        String[] data = new Config().getConfig(getApplicationContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = lv.getItemAtPosition(position).toString();
                Utill.turnToSelectModul(mContext, itemName);
            }
        });
    }
}
