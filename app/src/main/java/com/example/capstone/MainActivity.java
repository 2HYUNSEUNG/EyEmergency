package com.example.capstone;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";

    private Mat matInput;
    private Mat matResult;

    private CameraBridgeViewBase mOpenCvCameraView;
    private JavaCameraView javaCameraView;

    private CascadeClassifier mJavaDetectorLeftEye;

    private Mat mIntermediateMat;
    private Mat hierarchy;

    private Mat mZoomWindow;

    private ArrayList<Double> resList = new ArrayList<Double>();

    private Camera camera;


    double first_radius = 0, second_radius = 0;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
                                                                                                        //비동기 초기화, OnManagerConnected 초기화가 완료되면, UI 스레드에서 콜백이 호출
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
// 기존 시작
//                case LoaderCallbackInterface.SUCCESS: {
//                    mOpenCvCameraView.enableView();
//                }
// 기존 끝
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // load cascade file from application resources
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                    mJavaDetectorLeftEye = loadClassifier(R.raw.haarcascade_lefteye_2splits, "haarcascade_eye_left.xml", cascadeDir);

                    cascadeDir.delete();
                    mOpenCvCameraView.setCameraIndex(0);

                    // 카메라 지연을 피하기 위해 해상도 줄이기
                    mOpenCvCameraView.setMaxFrameSize(640, 480);

                    //mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();
                }

                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    ImageButton Button1;
    ImageButton btnFlash;
    ImageView eyeimg1;
    ImageButton btnGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//기존
        //상단바 안보이게 하는 코드
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);                                           // 지우면 카메라 안보임
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0);                                                            // front-camera(1),  back-camera(0)
//기존

        Button1 = (ImageButton) findViewById(R.id.Button1);
        btnFlash = (ImageButton) findViewById(R.id.btnFlash);
        eyeimg1 = (ImageView) findViewById(R.id.eyeimg1);
        btnGuide = (ImageButton) findViewById(R.id.btnGuide);

        javaCameraView = (JavaCameraView) findViewById(R.id.activity_surface_view);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        //화면 꺼지지 않게 유지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("EyEmergency 사용방법"); //제목
                dlg.setMessage("1. 가이드라인에 환자의 눈을 맞추세요. " +
                        "\n\n2. 눈과 동공이 인식되면 플래시버튼을 누르세요. " +
                        "\n\n3. 플래시가 켜지고, 눈과 동공이 인식되면 촬영버튼을 누르세요."); // 메시지
                dlg.setIcon(R.drawable.eye); // 아이콘 설정
                // 버튼 클릭시 동작
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //토스트 메시지
                        Toast.makeText(MainActivity.this, "이제 환자 상태를 확인하겠습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });

        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                javaCameraView.setFlashMode(FLASH_MODE_OFF);                                      // 얘도 가능

                int s = resList.size();                                                     //배열의 사이즈 저장
                double avg = 0;                                                             //동공의 평균 지름값 저장을 위한 변수

                if(s == 0) {
                    Toast.makeText(MainActivity.this, "동공이 검출되지 않았습니다.", Toast.LENGTH_SHORT).show();                //동공이 검출되지 않았으면 토스트 메세지 출력
                } else {                                                                    //동공 반지름 값이 검출되었다면
                    for (int i=0; i<s; i++) {                                               //배열내의 값을 모두 더함.
                        avg += resList.get(i);
                    }
                    second_radius = avg / s;                                                //빛을 쐰 후, 반지름 값으로 저장.
                    resList.clear();

                    javaCameraView.turnOffTheFlash();

                    Intent resultIntent = new Intent(MainActivity.this, ResultActivity.class);              //화면 전환 설정
                    resultIntent.putExtra("first_radius", first_radius);                                           //전환된 화면에 빛 쐬기 전 반지름 값 넘기기
                    resultIntent.putExtra("second_radius", second_radius);                                         //전환된 화면에 빛 쐰 후 반지름 값 넘기기
                    startActivity(resultIntent);                                                                         // 화면 전환 실행
                }
            }
        });

        // 플래쉬 버튼 눌렀을때
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int s = resList.size();                                                             //배열의 사이즈 저장
                double avg =0;                                                                      //동공의 평균 지름값 저장을 위한 변수

                for(int i = 0; i < s; i ++) {
                    avg += resList.get(i);                                                          //배열내의 값을 모두 더함.
                }

                if(s == 0) {                                                                        //동공이 검출되지 않았으면 토스트 메세지 출력
                    Toast.makeText(MainActivity.this, "동공이 검출되지 않았습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    first_radius = avg / s;                                                         //동공의 값이 검출되었다면, 빛을 쐬기 전 반지름 값으로 저장.

                    Toast.makeText(MainActivity.this, "동공이 검출 되었습니다.", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            javaCameraView.turnOnTheFlash(); //딜레이 후 시작할 코드 작성
                        }
                    }, 500);// 0.6초 정도 딜레이를 준 후 시작
                }
                resList.clear();


//                javaCameraView.setFlashMode(FLASH_MODE_TORCH);                                    // 얘도 가능

            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // 8U = 부호없는 정수 8비트, C4 = 4채널, R G B와 투명도 Alpha
        // 즉, 0 ~ 255 범위의 값과 4개의 색상 채널이 있는 매트릭스를 만듦
        matInput = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        matResult = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        // 해제
        matResult.release();
        matInput.release();

        mIntermediateMat.release();
        hierarchy.release();

        mZoomWindow.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //기존 시작
        matInput = inputFrame.rgba();
        matResult = inputFrame.gray();

//        if (matResult == null)
//
//            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
//
//        ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
//
//        return matResult;
        //기존 끝

        if (mZoomWindow == null)
            createAuxiliaryMats();

        Rect area = new Rect(new Point(20, 20), new Point(matResult.width() - 20, matResult.height() - 20));
        detectEye(mJavaDetectorLeftEye, area, 100);

        return matInput;
    }

    //보조 Mat 만들기
    private void createAuxiliaryMats() {
        if (matResult.empty())
            return;

        int rows = matResult.rows();
        int cols = matResult.cols();

        if (mZoomWindow == null) {
            mZoomWindow = matInput.submat(rows / 2 + rows / 10, rows, cols / 2 + cols / 10, cols);
        }

    }
    //눈 검출 메소드
    private Mat detectEye(CascadeClassifier clasificator, Rect area, int size) {

        Mat template = new Mat();
        Mat mROI = matResult.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();

        // 눈을 먼저 분리
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            //눈을 직사각형으로 감싸기
            Rect eye_only_rectangle = new Rect((int) e.tl().x, (int) (e.tl().y + e.height * 0.4), (int) e.width, (int) (e.height * 0.6));

            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            // 직사각형 그리기
            Imgproc.rectangle(matInput, eye_only_rectangle.tl(), eye_only_rectangle.br(), new Scalar(255, 255, 0, 255), 2);

            // 눈안에서 동공 찾기
            detectPupil(eye_only_rectangle);

            return template;
        }

        return template;
    }
    // 동공 검출 메소드
    protected void detectPupil(Rect eyeRect) {
        hierarchy = new Mat();

        Mat img = matInput.submat(eyeRect);
        Mat img_hue = new Mat();

        Mat circles = new Mat();

        // 색조로 변환, 범위 색상으로 변환하고 실패 줄이기 위해 흐리게 처리
        Imgproc.cvtColor(img, img_hue, Imgproc.COLOR_RGB2HSV);
        // 범위
        Core.inRange(img_hue, new Scalar(0, 0, 0), new Scalar(255, 255, 32), img_hue);
        // 침식
        Imgproc.erode(img_hue, img_hue, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        // 확장
        Imgproc.dilate(img_hue, img_hue, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6)));
        // 캐니 엣지 검출
        Imgproc.Canny(img_hue, img_hue, 170, 220);
        // 가우시안 필터링으로 블러주기, sigma 값 높을수록 강한 블러
        Imgproc.GaussianBlur(img_hue, img_hue, new Size(9, 9), 2, 2);
        // 허프 변환으로 원 찾기
        Imgproc.HoughCircles(img_hue, circles, Imgproc.CV_HOUGH_GRADIENT,
                1, 20, 50, 30, 7, 21);


        if (circles.cols() > 0) {
            for (int x = 0; x < circles.cols(); x++) {
                double vCircle[] = circles.get(0, x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int) Math.round(vCircle[2]);

                resList.add(vCircle[2]);

                // 찾은 원 그리기, 색 바꾸기 가능
                Imgproc.circle(img, pt, radius, new Scalar(0, 255, 0), 2);
                //Imgproc.circle(img, pt, 3, new Scalar(0, 0, 255), 2);
            }
        }
    }
    //캐스케이드 분류기 > 영상에서 불필요한 부분 버리고 필요한 부분(눈)부분만 빠르게 분류
    private CascadeClassifier loadClassifier(int rawResId, String filename, File cascadeDir) {
        CascadeClassifier classifier = null;
        try {
            InputStream is = getResources().openRawResource(rawResId);
            File cascadeFile = new File(cascadeDir, filename);
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (classifier.empty()) {
                Log.e(TAG, "cascade 분류기를 로드하지 못했습니다.");
                classifier = null;
            } else
                Log.i(TAG, "다음에서 로드된 cascade 분류기: " + cascadeFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "cascade를 로드하지 못했습니다. 예외 발생: " + e);
        }
        return classifier;
    }


    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }


    //여기서부턴 퍼미션 관련 메소드
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase : cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission) {
            onCameraPermissionGranted();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        } else {
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}
