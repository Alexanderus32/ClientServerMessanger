package sabernero.com;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class SocketConnection {

    public static Socket mSocket;

    static {
        try {
            mSocket = IO.socket("https://mysterious-lake-76125.herokuapp.com/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    /*
    {
        try {
            //https://mysterious-lake-76125.herokuapp.com/
            mSocket = IO.socket("http://192.168.0.101:5000/");
        } catch (URISyntaxException e) {}
    }*/

    public static void SocketStart(){
        mSocket.connect();
    }

    public static void SocketClose(){
        mSocket.disconnect();
    }


}
