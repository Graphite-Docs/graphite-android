package com.graphitedocs.graphitedocs.utils.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class DocsList {

    public List<DocsListItem> docsList;

    public DocsList() {
        docsList = new ArrayList<>();
    }

    public static DocsList parseJSON(String response) {
        String DATE_FORMAT = "MM/dd/yyyy";

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(DATE_FORMAT);
        Gson gson = gsonBuilder.create();

        DocsList docsListResponse = gson.fromJson(response, DocsList.class);

        if (docsListResponse == null) {
            docsListResponse = new DocsList();
        }
        return docsListResponse;
    }
}
