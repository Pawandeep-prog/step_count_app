package com.programminghut.stepcountapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.programminghut.stepcountapp.databinding.ActivityMainBinding
import com.programminghut.stepcountapp.viewmodels.StepCountViewModel
import java.time.LocalDateTime
import java.time.ZoneId

class MainActivity : AppCompatActivity() {
    lateinit var xmls: ActivityMainBinding
    lateinit var stepCountViewModel: StepCountViewModel

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepCountViewModel = StepCountViewModel()
        xmls = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        xmls.steps = stepCountViewModel

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            signIn()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        if (shouldProvideRationale) {
            Toast.makeText(this, "permission is required to run the app. Now go to setting and allow permission!", Toast.LENGTH_SHORT).show()

        } else {
            Log.d("Fit", "Requesting permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun signIn(){

        Toast.makeText(this, "sign in 2 called", Toast.LENGTH_SHORT).show()
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        Log.d("account info", account.email.toString())
        Toast.makeText(this, account.email.toString(), Toast.LENGTH_SHORT).show()

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                101,
                account,
                fitnessOptions)
        } else {
            accessGoogleFit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                101 -> accessGoogleFit()
                else -> {
                    Toast.makeText(this, "err 1", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show()

            }
        }
    }

    var str = ""
    @RequiresApi(Build.VERSION_CODES.O)
    private fun accessGoogleFit() {
        val end = System.currentTimeMillis()
        val start = end - TimeUnit.HOURS.toMillis(24)
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(start, end, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener({ response ->
                str = ""

                for (dataSet in response.buckets.flatMap { it.dataSets }) {
                    dumpDataSet(dataSet)
                }

                stepCountViewModel.stepCount.set(str)
            })
            .addOnFailureListener({ e -> Log.d("errror 2", "OnFailure()", e) })
    }

    fun dumpDataSet(dataSet: DataSet) {
        for (dp in dataSet.dataPoints) {

            str += "Data Point: \n"
            str += "Type: ${dp.dataType.name} \n"
            str += "start time: ${dp.getStartTime(TimeUnit.HOURS)} \n"
            str += "end time : ${dp.getEndTime(TimeUnit.HOURS)} \n"

            for (field in dp.dataType.fields) {
                str += "Field: ${field.name.toString()} Value: ${dp.getValue(field)} \n"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                signIn()
            } else {
                // Permission denied
                Log.d("Fit", "Permission denied.")
                Toast.makeText(this, "Permission is required!", Toast.LENGTH_SHORT)
                requestPermissions()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun signInBtn(view: View){
        signIn()
    }
}


