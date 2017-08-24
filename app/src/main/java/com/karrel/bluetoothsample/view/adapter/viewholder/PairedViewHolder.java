package com.karrel.bluetoothsample.view.adapter.viewholder;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;

import com.karrel.bluetoothsample.databinding.ItemPairedHolderBinding;

/**
 * Created by Rell on 2017. 8. 24..
 */

public class PairedViewHolder extends RecyclerView.ViewHolder {
    private final ItemPairedHolderBinding binding;
    private BluetoothDevice data;

    public PairedViewHolder(ItemPairedHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void setData(BluetoothDevice data) {
        this.data = data;

        // name
        binding.deviceName.setText(data.getName());
        // address
        binding.address.setText(data.getAddress());
    }
}
