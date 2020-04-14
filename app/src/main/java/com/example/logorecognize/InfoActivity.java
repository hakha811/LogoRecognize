package com.example.logorecognize;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;

public class InfoActivity extends AppCompatActivity {

    private TextView textView;
    DatabaseHelper database;
    public static Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        context = getApplicationContext();
        textView = findViewById(R.id.textView_Info);
        database = new DatabaseHelper();
        textView.setText(load());
        database.close();
    }
    private String load(){
        Intent intent = getIntent();
        String[] arr = intent.getStringArrayExtra("Info");
        if(arr == null || !(arr.length > 0))
            return "Nothing Found!!!";
        String result = "";
        String query = "SELECT * FROM Logo WHERE ID = '"+arr[0].replace("'","''")+"' ";
        for(int i = 1; i < arr.length; i++){
            query+="OR ID = '"+arr[i].replace("'","''")+"' ";
        }
        Cursor cursor = database.getData("SELECT * FROM Logo");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String ID = cursor.getString(0);
            String Name = cursor.getString(1);
            String Founding = cursor.getString(2);
            String Founder = cursor.getString(3);
            String Key = cursor.getString(4);
            String Revenue = cursor.getString(5);
            result += "ID: "+ID+"\nName: "+Name+"\nDate: "+Founding+"\nFounder: "+Founder+"\nKey People: "+Key+"\nRevenue: "+Revenue+"\n\n";
            System.out.println("abc"+result);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }
}
