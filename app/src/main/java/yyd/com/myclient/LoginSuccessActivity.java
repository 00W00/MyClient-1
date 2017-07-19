package yyd.com.myclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

public class LoginSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent=getIntent();
                Bundle bundle=intent.getExtras();
                Set<String> keySet=bundle.keySet();
                StringBuffer sb=new StringBuffer();
                for(String key:keySet){
                    sb.append(key+","+bundle.get(key)+"\n");
                }
                TextView textView=(TextView)findViewById(R.id.text_login_success);
                textView.setText(sb.toString());
            }
        }).start();
    }

    /**
     * 去下委托
     * @param view
     */
    public void quxiaweituo(View view){
        Intent intent=new Intent(this, RuWeiTuoDanActivity.class);
        startActivity(intent);
    }
}
