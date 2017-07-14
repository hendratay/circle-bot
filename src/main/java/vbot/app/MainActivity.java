package vbot.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;;
import java.lang.Thread;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mBluetoothName;
    private ArrayList<Drawable> mBluetoothImage;
    private ArrayList<BluetoothDevice> mBluetoothDevice;;
    private Set<BluetoothDevice> pairedDevices;
    private ListView mListView;
    private ListView mUnpairedListView;
    private TextView connectedBluetooth;
    private Button scanButton;
    private ProgressBar scanProgress;
    private ProgressDialog connectDialog;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); 
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        Button upButton = (Button) findViewById(R.id.up);
        Button downButton = (Button) findViewById(R.id.down);
        Button leftButton = (Button) findViewById(R.id.left);
        Button rightButton = (Button) findViewById(R.id.right);
        connectedBluetooth = (TextView) findViewById(R.id.connectedbluetooth);
        Button bluetoothButton = (Button) findViewById(R.id.bluetooth);
        Switch vacuumSwitch = (Switch) findViewById(R.id.vacuum);
        Switch autoSwitch = (Switch) findViewById(R.id.auto);
        setSupportActionBar(myToolbar);
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mConnectedThread.write("u".toString().getBytes());
                    return true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mConnectedThread.write("s".toString().getBytes());
                }
                return false;
            }
        });
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mConnectedThread.write("d".toString().getBytes());
                    return true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mConnectedThread.write("s".toString().getBytes());
                }
                return false;
            }
        });
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mConnectedThread.write("l".toString().getBytes());
                    return true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mConnectedThread.write("s".toString().getBytes());
                }
                return false;
            }
        });
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mConnectedThread.write("r".toString().getBytes());
                    return true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mConnectedThread.write("s".toString().getBytes());
                }
                return false;
            }
        });
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBluetoothDialog();
            }
        });
        vacuumSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mConnectedThread.write("a".toString().getBytes());
                } else {
                    mConnectedThread.write("b".toString().getBytes());
                }
            }
        });
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                } else {
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart(){
        super.onStart();
        checkBluetooth();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
        unregisterReceiver(mReceiver);
    }

    public void checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    public void openBluetoothDialog() {
        DialogFragment newFragment = new bluetoothDialogFragment();
        newFragment.show(getFragmentManager(), "bluetooth");
    }

    private void pairedDevicesList() {
        mBluetoothDevice = new ArrayList<BluetoothDevice>();
        mBluetoothName = new ArrayList<String>();
        mBluetoothImage = new ArrayList<Drawable>();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mBluetoothDevice.add(device);
                mBluetoothName.add(device.getName());
                mBluetoothImage.add(getDrawableByMajorClass(device.getBluetoothClass().getMajorDeviceClass()));
            }
            CustomAdapter adapter = new CustomAdapter(this, mBluetoothDevice, mBluetoothName, mBluetoothImage);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView av, View v, int position, long id) {
                    mBluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = (BluetoothDevice) mListView.getItemAtPosition(position);
                    if (mConnectThread != null) {
                        mConnectThread.cancel();
                    }
                    mConnectThread = new ConnectThread(device);
                    mConnectThread.start();
                    connectDialog = new ProgressDialog(MainActivity.this);
                    connectDialog.setMessage("Start Connecting ...");
                    connectDialog.show();
                }
            });
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanProgress.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanProgress.setVisibility(View.GONE);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBluetoothDevice = new ArrayList<BluetoothDevice>();
                mBluetoothName = new ArrayList<String>();
                mBluetoothImage = new ArrayList<Drawable>();
                if (device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                    mBluetoothDevice.add(device);
                    mBluetoothName.add(device.getName());
                    mBluetoothImage.add(getDrawableByMajorClass(device.getBluetoothClass().getMajorDeviceClass()));
                    CustomAdapter adapter = new CustomAdapter(MainActivity.this, mBluetoothDevice, mBluetoothName, mBluetoothImage);
                    mUnpairedListView.setAdapter(adapter);
                    mUnpairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView av, View v, int position, long id) {
                            mBluetoothAdapter.cancelDiscovery();
                            BluetoothDevice device = (BluetoothDevice) mUnpairedListView.getItemAtPosition(position);
                            try {
                                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                                method.invoke(device, (Object[]) null);
                            } catch (Exception e) {
                                
                            }
                        }
                    });
                } 
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    scanProgress.setVisibility(View.VISIBLE);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    scanProgress.setVisibility(View.GONE);
                    Fragment frag = getFragmentManager().findFragmentByTag("bluetooth");
                    DialogFragment df = (DialogFragment) frag;
                    df.dismiss();
                    openBluetoothDialog();
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    scanProgress.setVisibility(View.GONE);
                }
            }
        }
    };

    private Drawable getDrawableByMajorClass(int major) {
        Drawable drawable = null;
            switch (major) {
                case BluetoothClass.Device.Major.COMPUTER:
                    drawable = getResources().getDrawable(R.drawable.computer_icon);
                    break;
                case BluetoothClass.Device.Major.PHONE:
                    drawable = getResources().getDrawable(R.drawable.phone_icon);
                    break;
                default:
                    drawable = getResources().getDrawable(R.drawable.device_icon);
                    break;
            }
        return drawable;
    }

    public class bluetoothDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstance) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.bluetooth_dialog, null);

            mListView = (ListView) view.findViewById(R.id.pairedlist);
            mUnpairedListView = (ListView) view.findViewById(R.id.unpairedlist);
            scanButton = (Button) view.findViewById(R.id.scan);
            scanProgress = (ProgressBar) view.findViewById(R.id.progressbar);
            scanButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mBluetoothAdapter.startDiscovery();
                }
            });

            builder.setView(view);
            pairedDevicesList();
            return builder.create();
        }
    }

    public class CustomAdapter extends ArrayAdapter<BluetoothDevice> {
        private ArrayList<String> mNames;
        private ArrayList<Drawable> mImages;

        public CustomAdapter(Context context, ArrayList<BluetoothDevice> bluetooth, ArrayList<String> names, ArrayList<Drawable> images) {
            super(context, R.layout.bluetooth_adapter, bluetooth);
            mNames = names;
            mImages = images;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View layoutview = inflater.inflate(R.layout.bluetooth_adapter, parent, false);
            TextView mTextView = (TextView) layoutview.findViewById(R.id.bname);
            ImageView mImageView = (ImageView) layoutview.findViewById(R.id.bimage);
            mTextView.setText(mNames.get(position));
            mImageView.setImageDrawable(mImages.get(position));
            return layoutview;
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }
        
        public void run() {
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                //return;
            }
            if (mmSocket.isConnected()) {
                Fragment frag = getFragmentManager().findFragmentByTag("bluetooth");
                DialogFragment df = (DialogFragment) frag;
                df.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectDialog.setMessage("Connection Successfull");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                connectDialog.dismiss();
                            }
                        }, 2000);
                        connectedBluetooth.setText("Connected To : " + mmDevice.getName());
                    }
                });
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectDialog.setMessage("Connection Failed");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                connectDialog.dismiss();
                            }
                        }, 2000);
                    }
                });
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = null;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
