package com.graphitedocs.graphitedocs

import android.content.Intent
import android.os.Bundle
import org.blockstack.android.sdk.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

import java.net.URI

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var session: BlockstackSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val appDomain = URI("https://app.graphitedocs.com") //PROD
        val appDomain = URI("https://serene-hamilton-56e88e.netlify.com") //STAGING
        val redirectURI = URI("${appDomain}/redirect.html")
        val manifestURI = URI("${appDomain}/manifest.json")
        val scopes = arrayOf("store_write")

        session = BlockstackSession(this, appDomain, redirectURI, manifestURI, scopes,
            onLoadedCallback = {
            // Enable sign in your app
            signInButton.isEnabled = true
        })

        signInButton.setOnClickListener { v: View ->
            blockstackSession().redirectUserToSignIn { userData ->
                // signed in!
                Log.d(TAG, "signed in!")
                Toast.makeText(this, "Signed in as ${userData.did}", Toast.LENGTH_LONG).show();

                // update your UI with signed in state
                 runOnUiThread {
                     onSignIn(userData)
                 }
            }
        }

    }

    private fun onSignIn(userData: UserData) {
        val intent = Intent(this, SelectAppActivity::class.java)
        startActivity(intent)
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
                    Log.d(TAG, "signed in!")
                    runOnUiThread {
                        onSignIn(userData)
                    }
                })
            }
        }
    }

    fun blockstackSession() : BlockstackSession {
        val session = this.session
        if(session != null) {
            return session
        } else {
            throw IllegalStateException("No session.")
        }
    }
}
