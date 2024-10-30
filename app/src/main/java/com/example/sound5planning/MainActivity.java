
package com.example.sound5planning;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.sound5planning.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    
    
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    private final String espAdd = "41:42:69:31:E4:7D";
    private static final int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        
        
        
        checkPermission();
        
        
        binding.btnTurnOn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkBluetoothSupport()){
                    seText("bluetooth is supported");
                        if(checkBluetoothEnable()){
                            seText("bluetooth is enable");
                        } else {
                            seText("Enabling Bluetooth");
                            enableBluetooth();
                            
                        }
                        
                }else {
                    seText("Bluetooth not supported");
                }
            }
            
        });
        
        binding.btnConnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkBluetoothEnable()) {
                	BluetoothDevice device = getEspDevice();
                    if(checkEspDevice(device)){
                    	seText("Device found");
                        if(checkEspBonded()) {
                        	seText("ESP is bonded");
                                
                        } else {
                            seText("Bonding Esp...");
                            bondEsp(device);
                        }
                    } else {
                        seText("Device Not Found");
                    }
                } else {
                    seText("Bluetooth is not enable");
                }
            }
            
        });
        
        
    }
    
    
private void checkPermission(){
    String[] permissions = {
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT
    };
        
    List<String> permissionsToRequest = new ArrayList<>(); // Create a list to hold permissions that need to be requested

    for(String permission : permissions){
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(permission); // Add to the list if permission is not granted
        }
    }

    // If there are permissions to request, do so
    if (!permissionsToRequest.isEmpty()) {
        ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }
}
    
    
    private boolean checkBluetoothSupport(){
        if (bluetoothAdapter != null){
            return true;
        } 
        return false;
    }
    
    private boolean checkBluetoothEnable(){
        return bluetoothAdapter.isEnabled();
    }
    
    private void enableBluetooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBtIntent);
    }
    private BluetoothDevice getEspDevice(){
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(espAdd);
        return device;
    }
    private boolean checkEspDevice(BluetoothDevice device){
        if(device != null){
            return true;
        } else {
            return false;
        }
        
    }
    
    private boolean checkEspBonded(){
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        
        for(BluetoothDevice device : pairedDevice){
            if(device.getAddress().equals(espAdd)) {
            	return true;
            } 
        }
        return false;
    }
    
    private void bondEsp(BluetoothDevice device){
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondReciver,filter);
        device.createBond();
        seText("Bonding...");
        
    }
    
    private void seText(String s){
        binding.textView.setText(s);
    }
    
    private ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            seText("Bluetooth is enabled");
        } else {
            seText("Error in enabling Bluetooth");
        }
    }
);
    private final BroadcastReceiver bondReciver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.ERROR);
                switch(state){
                    case BluetoothDevice.BOND_BONDED:
                        seText("Device Bonded");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        seText("Error in Bonding");
                        break;
                }
            } {
            	
            }
        }
        
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
        unregisterReceiver(bondReciver);
    }
}
