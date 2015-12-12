package me.jimm.popularmovies;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Created by generaluser on 12/10/15.
 */
public class MovieDbApiResponseReceiver extends ResultReceiver {

    private static final String TAG = "JIMM" + MovieDbApiResponseReceiver.class.getSimpleName();

    private Receiver mReceiver;

    public interface Receiver {
        public void onReceiveResponse(int resultCode, Bundle data) ;
    }
    public MovieDbApiResponseReceiver(Handler handler) {
        super(handler);
        Log.d(TAG, "MovieDbApiResponseReceiver");
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle data) {
        if (mReceiver != null) {
            mReceiver.onReceiveResponse(resultCode, data);
        }
    }
}
