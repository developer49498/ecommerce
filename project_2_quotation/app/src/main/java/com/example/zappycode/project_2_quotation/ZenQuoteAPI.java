package com.example.zappycode.project_2_quotation;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface ZenQuoteAPI {
    @GET("random")
    Call<List<Quote>> getRandomQuote();
}