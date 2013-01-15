package com.example.ContextRequesterApp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
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
    private Button bCloseAppBtn;

    private Spinner bContextSelection;

    private CheckBox featureSelectBox1;
    private CheckBox featureSelectBox2;
    private CheckBox featureSelectBox3;
    private CheckBox featureSelectBox4;
    private int numCheckBoxes = 4;

    private IContextService targetContextService;
    private Bundle appThreadArgs;
    private Message appThreadMsg;
    private ArrayList<String> stringArrayInput = new ArrayList<String>();

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
            else if ( args.containsKey("train_data") ) {
                remoteActivityCall(args.getStringArrayList("FeaturesList"), args.getString("ContextGroup"));
            }
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

        bCloseAppBtn = (Button)findViewById(R.id.btnCloseApp);
        bCloseAppBtn.setOnClickListener(this);

        bContextSelection = (Spinner) findViewById(R.id.context_spinner);

        featureSelectBox1 = (CheckBox)findViewById(R.id.feature_checkbox1);
        featureSelectBox2 = (CheckBox)findViewById(R.id.feature_checkbox2);
        featureSelectBox3 = (CheckBox)findViewById(R.id.feature_checkbox3);
        featureSelectBox4 = (CheckBox)findViewById(R.id.feature_checkbox4);

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

                // Use selected variables to pass arguments to remote call
                // Features List

                appThreadMsg = appServiceHandler.obtainMessage();
                appThreadArgs.clear();
                appThreadArgs.putBoolean("context_case", true);

                // For now, hard-code arguments to test remote call
                stringArrayInput.clear();
                // Add checked features to array list
                if (featureSelectBox1.isChecked())
                    stringArrayInput.add(featureSelectBox1.getText().toString());
                if (featureSelectBox2.isChecked())
                    stringArrayInput.add(featureSelectBox2.getText().toString());
                if (featureSelectBox3.isChecked())
                    stringArrayInput.add(featureSelectBox3.getText().toString());
                if (featureSelectBox4.isChecked())
                    stringArrayInput.add(featureSelectBox4.getText().toString());

                appThreadArgs.putStringArrayList("FeaturesList", stringArrayInput);
                appThreadArgs.putString("ClassifierName",bContextSelection.getSelectedItem().toString());
                appThreadArgs.putString("ContextGroup","activity");

                appThreadMsg.obj = appThreadArgs;
                appServiceHandler.sendMessage(appThreadMsg);
                break;
            case R.id.btnTrainData:
//
                // Use selected variables to pass arguments to remote call
                // Features List

                appThreadMsg = appServiceHandler.obtainMessage();
                appThreadArgs.clear();
                appThreadArgs.putBoolean("train_data", true);

                // For now, hard-code arguments to test remote call
                stringArrayInput.clear();
                // Add checked features to array list
                if (featureSelectBox1.isChecked())
                    stringArrayInput.add(featureSelectBox1.getText().toString());
                if (featureSelectBox2.isChecked())
                    stringArrayInput.add(featureSelectBox2.getText().toString());
                if (featureSelectBox3.isChecked())
                    stringArrayInput.add(featureSelectBox3.getText().toString());
                if (featureSelectBox4.isChecked())
                    stringArrayInput.add(featureSelectBox4.getText().toString());

                appThreadArgs.putStringArrayList("FeaturesList", stringArrayInput);
                appThreadArgs.putString("ContextGroup","activity");

                appThreadMsg.obj = appThreadArgs;
                appServiceHandler.sendMessage(appThreadMsg);
                break;
            case R.id.btnCloseApp:
                Process.killProcess(Process.myPid());
                break;

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

    private void remoteActivityCall(List<String> featuresToUse, String contextGroup) {
        try {
            targetContextService.gatherTrainingData(featuresToUse, contextGroup, "trainingDataFile.txt");
            Toast.makeText(getApplicationContext(),
                    "Launch",
                    Toast.LENGTH_LONG).show();
        }
        catch ( RemoteException ex ) {
            Toast.makeText(getApplicationContext(),
                    ex.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
