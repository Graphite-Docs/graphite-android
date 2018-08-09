package com.graphitedocs.graphitedocs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.graphitedocs.graphitedocs.docs.DocsListActivity
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.blockstack.android.sdk.UserData

class MainActivity : GraphiteActivity() {

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarMainActivity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        signInButton.setOnClickListener { v: View ->
            blockstackSession().redirectUserToSignIn { userData ->
                // signed in!
                Log.d(TAG, "signed in!")

                // update your UI with signed in state
                 runOnUiThread {
                     onSignIn(userData)
                 }
            }
        }

    }

    override fun onLoaded() {
        Log.d(TAG, "Blockstack loaded.")
    }

    override fun onSignIn(userData: UserData) {
        super.onSignIn(userData)
        val intent = Intent(this, DocsListActivity::class.java)
        startActivity(intent)
        finish()
    }
}
