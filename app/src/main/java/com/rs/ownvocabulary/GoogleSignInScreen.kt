package com.rs.ownvocabulary

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.rs.ownvocabulary.api.AuthApi
import com.rs.ownvocabulary.api.OnboardResponseData


@Composable
fun GoogleSignInScreen(onCompleted: (it: OnboardResponseData) -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val id = "721697891458-v1u6ph5toejt1d0gkutmqhq1qk57e5c3.apps.googleusercontent.com"
    val auth = remember { Firebase.auth }
    val credentialManager = remember { CredentialManager.create(context) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun joinUser(
        email: String,
        username: String,
        phoneNumber: String,
        avatar: String,
        uid: String,
    ) {
        scope.launch {
            try {
                loadingMessage = "Setting up your account..."

                val result = AuthApi.onboard(
                    email = email,
                    username = username,
                    phoneNumber = phoneNumber,
                    photoUrl = avatar,
                    uid = uid
                )

                if (result != null) {
                    loadingMessage = "Almost done..."
                    onCompleted(result)
                } else {
                    isLoading = false
                    errorMessage = "Failed to set up account. Please try again."
                }

            } catch (ex: Exception) {
                isLoading = false
                errorMessage = "Account setup failed: ${ex.message}"
                println(ex)
            }
        }
    }

    fun startGoogleSignIn() {
        isLoading = true
        loadingMessage = "Opening Google Sign-In..."
        errorMessage = null

        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(id)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                loadingMessage = "Authenticating..."
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(result.credential.data)

                    val idToken = googleIdTokenCredential.idToken
                    val credential = GoogleAuthProvider.getCredential(idToken, null)

                    loadingMessage = "Signing in..."
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                joinUser(
                                    email = user?.email ?: "",
                                    username = user?.displayName ?: "",
                                    phoneNumber = user?.phoneNumber ?: "",
                                    avatar = user?.photoUrl?.toString() ?: "",
                                    uid = auth.uid ?: ""
                                )
                            } else {
                                isLoading = false
                                errorMessage = "Sign in failed: ${task.exception?.message}"
                                println("signInWithCredential ${task.exception}")
                            }
                        }
                } else {
                    isLoading = false
                    errorMessage = "Invalid credential type received"
                }

            } catch (e: Exception) {
                isLoading = false
                when {
                    e.message?.contains("cancelled by the user", ignoreCase = true) == true -> {
                        // User cancelled the sign-in, don't show error message
                        errorMessage = null
                    }
                    e.message?.contains("Cannot find a matching credential", ignoreCase = true) == true -> {
                        errorMessage = "No Google accounts found. Please add a Google account to your device."
                    }
                    else -> {
                        errorMessage = "Sign in failed: ${e.message}"
                    }
                }
                println("Google Sign-In Error: $e")
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sign In Button
        Button(
            onClick = { startGoogleSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = loadingMessage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // If you have a Google icon drawable, uncomment this:
                    // Icon(
                    //     painter = painterResource(id = R.drawable.ic_google),
                    //     contentDescription = null,
                    //     modifier = Modifier.size(20.dp),
                    //     tint = Color.Unspecified
                    // )
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Loading Overlay Message
        if (isLoading) {
            Text(
                text = "Please wait while we set up your account",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}