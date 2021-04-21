package com.sobydamn.picontroller

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.Socket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun displayStatus(status: String){
        findViewById<TextView>(R.id.connection_status).text = status
    }
    fun getCommand():String{
        val command = findViewById<EditText>(R.id.command_val).text.toString();
        return command;
    }


    fun makeConnecteion(address:String = "192.168.43.231",port:Int = 65432){
        displayStatus("Connecting.....")
        thread {
            val controlButton = findViewById<Button>(R.id.controller_btn)
            val sendButton = findViewById<Button>(R.id.send_btn)
            try {
                val socket = Socket(address,port);
                val inStream:Scanner;
                val output:OutputStream;
                Log.d("trying:","Connecting....")
                output = socket.getOutputStream()
                inStream = Scanner(socket.getInputStream())
                runOnUiThread{
                    displayStatus("Connected")
                    controlButton.text = "ON"
                    sendButton.isEnabled = true
                }
                var status = false
                controlButton.setOnClickListener{
                    thread {
                        status = controlSwitch(output,status)
                        runOnUiThread{
                            if(status){
                                controlButton.text = "OFF"
                            }
                            else {
                                controlButton.text = "ON"
                            }
                        }
                    }
                }
                sendButton.setOnClickListener{
                    val command:String = getCommand();
                    thread {
                        try {
                            output.write(command.toByteArray())
                        }
                        catch (e:Exception){
                            Log.d("Error in sending",e.toString())
                        }
                    }
                }
                var inputVal:String = ""
                output.write("Successfully Connected".toByteArray())
                inStream.useDelimiter("\r\n")
                while(inputVal !="exit" && inStream.hasNext() && socket.isConnected){
                    try {
                        inputVal = inStream.next()
                        runOnUiThread{
                            displayStatus(inputVal)
                            Log.d("Received Data",inputVal)
                        }
                    }
                    catch (e:Exception){
                        Log.d("Error Reading Data",e.message.toString());
                        displayStatus(e.message.toString())
                    }
                }
                inStream.close()
                output.close()
                socket.close()
            }
            catch (e:Exception){
                Log.d("Error",e.toString());
                displayStatus("ERROR!")
            }
            finally {
                runOnUiThread{
                    controlButton.text = "Connect"
                    sendButton.isEnabled = false
                    controlButton.setOnClickListener{
                        makeConnecteion()
                    }
                }
            }
        }
    }
    fun onControl(view: View){
        makeConnecteion()
    }
    fun controlSwitch(output: OutputStream,isOn:Boolean=false):Boolean{
        if(isOn){
            output.write("OFF".toByteArray())
            return false
        }
        else {
            output.write("ON".toByteArray())
            return true
        }
    }
    fun testClick(view: View){
        val command = findViewById<EditText>(R.id.command_val).text.toString();
        Log.d("Debug",command)
    }
}