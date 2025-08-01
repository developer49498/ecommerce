package com.example.zappycode.project_2_quotation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private TextView resendTextView;
    private ProgressBar progressBar;
    private String verificationId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Bind views
        otpEditText = findViewById(R.id.otp_edittext);
        verifyButton = findViewById(R.id.verify_otp_button);
        resendTextView = findViewById(R.id.resend_otp_text);
        progressBar = findViewById(R.id.progressBar);

        // Get verification ID from Intent
        verificationId = getIntent().getStringExtra("verificationId");

        // Verify OTP button listener
        verifyButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();
            if (otp.length() == 6) {
                verifyOtp(otp);
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Resend OTP listener
        resendTextView.setOnClickListener(v -> {
            String phoneNumber = "+91"+getIntent().getStringExtra("phoneNumber");
            if (phoneNumber != null) {
                resendOtp(phoneNumber);
            }
        });
    }

    private void verifyOtp(String otp) {
        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(OtpVerificationActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity
                        Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOtp(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Auto-retrieval or instant verification
                                auth.signInWithCredential(credential)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newVerificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                Toast.makeText(OtpVerificationActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
                                verificationId = newVerificationId;
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}