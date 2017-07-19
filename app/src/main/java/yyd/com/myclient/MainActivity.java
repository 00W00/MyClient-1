package yyd.com.myclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import yyd.com.myclient.activity.FragmentDrawer;
import yyd.com.myclient.activity.LoginFragment;
import yyd.com.myclient.activity.OrderFragment;


public class MainActivity extends ActionBarActivity implements FragmentDrawer.FragementDrawerlistener{
    private FragmentDrawer drawer;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mToolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        drawer=(FragmentDrawer)getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawer.setUp(R.id.fragment_navigation_drawer,(DrawerLayout)findViewById(R.id.drawer_layout),mToolbar);
        drawer.setDrawerListener(this);
    }


    @Override
    public void onDrawerItemSelected(View view, int position) {

       displayView(position);
    }

    private void displayView(int position){
        Fragment fragment=null;
        String title=getString(R.string.app_name);
        switch (position){
            case 0:
                fragment=new LoginFragment();
                title=getString(R.string.title_login);
                break;
            case 1:
                fragment=new OrderFragment();
                title=getString(R.string.title_order);
                break;
            default:
                break;
        }
        if (fragment!=null){
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body,fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(title);
        }
    }

    public void login(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                EditText usernameTV=(EditText)findViewById(R.id.username);
                EditText passwordTV=(EditText)findViewById(R.id.password);
                login(usernameTV.getText().toString().trim(),passwordTV.getText().toString().trim());
            }
        }).start();

    }

    private void login(String username,String password){
        AssetManager asset = this.getAssets();
        InputStream input=null;
        BufferedReader br=null;
        try{
            input = asset.open("protocol.xml");
            //获取登录协议的输入流
            br=new BufferedReader(new InputStreamReader(input));
            SAXReader readerxml = new SAXReader();
            Document doc = readerxml.read(br);
            Element REQElement=doc.getRootElement().elements("REQ").get(0);
            Element usernameElement=REQElement.elements("USER_ID").get(0);
            Element passwordElement=REQElement.elements("PASSWORD").get(0);
            //给协议赋值
            usernameElement.setText(username);
            passwordElement.setText(password);
            //将赋值后的协议转为xml字符串，传给工具方法。返回值是返回协议的Document对象
            Document document=ClientUtil.post("http://172.18.3.16:23405/issue_tradeweb/httpXmlServlet",doc.asXML());
            Element root = document.getRootElement();
            List<Element> list=root.elements("REP").get(0).elements();
            Element RESULTElement=list.get(0);


            Iterator<Element> childOfRESULTE=RESULTElement.elementIterator();
            Intent intent=new Intent(this, LoginSuccessActivity.class);
            while(childOfRESULTE.hasNext()){
                Element tempElement=childOfRESULTE.next();
                intent.putExtra(tempElement.getName(),tempElement.getText());
            }
            Element RETCODEElement=RESULTElement.element("RETCODE");
            System.out.println(RETCODEElement.getText());
            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.sessionStoreFile), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.SessionID), RETCODEElement.getText().toString());
            editor.putString(getString(R.string.user_id), username);
            editor.putString(getString(R.string.user_id), username);
            editor.commit();
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
    /**
     * Converts the contents of an InputStream to a String.
     */
    private String readStream(InputStream stream, int maxLength) throws IOException {
        String result = null;
        // Read InputStream using the UTF-8 charset.
        InputStreamReader reader = new InputStreamReader(stream, "GBK");
        // Create temporary buffer to hold Stream data with specified max length.
        char[] buffer = new char[maxLength];
        // Populate temporary buffer with Stream data.
        int numChars = 0;
        int readSize = 0;
        while (numChars < maxLength && readSize != -1) {
            numChars += readSize;
            int pct = (100 * numChars) / maxLength;
            readSize = reader.read(buffer, numChars, buffer.length - numChars);
        }
        if (numChars != -1) {
            // The stream was not empty.
            // Create String that is actual length of response body if actual length was less than
            // max length.
            numChars = Math.min(numChars, maxLength);
            result = new String(buffer, 0, numChars);
        }
        return result;
    }
}
