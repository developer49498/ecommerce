package com.example.zappycode.project_2_quotation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.zappycode.project_2_quotation.Quote;
import com.example.zappycode.project_2_quotation.R;
import com.example.zappycode.project_2_quotation.UnsplashApi;
import com.example.zappycode.project_2_quotation.UnsplashClient;
import com.example.zappycode.project_2_quotation.UnsplashResponse;
import com.example.zappycode.project_2_quotation.UnsplashResult;
import com.example.zappycode.project_2_quotation.ZenQuoteAPI;
import com.example.zappycode.project_2_quotation.ZenQuoteClient;
import com.google.firebase.FirebaseApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView quoteTextView, authorTextView;
    private ImageView backgroundImageView;
    private Button refresh_btn;
    private Button share_btn;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI components
        quoteTextView = findViewById(R.id.quoteTxt);
        authorTextView = findViewById(R.id.autherTxt);
        backgroundImageView = findViewById(R.id.imageView);
        refresh_btn = findViewById(R.id.refresh);
        share_btn = findViewById(R.id.share_btn);

        // Set button click listeners
        refresh_btn.setOnClickListener(v -> fetchRandomQuote());
        share_btn.setOnClickListener(v -> createAndShareCard());

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initial fetch of quote
        fetchRandomQuote();
    }

    private void fetchRandomQuote() {
        // Call ZenQuote API
        ZenQuoteAPI zenQuoteApi = ZenQuoteClient.getRetrofitInstance().create(ZenQuoteAPI.class);
        Call<List<Quote>> call = zenQuoteApi.getRandomQuote();

        call.enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get the first quote from the response
                    Quote quote = response.body().get(0);
                    String quoteText = quote.getQuote();
                    String authorText = quote.getAuthor();

                    // Display the quote and author
                    quoteTextView.setText("\"" + quoteText + "\"");
                    authorTextView.setText("- " + authorText);

                    // Fetch image based on the quote text
                    fetchImageForQuote(quoteText);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch quote", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchImageForQuote(String quoteText) {
        // Call Unsplash API
        UnsplashApi unsplashApi = UnsplashClient.getClient().create(UnsplashApi.class);
        Call<UnsplashResponse> call = unsplashApi.searchPhotos(quoteText, "itpPsTeImSaHBAV7yHEJop6vtXg44knHblbOFrc8PMg");

        call.enqueue(new Callback<UnsplashResponse>() {
            @Override
            public void onResponse(Call<UnsplashResponse> call, Response<UnsplashResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UnsplashResult> results = response.body().getResults();
                    if (!results.isEmpty()) {
                        // Get the first image URL
                        String imageUrl = results.get(0).getUrls().getRegular();

                        // Load the image into the ImageView using Glide
                        updateImageView(imageUrl);
                    } else {
                        Toast.makeText(MainActivity.this, "No images found for this quote", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UnsplashResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageView(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .into(backgroundImageView);
    }

    private void createAndShareCard() {
        // Start the AsyncTask to create and share the image
        new CreateImageTask().execute();
    }

    private class CreateImageTask extends AsyncTask<Void, Void, Uri> {
        @Override
        protected Uri doInBackground(Void... params) {
            // Inflate the share card layout in the background
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View cardView = inflater.inflate(R.layout.card_layout, null);

            // Set the quote and author dynamically
            TextView quoteText = cardView.findViewById(R.id.quoteText);
            TextView authorText = cardView.findViewById(R.id.authorText);
            ImageView cardImage = cardView.findViewById(R.id.cardImage);

            // Set the quote and author from MainActivity TextViews
            quoteText.setText(quoteTextView.getText().toString());
            authorText.setText("~By "+authorTextView.getText().toString());

            // Set the image directly from the ImageView in MainActivity
            cardImage.setImageDrawable(backgroundImageView.getDrawable());

            // Now, create the bitmap in the background thread
            cardView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            cardView.layout(0, 0, cardView.getMeasuredWidth(), cardView.getMeasuredHeight());
            Bitmap bitmap = Bitmap.createBitmap(cardView.getMeasuredWidth(), cardView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cardView.draw(canvas);

            // Save the bitmap to the MediaStore for API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "quote_card.png");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return imageUri; // Return the Uri of the saved image
            } else {
                // For API levels below 29, save to the app's external storage directory
                File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "quote_card.png");
                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    return FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(Uri imageUri) {
            if (imageUri != null) {
                // Share the image
                shareImage(imageUri);
            } else {
                Toast.makeText(MainActivity.this, "Failed to create image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareImage(Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}
