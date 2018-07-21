package com.graphitedocs.graphitedocs.utils.models

import com.google.gson.GsonBuilder

class DocsList (var value : ArrayList<DocsListItem>) {

    companion object {
        fun parseJSON(response : String) :  DocsList {
            val DATE_FORMAT = "MM/dd/yyyy"
            val gsonBuilder = GsonBuilder()

            gsonBuilder.setDateFormat(DATE_FORMAT)

            val gson = gsonBuilder.create()
            val docsList = gson.fromJson(response, DocsList::class.java)

            return docsList
        }
    }
}