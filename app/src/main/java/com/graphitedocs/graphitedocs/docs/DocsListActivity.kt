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
                    val arrayList = parseToArray(content.toString())

                    arrayList.add(newDoc)
                    sortArrayByDate(arrayList)

                    val putOptions = PutFileOptions()
                    val json = Gson().toJson(arrayList)

                    blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->

                        runOnUiThread {
                            // Start new Activity with this doc
                            startActivity(DocsActivity.newIntent(baseContext, "Untitled", id, date))
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

                    sortArrayByDate(arrayList)

                    rvDocs.adapter = DocsListAdapter(this, arrayList)

                    val sectionItemDecoration = RecyclerSectionItemDecoration(resources.getDimensionPixelSize(R.dimen.docs_list_header), true, getSectionCallback(arrayList))
                    rvDocs.addItemDecoration(sectionItemDecoration)
                }
            }
        })
    }

    private fun parseToArray(response : String) :  ArrayList<DocsListItem> {
        val DATE_FORMAT = "MM/dd/yyyy"
        val gsonBuilder = GsonBuilder()

        gsonBuilder.setDateFormat(DATE_FORMAT)

        val gson = gsonBuilder.create()
        val arr = gson.fromJson(response, Array<DocsListItem>::class.java)

        return arr.toCollection(ArrayList())
    }

    private fun sortArrayByDate(arrayList: ArrayList<DocsListItem>) {

        arrayList.sortWith(kotlin.Comparator { o1, o2 ->
            val a = o1.date.substring(o1.date.length - 4) + o1.date
            val b = o2.date.substring(o2.date.length - 4) + o2.date
            return@Comparator if (a > b) -1 else if (a < b) 1 else 0;
        })
    }

    private fun convertToDate(date : String) : String {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy")
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy")
        val dateFormat = inputFormat.parse(date)
        return outputFormat.format(dateFormat)
    }

    private fun getSectionCallback (list : List<DocsListItem> ) : RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            override fun isSection(position: Int): Boolean {
                return position == 0 || convertToDate(list[position].date) != convertToDate(list[position - 1].date)
            }

            override fun getSectionHeader(position: Int): CharSequence {
                return convertToDate(list[position].date)
            }
        }
    }
}
