package com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Helpers.NoteMessage;
import com.hopeland.pda.example.SAED.Helpers.system.HardWar;
import com.hopeland.pda.example.SAED.ViewModels.All;
import com.hopeland.pda.example.SAED.ViewModels.MyViewModel;
import com.hopeland.pda.example.SAED.ViewModels.Report;
import com.hopeland.pda.example.uhf.ClientActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;


@SuppressLint("NonConstantResourceId")
public class InventoryFragment extends Fragment implements View.OnClickListener,
        ClientActivity.OnReadTag {

    //region GLOBAL VAR

    //region Views
    Spinner warehousesSpinner;
    Spinner inventorySpinner;

    TextView expected;
    TextView scanned;
    TextView notFound;
    TextView undefined;

    Button read;
    Button stop;
    Button report;
    ProgressBar progressBar;

    //endregion

    //region spinners list
    ArrayList<String> listStringWarehouses = new ArrayList<>();
    ArrayList<String> listStringInventory = new ArrayList<>();
    ArrayAdapter<String> adapterWarehouses;
    ArrayAdapter<String> adapterInventory;

    //endregion

    //region lists models
    ArrayList<All> list = new ArrayList<>();
    ArrayList<All.Location> listLocations;
    ArrayList<All.Epc> listEpc;

    ArrayList<String> listStringEpc = new ArrayList<>();
    ArrayList<String> listStringEpc1 = new ArrayList<>();
    ArrayList<String> scannedList = new ArrayList<>();
    ArrayList<String> undefinedList = new ArrayList<>();

    //endregion

    ClientActivity myActivity;
    MyViewModel myViewModel;
    View view;

    //endregion

    private static final String TAG = "InventoryFragment";

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (ClientActivity) requireActivity();
        myViewModel = myActivity.myViewModel;
        view = inflater.inflate(R.layout.fragment_inventory1, container, false);
        initViews();

        listeners();

        getAll();

        return view;
    }

    private void initViews() {
        warehousesSpinner = view.findViewById(R.id.warehouses_spinner);
        inventorySpinner = view.findViewById(R.id.inventory_spinner);
        expected = view.findViewById(R.id.expected);
        scanned = view.findViewById(R.id.scaned);
        notFound = view.findViewById(R.id.not_found);
        undefined = view.findViewById(R.id.undefined);
        read = view.findViewById(R.id.read);
        stop = view.findViewById(R.id.stop);
        report = view.findViewById(R.id.report);
        progressBar = view.findViewById(R.id.progressIndicator);

    }

    void listeners() {

        warehousesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initInventorySpinners(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        inventorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initListEpc(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        read.setOnClickListener(this);
        stop.setOnClickListener(this);
        report.setOnClickListener(this);
    }

    //region data socket

    void getAll() {
        startLoading();
        myViewModel.getAll(myActivity.socket);
        myViewModel.allLiveData.observe(myActivity, observer);
    }

    final Observer<ArrayList<All>> observer = list -> {
        if (!isAdded())
            return;
        endLoading();

        if (list == null || list.isEmpty())
            return;

        this.list = list;

        initWarehousesSpinners();
    };

    //endregion

    //region adapters spinner and init list

    // 1
    void initWarehousesSpinners() {

        listStringWarehouses.clear();
        for (All all : list)
            listStringWarehouses.add(all.name);

        adapterWarehouses = new ArrayAdapter<>(myActivity,
                R.layout.item_spinner, R.id.textView, listStringWarehouses);

        adapterWarehouses.setDropDownViewResource(R.layout.item_spinner_drop);
        warehousesSpinner.setAdapter(adapterWarehouses);
    }

    //2
    void initInventorySpinners(int id) {

        if (list == null || list.isEmpty())
            return;

        listLocations = list.get(id).locations;

        if (listLocations == null)
            return;

        listStringInventory.clear();
        for (All.Location location : listLocations)
            listStringInventory.add(location.name);

        adapterInventory = new ArrayAdapter<>(myActivity,
                R.layout.item_spinner, R.id.textView, listStringInventory);
        adapterInventory.setDropDownViewResource(R.layout.item_spinner_drop);

        inventorySpinner.setAdapter(adapterInventory);
    }

    //3
    void initListEpc(int id) {
        if (listLocations == null)
            return;

        listEpc = listLocations.get(id).epcs;

        listStringEpc.clear();
        listStringEpc1.clear();
        scannedList.clear();
        undefinedList.clear();

        if (listEpc == null)
            return;

        for (All.Epc epc : listEpc) {
            listStringEpc.add(epc.epc);
            listStringEpc1.add(epc.epc);
        }

        Log.d(TAG, "initListEpc: " + listEpc);
        expected.setText(String.valueOf(listEpc.size()));
    }

    //endregion

    //region Handlers

    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    Log.d(TAG, "handleMessage: message 0");
                    scanned.setText(String.valueOf(scannedList.size()));
                    notFound.setText(String.valueOf(listStringEpc.size()));
                    break;
                }
                case 1: {
                    Log.d(TAG, "handleMessage: message 1");
                    undefined.setText(String.valueOf(undefinedList.size()));
                    notFound.setText(String.valueOf(listStringEpc.size()));
                    break;
                }
            }
        }

    };

    //endregion

    //region show hied
    void startLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    void endLoading() {
        progressBar.setVisibility(View.GONE);
    }

    //endregion

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read) {
            myActivity.read();
        } else if (v.getId() == R.id.stop) {
            myActivity.stop();
        } else if (v.getId() == R.id.report) {
            sendReport();
        }

    }

    private void sendReport() {

        if (list == null) {
            NoteMessage.showSnackBar(myActivity, getString(R.string.no_data_error));
            return;
        }

        startLoading();
        Report report = createReport();

        myViewModel.sendReport(myActivity.socket, report);
        myViewModel.sendReportLiveData.observe(myActivity, doneSend -> {

            if (doneSend == null || !isAdded())
                return;

            endLoading();

            if (doneSend)
                NoteMessage.showSnackBar(myActivity, getString(R.string.done_send_report));
            else
                NoteMessage.showSnackBar(myActivity, getString(R.string.format_error));

        });
    }

    private Report createReport() {

        Report report = new Report();
        report.setWid(list.get(warehousesSpinner.getSelectedItemPosition()).id);
        report.setDt(new Date());
        report.setLid(listLocations.get(inventorySpinner.getSelectedItemPosition()).id);
        report.setUsr(HardWar.GetIMEI(myActivity));
//        report.setUsr(HardWar.getIMEINumber(myActivity));

        report.setEpcs1(listStringEpc1);
        report.setEpcs2(scannedList);
        report.setEpcs3(undefinedList);
        report.setEpcs4(listStringEpc);


        return report;
    }

    void removeObserver() {
        if (myViewModel.allLiveData != null)
            myViewModel.allLiveData.removeObserver(observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeObserver();
    }

    @Override
    public void onStop() {
        super.onStop();
        myActivity.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        myActivity.onReadTag = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        myActivity.onReadTag = null;
    }

    @Override
    public void onRead(@NotNull String epc, byte rssi) {

        if (listStringEpc.contains(epc)) {
            scannedList.add(epc);
            listStringEpc.remove(epc);
            handler.sendEmptyMessage(0);

        } else if (!undefinedList.contains(epc) && !scannedList.contains(epc)) {
            undefinedList.add(epc);
            handler.sendEmptyMessage(1);
        }
    }


//    public Pair<File, String> createImageFile(Context context, byte[] bytes) throws Exception {
//
//        String path;
//
////        // Create an image file name
////        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.ENGLISH).format(new Date());
////        String imageFileName = "JPEG_" + timeStamp + "_";
//
//        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//
//        File image = File.createTempFile(
//                String.valueOf(Calendar.getInstance().getTimeInMillis()),  /* prefix */
//                ".txt",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        path = image.getAbsolutePath();
//        FileUtils.writeByteArrayToFile(new File(path), bytes);
//        return new Pair<>(image, path);
//    }


}