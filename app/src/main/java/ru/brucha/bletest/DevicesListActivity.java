package ru.brucha.bletest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Prog on 27.05.2015.
 */
public class DevicesListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView devicesListView;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private final int REQUEST_ENABLE_BT = 1;
    private Handler handler;
    private AdapterDev adapterDev;
    private Set<BluetoothDevice> deviceSet;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list_layout);
        handler = new Handler();
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresher);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deviceSet.clear();
                adapterDev.notifyDataSetChanged();
                checkBleAdapter();
            }
        });
        devicesListView = (ListView) findViewById(R.id.device_list);
        devicesListView.setOnItemClickListener(this);
        deviceSet = new HashSet<>();
        adapterDev = new AdapterDev();
        devicesListView.setAdapter(adapterDev);
        if(Helper.checkBLE(this)) {
            BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            checkBleAdapter();
        }
    }

    private void checkBleAdapter() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                checkBleAdapter();
                break;
        }
    }

    private void scanLeDevice(final boolean enable) {
        ScanFilter.Builder filter = new ScanFilter.Builder();
        filter.setServiceUuid(GattService.UUID);
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter.build());
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanner.stopScan(scanCallback);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                        if(dialog.isShowing()){
                            dialog.hide();
                        }
                    }
                }
            }, 10000);
            //scanner.startScan(scanCallback);
            scanner.startScan(filters, new ScanSettings.Builder().build(), scanCallback);
        } else {
            scanner.stopScan(scanCallback);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(dialog.isShowing()){
                        dialog.hide();
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = deviceSet.toArray(new BluetoothDevice[0])[position];
        Intent intent = new Intent(this, ServicesList.class);
        intent.putExtra("device", device);
        startActivity(intent);

    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();
                    deviceSet.add(device);
                    adapterDev.notifyDataSetChanged();
                    if(dialog.isShowing()){
                        dialog.hide();
                    }
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private class AdapterDev extends ArrayAdapter<BluetoothDevice>{

        public AdapterDev() {
            super(DevicesListActivity.this, android.R.layout.simple_list_item_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            BluetoothDevice device = deviceSet.toArray(new BluetoothDevice[0])[position];
            String name = device.getName();
            if (name == null || name.isEmpty()){
                name = device.getAddress();
            }
            v.setText(name);
            return v;
        }

        @Override
        public int getCount() {
            return deviceSet.size();
        }
    }
}
