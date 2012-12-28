package com.example.ContextRequesterApp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.jingbling.ContextEngine.IContextService;

import java.util.ArrayList;
import java.util.List;

public class ContextRequestAct extends Activity implements View.OnClickListener {
    // declare variables
    private Button bBindServiceBtn;
    private Button bUnbindServiceBtn;
    private Button bLaunchContextActBtn;
    private Button bLaunchTrainingActBtn;

    private IContextService targetContextService;
    private Bundle appThreadArgs;
    private Message appThreadMsg;

    // Temp variables for debugging
    String ACT_TAG="ContextRequestAct";

    private ServiceConnection appServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(ACT_TAG, "SERVICE CONNECTED");
            targetContextService = IContextService.Stub.asInterface(service);
            Toast.makeText(getApplicationContext(), "service connected",
                    Toast.LENGTH_LONG);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(ACT_TAG, "SERVICE DISCONNECTED");
            targetContextService = null;
            Toast.makeText(getApplicationContext(), "service disconnected",
                    Toast.LENGTH_LONG);
        }
    };

    private volatile Looper appServiceLooper;
    private volatile ServiceHandler appServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Bundle args = (Bundle)msg.obj;

            if ( args.containsKey("context_case") ) {
                remoteContextCall(args.getStringArrayList("FeaturesList"), args.getString("ClassifierName"), args.getString("ContextGroup"));
            }
//            else if ( args.containsKey("fibsum") ) {
//                remoteActivityCall();
//            }
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialize buttons

        bBindServiceBtn = (Button)findViewById(R.id.btnBindService);
        bBindServiceBtn.setOnClickListener(this);

        bUnbindServiceBtn = (Button)findViewById(R.id.btnUnbindService);
        bUnbindServiceBtn.setOnClickListener(this);

        bLaunchContextActBtn = (Button)findViewById(R.id.btnGetContext);
        bLaunchContextActBtn.setOnClickListener(this);
        bLaunchContextActBtn.setEnabled(false);

        bLaunchTrainingActBtn = (Button)findViewById(R.id.btnTrainData);
        bLaunchTrainingActBtn.setOnClickListener(this);
        bLaunchTrainingActBtn.setEnabled(false);

        HandlerThread hthr =
                new HandlerThread("StartedContextLooperService2Thread",
                        android.os.Process.THREAD_PRIORITY_BACKGROUND);
        hthr.start();
        appServiceLooper = hthr.getLooper();
        appServiceHandler = new ServiceHandler(appServiceLooper);
        appThreadArgs = new Bundle();
    }


    @Override
    public void onClick(View v) {
        switch ( v.getId() ) {
            case R.id.btnBindService:
                bindService(new Intent(IContextService.class.getName()),
                        appServiceConn, Context.BIND_AUTO_CREATE);
                bBindServiceBtn.setEnabled(false);
                bLaunchContextActBtn.setEnabled(true);
                bLaunchTrainingActBtn.setEnabled(true);
                bUnbindServiceBtn.setEnabled(true);
                break;
            case R.id.btnUnbindService:
                unbindService(appServiceConn);
                bBindServiceBtn.setEnabled(true);
                bLaunchContextActBtn.setEnabled(false);
                bLaunchTrainingActBtn.setEnabled(false);
                bUnbindServiceBtn.setEnabled(false);
                break;
            case R.id.btnGetContext:

                appThreadMsg = appServiceHandler.obtainMessage();
                appThreadArgs.clear();
                appThreadArgs.putBoolean("context_case", true);

                // For now, hard-code arguments to test remote call
                ArrayList<String> stringArrayInput = null;
//                stringArrayInput.add(0,"accelx");
//                stringArrayInput.add(1,"accely");
//                stringArrayInput.add(2,"accelz");
                appThreadArgs.putStringArrayList("FeaturesList", stringArrayInput);
                appThreadArgs.putString("ClassifierName","libSVM");
                appThreadArgs.putString("ContextGroup","activity");

                appThreadMsg.obj = appThreadArgs;
                appServiceHandler.sendMessage(appThreadMsg);
                break;
//            case R.id.btnTrainData:
//                //long fibn = Long.parseLong(mNEdtTxt.getText().toString());
//                //remoteFibonacciSum(fibn);
//                //mFibThread = new Thread(null, new FibSumResponseWorker(),
//                //		"FibSumResponseWorker");
//                //mFibThread.start();
//                mThreadMsg = mServiceHandler.obtainMessage();
//                mThreadArgs.clear();
//                mThreadArgs.putBoolean("fibsum", true);
//                mThreadArgs.putLong("n", Long.parseLong(mNEdtTxt.getText().toString()));
//                mThreadMsg.obj = mThreadArgs;
//                mServiceHandler.sendMessage(mThreadMsg);
//                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Calls to remote service

    private void remoteContextCall(List<String> featuresToUse, String classifierToUse, String contextGroup) {
        try {
            String contextOut = targetContextService.getContext(featuresToUse, classifierToUse, contextGroup);
            Toast.makeText(getApplicationContext(),
                    "Context = "+contextOut,
                    Toast.LENGTH_LONG).show();
        }
        catch ( RemoteException ex ) {
            Toast.makeText(getApplicationContext(),
                    ex.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void remoteActivityCall(long n) {
//        try {
//            long v = targetContextService.fibonacciSum(n);
//            Toast.makeText(getApplicationContext(),
//                    "fibsum="+v,
//                    Toast.LENGTH_LONG).show();
//        }
//        catch ( RemoteException ex ) {
//            Toast.makeText(getApplicationContext(),
//                    ex.toString(),
//                    Toast.LENGTH_LONG).show();
//        }
    }
}
