package com.graphitedocs.graphitedocs

import android.content.Intent
import android.os.Bundle
import org.blockstack.android.sdk.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

import java.net.URI

class MainActivity : AppCompatActivity() {

    lateinit var session : BlockstackSession;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)


        val appDomain = URI("https://app.graphitedocs.com")
        val redirectURI = URI("${appDomain}/redirect")
        val manifestURI = URI("${appDomain}/manifest.json")
        val scopes = arrayOf("store_write")

        session = BlockstackSession(this, appDomain, redirectURI, manifestURI, scopes,
            onLoadedCallback = {
            // Enable sign in your app
            signin_button.isEnabled = true
        })

        signin_button.setOnClickListener { v: View ->
            session.redirectUserToSignIn { userData ->
                // signed in!
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
        if (intent?.action == Intent.ACTION_VIEW) {
            val response = intent?.dataString
            if (response != null) {
                val authResponseTokens = response.split(':')
                if (authResponseTokens.size > 1) {
                    val authResponse = authResponseTokens[1]
                    val signInCallBack: (UserData) -> Unit = {
                        Toast.makeText(this, "Pending sign in call", Toast.LENGTH_LONG).show();
                    }
                    session.handlePendingSignIn(authResponse, signInCallBack)
                }
            }
        }
    }
}
