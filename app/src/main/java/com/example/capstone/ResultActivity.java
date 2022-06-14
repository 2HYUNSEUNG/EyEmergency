package com.example.capstone;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    TextView tv_sec, fir_radius, sec_radius, tv_condition, tv_guide;
    Button btn_call;
    LinearLayout resultLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent resIntent = getIntent();

        double first_radius = resIntent.getExtras().getDouble("first_radius");                  //이전 화면에서 빛 쐬기 전 동공크기 가져오기
        double second_radius = resIntent.getExtras().getDouble("second_radius");                //이전 화면에서 빛 쐰 후 동공크기 가져오기
        int sec = resIntent.getExtras().getInt("sec");


        tv_sec = (TextView) findViewById(R.id.tv_sec);
        fir_radius = (TextView) findViewById(R.id.first_rad);
        sec_radius = (TextView) findViewById(R.id.second_rad);
        tv_condition = (TextView) findViewById(R.id.tv_condition);
        tv_guide = (TextView) findViewById(R.id.tv_guide);

        btn_call = (Button) findViewById(R.id.btn_call);

        resultLayout = (LinearLayout) findViewById(R.id.resultLayout);

        tv_sec.setText("동공반응 걸린시간: " + sec + "초");                                                                  //동공반응에 걸린시간 출력
        fir_radius.setText("동공의 크기: " + String.format("%.2f", first_radius));                                           //빛 쐬기 전 동공크기 값 출력
        sec_radius.setText("빛을 쐰 동공의 크기: " + String.format("%.2f", second_radius));                                   //빛 쐰 후 동공크기 값 출력


        if (first_radius - second_radius >= 3 && sec < 5) {                       //임의의 값 설정 후에 수정 (정상적인 동공수축과, 걸린 시간이 적을때)
            tv_condition.setText("동공 반응 정상!");                                                      //값에 맞는 상태 출력 (상태 정상 출력)

            resultLayout.setBackgroundColor(Color.parseColor("#D0FA58"));
        } else if (first_radius - second_radius < 3 && sec >= 5) {                                  //정상적인 동공수축 but 시간이 오래걸림
            tv_condition.setText("동공 반응 이상!\n뇌부종, 경막하출혈 징후\n동안신경 눌림");         // 뇌부종일 수도 있으므로 사용자에게 표시
            tv_guide.setText("행동요령\n1. 119 신고\n2. 환자상태 지속 확인\n3. 편평한 바닥에 눕히고 고개돌리기\n4. 의복을 편안하게 하기");

            resultLayout.setBackgroundColor(Color.parseColor("#FFA500"));                  // 위험 상태를 알리는 주황색으로 배경색 변경

        } else if (first_radius - second_radius < 3) {                                              // 동공 수축이 없을때
            tv_condition.setText("동공 반응 없음!\n심한 뇌허혈, 경막하출혈\n동안신경 눌림 심각");           // 매우 위험 단계
            tv_guide.setText("행동요령\n1. 119 신고\n2. 환자상태 지속 확인\n3. 편평한 바닥에 눕히고 고개돌리기\n4. 의복을 편안하게 하기");

            resultLayout.setBackgroundColor(Color.parseColor("#FA5858"));
        }
//        else {
//            tv_condition.setText("동공 반응 이상!\n뇌부종, 경막하출혈 징후\n동안신경 눌림");         // 뇌부종일 수도 있으므로 사용자에게 표시
//            tv_guide.setText("행동요령\n1. 119 신고\n2. 환자상태 지속 확인\n3. 편평한 바닥에 눕히고 고개돌리기\n4. 의복을 편안하게 하기");
//
//            resultLayout.setBackgroundColor(Color.parseColor("#FFA500"));                  // 위험 상태를 알리는 주황색으로 배경색 변경
//        }

        btn_call.setOnClickListener(new View.OnClickListener() {                                              //버튼을 클릭했을때
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:/119"));                       // 119로 전화 걸게 설정(바로 전화 X)
                startActivity(mIntent);
            }
        });
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