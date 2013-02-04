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

public class ContextRequestAct extends Activity implements View.OnClickListener {
    // declare variables
    private Button bBindServiceBtn;
    private Button bUnbindServiceBtn;
    private Button bLaunchContextActBtn;
    private Button bLaunchTrainingActBtn;
    private Button bCloseAppBtn;

    private Spinner bClassifierSelection;
    private Spinner bContextSelection;

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

    }


    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Bundle output = message.getData();
            if (output != null) {
                Toast.makeText(getApplicationContext(),
                        "Classified: " + output.getString("label"), Toast.LENGTH_LONG)
                        .show();
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
                Bundle bundleInput = new Bundle();
                Intent InputIntent = new Intent("org.jingbling.ContextEngine.ContextService");

                // Create JSON format as input
                JSONObject jsonInput = new JSONObject();
                try {
                    jsonInput.put("classifier", bClassifierSelection.getSelectedItem().toString());
                    jsonInput.put("contextGroup", bContextSelection.getSelectedItem().toString());
                    JSONArray featuresArray = new JSONArray();
                    // ??? TBR - for now hardcode features used, to be replaced with selectable list
                    featuresArray.put("AvgFFTx");
                    featuresArray.put("AvgFFTy");
                    featuresArray.put("AvgFFTz");
                    jsonInput.put("features", featuresArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v("GETContextButton", jsonInput.toString());
                bundleInput.putString("JSONInput",jsonInput.toString());

                // Create a new Messenger for the communication back
                Messenger messenger = new Messenger(handler);
                InputIntent.putExtra("MESSENGER", messenger);

                InputIntent.putExtras(bundleInput);
                startService(InputIntent);

                break;
            case R.id.btnTrainData:

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
