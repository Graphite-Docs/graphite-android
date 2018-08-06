package com.graphitedocs.graphitedocs.docs

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import com.graphitedocs.graphitedocs.utils.adapters.DocsListAdapter
import com.graphitedocs.graphitedocs.utils.adapters.RecyclerSectionItemDecoration
import com.graphitedocs.graphitedocs.utils.models.DocsList
import com.graphitedocs.graphitedocs.utils.models.DocsListItem
import kotlinx.android.synthetic.main.activity_docs_list.*
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DocsListActivity : GraphiteActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        loadData()
    }

    private val TAG = DocsListActivity::class.java.simpleName
    private var progressDialog : MaterialDialog? = null
    private val currentSelectedItems = ArrayList<DocsListItem>()
    private var showDeleteDocs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs_list)
        setSupportActionBar(toolbarMainActivity)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        rvDocs.layoutManager = LinearLayoutManager(this)

        progressDialog = MaterialDialog.Builder(this)
                .content("Loading documents")
                .progress(true, 0)
                .show()
    }

    override fun onLoaded() {

        newDocFab.setOnClickListener {
            val getOptions = GetFileOptions()
            val fileName = getString(R.string.documents_list)

            blockstackSession().getFile(fileName, getOptions, {content: Any ->
                // content can be a `String` or a `ByteArray`
                if (content !is ByteArray) Log.d(TAG, content.toString())

                runOnUiThread {
                    val date = SimpleDateFormat("MM/dd/yyyy").format(Date())
                    val id =  Date().time
                    val newDoc = DocsListItem("Untitled", ArrayList(), ArrayList(), userData().json["username"].toString(), id, date, date, "documents")
                    val docsList = if (content !is ByteArray) {
                        DocsList.parseJSON(content.toString())
                    } else {
                        DocsList(ArrayList())
                    }

                    docsList.value.add(newDoc)
                    sortArrayByUpdatedDate(docsList.value)

                    val putOptions = PutFileOptions()
                    val json = Gson().toJson(docsList)

                    blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->

                        runOnUiThread {
                            // Start new Activity with this doc
                            startActivity(DocsActivity.newIntent(baseContext, "Untitled", id, date))
                        }
                    })
                }
            })
        }

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.lightGrey, R.color.colorPrimaryDark)

        loadData()
    }

    private fun loadData () {

        val options = GetFileOptions()
        val fileName = getString(R.string.documents_list)

        blockstackSession().getFile(fileName, options, {content: Any ->
            // content can be a `String` or a `ByteArray`
            if (content !is ByteArray) Log.d(TAG, content.toString())

            runOnUiThread {
                val arrayList = if (content !is ByteArray) {
                    DocsList.parseJSON(content.toString()).value
                } else {
                    ArrayList()
                }

                if (content is ByteArray) {
                    val putOptions = PutFileOptions()
                    val json = Gson().toJson(arrayList)

                    blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->
                        Log.d(TAG, readURL)
                    })
                }

                if (arrayList.isEmpty()) {
                    emptyDocsListTextView.visibility = View.VISIBLE
                    rvDocs.visibility = View.GONE
                } else {
                    emptyDocsListTextView.visibility = View.GONE
                    rvDocs.visibility = View.VISIBLE

                    sortArrayByUpdatedDate(arrayList)

                    rvDocs.adapter = DocsListAdapter(this, arrayList, object : DocsListAdapter.OnItemCheckListener {
                        override fun onItemCheck(item: DocsListItem?) {
                            if (item != null) {
                                currentSelectedItems.add(item)
                                if (!showDeleteDocs) {
                                    // Set the toolbar to show a delete option for these documents
                                    showDeleteDocs = true
                                    invalidateOptionsMenu()
                                }
                            }
                        }

                        override fun onItemUncheck(item: DocsListItem?) {
                            currentSelectedItems.remove(item)
                            if (currentSelectedItems.size == 0) {
                                // Hide the delete and bring in original options
                                showDeleteDocs = false
                                invalidateOptionsMenu()
                            }
                        }
                    })

                    val sectionItemDecoration = RecyclerSectionItemDecoration(resources.getDimensionPixelSize(R.dimen.docs_list_header), true, getSectionCallback(arrayList))
                    if (rvDocs.itemDecorationCount > 0) {
                        rvDocs.removeItemDecorationAt(0)
                    }
                    rvDocs.addItemDecoration(sectionItemDecoration)
                }
                swipeRefresh.isRefreshing = false
                progressDialog!!.cancel()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.docslist_toolbar, menu)

        if (menu != null) {
            for (i in 0 until menu.size())
                menu.getItem(i).isVisible = showDeleteDocs
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            for (i in 0 until menu.size())
                menu.getItem(i).isVisible = showDeleteDocs
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_delete) {
            deleteDocs()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteDocs() {
        showDeleteDocs = false

        val getOptions = GetFileOptions()
        val fileName = getString(R.string.documents_list)

        blockstackSession().getFile(fileName, getOptions, {content: Any ->
            // content can be a `String` or a `ByteArray`
            if (content !is ByteArray) Log.d(TAG, content.toString())

            runOnUiThread {

                val docsList = if (content !is ByteArray) {
                    DocsList.parseJSON(content.toString())
                } else {
                    DocsList(ArrayList())
                }

                val set = currentSelectedItems.map { it.id }.toSet()


                docsList.value.forEach {
                    if (set.contains(it.id)) {
                        docsList.value.remove(it)
                    }
                }

                sortArrayByUpdatedDate(docsList.value)

                val putOptions = PutFileOptions()
                val json = Gson().toJson(docsList)

                blockstackSession().putFile(fileName, json, putOptions, {readURL: String ->

                    runOnUiThread {
                        currentSelectedItems.clear()
                        invalidateOptionsMenu()
                        loadData()
                    }
                })
            }
        })

    }

    private fun sortArrayByUpdatedDate(arrayList: ArrayList<DocsListItem>) {

        arrayList.sortWith(kotlin.Comparator { o1, o2 ->
            val a = o1.updated.substring(o1.updated.length - 4) + o1.updated
            val b = o2.updated.substring(o2.updated.length - 4) + o2.updated
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
                return position == 0 || convertToDate(list[position].updated) != convertToDate(list[position - 1].updated)
            }

            override fun getSectionHeader(position: Int): CharSequence {
                return convertToDate(list[position].updated)
            }
        }
    }
}
