# ScanUIAndroid

## 概要

- [MaBeeeAndroidSDK](https://github.com/novars-jp/MaBeeeAndroidSDK)を使って、MaBeeeデバイスをスキャンして接続するサンプルプロジェクトです。

## ライブラリのインポート

AndroidStudioで[build.gradle(Module: app)](https://github.com/novars-jp/ScanUIAndroid/blob/master/app/build.gradle)に以下を追加します。

```gradle
repositories {
    maven { url 'http://raw.github.com/novars-jp/MaBeeeAndroidSDK/master/repository/' }
}
dependencies {
    compile 'jp.novars.mabeee.sdk:sdk:1.1'
}
```

## ソースの編集

### 1. activity_main.xmlのレイアウト編集

Scanボタン1つと、SeekBarをもつ画面を作ります。
- [activity_main.xml](https://github.com/novars-jp/ScanUIAndroid/blob/master/app/src/main/res/layout/activity_main.xml)

### 2. activity_my_scan.xmlのレイアウト編集

ListViewを1つもつ画面を作ります。
- [activity_main.xml](https://github.com/novars-jp/ScanUIAndroid/blob/master/app/src/main/res/layout/activity_main.xml)

### 3. ライブラリの初期化

- [MainActivity.java](https://github.com/novars-jp/ScanUIAndroid/blob/master/app/src/main/java/jp/novars/scanui/MainActivity.java)のonCreateに以下のソース追加します。
- [MyScanActivity.java](https://github.com/novars-jp/ScanUIAndroid/blob/master/app/src/main/java/jp/novars/scanui/MyScanActivity.java)のonCreateに以下のソース追加します。

```java
App.getInstance().initializeApp(getApplicationContext());
```

- [Appクラス](http://developer.novars.jp/mabeee/android/javadoc/jp/novars/mabeee/sdk/App.html)はこのSDKの中心となるクラスで、Android端末のBluetoothや接続済みのMaBeeeデバイスを管理します。
- App.getInstance()でAppクラスのSingletonインスタンスを取得します。
- AppクラスのinitializeApp(Context context)関数で、Appクラス、ライブラリの初期化を行ないます。
  - これは一般的にはApplicationクラスで最初に呼び出すか、複数回呼び出しても問題ないので、最初のActivityのonCreateなどで呼び出してください。

### 3. Scanボタンのイベント編集

- ScanボタンにOnClickListenerを設定し、MyScanAcitivtyを呼び出す処理を追加します。

```java
Button scanButton = (Button)findViewById(R.id.scanButton);
scanButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
       Intent intent = new Intent(MainActivity.this, MyScanActivity.class);
       startActivity(intent);
    }
 });
```

### 4. SeekBarのイベント編集

- MainActivityのSeekBarにOnSeeekBarChangeListenerを設定し、MaBeeeデバイスの出力を変更する処理を追加します。

```java
SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        //Log.d("", "" + seekBar.getProgress());
        Device[] devices = App.getInstance().getDevices();
        for (Device device : devices) {
            device.setPwmDuty(seekBar.getProgress());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
});
```

- App.getInstance().getDevices()で接続済みのMaBeeeデバイスの配列が取得できます。
- DeviceクラスのsetPwmDuty関数で、MaBeeeデバイスの出力を0から100の範囲で調整できます。

### 5. Scanの開始

- MyScanActivityのonResumeでスキャンを開始します。

```java
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
```

- App.getInstance().startScan()でスキャンを開始します。
- App.ScanListenerのdidUpdateDevices関数が、スキャンの状態に変更があったときに呼ばれます。
- ここではログを出力して、スキャンされたデバイスの配列を保持して、ListViewを更新しています。

### 6. Scanの終了

- MyScanActivityのonPauseでスキャンを停止します。

```java
@Override
protected void onPause() {
    super.onPause();
    App.getInstance().stopScan();
}
```

### 7. ScanAdapterの実装

- Scan結果を表示するためのScanAdapterを実装します。

```java
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
```

- device.getName()でMaBeeeデバイスの名前を表示しています。
- device.getState()でMaBeeeデバイスの状態を取得して表示しています。
- device.getRssi()で電波の強さを取得して表示しています。


### 8. MaBeeeデバイスとの接続・切断

- MyScanAdapterのonCreateでListViewの設定をしています。

```java
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
```
- ListItemのタップ時に、対象のMaBeeeデバイスを取得しています。
- App.getInstance().connect(device)で接続の要求を出しています。
- App.getInstance().disconnect(device)で切断の要求を出しています。


## 実行

### スキャン実行

- ビルドして実行します。
- AndroidのBluetoothがONになっているかを確認してください。
- MaBeeeをおもちゃなどにセットして、おもちゃなどの電源をONにしてください。
- スキャンボタンを押すと、MyScanActivityが表示されます。
- セルには、MaBeeeの名前、RSSI、接続しているかどうかが表示されます。

### 接続

- セルをタップすると接続します。
- もう一度タップすると切断します。
- MaBeeeデバイスの接続状況によりセルの値が変化します。

### 出力の調整

- 接続後MyScanActivityから戻って、SeekBarを調整すると、MaBeeeデバイスの出力が変化します。
