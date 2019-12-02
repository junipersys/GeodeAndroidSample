package com.example.bluetoothtest2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private InputStream InputStream;
    private OutputStream OutputStream;
    ArrayList<BluetoothDevice> DevicesArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        List<String> deviceList;


        ArrayList<String> bluetoothEntrieNames = new ArrayList<>();



        // Get a reference to the bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || bluetoothAdapter.getAddress().equals(null))
            return; // device doesn't support bluetooth

        // Get a list of the paired devices, add to a list for display/later use
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : devices) {
            DevicesArray.add(device);
            bluetoothEntrieNames.add(device.getName());
        }

        // set up the RecyclerView for displaying list
        RecyclerView recyclerView = findViewById(R.id.rvAnimals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(MainActivity.this, bluetoothEntrieNames);
        // clicking an item in the list will trigger the onItemClick below
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        AttemptConnect(position);
    }

    public void AttemptConnect(int position) {
        // stopping discovery to make connection faster
        bluetoothAdapter.cancelDiscovery();

        // get the remote device selected in UI
        BluetoothDevice device = DevicesArray.get(position);
        device = bluetoothAdapter.getRemoteDevice(device.getAddress());

        // this is the commonly known UUID for serial port profile (SPP)
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        BluetoothSocket tmp = null;
        try {
            // Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection
            // to this remote device using SDP lookup of uuid.
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException ex) {
            tmp = null;
            Toast.makeText(this, "connection attempt to " + device.getName() + " failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        bluetoothSocket = tmp;

        try {
            // This method will block until a connection is made or the connection fails.
            // If this method returns without an exception then this socket is now connected
            bluetoothSocket.connect();
        } catch (IOException ex) {
            try {
                // cleanup after failed attempt
                bluetoothSocket.close();
            } catch (IOException e2) { }
            // notify user of failure
            Toast.makeText(this, "connection attempt to " + device.getName() + " failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            // Get the input and output streams associated with this socket
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) { }

        InputStream = tmpIn;
        OutputStream = tmpOut;

        // At this point we should be connected to the geode.
        // We would still need to open threads to read and write
        // to the input and output streams, and retry connection if it drops.
        // See following example:

        // ConnectedThread connectedThread = new ConnectedThread(socket);
        // connectedThread.start();


        // This could be how ConnectedThread is defined:

        /*private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the BluetoothSocket input and output streams
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) { }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                byte[] buffer;
                ArrayList<Integer> arr_byte = new ArrayList<Integer>();

                // Keep listening to the InputStream while connected
                while (true) {
                    try {
                        int data = mmInStream.read();
                        // TODO: do something with data
                    } catch (IOException e) {
                        // TODO: check for connection lost

                        // TODO: Start the service over to restart listening mode

                        break;
                    }
                }
            }

            // Write to the connected OutStream.
            // @param buffer  The bytes to write
            public void write(byte[] buffer) {
                try {
                    // send data out
                    mmOutStream.write(buffer);
                    // TODO: do something to notify UI of data sent
                } catch (IOException e) { }
            }

            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) { }
            }
        }*/

        // notify user of success
        Toast.makeText(this, "You are connected to " + device.getName(), Toast.LENGTH_SHORT).show();
    }
}
