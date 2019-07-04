package com.tjoeun.imagedownload0702;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RootActivity extends AppCompatActivity {
    class SAXHandler extends DefaultHandler{
        //태그 2개를 저장할 임시 변수
        Hani temp = new Hani();
        //태그 안의 내용을 저장할 임시 변수
        String content = null;
        //태그가 열렸는지 확인하기 위한 변수
        boolean initem = false;
        //태그가 열릴 때 호출되는 메소드
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs){
            // item 태그가 시작되면 2개의 태그를 저장할 객체를 생성
            if(localName.equals("item")){
                temp = new Hani();
                initem = true;
            }
        }

        //태그안에 내용을 만나면 호출되는 메소드
        @Override
        public void characters(char [] chars, int start, int length){
            if(initem == true){
                content = new String(chars, start, length);
            }
        }

        //닫는 태그를 만났을 때 호출되는 메소드
        @Override
        public void endElement(String uri, String localName, String qName){
            if(initem == true){
                if(localName.equals("item")){
                    initem = false;
                    dongalist.add(temp);
                }else if(localName.equals("title")){
                    temp.title = content;
                }else if(localName.equals("link")){
                    temp.link = content;
                }
            }
        }
    }

    class Hani{
        public String title;
        public String link;

        //객체를 문자열로 표현하는 메소드
        //출력하는 곳에 객체 이름을 설정하면 이 메소드의 호출 결과를 출력 합니다.
        @Override
        public String toString(){
            return title;
        }
    }

    ListView listView;
    ArrayAdapter<Hani> adapter;
    ArrayList<Hani> list;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    ArrayList<Hani> dongalist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        dongalist = new ArrayList<>();

        listView = (ListView)findViewById(R.id.articlelist);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(RootActivity.this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 하위 Activity 출력
                Intent intent = new Intent(RootActivity.this, SubActivity.class);
                intent.putExtra("link", list.get(i).link);
                startActivity(intent);
            }
        });

        Thread th = new Thread(){
            String xml = null;
            @Override
            public void run(){
                try{
                    String addr = "http://rss.donga.com/total.xml";
                    URL url = new URL(addr);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while (true){
                        String line = br.readLine();
                        if(line == null)
                            break;
                        sb.append(line);
                    }
                    br.close();
                    con.disconnect();
                    xml = sb.toString();
                    Log.e("XML", xml);

                }catch(Exception e){
                    Log.e("다운로드 예외", e.getMessage());
                }

                try{
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    XMLReader reader = parser.getXMLReader();

                    //Parsing을 Handler에게 위임
                    SAXHandler handler = new SAXHandler();
                    reader.setContentHandler(handler);
                    InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));
                    reader.parse(new InputSource(is));
                    Log.e("DongA", dongalist.toString());

                }catch (Exception e){
                    Log.e("파싱 예외", e.getMessage());
                }
            }
        };
        th.start();
    }

    //화면이 출력될 때 마다 호출되는 메소드
    @Override
    public void onResume(){
        super.onResume();
        Thread th = new Thread(){
            String xml = null;
            public void run(){
                try{
                    String addr = "http://www.hani.co.kr/rss/";
                    URL url = new URL(addr);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while (true){
                        String line = br.readLine();
                        if(line == null)
                            break;
                        sb.append(line);
                    }
                    br.close();
                    con.disconnect();
                    xml = sb.toString();
                    Log.e("XML", xml);

                }catch(Exception e){
                    Log.e("다운로드 예외", e.getMessage());
                }

                try {
                    //생성자를 이용해서 객체를 생성하지 않고, Factory Class를 이용하여 생성하는 이유는
                    //만드는 과정이 복잡한 경우에 개발자는 굳이 생성하는 과정을 알 필요없도록 하기 위해서 입니다.
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();

                    //문자열을 Stream으로 변환
                    InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));

                    //메모리에 펼치기
                    Document document = builder.parse(is);

                    //원하는 태그의 Data 가져오기
                    NodeList titles = document.getElementsByTagName("title");
                    NodeList links = document.getElementsByTagName("link");

                    //순회
                    for(int i=0 ; i<titles.getLength() ; i=i+1){
                        Node title = titles.item(i);
                        Node link = links.item(i);

                        Hani hani = new Hani();

                        //태그안의 내용도 자식으로 간주 합니다.
                        Node text = title.getFirstChild();
                        hani.title = text.getNodeValue();

                        text = link.getFirstChild();
                        hani.link = text.getNodeValue();

                        list.add(hani);
                    }
                    handler.sendEmptyMessage(0);

                }catch (Exception e){
                    Log.e("파싱 예외", e.getMessage());
                }

            }
        };
        th.start();
    }
}