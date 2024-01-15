package com.example.sensorble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.sensorble.presentation.Navigation
import com.example.sensorble.ui.theme.SensorBLETheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter
    val list = mutableListOf<ListPaired>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            var isClick by remember {
                mutableStateOf(false)
            }
            SensorBLETheme {
                /*Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialog()
                    }
                )*/
                Column {
                    Button(onClick = {
                        isClick = true
                    }) {
                        Text(text = "Paired List")
                    }
                    Button(onClick = {
                        autoConnect()
                    }) {
                        Text(text = "Connected")
                    }
                    if (isClick) {
                        ListPaired(list)
                    }

                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
        //pairedDevice()
    }

    private var isBluetoothDialogAlreadyShow = false
    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled) {
            if (!isBluetoothDialogAlreadyShow) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                isBluetoothDialogAlreadyShow = true
            } else {
                Toast.makeText(this, "Enabled 1", Toast.LENGTH_SHORT).show()
            }
        } else {
            pairedDevice()
            Toast.makeText(this, "Enabled 3", Toast.LENGTH_SHORT).show()
        }

    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isBluetoothDialogAlreadyShow = false
            if (result.resultCode != Activity.RESULT_OK) {
                showBluetoothDialog()
            } else {
                pairedDevice()
                Toast.makeText(this, "Enabled 2", Toast.LENGTH_SHORT).show()
            }
        }


    private val bleScanner by lazy {
        bluetoothAdapter.bondedDevices
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    fun pairedDevice() {
        list.clear()
        bleScanner.forEach {
            list.add(ListPaired(it.name, it.address))
            Log.d("TAGS", "pairedDevice: Address: ${it.address} ,Name: ${it.name}")
        }
    }
   val myMac="E4:8C:73:0A:8A:14"
    var receveMessage=""
    fun autoConnect(){
        for(i in list){
            //
            if(myMac==i.address){
                //create a bluetooth socket for communication
                val device:BluetoothDevice?= bluetoothAdapter.getRemoteDevice(myMac)
                if(device !=null){

                    val socket=device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))

                    try {

                        socket?.connect()
                        val inputStream:InputStream? = socket?.inputStream
                        val outputStream:OutputStream? = socket?.outputStream

                        val message="Hello Priya"
                        outputStream?.write(message.toByteArray())


                        val buffer = ByteArray(1024)
                        val bytesRead = inputStream?.read(buffer)
                        if(bytesRead !=null && bytesRead>0){
                            val receiveData=buffer.copyOfRange(0,bytesRead)
                            receveMessage= String(receiveData)
                            Toast.makeText(this, "Received: $receveMessage", Toast.LENGTH_SHORT).show()
                        }

                        socket?.close()
                        inputStream?.close()
                        outputStream?.close()

                    }catch (e:Exception){
                        socket?.close()
                        e.printStackTrace()
                        Toast()
                    }
                }
            }
        }
    }
}

data class ListPaired(val name: String, val address: String)

@Composable
fun ListPaired(list:List<ListPaired>) {
    LazyColumn(Modifier.padding(10.dp)) {
        items(list) {
            Column {
                Text(text = it.name, modifier = Modifier.padding(10.dp))
                Text(text = it.address, Modifier.padding(10.dp))
            }
        }
    }

}
