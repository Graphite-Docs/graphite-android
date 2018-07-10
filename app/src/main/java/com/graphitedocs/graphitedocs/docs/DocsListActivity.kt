package com.graphitedocs.graphitedocs.docs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import com.graphitedocs.graphitedocs.utils.adapters.DocsListAdapter
import com.graphitedocs.graphitedocs.utils.adapters.RecyclerSectionItemDecoration
import com.graphitedocs.graphitedocs.utils.models.DocsListItem
import kotlinx.android.synthetic.main.activity_docs_list.*
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DocsListActivity : GraphiteActivity() {

    private val TAG = DocsListActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs_list)

        rvDocs.layoutManager = LinearLayoutManager(this)

    }

    override fun onLoaded() {

        newDocFab.setOnClickListener {
            val getOptions = GetFileOptions()
            val fileName = getString(R.string.documents_list)

            blockstackSession().getFile(fileName, getOptions, {content: Any ->
                // content can be a `String` or a `ByteArray`
                Log.d(TAG, content.toString())

                runOnUiThread {
                    val date = SimpleDateFormat("MM/dd/yyyy").format(Date())
                    val id =  Date().time
                    val newDoc = DocsListItem("Untitled", date, ArrayList(), ArrayList(), userData().json["username"].toString(), id, date, date)
                    var arrayList = parseToArray(content.toString())
                    if (arrayList == null) {
                        arrayList = java.util.ArrayList()
                    }
                    arrayList.add(newDoc)

                    val putOptions = PutFileOptions()

                    val json = Gson().toJson(arrayList)

                    blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->

                        runOnUiThread {
                            // Start new Activity with this doc
                            startActivity(DocsActivity.newIntent(baseContext, "Untitled", id))
                        }
                    })
                }
            })
        }

        loadData()
    }

    private fun loadData () {

        val options = GetFileOptions()

        val fileName = getString(R.string.documents_list)

        var arrayList : ArrayList<DocsListItem> = ArrayList()

        blockstackSession().getFile(fileName, options, {content: Any ->
            // content can be a `String` or a `ByteArray`
            Log.d(TAG, content.toString())

            runOnUiThread {
                arrayList = parseToArray(content.toString())

                if (arrayList.isEmpty()) {
                    emptyDocsListTextView.visibility = View.VISIBLE
                    rvDocs.visibility = View.GONE
                } else {
                    emptyDocsListTextView.visibility = View.GONE
                    rvDocs.visibility = View.VISIBLE

                    // Sort array by most recent dates

                    rvDocs.adapter = DocsListAdapter(this, arrayList)

                    val sectionItemDecoration = RecyclerSectionItemDecoration(resources.getDimensionPixelSize(R.dimen.docs_list_header), true, getSectionCallback(arrayList))
                    rvDocs.addItemDecoration(sectionItemDecoration)
                }
            }
        })

//        Mock data
//        var doc1 = DocsListItem("Daniel's Story", "May 9, 2018", listOf("danielwang.id"), listOf(""), "danielwang.id", 123, "06/12/2018", "")
//        var doc2 = DocsListItem("Justin's Story", "April 1, 2018", listOf("justinhunter.id"), listOf("Story"), "danielwang.id", 123, "06/12/2018", "")
//        var doc3 = DocsListItem("The Great Gatsby", "March 22, 2018", listOf("graphitetest.id"), listOf("A long story"), "danielwang.id", 123, "06/12/2018", "")
//        arrayList = arrayListOf(doc1, doc1, doc1, doc1, doc1, doc2, doc2, doc2, doc3, doc3, doc3, doc3, doc3)
    }

    private fun parseToArray(response : String) :  ArrayList<DocsListItem> {
        val DATE_FORMAT = "MM/dd/yyyy"

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setDateFormat(DATE_FORMAT)
        val gson = gsonBuilder.create()

        val arr = gson.fromJson(response, Array<DocsListItem>::class.java)

        return arr.toCollection(ArrayList())
    }

    private fun getSectionCallback (list : List<DocsListItem> ) : RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            override fun isSection(position: Int): Boolean {
                return position == 0 || list[position].date != list[position - 1].date
            }

            override fun getSectionHeader(position: Int): CharSequence {
                return list[position].date
            }
        }
    }
}
