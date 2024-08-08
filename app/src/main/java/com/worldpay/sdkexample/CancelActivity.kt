package com.worldpay.sdkexample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.worldpay.error.UnhandledError
import com.worldpay.payment.PaymentHandler
import com.worldpay.payment.PaymentManagerCompat
import com.worldpay.payment.model.CardPaymentCancel
import com.worldpay.websocket.api.payment.*
import com.worldpay.websocket.api.payment.card.CardDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CancelActivity : AppCompatActivity() {

    private lateinit var paymentHandler: PaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel)

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
            }

            override fun onErrorReceived(error: UnhandledError) {
                showToast("Payment failed: ${error.message}")
                resetUI()
                navigateToFailedActivity()
            }
        }

        // Call handlePaymentCancel when needed, e.g., on button click

    }

    private fun handlePaymentCancel() {
        val cardDate = CardDate(12, 21)

        val cardPaymentCancel = CardPaymentCancel(
            merchantTransactionReference = "Kiosk",
            gatewayTransactionReference = "gatewayRef456",
            cardNumber = "123456XXXXXX1234",
            cardExpiryDate = cardDate
        )

        // Launch a coroutine to handle the payment cancellation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the instance of PaymentHandler here
                PaymentManagerCompat.cancelPayment(
                    Dispatchers.IO,
                    cardPaymentCancel,
                    paymentHandler // Pass the instance of PaymentHandler
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Cancellation failed: ${e.message}")
                    resetUI()
                    navigateToFailedActivity()
                }
            }
        }
    }

    // Implement these methods to handle payment events
    private fun notificationReceived(paymentNotification: PaymentNotification) {
        showToast("Payment Notification Received")
    }

    private fun receiptReceived(paymentReceipt: PaymentReceipt) {
        showToast("Payment Receipt Received")
    }

    private fun resultReceived(paymentResult: PaymentResult) {
        showToast("Payment Result Received")
    }

    private fun actionRequested(paymentActionRequired: PaymentActionRequired) {
        showToast("Payment Action Required")
    }

    private fun paymentComplete() {
        showToast("Payment Complete")
        // You can also navigate to another activity or update UI here
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun resetUI() {
        // Implement UI reset logic here
    }

    private fun navigateToFailedActivity() {
        // Implement navigation to a failure activity here
    }

    fun cancel(view: View) {
        handlePaymentCancel()
    }
}
