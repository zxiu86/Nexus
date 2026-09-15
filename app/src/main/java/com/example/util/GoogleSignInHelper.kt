package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInHelper(private val appContext: Context) {

    companion object {
        private const val TAG = "NexusGoogleSignInHelper"
    }

    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    suspend fun getGoogleIdToken(callerContext: Context? = null, serverClientId: String): Result<String> {
        return try {
            val targetContext = callerContext?.let { findActivity(it) } 
                ?: findActivity(appContext) 
                ?: callerContext 
                ?: appContext
                
            val credentialManager = CredentialManager.create(targetContext)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(targetContext, request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleCredential.idToken
                Log.d(TAG, "Successfully received Google ID Token")
                Result.success(idToken)
            } else {
                Result.failure(Exception("نوع بيانات الاعتماد المستلمة غير متطابقة"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by user")
            Result.failure(Exception("تم إلغاء عملية تسجيل الدخول"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            val message = when {
                e.message?.contains("No credentials", ignoreCase = true) == true -> 
                    "لا توجد حسابات Google مسجلة في هذا الجهاز أو لم يتم اختيار حساب"
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "تعذر الاتصال بخدمات Google، تحقق من اتصالك بالإنترنت"
                else -> e.localizedMessage ?: "فشل تسجيل الدخول بحساب Google"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Google Credential: ${e.message}", e)
            Result.failure(e)
        }
    }
}
