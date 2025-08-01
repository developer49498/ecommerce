package com.example.zappycode.project_2_quotation;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Quote {
    private String q; // The quote text
    private String a; // The author name

    public String getQuote() {
        return q;
    }

    public String getAuthor() {
        return a;
    }
}
