package com.tjoeun.imagedownload0702;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class OpenAPIJSON extends AppCompatActivity {
    EditText keyword;
    ListView listView;

    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    Handler handler = new Handler(){
      @Override
      public  void handleMessage(Message msg){
          adapter.notifyDataSetChanged();
      }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_apijson);

        keyword = (EditText)findViewById(R.id.keyword);
        listView = (ListView)findViewById(R.id.listview);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(OpenAPIJSON.this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        Button search = (Button)findViewById(R.id.search);

        search.setOnClickListener((view)->{
            //데이터를 다운로드 받아야 하므로 스레드 이용
            Thread th = new Thread(){
                @Override
                public void run() {
                    //검색어 가져오기
                    String key = keyword.getText().toString().trim();
                    //문자열을 가져온 결과를 저장
                    String json = null;
                    try {
                        //주소만들기
                        //한글 인코딩
                        key = URLEncoder.encode(key, "utf-8");
                        //String addr = "https://dapi.kakao.com/v2/search/image&query=" + key;
                        String addr =
                                "https://dapi.kakao.com/v3/search/book?target=title&query=" +
                                        key;

                        URL url = new URL(addr);

                        //Log.e("URL", addr);

                        //연결
                        HttpURLConnection con =
                                (HttpURLConnection) url.openConnection();
                        //헤더 만들기
                        con.setRequestProperty(
                                "Authorization",
                                "KakaoAK 91ef79e25c29f0dff7c35c3b3a9c0228");
                        //옵션 설정
                        con.setConnectTimeout(30000);
                        con.setUseCaches(false);
                        //요청에 정상적으로 응답을 하면
                        if (con.getResponseCode() == 200) {
                            //데이터를 문자열로 읽기위한 스트림 생성
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            //데이터 읽기
                            while (true) {
                                String line = br.readLine();
                                if (line == null) {
                                    break;
                                }
                                sb.append(line);
                            }
                            json = sb.toString();
                            br.close();
                            con.disconnect();

                            //Log.e("json1", json);
                        }

                    } catch (Exception e) {
                        Log.e("다운로드 예외", e.getMessage());
                    }
                    Log.e("json", json);

                    try{
                    //JSON 파싱 - price 와  title

                    //문자열을 JSON 객체로 변환
                    JSONObject documents = new JSONObject(json);
                    //documents 키에 해당하는 배열 가져오기
                         JSONArray books =
                                documents.getJSONArray("documents");

                        //JSONArray images = documents.getJSONArray("documents");
                        //배열을 순회
                        list.clear();
                        /*
                        for(int i=0; i<images.length(); i=i+1){
                            JSONObject image = images.getJSONObject(i);
                            list.add(image.getString("collection") + ":" +
                                    image.getString("display_sitename"));
                        }
                        */

                        for(int i=0; i<books.length(); i=i+1){
                            JSONObject book = books.getJSONObject(i);
                            //price와 title을 가져옴
                            //list.add(book.getString("title") + ":" + book.getInt("price"));
                            //authors와 title을 가져옴
                            list.add(book.getString("title") + ":" + book.getString("authors"));
                        }

                        //핸들러를 호출
                        handler.sendEmptyMessage(0);
                        //키보드를 화면에서 제거하기 위한 코드
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                keyword.getWindowToken(), 0);


                }catch (Exception e) {
                        Log.e("파싱 예외", e.getMessage());

                      }
                    }
            };
            th.start();
        });

    }
}
