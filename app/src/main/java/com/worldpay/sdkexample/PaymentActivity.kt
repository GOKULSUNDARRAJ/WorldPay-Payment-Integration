package com.worldpay.sdkexample

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.worldpay.error.UnhandledError
import com.worldpay.monitor.ServiceStatusCallback
import com.worldpay.monitor.StatusMonitorCompat
import com.worldpay.monitor.model.PaymentServiceStatus
import com.worldpay.monitor.model.ServiceStatus
import com.worldpay.payment.PaymentHandler
import com.worldpay.payment.PaymentManagerCompat
import com.worldpay.payment.model.CardInteraction
import com.worldpay.payment.model.CardSale
import com.worldpay.websocket.api.payment.PaymentActionRequired
import com.worldpay.websocket.api.payment.PaymentComplete
import com.worldpay.websocket.api.payment.PaymentEvent
import com.worldpay.websocket.api.payment.PaymentNotification
import com.worldpay.websocket.api.payment.PaymentReceipt
import com.worldpay.websocket.api.payment.PaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PaymentActivity : AppCompatActivity() {
    private lateinit var paymentHandler: PaymentHandler

    private lateinit var merchant: TextInputEditText
    private lateinit var amount: TextInputEditText
    private lateinit var save: TextView
    private lateinit var progress: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        merchant = findViewById(R.id.email145)
        amount = findViewById(R.id.email445)
        save = findViewById(R.id.login34)
        progress = findViewById(R.id.pogress)

        save.setOnClickListener { handlePayment() }

        Log.d("PaymentActivity", "onCreate: Activity created")




        val serviceStatusCallback = object : ServiceStatusCallback() {
            override fun onStatusReceived(paymentServiceStatus: PaymentServiceStatus) {
                // Handle the status of IPS
                when (paymentServiceStatus.status) {
                    ServiceStatus.BUSY -> {
                        Log.d("PaymentActivity", "Service is busy.")
                        Toast.makeText(this@PaymentActivity, "Device is in Offline", Toast.LENGTH_SHORT).show()
                        // Add additional handling for busy status
                    }
                    ServiceStatus.NOT_BUSY -> {
                        Log.d("PaymentActivity", "Service is not busy.")

                        // Add additional handling for not busy status
                    }
                    // Handle other statuses as needed
                    else -> {
                        Log.d("PaymentActivity", "Unknown service status: ${paymentServiceStatus.status}")

                    }
                }
            }

            override fun onError(error: UnhandledError) {
                // Handle unexpected errors
                Log.e("PaymentActivity", "An error occurred: ${error.message}")
                Toast.makeText(this@PaymentActivity, "An error occurred: ${error.message}", Toast.LENGTH_LONG).show()
                // Add additional error handling logic
            }
        }


        // Fetch the service status using the defined callback

            StatusMonitorCompat.getServiceStatus(
                coroutineContext = Dispatchers.IO, // Context for background work
                callback = serviceStatusCallback // Callback to handle status updates
            )


    }

    private fun handlePayment() {

        val serviceStatusCallback = object : ServiceStatusCallback() {
            override fun onStatusReceived(paymentServiceStatus: PaymentServiceStatus) {
                // Handle the status of IPS
                when (paymentServiceStatus.status) {
                    ServiceStatus.BUSY -> {
                        Log.d("PaymentActivity", "Service is busy.")
                        Toast.makeText(this@PaymentActivity, "Device is in Offline", Toast.LENGTH_SHORT).show()
                        // Add additional handling for busy status
                    }
                    ServiceStatus.NOT_BUSY -> {
                        Log.d("PaymentActivity", "Service is not busy.")

                        // Add additional handling for not busy status
                    }
                    // Handle other statuses as needed
                    else -> {
                        Log.d("PaymentActivity", "Unknown service status: ${paymentServiceStatus.status}")

                    }
                }
            }

            override fun onError(error: UnhandledError) {
                // Handle unexpected errors
                Log.e("PaymentActivity", "An error occurred: ${error.message}")
                Toast.makeText(this@PaymentActivity, "An error occurred: ${error.message}", Toast.LENGTH_LONG).show()
                // Add additional error handling logic
            }
        }


        // Fetch the service status using the defined callback

        StatusMonitorCompat.getServiceStatus(
            coroutineContext = Dispatchers.IO, // Context for background work
            callback = serviceStatusCallback // Callback to handle status updates
        )

        Log.d("PaymentActivity", "handlePayment: Payment initiated")

        save.visibility = View.GONE
        progress.visibility = View.VISIBLE

        // Get the merchant ID and amount
        val idMerchant = merchant.text.toString()
        val amountString = amount.text.toString()

        // Convert amount to Long
        val amount = amountString.toLongOrNull()
        if (amount == null) {
            showToast("Invalid amount format")
            resetUI()
            Log.e("PaymentActivity", "handlePayment: Invalid amount format")
            return
        }

        // Create a payment request
        val paymentRequest = CardSale(
            CardInteraction.CARD_PRESENT,  // Card interaction type
            amount,  // Amount in minor currency units
            "000000000033709" // Transaction reference
        )

        // Define the event handlers
        paymentHandler = object : PaymentHandler() {
            override fun onEvent(paymentEvent: PaymentEvent) {
                when (paymentEvent) {
                    is PaymentNotification -> notificationReceived(paymentEvent)
                    is PaymentReceipt -> receiptReceived(paymentEvent)
                    is PaymentResult -> resultReceived(paymentEvent)
                    is PaymentActionRequired -> actionRequested(paymentEvent)
                    is PaymentComplete -> paymentComplete()
                }
                Log.d("PaymentActivity", "onEvent: ${paymentEvent.javaClass.simpleName}")
            }

            override fun onErrorReceived(error: UnhandledError) {
                showToast("Payment failed: ${error.message}")
                resetUI()
                Log.e("PaymentActivity", "onErrorReceived: ${error.message}", error)
            }
        }

        // Start the payment process
        PaymentManagerCompat.startPayment(
            Dispatchers.IO,
            paymentRequest,
            paymentHandler
        )


    }

    private fun notificationReceived(notification: PaymentNotification) {
        showToast("Notification: ${notification.notificationText}")
        Log.d("PaymentActivity", "notificationReceived: ${notification.notificationText}")

        val SPLASH_TIME_OUT = 5000L
        val handler = android.os.Handler()

        val runnable = Runnable {
            sendReceiptResponse(false)
        }

        handler.postDelayed(runnable, SPLASH_TIME_OUT)


        // Define the alert dialog message and title based on notification text
        val (title, message) = when {
            notification.notificationText.contains("CANCELLED", ignoreCase = true) -> {
                "Payment Canceled" to "Your payment was canceled. Please try again."
            }

            notification.notificationText.contains("SUCCESS", ignoreCase = true) -> {
                "Payment Successful" to "Your payment was successful. Thank you for your purchase!"
            }

            else -> return
        }

        showCustomDialog(title, message)
    }

    private fun sendReceiptResponse(wantReceipt: Boolean) {
        // Assuming there is a method in your SDK or your code to send the receipt response
        // Replace this with the actual method you use to send the response
        if (wantReceipt) {
            Log.d("PaymentActivity", "Sending receipt response: Yes")
            // Code to send "Yes" response
        } else {
            Log.d("PaymentActivity", "Sending receipt response: No")
            // Code to send "No" response
        }
    }

    private fun showCustomDialog(title: String, message: String) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null)

        // Initialize dialog view elements
        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        val messageView = view.findViewById<TextView>(R.id.dialog_message)
        val button = view.findViewById<TextView>(R.id.dialog_button)

        titleView.text = title
        messageView.text = message

        button.setOnClickListener {
            dialog.dismiss()
            resetUI()
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.setContentView(view)
        dialog.show()

        Log.d(
            "PaymentActivity",
            "showCustomDialog: Dialog shown with title $title and message $message"
        )
    }

    private fun receiptReceived(receipt: PaymentReceipt) {
        // Send the receipt content to a printer
        showToast("Receipt received. $receipt")
        Log.d("PaymentActivity", "receiptReceived: $receipt")
    }

    private fun resultReceived(result: PaymentResult) {
        if (result.payments.isEmpty()) {
            showToast("Payment failed or was canceled.")
            Log.w("PaymentActivity", "resultReceived: Payment failed or was canceled.")
        } else {
            showToast("Payment successful: ${result.payments.size} payment(s) processed.")
            Log.d(
                "PaymentActivity",
                "resultReceived: Payment successful with ${result.payments.size} payment(s) processed."
            )
        }
    }

    private fun actionRequested(action: PaymentActionRequired) {
        Handler(Looper.getMainLooper()).post {
            showToast("Action required: ${action.data}")
            Log.d("PaymentActivity", "actionRequested: Action required with data ${action.data}")
            // Handle action as per your app's logic
        }
    }

    private fun paymentComplete() {
        showToast("Payment process complete.")
        Log.d("PaymentActivity", "paymentComplete: Payment process complete")
        resetUI()
    }

    private fun resetUI() {
        save.visibility = View.VISIBLE
        progress.visibility = View.GONE
        amount.setText("")
        Log.d("PaymentActivity", "resetUI: UI reset")
    }

    private fun navigateToSuccessActivity() {
        val intent = Intent(this, SuccessActivity::class.java)
        startActivity(intent)
        finish()
        Log.d("PaymentActivity", "navigateToSuccessActivity: Navigating to SuccessActivity")
    }

    private fun navigateToFailedActivity() {
        val intent = Intent(this, FailedActivity::class.java)
        startActivity(intent)
        finish()
        Log.d("PaymentActivity", "navigateToFailedActivity: Navigating to FailedActivity")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d("PaymentActivity", "showToast: $message")
    }

    fun cancel(view: View) {
        val intent = Intent(this, CancelActivity::class.java)
        startActivity(intent)
        Log.d("PaymentActivity", "cancel: Navigating to CancelActivity")
    }




    suspend fun fetchServiceStatus() {
        // Define the service status callback

    }

}





