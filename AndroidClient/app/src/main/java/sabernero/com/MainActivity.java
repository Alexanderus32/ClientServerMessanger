package sabernero.com;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText textField;
    private ImageButton sendButton;

    public static final String TAG  = "MainActivity";
    public static String uniqueId;

    private String Username;

    private Boolean hasConnection = false;

    private ListView messageListView;
    private MessageAdapter messageAdapter;
    final String MenuItem1 = "Перейти к диалогу";
    
    private Thread thread2;
    private boolean startTyping = false;
    private int time = 2;

    private ArrayList<String> UsersCount = new ArrayList<String>();

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://mysterious-lake-76125.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    @SuppressLint("HandlerLeak")
    Handler handler2=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage: typing stopped " + startTyping);
            if(time == 0){
                setTitle("Пожилой голубь");
                Log.i(TAG, "handleMessage: typing stopped time is " + time);
                startTyping = false;
                time = 2;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Username = getIntent().getStringExtra("username");

        uniqueId = UUID.randomUUID().toString();
        Log.i(TAG, "onCreate: " + uniqueId);

        if(savedInstanceState != null){
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if(!hasConnection){
            mSocket.connect();
            mSocket.on("connect user", onNewUser);
            mSocket.on("chat message", onNewMessage);
            mSocket.on("on typing", onTyping);
            //mSocket.on("create", onNewChat);
            //сделать Событие для личного чата


            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " Connected");
                mSocket.emit("connect user", userId);
                UsersCount.add(getIntent().getStringExtra("uniqueId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "onCreate: " + hasConnection);
        hasConnection = true;


        Log.i(TAG, "onCreate: " + Username + " " + "Connected");

        textField = findViewById(R.id.textField);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);
        /////
        registerForContextMenu(messageListView);

        List<MessageFormat> messageFormatList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.item_message, messageFormatList);
        messageListView.setAdapter(messageAdapter);

        onTypeButtonEnable();
    }

    /////
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()){
            case R.id.messageListView:
                menu.add(MenuItem1);
                menu.add("Въебать");
                menu.add("Послать на пересдачу");
                break;
        }
    }
    //хуйня. В метод даже не попадает
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        JSONObject jsonObject = new JSONObject();
        try {
            Log.i(TAG, "room:  "+ uniqueId);
            jsonObject.put("username", Username+ " тупа хочет начать чат");
            jsonObject.put("room", uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "NEWROOM: 1"+ mSocket.emit("create room", jsonObject));


        return super.onContextItemSelected(item);
    }

    //Отправка сообщения в комнату
    public void sendRoomMessage(View view){
        Log.i(TAG, "sendMessage: ");
        String message = textField.getText().toString().trim();
        if(TextUtils.isEmpty(message)){
            Log.i(TAG, "sendMessage:2 ");
            return;
        }
        textField.setText("");
        JSONObject roomMessage = new JSONObject();


        try {
            roomMessage.put("username", Username);
            roomMessage.put("message", message);
            roomMessage.put("uniqueid", uniqueId);
            roomMessage.put("room", uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendRoomMessage: 1 "+mSocket.emit("room message", roomMessage));
        //Log.i(TAG, "sendMessage: 1"+ mSocket.emit("chat message", jsonObject));
    }

    // socket.on('create room', function(room) {
    //	socket.join(room);
    //	io.emit('new wroom', room);
    //});
    //
    // socket.on('room message', function(msg){
    //    console.log("Message " + msg['message']);
    //    socket.to(msg['room']).emit( 'room message', msg);
    //  });

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasConnection", hasConnection);
    }

    public void onTypeButtonEnable(){
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                JSONObject onTyping = new JSONObject();
                try {
                    onTyping.put("typing", true);
                    onTyping.put("username", Username);
                    onTyping.put("uniqueId", uniqueId);
                    mSocket.emit("on typing", onTyping);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    //событие о новом сообщении
    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: ");
                    Log.i(TAG, "run: " + args.length);
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String id;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        id = data.getString("uniqueId");

                        Log.i(TAG, "run: " + username + message + id);

                        MessageFormat format = new MessageFormat(id, username, message);
                        Log.i(TAG, "run:4 ");
                        messageAdapter.add(format);
                        Log.i(TAG, "run:5 ");

                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    //Событие о новом пользователе
    Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int length = args.length;

                    if(length == 0){
                        return;
                    }
                    //Here i'm getting weird error..................///////run :1 and run: 0
                    Log.i(TAG, "run: ");
                    Log.i(TAG, "run: " + args.length);
                    String username =args[0].toString();
                    try {
                        JSONObject object = new JSONObject(username);
                        username = object.getString("username");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MessageFormat format = new MessageFormat(null, username, null);
                    messageAdapter.add(format);
                    messageListView.smoothScrollToPosition(0);
                    messageListView.scrollTo(0, messageAdapter.getCount()-1);
                    Log.i(TAG, "run: " + username);
                }
            });
        }
    };


    //Событие о новом пользователе
    Emitter.Listener onNewChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int length = args.length;

                    if(length == 0){
                        return;
                    }
                    //Here i'm getting weird error..................///////run :1 and run: 0
                    Log.i(TAG, "chat: ");
                    Log.i(TAG, "chat: " + args.length);
                    String username =args[0].toString();
                    try {
                        JSONObject object = new JSONObject(username);
                        username = object.getString("username");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MessageFormat format = new MessageFormat(null, username, " Хочет начать с вами личный чат");
                    messageAdapter.add(format);
                    messageListView.smoothScrollToPosition(0);
                    messageListView.scrollTo(0, messageAdapter.getCount()-1);
                    Log.i(TAG, "chat: " + username);
                }
            });
        }
    };


    Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, "run: " + args[0]);
                    try {
                        Boolean typingOrNot = data.getBoolean("typing");
                        String userName = data.getString("username") + " is Typing......";
                        String id = data.getString("uniqueId");

                        if(id.equals(uniqueId)){
                            typingOrNot = false;
                        }else {
                            setTitle(userName);
                        }

                        if(typingOrNot){

                            if(!startTyping){
                                startTyping = true;
                                thread2=new Thread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                while(time > 0) {
                                                    synchronized (this){
                                                        try {
                                                            wait(1000);
                                                            Log.i(TAG, "run: typing " + time);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        time--;
                                                    }
                                                    handler2.sendEmptyMessage(0);
                                                }

                                            }
                                        }
                                );
                                thread2.start();
                            }else {
                                time = 2;
                            }
                            
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void addMessage(String username, String message) {

    }

    public void sendMessage(View view){
        Log.i(TAG, "sendMessage: ");
        String message = textField.getText().toString().trim();
        if(TextUtils.isEmpty(message)){
            Log.i(TAG, "sendMessage:2 ");
            return;
        }
        textField.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
            jsonObject.put("username", Username);
            jsonObject.put("uniqueId", uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendMessage: 1"+ mSocket.emit("chat message", jsonObject));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isFinishing()){
            Log.i(TAG, "onDestroy: ");

            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " DisConnected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("chat message", onNewMessage);
            mSocket.off("connect user", onNewUser);
            mSocket.off("on typing", onTyping);
           // mSocket.off("create", onNewChat);
            Username = "";
            messageAdapter.clear();
        }else {
            Log.i(TAG, "onDestroy: is rotating.....");
        }

    }


}
