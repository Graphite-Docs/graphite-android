package com.graphitedocs.graphitedocs

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.graphitedocs.graphitedocs.docs.DocsMainActivity
import kotlinx.android.synthetic.main.activity_select_app.*

class SelectAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_app)

        documentsContainer.setOnClickListener {
            val intent = Intent(this, DocsMainActivity::class.java)
            startActivity(intent)
        }

        contactsContainer.setOnClickListener {

        }

        vaultContainer.setOnClickListener {

        }
    }
}
