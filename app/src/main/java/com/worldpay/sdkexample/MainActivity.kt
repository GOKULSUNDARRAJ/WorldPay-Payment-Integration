package com.worldpay.sdkexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.worldpay.AppDetails
import com.worldpay.Configuration
import com.worldpay.Logger
import com.worldpay.error.UnhandledError
import com.worldpay.registration.PosRegistrationCallback
import com.worldpay.registration.RegistrationManagerCompat
import com.worldpay.registration.model.PointOfSale
import com.worldpay.registration.model.PosRegistration
import com.worldpay.websocket.api.payment.Payment
import com.worldpay.websocket.api.registration.PosRegistrationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {


    private lateinit var poid: TextInputEditText
    private lateinit var reference: TextInputEditText
    private lateinit var activationcode: TextInputEditText
    private lateinit var save: TextView

    private lateinit var payment: TextView
    private lateinit var progreess: ProgressBar
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        payment=findViewById(R.id.gotopay)
        poid = findViewById(R.id.email1)
        reference = findViewById(R.id.email4)
        activationcode = findViewById(R.id.email2)
        save=findViewById(R.id.login)
        progreess = findViewById(R.id.pogress11)
        payment.setOnClickListener{
            val intent = Intent(this@MainActivity, PaymentActivity::class.java)
            startActivity(intent)
        }

        save.setOnClickListener {
            save.visibility = View.GONE
            progreess.visibility = View.VISIBLE
            // Create a custom logger instance
            val customLogger = object : Logger {
                override fun info(tag: String, message: String, throwable: Throwable?) {
                    println("INFO [$tag]: $message")
                    Handler(Looper.getMainLooper()).post {
                        //  Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }

                    throwable?.printStackTrace()
                }

                override fun debug(tag: String, message: String, throwable: Throwable?) {
                    println("DEBUG [$tag]: $message")
                    Handler(Looper.getMainLooper()).post {
                        //    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                    throwable?.printStackTrace()
                }

                override fun warn(tag: String, message: String, throwable: Throwable?) {
                    println("WARN [$tag]: $message")
                    Handler(Looper.getMainLooper()).post {
                        //   Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                    throwable?.printStackTrace()
                }

                override fun error(tag: String, message: String, throwable: Throwable?) {
                    System.err.println("ERROR [$tag]: $message")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                    throwable?.printStackTrace()
                }


            }

            // Create an instance of AppDetails (adjust if necessary)
            val appDetails = AppDetails("YourAppName", "1.0")

            try {
                // Convert the server URL string to URI
                val serverUri = URI("wss://ws.muat.worldpaypp.com:443/ipc-app/")

                // Configure the SDK
                Configuration.configure(
                    serverUri, // Server URL
                    300L, // Timeout in seconds
                    10000, // Host heartbeat interval in seconds
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJwZHBvc3JlZiI6ImRkY2hlY2tvdXQiLCJhdWQiOiJhY2Nlc3Mud29ybGRwYXkuY29tL3RvdGFsL3BvcyIsInN1YiI6IjE3Njg3MjA1NCIsInBkcG9zaWQiOiI0OTJ4eVkiLCJpc3MiOiJpcHMtZHMtd29ybGRwYXkiLCJwZG0iOlsiMDAwMDAwMDAwMDMzNzA5Il0sImV4cCI6MTc1MjkxNzUxMiwiaWF0IjoxNzIxMzgxNTEyLCJqdGkiOiI3OTEyOTVhMi02ZmU5LTQ5NjktYTJmNC04ZTlhZmFkMTliZmUifQ.v0nNg_W25_hVjT-u6uXg0g2mJ1ii4nbuF9X99MoomKquh-ZNiVeWJ_QZM-KSpHlV1toFP5HWMZcpIxR_oZoVLYLV_QTNNy4PJqBvNiR6zANP2QLY5HlHTPI0smbIcJa_ZcJvznWJZ2VIDPSPk-T2sSw9jVBM2y8RTx_m_aCqjdRnZevXleyCV8h5BmLq9IUqnHPsW-kHiSJP4gDCDTpkoQqoueKQgXzyVTdc79MWQKtp1sZw16u6F3BzdUZISHtWNC14HdI8gPQwvWLLfL2y1jNQtteNQPi2IZr3ptF-Z2tp-Bttbg4kWYxR_s6t1IUnEqWNkr_bVyYU3vAU-7oTiA", // POS License Key
                    "kiosk", // Paypoint ID
                    customLogger, // Logger
                    appDetails // Application details
                )

                // Show a Toast message indicating successful configuration
                //  Toast.makeText(this, "SDK Configured Successfully", Toast.LENGTH_LONG).show()

            } catch (e: URISyntaxException) {
                e.printStackTrace()
                // Show a Toast message indicating failure to configure
                //  Toast.makeText(this, "Failed to Configure SDK", Toast.LENGTH_LONG).show()
            }

            registerPos()
        }



    }


    private fun registerPos() {
        // Define the callback for registration
        val registrationCallback = object : PosRegistrationCallback() {
            override fun onRegistrationComplete(response: PosRegistrationResponse) {
                // Re-configure the SDK
                // Store the POS License Key

                // Show a Toast message indicating successful registration
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "Registration Successful!"+response.pointOfSaleLicenseKey, Toast.LENGTH_LONG).show()

                    val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("posLicenseKey", response.pointOfSaleLicenseKey)
                    editor.apply()
                    val intent = Intent(this@MainActivity, PaymentActivity::class.java)
                    intent.putExtra("posLicenseKey", response.pointOfSaleLicenseKey)
                    startActivity(intent)
                    save.visibility = View.VISIBLE
                    progreess.visibility = View.GONE

                }
            }

            override fun onError(error: UnhandledError) {
                // Handle error

                // Show a Toast message indicating registration failure
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "Registration Failed: ${error.errorType}", Toast.LENGTH_LONG).show()
                    save.visibility = View.VISIBLE
                    progreess.visibility = View.GONE

                }
            }
        }

        val id = poid.text.toString()
        val reference = reference.text.toString()
        val activecod = activationcode.text.toString()

        // Create PointOfSale object with appropriate constructor parameters
        val pointOfSale = PointOfSale(id, reference, activecod)

        // Create PosRegistration request
        val request = PosRegistration(pointOfSale, "kiosk") // Adjust "paypointId" as necessary

        // Register the POS - Using CoroutineScope to handle coroutine context
        CoroutineScope(Dispatchers.IO).launch {
            RegistrationManagerCompat.registerPointOfSale(
                Dispatchers.IO,
                request,
                registrationCallback
            )
        }
    }

}
