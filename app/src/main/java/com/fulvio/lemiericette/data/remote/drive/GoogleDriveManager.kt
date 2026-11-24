package com.fulvio.lemiericette.data.remote.drive

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

/**
 * Gestore dell'autenticazione Google Sign-In e della creazione del client Google Drive API v3.
 */
object GoogleDriveManager {

    private val DRIVE_SCOPE = Scope(DriveScopes.DRIVE_APPDATA)

    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(DRIVE_SCOPE)
            .build()
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(context, getGoogleSignInOptions())
    }

    fun getSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, DRIVE_SCOPE)) {
            account
        } else {
            null
        }
    }

    fun createDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("Le mie Ricette")
        .build()
    }
}
