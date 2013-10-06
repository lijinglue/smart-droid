package org.solopie.smartdroid.component;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * @author A.L.
 *
 * Decorate ResultReceiver, provide a way to release the reciever it self in the main thread
 */
public class WebserviceResultReceiver extends ResultReceiver{

    private Receiver receiver;
    public WebserviceResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(null != this.receiver) {
            this.receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
