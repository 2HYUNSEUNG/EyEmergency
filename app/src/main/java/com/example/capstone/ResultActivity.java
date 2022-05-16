package com.example.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    TextView fir_radius, sec_radius, tv_condition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent resIntent = getIntent();

        double first_radius = resIntent.getExtras().getDouble("first_radius");                  //이전 화면에서 빛 쐬기 전 동공크기 가져오기
        double second_radius = resIntent.getExtras().getDouble("second_radius");                //이전 화면에서 빛 쐰 후 동공크기 가져오기


        fir_radius = (TextView) findViewById(R.id.first_rad);
        sec_radius = (TextView) findViewById(R.id.second_rad);
        tv_condition = (TextView) findViewById(R.id.tv_condition);

        fir_radius.setText("동공의 크기 " + first_radius);                                           //빛 쐬기 전 동공크기 값 출력
        sec_radius.setText("빛을 쐰 동공의 크기 " + second_radius);                                     //빛 쐰 후 동공크기 값 출력

        if (first_radius - second_radius > 5) {                                                      //임의의 값 설정 후에 수정
            tv_condition.setText("정상입니다.");                                                         //값에 맞는 상태 출력
        }
    }
}


//import android.content.Intent;
//import android.os.Bundle;
//import androidx.appcompat.app.AppCompatActivity;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//public class ResultActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_result);
//        Intent resIntent = this.getIntent();
//        //동공 측정값 표시
//        if(resIntent != null && resIntent.hasExtra("resList")) {
//	        ArrayList<Double> resList = (ArrayList<Double>) resIntent.getSerializableExtra("resList");
//	        int s = resList.size();
//	        double avg = 0;
//	        for(int i = 0; i < s; i ++) {
//	        	avg += resList.get(i);
//	        }
//            String res;
//	        if(s == 0) {
//                res = "No pupil detected";
//            } else {
//                avg = avg / s;
//                res = avg + "";
//            }
//	        TextView resView = (TextView) findViewById(R.id.result);
//	        resView.setText(res);
//	    }
//    }
//}