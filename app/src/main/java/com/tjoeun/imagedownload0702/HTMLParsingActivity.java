package com.tjoeun.imagedownload0702;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HTMLParsingActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    //ListView를 재출력하는 핸들러
    Handler handler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            //어댑터가 리스트뷰에 데이터의 변경이 발생했으니
            //데이터를 다시 출력하라고 메시지를 전송
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_htmlparsing);

        //리스트 뷰 출력
        listView = (ListView)findViewById(R.id.htmllistview);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                HTMLParsingActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        Button htmlparsing = (Button)findViewById(R.id.htmlparsing);
        htmlparsing.setOnClickListener((view)->{
            Thread th = new Thread(){
                @Override
                public void run(){
                    String html = null;
                    try{
                        //다운로드 받을 주소
                        String addr = "https://finance.naver.com";
                        //URL 생성
                        URL url = new URL(addr);
                        //URL 연결
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        //문자열로 읽을 스트림 생성
                        //UTF-8이 아니라소 인코딩 설정을 해주지 않으면 한글이 깨집니다.
                        //EUC-KR로 읽어오도록 설정
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"));
                        //문자열을 저장할 객체 생성
                        StringBuilder sb = new StringBuilder();
                        //문자열을 줄 단위로 읽어서 sb에 저장
                        while (true){
                            String line = br.readLine();
                            if(line == null)
                                break;
                            sb.append(line + "\n");
                        }
                        br.close();
                        con.disconnect();

                        html = sb.toString();
                        //Log.e("html", html);
                    }catch (Exception e){
                        Log.e("다운로드 예외", e.getMessage());

                    }
                    //html 파싱
                    try{
                        //html을 DOM 객체로 만들기
                        Document doc = Jsoup.parse(html);
                        Elements elements = doc.select("span > a");
                        Log.e("elements", elements.toString());
                        //선택된 데이터 순회
                        for(Element element : elements){
                            Log.e("element", element.text().trim());
                            //list.add(element.attr("href"));
                            list.add(element.text().trim());
                        }
                        //핸들러에게 listview 출력을 다시하라고 메시지를 전송
                        handler.sendEmptyMessage(0);
                    }catch (Exception e){
                        Log.e("HTML 파싱 예외", e.getMessage());
                    }

                }
            };
            th.start();

        });
    }
}
