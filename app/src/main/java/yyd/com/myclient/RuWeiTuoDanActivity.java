package yyd.com.myclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class RuWeiTuoDanActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruweiguodan);
        handler=new Handler();
        sharedPref = this.getSharedPreferences(
                getString(R.string.sessionStoreFile), Context.MODE_PRIVATE);
        String sessionID = sharedPref.getString(getString(R.string.SessionID),"sessionID is null");
        System.out.println(sessionID);
    }
    public void buy(View view){
       new Thread(new Runnable() {
           @Override
           public void run() {
               String commodityCode;
               String price;
               String count;
               try(AssetManager asset = getAssets();
                   InputStream input= asset.open("ruweituodan.xml");
                   BufferedReader br =new BufferedReader(new InputStreamReader(input));)
               {
                   SAXReader readerxml = new SAXReader();
                   Document doc = null;
                   doc = readerxml.read(br);
                   Element REQElement = doc.getRootElement().element(getString(R.string.REQ));

                   String userID = sharedPref.getString(getString(R.string.user_id), "");
                   REQElement.element(getString(R.string.user_id)).setText(userID);
                   REQElement.element(getString(R.string.CUSTOMER_ID)).setText(userID+"00");

                   String sessionID = sharedPref.getString(getString(R.string.SessionID), "");
                   REQElement.element(getString(R.string.SessionID)).setText(sessionID);

                   EditText commodityCodeEditText = (EditText) findViewById(R.id.commodityCode);
                   commodityCode = commodityCodeEditText.getText().toString();
                   REQElement.element(getString(R.string.COMMODITY_ID)).setText("66"+commodityCode);

                   EditText priceEditText = (EditText) findViewById(R.id.price);
                   price = priceEditText.getText().toString();
                   REQElement.element(getString(R.string.PRICE)).setText(price);

                   EditText countEditText = (EditText) findViewById(R.id.count);
                   count = countEditText.getText().toString();
                   REQElement.element(getString(R.string.QTY)).setText(count);

                   REQElement.element(getString(R.string.BUY_SELL)).setText("1");
                   REQElement.element(getString(R.string.SETTLE_BASIS)).setText("1");
                   REQElement.element(getString(R.string.CLOSEMODE)).setText("0");
                   REQElement.element(getString(R.string.L_PRICE)).setText("0");
                   REQElement.element(getString(R.string.TIMEFLAG)).setText("0");
                   REQElement.element(getString(R.string.BILLTYPE)).setText("0");
                   REQElement.element(getString(R.string.SO)).setText("0");
                   Document resultDoc = ClientUtil.post(getString(R.string.url), doc.asXML());
                   Element root = resultDoc.getRootElement();
                   List<Element> list = root.element(getString(R.string.REP)).element(getString(R.string.RESULT)).elements();
                   StringBuffer sb = new StringBuffer();
                   for (Element temp : list) {
                       sb.append(temp.getName()).append(",").append(temp.getText()).append("\n");
                   }
                   handler.post(new UpdateTextView(sb));

               } catch (IOException e) {
                   e.printStackTrace();
               } catch (DocumentException e) {
                   e.printStackTrace();
               }

           }
       }).start();

    }

    class UpdateTextView implements Runnable{
        StringBuffer sb;

        public UpdateTextView(StringBuffer sb) {
            this.sb = sb;
        }

        @Override
        public void run() {
            TextView textView = (TextView) findViewById(R.id.display);
            textView.setText(sb.toString());
        }
    }

    public void sell(View view){
        String commodityCode;
        String commodityName;
        double price;
        int count;

    }
}
