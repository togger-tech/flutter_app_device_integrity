package co.bubotech.app_device_integrity

import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import android.content.Context
import android.util.Base64

// Validate nonce format
private fun validateNonce(nonce: String): Boolean {
    return try {
        val decoded = Base64.decode(nonce, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        decoded.isNotEmpty()
    } catch (e: IllegalArgumentException) {
        false
    }
}

class AppDeviceIntegrity(context: Context, cloudProjectNumber: Long, nonce: String) {

    // //    var nonceBytes = ByteArray(40)
    // //    var randomized = SecureRandom().nextBytes(nonceBytes)
    // var nonce = Base64.encodeToString(ByteArray(40),  Base64.URL_SAFE)

    init {
        require(validateNonce(nonce)) { "Invalid nonce format. Must be Base64 URL_SAFE, NO_WRAP, NO_PADDING encoded." }
    }

    // Create an instance of a manager.
    val integrityManager: IntegrityManager = IntegrityManagerFactory.create(context)

    // Request the integrity token by providing a nonce.
    val integrityTokenResponse: Task<IntegrityTokenResponse> = integrityManager.requestIntegrityToken(
        IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .setCloudProjectNumber(cloudProjectNumber)
            .build())

}