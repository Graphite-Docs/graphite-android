package com.graphitedocs.graphitedocs.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.UserData
import java.net.URI
import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


open class GraphiteActivity : AppCompatActivity() {

    private val TAG = GraphiteActivity::class.java.simpleName
    private var _blockstackSession: BlockstackSession? = null
    private var _userData: UserData? = null

    //        val appDomain = URI("https://app.graphitedocs.com") //PROD
    val appDomain = URI("https://serene-hamilton-56e88e.netlify.com")
    val redirectURI = URI("${appDomain}/redirect.html")
    val manifestURI = URI("${appDomain}/manifest.json")
    val scopes = arrayOf(Scope.StoreWrite)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    companion object {
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px
        }

        /**
         * This method converts device specific pixels to density independent pixels.
         *
         * @param px A value in px (pixels) unit. Which we need to convert into db
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent dp equivalent to px value
         */
        fun convertPixelsToDp(px: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            val dp = px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return dp
        }
    }
}
