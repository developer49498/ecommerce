package com.example.zappycode.project_2_quotation;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ZenQuoteClient {
    private static final String BASE_URL = "https://zenquotes.io/api/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}


