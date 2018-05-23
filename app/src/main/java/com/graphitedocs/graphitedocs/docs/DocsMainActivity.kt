package com.graphitedocs.graphitedocs.docs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Adapter
import com.graphitedocs.graphitedocs.R
import com.graphitedocs.graphitedocs.utils.GraphiteActivity
import kotlinx.android.synthetic.main.activity_docs_main.*

class DocsMainActivity : GraphiteActivity() {

    private var adapter: Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs_main)

        rvDocs.layoutManager = LinearLayoutManager(this);

        // Get data
//        rvDocs.adapter = Adapter(this.getData());

    }

    private fun getData () {

    }
}
