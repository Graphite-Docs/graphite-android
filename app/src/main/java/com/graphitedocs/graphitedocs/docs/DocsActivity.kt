package com.graphitedocs.graphitedocs.docs

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.*
import com.google.gson.Gson
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import com.graphitedocs.graphitedocs.utils.UndoRedoHelper
import com.graphitedocs.graphitedocs.utils.models.SingleDoc
import kotlinx.android.synthetic.main.activity_docs.*
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import java.util.*


class DocsActivity : GraphiteActivity() {

    val FAB_MARGIN = 16f

    var gestureDetector : GestureDetector? = null
    var isPreview : Boolean = true

    var undoRedoHelper : UndoRedoHelper? = null

    var singleDoc : SingleDoc? = null
    var docTextHTML : SpannableStringBuilder? = null


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

        undoRedoHelper = UndoRedoHelper(docsEditText)

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

            override fun onLongPress(e: MotionEvent?) {
            }

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

            previewScrollView.visibility = View.GONE
            editDocsFab.hide()
            invalidateOptionsMenu()
        }

        docsEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveDoc(Html.toHtml(docTextHTML)) // Convert back to html with tags
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        // ******* Text Editor Functions *******
        // *************************************

        boldButton.setOnClickListener {
            val start = docsEditText.selectionStart
            val end = docsEditText.selectionEnd

            handleHtmlTag(start, end, StyleSpan(Typeface.BOLD))

            updateEditText(start, end)
        }

        italicButton.setOnClickListener {
            val start = docsEditText.selectionStart
            val end = docsEditText.selectionEnd

            handleHtmlTag(start, end, StyleSpan(Typeface.ITALIC))

            updateEditText(start, end)
        }

        underlineButton.setOnClickListener {
            val start = docsEditText.selectionStart
            val end = docsEditText.selectionEnd

            handleHtmlTag(start, end, UnderlineSpan())

            updateEditText(start, end)
        }

    }

    fun handleHtmlTag (start : Int, end : Int, styleSpan: Any) {
        docTextHTML!!.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    override fun onLoaded() {
        loadDoc()
    }

    fun saveDoc (text : String) {
        // Blockstack api save text
        val putOptions = PutFileOptions()
        val fileName = "/documents/" + intent.getLongExtra("id", 0) + ".json"

        singleDoc!!.content = Html.toHtml(docTextHTML)
        val json = Gson().toJson(singleDoc)

        blockstackSession().putFile(fileName, json, putOptions, { readURL: String ->

            runOnUiThread {
                // Doc saved
            }
        })

    }

    fun loadDoc () {
        // Blockstack api get text from file

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

                titleText.setText(singleDoc!!.title)
                docTextHTML = SpannableStringBuilder(singleDoc!!.content)
                previewTextView.text = docTextHTML

                updateEditText(0, 0)
            }
        })
    }


    override fun onBackPressed() {
        if (isPreview) {
            super.onBackPressed()
        } else {
            // Change to preview mode
            isPreview = true

            previewTextView.text = docTextHTML

            editScrollView.visibility = View.GONE
            previewScrollView.visibility = View.VISIBLE
            bottomDocsEditBar.visibility = View.GONE

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

    fun updateEditText (selectionStart : Int, selectionEnd : Int) {
        docsEditText.text = docTextHTML
        docsEditText.setSelection(selectionStart, selectionEnd)
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
            undoRedoHelper?.undo()
        } else if (id == R.id.action_redo) {
            undoRedoHelper?.redo()
        }

        return super.onOptionsItemSelected(item)
    }
}
