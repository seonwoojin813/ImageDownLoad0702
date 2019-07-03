package com.tjoeun.imagedownload0702;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailItemActivity extends AppCompatActivity {

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bitmap bitmap = (Bitmap)msg.obj;
            ImageView imageView = (ImageView)findViewById(R.id.imgview);
            imageView.setImageBitmap(bitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);

        Button backbtn = (Button)findViewById(R.id.backbtn);
        backbtn.setOnClickListener((view)->{
            //화면을 제거
            finish();
        });

        //앞에서 넘겨준 데이터 가져오기
        int itemid = getIntent().getIntExtra(
                "itemid", 1);

        Thread th = new Thread(){
            @Override
            public void run(){
                String addr = "http://192.168.0.108:8080/item/getitem?itemid="
                        + itemid;
                //Log.e("addr", addr);
                String json = null;
                try{
                    URL url = new URL(addr);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    while(true){
                        String line = br.readLine();
                        if(line == null){
                            break;
                        }
                        sb.append(line);
                    }
                    json = sb.toString();
                    br.close();
                    con.disconnect();

                }catch (Exception e){
                    Log.e("다운로드 예외", e.getMessage());
                }
                //Log.e("json", json);

                try{
                    JSONObject item = new JSONObject(json);
                    String itemname = item.getString("itemname");
                    int price = item.getInt("price");
                    String description = item.getString("description");
                    String pictureurl = item.getString("pictureurl");

                    TextView itemn =(TextView)findViewById(R.id.itemname);
                    itemn.setText(itemname);
                    TextView pri = (TextView)findViewById(R.id.price);
                    pri.setText(price + "");
                    TextView des = (TextView)findViewById(R.id.description);
                    des.setText(description);


                    //이미지를 다운로드 받아서 핸들러에게 전송
                    URL imageURL = new URL(
                            "http://192.168.0.108:8080/item/img/"
                            + pictureurl);
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            imageURL.openStream());
                    Message msg = new Message();
                    msg.obj = bitmap;
                    handler.sendMessage(msg);


                }catch (Exception e){
                    Log.e("파싱 예외", e.getMessage());
                }
            }
        };
        th.start();
    }
}
