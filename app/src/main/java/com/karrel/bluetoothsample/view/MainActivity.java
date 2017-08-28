package com.karrel.bluetoothsample.view;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.karrel.bluetoothsample.R;
import com.karrel.bluetoothsample.databinding.ActivityMainBinding;
import com.karrel.bluetoothsample.etc.RxBluetoothConnectEvent;
import com.karrel.bluetoothsample.presenter.MainPresenter;
import com.karrel.bluetoothsample.presenter.MainPresenterImpl;
import com.karrel.mylibrary.RLog;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    private ActivityMainBinding binding;
    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // create presenter;
        presenter = new MainPresenterImpl(this);

        setupRxEvent();

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.startBt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stopBt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_device:
                startActivity(new Intent(MainActivity.this, DeviceListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRxEvent() {
        RxBluetoothConnectEvent.getInstance().getObservable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if (o instanceof BluetoothDevice) {
                    BluetoothDevice device = (BluetoothDevice) o;
                    RLog.e(String.format("name : %s, address : %s", device.getName(), device.getAddress()));
                    addLog(String.format("name : %s, address : %s", device.getName(), device.getAddress()));
                    presenter.connectBt(device);
                }
            }
        });
    }

    @Override
    public void connectedDevice(String mConnectedDeviceName) {
        RLog.e("mConnectedDeviceName : " + mConnectedDeviceName);
        addLog("mConnectedDeviceName : " + mConnectedDeviceName);
    }

    @Override
    public void setSatus(String s) {
        RLog.e("setSatus : " + s);
        addLog("setSatus : " + s);
    }

    @Override
    public void readMessage(String readMessage) {
        RLog.e("readMessage : " + readMessage);
        addLog("readMessage : " + readMessage);
    }

    private void addLog(String message) {
        String text = binding.log.getText().toString();
        binding.log.setText(message + "\n" + text);
    }
}
