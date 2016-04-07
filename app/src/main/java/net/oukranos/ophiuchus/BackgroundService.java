package net.oukranos.ophiuchus;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

/**
 * Created by francis on 3/24/16.
 */
public class BackgroundService extends Service {
    public static final int MSG_START_SERVER_UPLOAD = 0;
    public static final int MSG_STOP_SERVER_UPLOAD  = 1;

    public static final int ACTION_UPLOAD_STARTED   = 0;
    public static final int ACTION_UPLOAD_PROGRESS  = 1;
    public static final int ACTION_UPLOAD_FINISHED  = 2;

    private final Messenger _messenger = new Messenger(new MessageHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return _messenger.getBinder();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SERVER_UPLOAD:
                    // TODO
                    break;
                case MSG_STOP_SERVER_UPLOAD:
                    // TODO
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

            return;
        }
    }

}
