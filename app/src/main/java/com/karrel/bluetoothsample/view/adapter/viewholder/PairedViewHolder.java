package com.karrel.bluetoothsample.view.adapter.viewholder;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.karrel.bluetoothsample.databinding.ItemPairedHolderBinding;
import com.karrel.bluetoothsample.etc.RxConnectEvent;

/**
 * Created by Rell on 2017. 8. 24..
 */

public class PairedViewHolder extends RecyclerView.ViewHolder {
    protected final ItemPairedHolderBinding binding;
    protected BluetoothDevice data;

    public PairedViewHolder(ItemPairedHolderBinding binding) {
        super(binding.getRoot());

        this.binding = binding;
        clickRoot();
    }

    public void setData(BluetoothDevice data) {
        this.data = data;

        // name
        binding.deviceName.setText(data.getName());
        // address
        binding.address.setText(data.getAddress());
    }

    protected void clickRoot() {
        this.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxConnectEvent.getInstance().sendEvent(data);
            }
        });
    }
}