package com.graphitedocs.graphitedocs.docs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.text.Spanned
import android.util.Log
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.docs.DocsListActivity.Companion.parseToArray
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import com.graphitedocs.graphitedocs.utils.models.SingleDoc
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_docs.*
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DocsActivity : GraphiteActivity() {

    private val FAB_MARGIN = 16f

    private var gestureDetector : GestureDetector? = null
    private var isPreview : Boolean = true

    private var singleDoc : SingleDoc? = null

    private val TAG = DocsActivity::class.java.simpleName
    private var subject: PublishSubject<String>? = null
    private var progressDialog : MaterialDialog? = null


    companion object {
        fun newIntent (mContext : Context, title : String, id : Long, date : String) : Intent {
            val intent = Intent(mContext, DocsActivity::class.java)

            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putLong("id", id)
            bundle.putString("date", date)

            intent.putExtras(bundle)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs)
        setSupportActionBar(toolbarDocsActivity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        invalidateOptionsMenu()

        editScrollView.visibility = View.GONE
        bottomDocsEditBar.visibility = View.GONE

        docsEditText.setEditorHeight(400)
        docsEditText.setEditorFontSize(18)
        docsEditText.setPadding(40, 40, 40, 60)
        docsEditText.setEditorBackgroundColor(Color.TRANSPARENT)

        progressDialog = MaterialDialog.Builder(this)
                .content("Loading doc")
                .progress(true, 0)
                .show()

        subject = PublishSubject.create<String>()
        (subject as PublishSubject<String>).throttleLast(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    text -> saveDoc(text)
                })


        gestureDetector = GestureDetector(this, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                val actionbar = supportActionBar
                val decorView = window.decorView

                if (actionbar != null) {
                    if (!actionbar.isShowing) {
                        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        decorView.systemUiVisibility = uiOptions

                        actionbar.show()
                        editDocsFab.show()
                        return true
                    }
                }
                return false
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                return false
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                val actionbar = supportActionBar
                val decorView = window.decorView
                if (actionbar != null) {
                    if (distanceY > 0) {
                        if (actionbar.isShowing) {

                            val uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN


                            decorView.systemUiVisibility = uiOptions


                            actionbar.hide()
                            editDocsFab.hide()

                            val marginParamsFab : ViewGroup.MarginLayoutParams= editDocsFab.layoutParams as ViewGroup.MarginLayoutParams
                            marginParamsFab.setMargins(0, 0, convertDpToPixel(FAB_MARGIN, this@DocsActivity).toInt(),
                                    convertDpToPixel(FAB_MARGIN, this@DocsActivity).toInt() + getBottomNavBarHeight())

                            val marginParamsBotBar : ViewGroup.MarginLayoutParams= bottomDocsEditBar.layoutParams as ViewGroup.MarginLayoutParams
                            marginParamsBotBar.setMargins(0, 0, 0, getBottomNavBarHeight())

                            return true
                        }
                    } else {
                        if (!actionbar.isShowing) {

                            val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                            decorView.systemUiVisibility = uiOptions

                            actionbar.show()
                            editDocsFab.show()
                            return true
                        }
                    }
                }
                return false
            }

            override fun onLongPress(e: MotionEvent?) {}

        })

//        TODO: Disabled fullscreen mode for now until we find a better way to do it
//        previewScrollView.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v : View, event : MotionEvent) : Boolean {
//
//                return gestureDetector!!.onTouchEvent(event)
//            }
//        })

        editDocsFab.setOnClickListener {
            isPreview = false

            editScrollView.visibility = View.VISIBLE
            bottomDocsEditBar.visibility = View.VISIBLE
            titleText.visibility = View.GONE
            previewScrollView.visibility = View.GONE
            editDocsFab.hide()
            invalidateOptionsMenu()
        }

        docsEditText.setOnTextChangeListener {
            subject!!.onNext(it)
        }

        titleText.setOnClickListener {
            MaterialDialog.Builder(this)
                    .title("Rename document")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("", singleDoc!!.title, { dialog, input ->
                        singleDoc!!.title = input.toString()
                        titleText.text = input
                        saveDoc(docsEditText.html)
                        updateDocsCollection()
                    }).show()
        }

        // ******* Text Editor Functions *******
        // *************************************

        boldButton.setOnClickListener {
            docsEditText.setBold()
        }

        italicButton.setOnClickListener {
            docsEditText.setItalic()
        }

        underlineButton.setOnClickListener {
            docsEditText.setUnderline()
        }
    }

    override fun onLoaded() {
        loadDoc()
        progressDialog!!.cancel()
    }

    private fun saveDoc (text : String) {
        val date = SimpleDateFormat("MM/dd/yyyy").format(Date())
        val putOptions = PutFileOptions()
        val fileName = "/documents/" + intent.getLongExtra("id", 0) + ".json"

        singleDoc!!.content = text.replace("\"", "\\\"")
        singleDoc!!.updated = date
        singleDoc!!.date = date

        val json = Gson().toJson(singleDoc)

        blockstackSession().putFile(fileName, json, putOptions, { readURL: String ->
            Log.d(TAG, readURL)
            runOnUiThread {
                // Doc saved
            }
        })
    }

    private fun loadDoc () {
        val options = GetFileOptions()
        val filename = "/documents/" + intent.getLongExtra("id", 0) + ".json"

        blockstackSession().getFile(filename, options, {content: Any ->

            runOnUiThread {
                singleDoc = if (content !is ByteArray) {
                    SingleDoc.parseJSON(content.toString())
                } else {
                    val author = userData().json["username"].toString()
                    val date = intent.getStringExtra("date")
                    SingleDoc(intent.getStringExtra("title"), date,
                            ArrayList(), ArrayList(), author, intent.getLongExtra("id", 0),
                            date, date, "")
                }

                if (content is ByteArray) {
                    docsEditText.setAlignLeft()
                    docsEditText.setFontSize(12)
                }

                titleText.text = singleDoc!!.title
                docsEditText.html = singleDoc!!.content
                previewTextView.text = fromHtml(singleDoc!!.content)
            }
        })
    }


    override fun onBackPressed() {
        if (isPreview) {
            super.onBackPressed()
        } else {
            // Change to preview mode
            isPreview = true

            previewTextView.text = fromHtml(docsEditText.html)

            editScrollView.visibility = View.GONE
            previewScrollView.visibility = View.VISIBLE
            bottomDocsEditBar.visibility = View.GONE
            titleText.visibility = View.VISIBLE

            editDocsFab.show()
            invalidateOptionsMenu()
        }
    }

    fun getBottomNavBarHeight() : Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private fun fromHtml(text : String?) : Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
    }

    private fun updateDocsCollection() {
        val options = GetFileOptions()
        val fileName = getString(R.string.documents_list)

        blockstackSession().getFile(fileName, options, { content: Any ->
            if (content !is ByteArray) Log.d(TAG, content.toString())

            runOnUiThread {
                val arrayList = parseToArray(content.toString())

                arrayList.forEach {
                    if (it.id == intent.getLongExtra("id", 0)) {
                        it.title = singleDoc!!.title
                        it.date = singleDoc!!.date
                        it.updated = singleDoc!!.updated
                    }
                }

                val putOptions = PutFileOptions()
                val json = Gson().toJson(arrayList)

                blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->

                })
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_toolbar_edit_docs, menu)

        if (menu != null) {
            for (i in 0 until menu.size())
                menu.getItem(i).isVisible = !isPreview
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val id = item!!.itemId

        if (id == R.id.action_undo) {
            docsEditText.undo()
        } else if (id == R.id.action_redo) {
            docsEditText.redo()
        }

        return super.onOptionsItemSelected(item)
    }
}
