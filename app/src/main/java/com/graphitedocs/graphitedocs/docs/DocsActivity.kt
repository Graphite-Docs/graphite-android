package com.graphitedocs.graphitedocs.docs

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import kotlinx.android.synthetic.main.activity_docs.*


class DocsActivity : GraphiteActivity() {

    val FAB_MARGIN = 16f

    var gestureDetector : GestureDetector? = null
    var isPreview : Boolean = true

    var docTextHTML : SpannableStringBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs)

        editScrollView.visibility = View.GONE
        bottomDocsEditBar.visibility = View.GONE

        docTextHTML = SpannableStringBuilder(loadText())
        previewTextView.text = docTextHTML

        updateEditText(0, 0)

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
        }

        docsEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveText(Html.toHtml(docTextHTML)) // Convert back to html with tags
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

//        if (docTextHTML.substring(Math.max(start - 3, 0), start) == startTag
//                && docTextHTML.substring(end, Math.min(end + 4, docTextHTML.length)) == endTag) {
//
//            docTextHTML.delete(end, Math.min(end + 4, docTextHTML.length))
//            docTextHTML.delete(Math.max(start - 3, 0), start)
//
//
//        } else if (docTextHTML.substring(Math.max(start - 3, 0), start) == startTag) {
//
//            docTextHTML.insert(end, startTag)
//            docTextHTML.delete(Math.max(start - 3, 0), start)
//
//        } else if (docTextHTML.substring(end, Math.min(end + 4, docTextHTML.length)) == endTag) {
//
//            docTextHTML.delete(end, Math.min(end + 4, docTextHTML.length))
//            docTextHTML.insert(start, endTag)
//
//        } else {
//            docTextHTML.insert(end, endTag)
//            docTextHTML.insert(start, startTag)
//        }


    }



    fun saveText (text : String) {
        // Blockstack api save text
    }

    fun loadText () : Spanned? {
        // Blockstack api get text from file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(getString(R.string.test), Html.FROM_HTML_MODE_COMPACT)
        } else {
            return Html.fromHtml(getString(R.string.test))
        }
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
}
