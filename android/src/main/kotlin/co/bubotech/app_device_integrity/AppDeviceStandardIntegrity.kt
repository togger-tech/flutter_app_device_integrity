package co.bubotech.app_device_integrity

import android.content.Context
import android.util.Base64
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.core.integrity.*
import java.security.MessageDigest

// Validate nonce format
private fun validateNonce(nonce: String): Boolean {
    return try {
        val decoded = Base64.decode(nonce, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        decoded.isNotEmpty()
    } catch (e: IllegalArgumentException) {
        false
    }
}

// SHA‑256 digest + URL_SAFE Base64
private fun sha256Base64Url(data: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(data)
    return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

class AppDeviceIntegrityStandard(
    context: Context
) {

    private val manager: StandardIntegrityManager =
        IntegrityManagerFactory.createStandard(context) // Standard manager :contentReference[oaicite:1]{index=1}
    private var provider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    /**
     * Prepara o provider de token de integridade.
     * Deve ser chamada uma única vez (ideal no onCreate da App ou Activity).
     */
    fun prepareProvider(cloudProjectNumber: Long): Task<StandardIntegrityManager.StandardIntegrityTokenProvider> {
        return manager.prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build()
        ).addOnSuccessListener { tokenProvider ->
            provider = tokenProvider
        }
        // .addOnFailureListener { exception -> 
        //     handleError(exception)
        // };
    }

    fun requestIntegrityToken(cloudProjectNumber: Long, nonce: String): Task<StandardIntegrityManager.StandardIntegrityToken> {
        require(validateNonce(nonce)) { "Invalid nonce format. Must be Base64 URL_SAFE, NO_WRAP, NO_PADDING encoded." }
        
        val taskCompletionSource = TaskCompletionSource<StandardIntegrityManager.StandardIntegrityToken>()
        
        val currentProvider = provider
        if (currentProvider != null) {
            // Provider já está preparado, fazer a requisição diretamente
            // val requestHash = sha256Base64Url(nonce.toByteArray())
            currentProvider.request(
                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .setRequestHash(nonce)
                    .build()
            ).addOnSuccessListener { response ->
                taskCompletionSource.setResult(response)
            }.addOnFailureListener { e ->
                taskCompletionSource.setException(e)
            }
        } else {
            // Provider não está preparado, tentar preparar primeiro
            prepareProvider(cloudProjectNumber)
                .addOnSuccessListener { currentProvider ->
                    // Provider preparado com sucesso, fazer a requisição
                    // val requestHash = sha256Base64Url(nonce.toByteArray())
                    currentProvider.request(
                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                            .setRequestHash(nonce)
                            .build()
                    ).addOnSuccessListener { response ->
                        taskCompletionSource.setResult(response)
                    }.addOnFailureListener { e ->
                        taskCompletionSource.setException(e)
                    }
                }
                .addOnFailureListener { e ->
                    // Falha ao preparar o provider
                    taskCompletionSource.setException(
                        IllegalStateException("Provider não está preparado e falha ao preparar: ${e.message}", e)
                    )
                }
        }
        
        return taskCompletionSource.task
    }
}
