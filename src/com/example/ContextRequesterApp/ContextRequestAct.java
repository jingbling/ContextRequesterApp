package com.example.ContextRequesterApp;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContextRequestAct extends Activity implements View.OnClickListener {
    // declare variables
    private Button bInitServiceBtn;
    private Button bUnbindServiceBtn;
    private Button bLaunchContextActBtn;
    private Button bLaunchTrainingActBtn;
    private Button bCloseAppBtn;

    private ListView bFeatureSelection;
    private Spinner bClassifierSelection;
    private ListView bLabelsSelection;

    // variables for selecting inputs, these need to be initialized by querying service
    private String[] allowedFeatures;
    private String[] allowedLabels;
    private String[] allowedAlgorithms;

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

        bInitServiceBtn = (Button)findViewById(R.id.btnInitService);
        bInitServiceBtn.setOnClickListener(this);
//
//        bUnbindServiceBtn = (Button)findViewById(R.id.btnUnbindService);
//        bUnbindServiceBtn.setOnClickListener(this);

        bLaunchContextActBtn = (Button)findViewById(R.id.btnGetContext);
        bLaunchContextActBtn.setOnClickListener(this);
        bLaunchContextActBtn.setEnabled(false);

        bLaunchTrainingActBtn = (Button)findViewById(R.id.btnTrainData);
        bLaunchTrainingActBtn.setOnClickListener(this);
        bLaunchTrainingActBtn.setEnabled(false);

        bCloseAppBtn = (Button)findViewById(R.id.btnCloseApp);
        bCloseAppBtn.setOnClickListener(this);

        bClassifierSelection = (Spinner) findViewById(R.id.classifier_spinner);

        bLabelsSelection = (ListView) findViewById(R.id.labels_list);
        bLabelsSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        bFeatureSelection = (ListView) findViewById(R.id.features_list);
        bFeatureSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
                } else if (output.getInt("return") == 2) {
                    // if return value is 2, this is an initialization bundle
                    allowedFeatures =output.getStringArray("allowedFeatures");
                    allowedLabels=output.getStringArray("allowedLabels");
                    allowedAlgorithms=output.getStringArray("allowedAlgorithms");
                    String returnedMessage = output.getString("message");

                    // set lists with return values
                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_spinner_dropdown_item, allowedAlgorithms);
                    bClassifierSelection.setAdapter(spinnerArrayAdapter);

                    ArrayAdapter featuresListAdapter = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_list_item_checked, allowedFeatures);
                    bFeatureSelection.setAdapter(featuresListAdapter);
                    ArrayAdapter labelsListAdapter = new ArrayAdapter(getApplicationContext(),
                            android.R.layout.simple_list_item_checked, allowedLabels);
                    bLabelsSelection.setAdapter(labelsListAdapter);

                    //after initializing input values, allow context and training mode to be selected
                    bLaunchContextActBtn.setEnabled(true);
                    bLaunchTrainingActBtn.setEnabled(true);

                    Toast.makeText(getApplicationContext(),
                            returnedMessage, Toast.LENGTH_LONG)
                            .show();
                } else {
                    // classifier was not found or other error, print return message
                    String returnedMessage = output.getString("message");
                    Toast.makeText(getApplicationContext(),
                            returnedMessage, Toast.LENGTH_LONG)
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
            case R.id.btnInitService:
                // initialize values for requesting service classification
                bundleServiceInput.putString("action","init");
                InputIntent.putExtras(bundleServiceInput);
                startService(InputIntent);

                break;
//            case R.id.btnBindService:
////                bindService(new Intent(IContextService.class.getName()),
////                        appServiceConn, Context.BIND_AUTO_CREATE);
////                bInitServiceBtn.setEnabled(false);
////                bLaunchContextActBtn.setEnabled(true);
////                bLaunchTrainingActBtn.setEnabled(true);
////                bUnbindServiceBtn.setEnabled(true);
//                break;
//            case R.id.btnUnbindService:
////                unbindService(appServiceConn);
////                bInitServiceBtn.setEnabled(true);
////                bLaunchContextActBtn.setEnabled(false);
////                bLaunchTrainingActBtn.setEnabled(false);
////                bUnbindServiceBtn.setEnabled(false);
//                break;
            case R.id.btnGetContext:
                // Pass intent to service
                // first save default set of inputs
                bundleServiceInput=bundleInputs();
                //then add ones specific to this button
                bundleServiceInput.putString("action","classify");
                bundleServiceInput.putLong("period", (long)5000);// classify every 5 seconds
                bundleServiceInput.putLong("duration", (long)60000); //total duration of 1 minute

                InputIntent.putExtras(bundleServiceInput);
                startService(InputIntent);

                break;
            case R.id.btnTrainData:

                // first save default set of inputs
                bundleServiceInput=bundleInputs();
                //then add ones specific to this button
//                Log.d("GETContextButton", jsonInput.toString());
                bundleServiceInput.putString("action","train");

                InputIntent.putExtras(bundleServiceInput);
                startService(InputIntent);

                break;
            case R.id.btnCloseApp:
                Process.killProcess(Process.myPid());
                break;

        }
    }

    private Bundle bundleInputs() {
        // this subroutine bundles the required inputs for calling the classifier and returns it
        Bundle bundleToSend = new Bundle();
        bundleToSend.putString("algorithm",bClassifierSelection.getSelectedItem().toString());
        // save features checked
        ArrayList<String> featureArrayList = new ArrayList<String>();
        SparseBooleanArray checked = new SparseBooleanArray();
        checked.clear();
        checked = bFeatureSelection.getCheckedItemPositions();
        int count = 0;
        for (int i = 0; i < checked.size(); i++)
        {
            //added if statement to check for true. The SparseBooleanArray
            //seems to maintain the keys for the checked items, but it sets
            //the value to false. Adding a boolean check returns the correct result.
            if(checked.valueAt(i) == true) {
                featureArrayList.add(count, allowedFeatures[checked.keyAt(i)]);
                count++;
            }
        }

        bundleToSend.putStringArrayList("features", featureArrayList);

        // repeat for labels
        ArrayList<String> labelsArrayList = new ArrayList<String>();
        checked.clear();
        checked = bLabelsSelection.getCheckedItemPositions();
        count = 0;
        for (int i = 0; i < checked.size(); i++)
        {
            //added if statement to check for true. The SparseBooleanArray
            //seems to maintain the keys for the checked items, but it sets
            //the value to false. Adding a boolean check returns the correct result.
            if(checked.valueAt(i) == true) {
                labelsArrayList.add(count, allowedLabels[checked.keyAt(i)]);
                count++;
            }
        }

        bundleToSend.putStringArrayList("contextLabels", labelsArrayList);

        return bundleToSend;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
