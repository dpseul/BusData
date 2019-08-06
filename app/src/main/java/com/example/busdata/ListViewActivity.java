package com.example.busdata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity {

    //공공데이터 API 사용을 위한 키값
    String key = "QzQ64Y0ttlhXPP7CVvMZKf6NKxitNjOameIBPVADX4f9%2FxPRnLqZkDljqmpTROuyOCabJF8ncXbxDqHGEFAtPA%3D%3D";

    XmlPullParser xpp;
    ArrayList<String> busnum = new ArrayList<>();

    int minute = 0;
    // 5분 이내에 오는 버스만 읽고 출력하도록 check 라는 boolean 함수를 선언
    boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);


        new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                getBusNumData();
                //Log.d("디버깅2", stationKey.get(0) + "" + stationName.get(0));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 2. 레이아웃 파일에 정의된 ListView를 자바 코드에서 사용할 수 있도록 합니다.
                        // findViewById 메소드는 레이아웃 파일의 android:id 속성을 이용하여 뷰 객체를 찾아 리턴합니다.
                        ListView listview = (ListView) findViewById(R.id.listview);

                        // 3. 실제로 문자열 데이터를 저장하는데 사용할 ArrayList 객체를 생성합니다.
                        final ArrayList<String> list = new ArrayList<>();

                        // 4. ArrayList 객체에 데이터를 집어넣습니다.
                        for (int i = 0; i < busnum.size(); i++) {
                            list.add(busnum.get(i) + " 번 버스");
                        }


                        // 5. ArrayList 객체와 ListView 객체를 연결하기 위해 ArrayAdapter객체를 사용합니다.
                        // 우선 ArrayList 객체를 ArrayAdapter 객체에 연결합니다.
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                ListViewActivity.this, //context(액티비티 인스턴스)
                                android.R.layout.simple_list_item_1, // 한 줄에 하나의 텍스트 아이템만 보여주는 레이아웃 파일
                                // 한 줄에 보여지는 아이템 갯수나 구성을 변경하려면 여기에 새로만든 레이아웃을 지정하면 됩니다.
                                list  // 데이터가 저장되어 있는 ArrayList 객체
                        );


                        // 6. ListView 객체에 adapter 객체를 연결합니다.
                        listview.setAdapter(adapter);


                        // 7. ListView 객체의 특정 아이템 클릭시 처리를 추가합니다.
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> adapterView,
                                                    View view, int position, long id) {

                                // 8. 클릭한 아이템의 문자열을 가져와서
                                String selected_item = (String) adapterView.getItemAtPosition(position);

                                // 9. 해당 아이템을 ArrayList 객체에서 제거하고
                                list.remove(selected_item);

                                // 10. 어댑터 객체에 변경 내용을 반영시켜줘야 에러가 발생하지 않습니다.
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }).start();


    }

    //https://movie13.tistory.com/1 --> 공공기관 데이터 가져오는 법 레퍼런스
    private String getBusNumData() {
        //버퍼 변수 선언, 스트링형으로 만들어져있음, 모든 정보 저장후 한번에 버퍼 출력 하는 형식
        StringBuffer buffer = new StringBuffer();

        //공공기관 데이터 가져오는 url형식, stationName에 nodeid를 GPS기능으로 찾아서 저장하면, 그 정보를 이용해서
        //도착정보 조회서비스의 정류소별 도착 예정 정보 목록 조회 API를 검색해서 버스 정보를 가져오기 위해, url만들기
        String queryUrl = "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?"//요청 URL
                + "&cityCode=37010" + "&nodeId=" + FindBusStation.sCode + "&ServiceKey=" + key;
        Log.d("디버깅11", queryUrl);

        try {
            //문자열로 된 요청 url을 URL 객체로 생성.
            URL url = new URL(queryUrl);

            //url위치로 입력스트림 연결
            InputStream is = url.openStream();
            Log.d("디버깅11", "들어옴" + queryUrl);

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
                                busnum.add(xpp.getText());
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

}