package ziyi.smstest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ziyi.smstest.Utils.ShowToast;

public class MainActivity extends AppCompatActivity {
    private TextView sms_num;
    private TextView sms_content;
    private EditText to_num;
    private EditText to_content;
    private Button mButton_send;


    private IntentFilter receiveFilter;
    private MessageReceiver mMessageReceiver;
    private IntentFilter sendFilter;
    private SendStatusReceiver sendStatusReceiver;

    private String RECEIVE_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private String SENT_ACTION = "SENT_SMS_ACTION";
    private final int SMS_TEST_NOTIFITION_ID = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sms_num = (TextView) findViewById(R.id.sms_num);
        sms_content = (TextView) findViewById(R.id.sms_content);
        to_num = (EditText) findViewById(R.id.to_num);
        to_content = (EditText) findViewById(R.id.to_content);
        mButton_send = (Button) findViewById(R.id.but_send);

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(RECEIVE_ACTION);
        receiveFilter.setPriority(100);
        mMessageReceiver = new MessageReceiver();
        registerReceiver(mMessageReceiver, receiveFilter);

        sendFilter = new IntentFilter();
        sendFilter.addAction(SENT_ACTION);
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver, sendFilter);

        mButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(to_num.getText())) {
                    ShowToast.showToast(MainActivity.this, "号码不能为空！");
                } else if (TextUtils.isEmpty(to_content.getText())) {
                    ShowToast.showToast(MainActivity.this, "内容不能为空！");
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    Intent sentIntent = new Intent(SENT_ACTION);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, sentIntent, 0);
                    smsManager.sendTextMessage(to_num.getText().toString(), null, to_content.getText().toString(),pendingIntent, null);
                }
            }
        });
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(sendStatusReceiver);
    }


    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = intent.getStringExtra("format");
                Log.d("TAG", "pdus:" + pdus.toString());
                Log.d("TAG", "format:" + format.toString());
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }

                String address = messages[0].getOriginatingAddress();
                String fullMessage = "";
                for (SmsMessage message : messages) {
                    fullMessage += message.getMessageBody();
                }

                SendNotification();
                Log.d("TAG", "Send notification!");

                sms_num.setText(address);
                Log.d("TAG", "phone number:" + address);
                sms_content.setText(fullMessage);
                Log.d("TAG", "message:" + fullMessage);
            } else {
                Log.d("TAG", "bundle is null!");
            }
        }

    }

    class SendStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == RESULT_OK) {
                ShowToast.showToast(context, "短信发送成功！");
            } else {
                ShowToast.showToast(context, "短信发送失败！");
            }
        }
    }

    public void SendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                .setAutoCancel(true)
                .setContentTitle("SMSTest")
                .setContentTitle("You have a new message.")
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent =  PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(SMS_TEST_NOTIFITION_ID, builder.build());

    }
}
