package com.example.zappycode.project_2_quotation;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class UnsplashClient {
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.unsplash.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
