package com.example.ContextRequesterApp;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContextRequestAct extends Activity implements View.OnClickListener {
    // declare variables
    private Button bBindServiceBtn;
    private Button bUnbindServiceBtn;
    private Button bLaunchContextActBtn;
    private Button bLaunchTrainingActBtn;
    private Button bCloseAppBtn;

    private Spinner bClassifierSelection;
    private Spinner bContextSelection;

    // initialize variables needed for training data
    private ArrayList featuresToUse=null;
    private ArrayList contextLabels=null;
    private String TrainingFileName=null;
    private String TrainContextGroup=null;
    private Bundle bundleServiceInput = new Bundle();
    private Intent InputIntent;
    private JSONObject jsonInput;
    private Messenger ReturnMessenger;
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
//        bLaunchContextActBtn.setEnabled(false);

        bLaunchTrainingActBtn = (Button)findViewById(R.id.btnTrainData);
        bLaunchTrainingActBtn.setOnClickListener(this);
//        bLaunchTrainingActBtn.setEnabled(false);

        bCloseAppBtn = (Button)findViewById(R.id.btnCloseApp);
        bCloseAppBtn.setOnClickListener(this);

        bClassifierSelection = (Spinner) findViewById(R.id.classifier_spinner);
        bContextSelection = (Spinner) findViewById(R.id.context_spinner);

        // Setup inputs and intents for calling service
        bundleServiceInput = new Bundle();
        InputIntent = new Intent("org.jingbling.ContextEngine.ContextService");
        // Create a new Messenger for the communication back
        ReturnMessenger = new Messenger(handler);
        InputIntent.putExtra("MESSENGER", ReturnMessenger);
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Bundle output = message.getData();
            if (output != null) {
                if (output.getInt("return") == 0) {
                    Toast.makeText(getApplicationContext(),
                            "Classified: " + output.getString("label"), Toast.LENGTH_LONG)
                            .show();
                } else {
                    // classifier was not found, return message and get bundled data
                    featuresToUse=output.getStringArrayList("features");
                    contextLabels=output.getStringArrayList("contextLabels");
                    TrainContextGroup=output.getString("contextGroup");
                    Toast.makeText(getApplicationContext(),
                            "Error getting classifier, please train data", Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Classification failed",
                        Toast.LENGTH_LONG).show();
            }

        };
    };

    @Override
    public void onClick(View v) {
        switch ( v.getId() ) {
            case R.id.btnBindService:
//                bindService(new Intent(IContextService.class.getName()),
//                        appServiceConn, Context.BIND_AUTO_CREATE);
//                bBindServiceBtn.setEnabled(false);
//                bLaunchContextActBtn.setEnabled(true);
//                bLaunchTrainingActBtn.setEnabled(true);
//                bUnbindServiceBtn.setEnabled(true);
                break;
            case R.id.btnUnbindService:
//                unbindService(appServiceConn);
//                bBindServiceBtn.setEnabled(true);
//                bLaunchContextActBtn.setEnabled(false);
//                bLaunchTrainingActBtn.setEnabled(false);
//                bUnbindServiceBtn.setEnabled(false);
                break;
            case R.id.btnGetContext:
                // Pass intent to service

                // Create JSON format as input
                jsonInput = new JSONObject();
                try {
                    jsonInput.put("classifier", bClassifierSelection.getSelectedItem().toString());
                    jsonInput.put("contextGroup", bContextSelection.getSelectedItem().toString());
                    JSONArray featuresArray = new JSONArray();
                    // ??? TBR - for now hardcode features used, to be replaced with selectable list
                    featuresArray.put("accel.FFT");
//                    featuresArray.put("AvgFFTy");
//                    featuresArray.put("AvgFFTz");
                    jsonInput.put("features", featuresArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v("GETContextButton", jsonInput.toString());
                bundleServiceInput.putString("JSONInput",jsonInput.toString());
                bundleServiceInput.putString("action","classify");
                bundleServiceInput.putLong("period", (long)15000);// classify every 15 seconds
                bundleServiceInput.putLong("duration", (long)60000); //total duration of 1 minute

                InputIntent.putExtras(bundleServiceInput);
                startService(InputIntent);

                break;
            case R.id.btnTrainData:

                // Create JSON format as input
                jsonInput = new JSONObject();
                try {
                    jsonInput.put("classifier", bClassifierSelection.getSelectedItem().toString());
                    jsonInput.put("contextGroup", bContextSelection.getSelectedItem().toString());
                    JSONArray featuresArray = new JSONArray();
                    // ??? TBR - for now hardcode features used, to be replaced with selectable list
                    featuresArray.put("accel.FFT");
//                    featuresArray.put("AvgFFTy");
//                    featuresArray.put("AvgFFTz");
                    jsonInput.put("features", featuresArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v("GETContextButton", jsonInput.toString());
                bundleServiceInput.putString("JSONInput",jsonInput.toString());
                bundleServiceInput.putString("action","train");

                InputIntent.putExtras(bundleServiceInput);
                startService(InputIntent);

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

}
