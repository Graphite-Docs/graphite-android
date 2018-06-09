package com.graphitedocs.graphitedocs.docs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Adapter
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import com.graphitedocs.graphitedocs.utils.adapters.DocsListAdapter
import com.graphitedocs.graphitedocs.utils.adapters.RecyclerSectionItemDecoration
import com.graphitedocs.graphitedocs.utils.models.DocsListItem
import kotlinx.android.synthetic.main.activity_docs_main.*
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import java.net.URL

class DocsListActivity : GraphiteActivity() {

    private var adapter: Adapter? = null
    private val TAG = DocsListActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs_main)

        rvDocs.layoutManager = LinearLayoutManager(this)
        // Get data
        rvDocs.adapter = DocsListAdapter(this, getData())

        val sectionItemDecoration = RecyclerSectionItemDecoration(resources.getDimensionPixelSize(R.dimen.docs_list_header), true, getSectionCallback(getData()))
        rvDocs.addItemDecoration(sectionItemDecoration)
    }

    private fun getData () : ArrayList<DocsListItem> {

//        val options = PutFileOptions()
//        blockstackSession().putFile(getString(R.string.documents_list), "Hello Daniel!", options,
//                {readURL: String ->
//                    Log.d(TAG, "File stored at: ${readURL}")
//                    runOnUiThread {
//                        Log.d("Put file", "File stored at: ${readURL}")
//                    }
//                })

        val options = PutFileOptions()
        blockstackSession().putFile(getString(R.string.documents_list), "Hello Daniel!", options,
                {readURL: String ->
                    Log.d(TAG, "File stored at: ${readURL}")
                    runOnUiThread {
                    }
                })

//        val options = GetFileOptions()
//
//        val fileName = getString(R.string.documents_list)
//
//        blockstackSession().getFile(fileName, options, {content: Any ->
//            // content can be a `String` or a `ByteArray`
//            Log.d(TAG, content.toString())
//
//            // do something with `content`
//        })

        // Create mock data for now
        var doc1 = DocsListItem("Daniel's Story", "May 9, 2018", listOf("danielwang.id"), listOf(""))
        var doc2 = DocsListItem("Justin's Story", "April 1, 2018", listOf("justinhunter.id"), listOf("Story"))
        var doc3 = DocsListItem("The Great Gatsby", "March 22, 2018", listOf("graphitetest.id"), listOf("A long story"))
        var arrayList = arrayListOf(doc1, doc1, doc1, doc1, doc1, doc2, doc2, doc2, doc3, doc3, doc3, doc3, doc3)

        return arrayList
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
