package com.graphitedocs.graphitedocs.utils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.UserData
import java.net.URI

open class GraphiteActivity : AppCompatActivity() {

    private val TAG = GraphiteActivity::class.java.simpleName
    private var _blockstackSession: BlockstackSession? = null
    private var _userData: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //        val appDomain = URI("https://app.graphitedocs.com") //PROD
        val appDomain = URI("https://serene-hamilton-56e88e.netlify.com")
        val redirectURI = URI("${appDomain}/redirect.html")
        val manifestURI = URI("${appDomain}/manifest.json")
        val scopes = arrayOf(Scope.StoreWrite)

        _blockstackSession = BlockstackSession(this, appDomain, redirectURI, manifestURI, scopes,
                onLoadedCallback = {
                    checkLogin()
                    onLoaded()
                })
    }

    private fun checkLogin() {
        blockstackSession().isUserSignedIn({ signedIn ->
            if (signedIn) {
                blockstackSession().loadUserData({userData ->
                    runOnUiThread {
                        if (userData != null) {
                            onSignIn(userData)
                        }
                    }
                })
            }
        })
    }

    // only do file operations after this method has been called
    open fun onLoaded() {
        throw IllegalStateException("Please override this function.")
    }

    override fun onResume() {
        super.onResume()
        if (_blockstackSession?.loaded == true) {
            checkLogin()
        }
    }

    open fun onSignIn(userData: UserData) {
        // Should only do this once per activity session
        // Write to shared preferences
        _userData = userData
        Log.d(TAG, "signed in!")
        Log.d(TAG, userData.json.toString())
       // Toast.makeText(this, "Signed in as ${userData.json.getJSONObject("profile").getString("name")}", Toast.LENGTH_LONG).show();
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == Intent.ACTION_MAIN) {
            blockstackSession().loadUserData {
                userData -> runOnUiThread {if (userData != null) {onSignIn(userData)}}
            }
        } else if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }
    }

    private fun handleAuthResponse(intent: Intent) {
        val response = intent.dataString
        Log.d(TAG, "response ${response}")
        if (response != null) {
            val authResponseTokens = response.split(':')

            if (authResponseTokens.size > 1) {
                val authResponse = authResponseTokens[1]
                Log.d(TAG, "authResponse: ${authResponse}")
                blockstackSession().handlePendingSignIn(authResponse, { userData ->

                    runOnUiThread {
                        onSignIn(userData)
                    }
                })
            }
        }
    }

    fun blockstackSession() : BlockstackSession {
        val session = _blockstackSession
        if(session != null) {
            return session
        } else {
            throw IllegalStateException("No session.")
        }
    }

    fun userData() : UserData {
        val data = _userData
        if (data != null) {
            return data
        } else {
            throw IllegalStateException("No user data.")
        }
    }
}