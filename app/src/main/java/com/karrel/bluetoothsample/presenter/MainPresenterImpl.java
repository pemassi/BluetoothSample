package com.karrel.bluetoothsample.presenter;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.karrel.bluetoothsample.bluetooth.BluetoothChatService;
import com.karrel.bluetoothsample.etc.Constants;
import com.karrel.bluetoothsample.event.RxProtocolEvent;
import com.karrel.bluetoothsample.event.RxWriteByteEvent;
import com.karrel.bluetoothsample.model.Protocol;
import com.karrel.bluetoothsample.model.ReadDataItem;
import com.karrel.bluetoothsample.model.WriteDataItem;
import com.karrel.bluetoothsample.util.ByteConverter;
import com.karrel.mylibrary.RLog;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.functions.Action1;

/**
 * Created by Rell on 2017. 8. 24..
 */

public class MainPresenterImpl implements MainPresenter {

    private MainPresenter.View view;

    private BluetoothChatService mChatService = null;

    private String mConnectedDeviceName = null;

    private boolean fixedToggle = true;

    private Realm realm = Realm.getDefaultInstance();

    private boolean isShowProtocolLayout = false;

    public MainPresenterImpl(MainPresenter.View view) {
        this.view = view;
        setupModifyProtocolEvent();
        setupRealmEvent();
        setupWriteEvent();
        view.setSatus("not connected");
    }

    /**
     * 프로토콜 송신을 위한 이벤트 버스를 선언한다.
     */
    private void setupWriteEvent() {
        RxWriteByteEvent.getInstance().getObservable()
                .subscribe(new Action1<Protocol>() {
                    @Override
                    public void call(Protocol protocol) {
                        sendMessage(protocol.hex, protocol.isChecksum);
                    }
                });
    }

    /**
     * Rx event bus 를 통해서 버튼추가 이벤트를 전달 받는다.
     */
    private void setupModifyProtocolEvent() {
        RxProtocolEvent.getInstance().getObservable()
                .subscribe(new Action1<Protocol>() {
                    @Override
                    public void call(Protocol protocol) {
                        // 전달된 객체가 없으면 아이템 추가로 간주하는데 이게 맞는건지는 모르겟군;
                        if (protocol == null) {
                            view.startCreateProtocolActivity();
                        } else {
                            RLog.d(String.format("setting protocol : " + protocol));
                            view.startCreateProtocolActivity(protocol);
                        }
                    }
                });
    }

    @Override
    public void connectBt(BluetoothDevice device) {
        // create chatService
        if (mChatService == null) {
            createBtService();
        }

        connectDevice(device, true);
    }

    @Override
    public void sendMessage(String hex, boolean isChecksum) {
        // Check that we're actually connected before trying anything
        if (mChatService == null) {
            view.showMessage("not connected device");
            return;
        }
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            view.showMessage("not connected device");
            return;
        }

        // Check that there's actually something to send
        if (hex.length() > 0) {
            RLog.e("hex : " + hex);
            // 뒤에 0은 모두 지운다.
            hex = hex.replaceAll("0*$", "");
            if (hex.length() == 0) return;
            if (hex.length() % 2 == 1) {
                hex += "0";
            }
            if (isChecksum) {
                String checksum = ByteConverter.checkSum(hex);
                RLog.e("checksum : " + checksum);
                hex += checksum;
            }
            RLog.e("hex : " + hex);

            byte[] bytes = ByteConverter.hexToByteArray(hex);
            mChatService.write(bytes);
            RLog.e("write " + hex);
        }
    }

    private void createBtService() {
        mChatService = new BluetoothChatService(mHandler);
    }

    private void connectDevice(BluetoothDevice device, boolean secure) {
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void startBt() {
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public void stopBt() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void clearData() {
        view.clearData();
    }

    @Override
    public void clickSendButton() {
        isShowProtocolLayout = !isShowProtocolLayout;

        if (isShowProtocolLayout) {
            view.showProtocolLayout();
        } else {
            view.hideProtocolLayout();
        }
    }

    @Override
    public void loadProtol() {
        loadProtocolItem(realm);
    }

    @Override
    public void onCheckedChangeToggle() {
        fixedToggle = !fixedToggle;
        view.setFixedToggleMenu(fixedToggle);
    }

    /**
     * The Handler that gets information byte_box from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            RLog.d("연결됨");
                            view.connectedDevice(mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            RLog.d("connecting...");
                            view.setSatus("connecting...");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            RLog.d("연결되지 않음");
                            view.setSatus("not connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    RLog.d("writeMessage : " + writeMessage);

                    WriteDataItem writeDataItem = new WriteDataItem(createByteList(writeBuf));
                    view.writeMessage(writeDataItem);
                    view.scrollToTop();

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    ReadDataItem readDataItem = new ReadDataItem(createByteList(readBuf));
                    view.readMessage(readDataItem);
                    if (fixedToggle) view.scrollToTop();
//                    RLog.d("readMessage : " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    RLog.d(String.format("%s에 연결됨", mConnectedDeviceName));
                    break;
                case Constants.MESSAGE_TOAST:
                    RLog.d(msg.getData().getString(Constants.TOAST));
                    break;
            }
        }
    };

    private List<String> createByteList(byte[] readBuf) {
        // construct a string from the valid bytes in the buffer
        String readMessage = ByteConverter.byteArrayToHexString(readBuf);
        // 뒤에 0은 모두 지운다.
        readMessage = readMessage.replaceAll("0*$", "");

        byte[] bytes = ByteConverter.hexToByteArray(readMessage);

        List<String> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(ByteConverter.byteToHex(b));
        }

        return list;
    }

    private void setupRealmEvent() {
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                loadProtocolItem(realm);
            }
        });
    }

    private void loadProtocolItem(Realm realm) {
        RealmResults<Protocol> protocols = realm.where(Protocol.class).findAll();
        List<Protocol> list = new ArrayList<>();
        for (int i = 0; i < protocols.size(); i++) {
            list.add(protocols.get(i));
        }
        view.setProtocolData(list);
    }
}
