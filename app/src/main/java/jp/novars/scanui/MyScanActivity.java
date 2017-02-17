package jp.novars.scanui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import jp.novars.mabeee.sdk.App;
import jp.novars.mabeee.sdk.Device;

public class MyScanActivity extends AppCompatActivity {

    ListView mListView;
    Device[] mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scan);

        mListView = (ListView) findViewById(jp.novars.mabeee.sdk.R.id.listView);
        mListView.setAdapter(new ScanAdapter());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Device device = mDevices[position];
                if (device.getState() == Device.State.Disconnected) {
                    App.getInstance().connect(device);
                } else {
                    App.getInstance().disconnect(device);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().startScan(new App.ScanListener() {
            @Override
            public void didUpdateDevices(Device[] devices) {
                for (Device device : devices) {
                    Log.d("DEVICE", device.getName());
                }
                mDevices = devices;
                ((ScanAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().stopScan();
    }

    private class ScanAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (null == mDevices) {
                return 0;
            }
            return mDevices.length;
        }

        @Override
        public Object getItem(int position) {
            return mDevices[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            }

            Device device = mDevices[position];

            String stateString = "";
            switch (device.getState()) {
                case Disconnected:
                    stateString = "Disconneced";
                    break;
                case Connecting:
                    stateString = "Connecting";
                    break;
                case Connected:
                    stateString = "Connected";
                    break;
            }

            TextView textView1 = (TextView)convertView.findViewById(android.R.id.text1);
            textView1.setText(device.getName());

            TextView textView2 = (TextView)convertView.findViewById(android.R.id.text2);
            textView2.setText("RSSI: " + device.getRssi() + ", STATE: " + stateString);

            return convertView;
        }
    }
}
