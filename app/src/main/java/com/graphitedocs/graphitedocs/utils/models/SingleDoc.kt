package com.graphitedocs.graphitedocs.utils.models

import com.google.gson.GsonBuilder

class SingleDoc(var title: String,
                var date: String,
                var sharedWith: List<String>?,
                var tags: List<String>?,
                var author: String,
                var id: Long,
                var updated: String?,
                var created: String?,
                var content: String) {

    companion object {
        fun parseJSON(response: String): SingleDoc {
            val DATE_FORMAT = "MM/dd/yyyy"

            val gsonBuilder = GsonBuilder()
            gsonBuilder.setDateFormat(DATE_FORMAT)
            val gson = gsonBuilder.create()

            var singleDoc: SingleDoc? = gson.fromJson(response, SingleDoc::class.java)

            if (singleDoc == null) {
                singleDoc = SingleDoc("Untitled", "", null, null, "", 0, "", "", "" )
            }
            return singleDoc
        }
    }
}
