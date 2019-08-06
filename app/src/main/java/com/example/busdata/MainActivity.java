package com.example.busdata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    //기본 위젯들
    EditText editBusStop;
    TextView text;
    Button button;
    TextView gpsdata;
    TextView stationdata;
    TextView internet;


    //공공데이터 API 사용을 위한 키값
    String key = "QzQ64Y0ttlhXPP7CVvMZKf6NKxitNjOameIBPVADX4f9%2FxPRnLqZkDljqmpTROuyOCabJF8ncXbxDqHGEFAtPA%3D%3D";

    //공공데이터 API에서 가져오는 데이터
    String data;
    String data1;

    //버스정류장 id와 관련된 변수
    String stationName = "null";
    String stationRealName = "null";

    //TTS 변수
    TextToSpeech tts;

    //경도와 위도 변수 선언
    double longitude = 0.0;
    double latitude = 0.0;

    int minute = 0;
    // 5분 이내에 오는 버스만 읽고 출력하도록 check 라는 boolean 함수를 선언
    boolean check = true;

    XmlPullParser xpp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //변수 레이아웃 연결
        editBusStop = (EditText) findViewById(R.id.edit);
        text = (TextView) findViewById(R.id.text);
        gpsdata = findViewById(R.id.gps);
        stationdata = findViewById(R.id.bus_station);
        internet = findViewById(R.id.internet);

        button = findViewById(R.id.button);

        //getGPSData();

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

//        바로 음성이 나와야 되니까 버튼 필요 없어서 주석으로 뺐습니다.
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
        //public void onClick(View v) {
//
//                switch( v.getId() ){
//                    case R.id.button2:

        getArrivalData();

        // http://blog.naver.com/PostView.nhn?blogId=ssarang8649&logNo=220947884163 참고
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                getArrivalData();
            }
        };

        // 버스도착예정시간을 새로고침하기 위한 타이머 _ 1분 간격으로 업데이트되도록 함.
        Timer timer = new Timer();
        timer.schedule(tt, 0, 60000);


        editBusStop.setText(FindBusStation.sName + FindBusStation.sKey);
    }

    /**public void getGPSData() {

        //gps정보 가져오기 링크 : https://bottlecok.tistory.com/54
        //Location Manager 생성
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //API버전확인
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        //위도, 경도 value 가져오기
        else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            // location 변수에 최근 gps정보 할당
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            } else {
                latitude = 0.0;
                longitude = 0.0;
            }
            //특정 시간이 지나거나 gps정보가 특정거리 이상 변경 되었을 때 gps 정보 업데이트
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    //시간 설정 (단위 ms)
                    1000,
                    //거리 설정  (단위 m)
                    1,
                    //gpslistner 연결
                    gpsLocationListener);

        }


    }*/

    // TextView 에 도착예정 버스를 set 해주고 읽어주는 함수
    private void getArrivalData() {

        new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                //stationName에 가까운 버스정류장을 넣어주는 함수
                getXmlData2();
                //아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기
                data1 = getXmlData();
                if(!data1.contentEquals("")) {
                    //빨리 오는 버스 순서대로 정렬하기 위해 stringArray 선언
                    String[] stringArray = data1.split(" \n");
                    //sorting해주기 위한 반복문
                    for (int j = 0; j < stringArray.length - 1; j++) {
                        for (int i = 0; i < stringArray.length - 1 - j; i++) {
                            //빨리 도착하는 순서로 바꿔주기
                            if (Integer.parseInt(stringArray[i].split("분")[0] + "") > Integer.parseInt(stringArray[i + 1].split("분")[0])) {
                                String temp;
                                temp = stringArray[i];
                                stringArray[i] = stringArray[i + 1];
                                stringArray[i + 1] = temp;
                            }
                        }
                    }

                    data = "";
                    for (int k = 0; k < stringArray.length; k++) {
                        data = data + stringArray[k] + " \n";
                    }
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        stationdata.setText(FindBusStation.sKey + " " + FindBusStation.sName);

                        /**if(longitude != 0.0 && latitude != 0.0) {
                            gpsdata.setText(longitude + "\n" + latitude + "");
                        } else {
                            gpsdata.setText("gps를 가져오지 못함");
                        }*/

                        if(xpp == null) {
                            internet.setText("인터넷 널값");
                        } else {
                            internet.setText("인터넷 연결 성공");
                        }



                        //data가 비었을 경우 도착 정보가 없음 표시
                        if(data1.contentEquals("")) {
                            text.setText("5분 이내에 도착하는 버스가 없습니다.");
                        } else {
                            //TextView에 문자열 data 출력
                            text.setText(data);
                        }
                        //http://stackoverflow.com/a/29777304 참고
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if(data1.contentEquals("")) {
                                ttsGreater21("5분 이내에 도착하는 버스가 없습니다.");
                            } else {
                                ttsGreater21(data);
                            }
                        } else {
                            if (data1.contentEquals("")) {
                                ttsUnder20("5분 이내에 도착하는 버스가 없습니다.");
                            } else {
                                ttsUnder20(data);
                            }
                        }
                    }
                });
            }
        }).start();

    }

    //https://bottlecok.tistory.com/54 참고
    //location listener 선언부분 : gps 정보가 바뀌는 이벤트를 받아주는 listener
    /** final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        //사용하지 않는 메서드들
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };*/


    //https://movie13.tistory.com/1 --> 공공기관 데이터 가져오는 법 레퍼런스
    private String getXmlData() {
        //버퍼 변수 선언, 스트링형으로 만들어져있음, 모든 정보 저장후 한번에 버퍼 출력 하는 형식
        StringBuffer buffer = new StringBuffer();

        //공공기관 데이터 가져오는 url형식, stationName에 nodeid를 GPS기능으로 찾아서 저장하면, 그 정보를 이용해서
        //도착정보 조회서비스의 정류소별 도착 예정 정보 목록 조회 API를 검색해서 버스 정보를 가져오기 위해, url만들기
        String queryUrl = "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?"//요청 URL
                + "&cityCode=37010" + "&nodeId=" + FindBusStation.sCode + "&ServiceKey=" + key;
        Log.d("디버깅", queryUrl);

        try {
            //문자열로 된 요청 url을 URL 객체로 생성.
            URL url = new URL(queryUrl);

            //url위치로 입력스트림 연결
            InputStream is = url.openStream();

            //XML형식의 API를 파싱하는 라이브러리
            //XML형식은 순차적으로 발생하기때문에 뒤로 못돌아가서 필요하면 변수에 미리 저장해둬야함.
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            xpp = factory.newPullParser();
            //inputstream 으로부터 xml 입력받기
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            //XML 안에 들어가는 태그
            String tag;

            //태그 하나 내려감
            xpp.next();
            int eventType = xpp.getEventType();

            //XML 끝이 아니라면
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //파싱 시작알리기, 이벤트가 문서의 시작
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    //태그의 시작
                    case XmlPullParser.START_TAG:
                        //태그 이름 얻어오기
                        tag = xpp.getName();
                        // 첫번째 검색결과
                        if (tag.equals("item")) ;

                        //태그의 이름이 arrtime이면,
                        else if (tag.equals("arrtime")) {
                            //buffer.append("도착예정버스 도착예상시간[초] : ");
                            //그 옆의
                            xpp.next();
                            //초를 가져와서 분으로 바꿈
                            minute = Integer.parseInt(xpp.getText()) / 60;

                            // 5분 이내에 오는 버스만 읽고 출력하도록 check 라는 boolean 함수를 세팅
                            if (minute > 10) check = false;
                            else check = true;
                            //버퍼에 순서대로 출력하고 싶은 형식으로 저장,
                            if (check) {
                                buffer.append(minute + "" + "분 뒤에");
                                buffer.append("\n");
                            }
                            //다시 반복문 올라갔다가, item 태그의 이름별로 찾음.
                        } /** else if (tag.equals("nodenm")) {
                            //buffer.append("정류소명 :");
                            xpp.next();
                            buffer.append(xpp.getText() + "정류소에");
                            buffer.append("\n");
                            //... 아런식으로 반복해서 API에서 필요한 정보 변수에 저장
                        } */else if (tag.equals("routeno")) {
                            if (check) {
                                //buffer.append("버스번호 :");
                                xpp.next();
                                buffer.append(xpp.getText() + "번 버스가 도착합니다");
                                buffer.append("\n");
                            }
                        } /** else if (tag.equals("vehicletp")) {
                            buffer.append("도착예정버스 차량유형은 :");
                            xpp.next();
                            buffer.append(xpp.getText());//
                            buffer.append("\n");
                        } */

                        break;

                    //태그의 시작과 끝 사이에서 나타난다. 예: <data>여기서 텍스트 이벤트 발생</data>
                    case XmlPullParser.TEXT:
                        break;

                    //이벤트가 문서의 끝
                    case XmlPullParser.END_TAG:
                        //태그 이름 얻어오기
                        tag = xpp.getName();
                        // 첫번째 검색결과종료..줄바꿈
                        if (tag.equals("item") && check) buffer.append(" \n");
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            // Auto-generated catch blocke.printStackTrace();
        }
        //buffer.append("파싱 끝\n");
        //StringBuffer 문자열 객체 반환
        return buffer.toString();
    }


    //https://movie13.tistory.com/1 참고
    private void getXmlData2() {

        //가장 가까운 버스정류장을 가져오기 위한 count 변수 선언
        int count = 0;

        StringBuffer buffer = new StringBuffer();
        String queryUrl = "http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getCrdntPrxmtSttnList?"
                + "serviceKey=" + key + "&gpsLati=" + latitude + "&gpsLong=" + longitude;
        Log.d("디버깅",queryUrl);



        try {
            Log.d("디버깅","들어와");

            stationName = "null";
            stationRealName = "null";
            URL url = new URL(queryUrl);
            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));
            Log.d("디버깅", xpp+"");

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            Log.d("디버깅",count+"" + eventType);

            //현재값 2, 스타트다큐 0, 엔드다큐 1, 스타트태그 2; 택스트 4, 엔드태그 3
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        Log.d("디버깅",count+"");

                        break;

                    case XmlPullParser.START_TAG:
                        //태그 이름 얻어오기
                        tag = xpp.getName();

                        // 첫번째 검색결과
                        if (tag.equals("item"));
                        else if (tag.equals("nodeid")) {
                            buffer.append("정류장 id");
                            buffer.append(" : ");
                            xpp.next();
                            //TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append(xpp.getText());
                            //줄바꿈 문자 추가
                            buffer.append("\n");
                            //첫번째 값이면 가장 가까운 버스정류장이므로 stationName 변수에 따로 값을 저장
                            if (count == 0) {
                                stationName = xpp.getText();
                                Log.d("디버깅",count + stationName);

                            }
                            //가장 가까운 정류장을 알아내기 위함이였으므로 count 변수 1씩 증가시킴.
                            //count++;
                        }
                        else if(tag.equals("nodenm")) {
                            buffer.append("정류장 이름");
                            buffer.append(":");
                            xpp.next();
                            //TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append(xpp.getText());
                            //줄바꿈 문자 추가
                            buffer.append("\n");
                            if(count == 0) {
                                stationRealName = xpp.getText();
                                Log.d("디버깅",stationRealName);
                            }
                            //가장 가까운 정류장을 알아내기 위함이였으므로 count 변수 1씩 증가시킴.
                            count++;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        //태그 이름 얻어오기
                        tag = xpp.getName();
                        //첫번째 검색결과종료..줄바꿈
                        if (tag.equals("item")) buffer.append("\n");
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            xpp = null;
            Log.d("디버깅","나왔어");

            //Auto-generated catch blocke.printStackTrace();
        }
        // buffer.append("파싱 끝\n");
    }



    //https://webnautes.tistory.com/847 -->음성인식 기능 레퍼런스
    //무조건 tts를 위해서 onclick할때 override 되어야 오류가 뜨지 않음. 그냥 두면 됨.
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            //음성인식 다하면 끝내기
            tts.stop();
            tts.shutdown();
        }
    }

    //API 버전 20 아래용
    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        //해쉬맵 만들기
        HashMap<String, String> map = new HashMap<>();
        //그냥 음성인식기술 해쉬맵에 넣어주는 코드
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        //text 파라미터로 전달받은 인자를 해쉬맵이랑 연결해서 음성으로 바꿔서 말해줌
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    //API버전 21 이상용
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        //text 파라미터로 전달받은 인자를 음성으로 바꿔서 말해줌
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}