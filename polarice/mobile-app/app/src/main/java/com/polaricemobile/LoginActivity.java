package com.polaricemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailField, passwordField; private MaterialButton loginButton; private ProgressBar loadingIndicator; private ExecutorService executor; private SharedPreferences preferences;
    @Override protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_login); initializeViews(); executor = Executors.newSingleThreadExecutor(); preferences = getSharedPreferences("polar_ice_prefs", MODE_PRIVATE); checkExistingLogin(); loginButton.setOnClickListener(this::handleLogin); }
    private void initializeViews() { emailField = findViewById(R.id.emailField); passwordField = findViewById(R.id.passwordField); loginButton = findViewById(R.id.loginButton); loadingIndicator = findViewById(R.id.loadingIndicator); }
    private void checkExistingLogin() { String accessToken = preferences.getString("access_token", null); String userId = preferences.getString("user_id", null); if (accessToken != null && userId != null) { goToDeliveriesActivity(); } }
    private void handleLogin(View view) { String email = emailField.getText().toString().trim(); String password = passwordField.getText().toString().trim(); if (email.isEmpty()) { emailField.setError(getString(R.string.email_hint)); emailField.requestFocus(); return; } if (password.isEmpty()) { passwordField.setError(getString(R.string.password_hint)); passwordField.requestFocus(); return; } setLoadingState(true); executor.execute(() -> { try { SupabaseConfig.AuthResponse response = SupabaseConfig.signIn(email, password); runOnUiThread(() -> { setLoadingState(false); if (response.error != null) { showErrorDialog(getString(R.string.login_error), response.error_description != null ? response.error_description : getString(R.string.invalid_credentials)); } else if (response.user != null && response.access_token != null) { saveUserData(response); goToDeliveriesActivity(); } else { showErrorDialog(getString(R.string.login_error), getString(R.string.invalid_credentials)); } }); } catch (Exception e) { runOnUiThread(() -> { setLoadingState(false); showErrorDialog(getString(R.string.error), getString(R.string.network_error)); }); } }); }
    private void setLoadingState(boolean loading) { loginButton.setEnabled(!loading); loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE); emailField.setEnabled(!loading); passwordField.setEnabled(!loading); }
    private void saveUserData(SupabaseConfig.AuthResponse response) { SharedPreferences.Editor editor = preferences.edit(); editor.putString("access_token", response.access_token); editor.putString("refresh_token", response.refresh_token); editor.putString("user_id", response.user.id); editor.putString("user_email", response.user.email); editor.apply(); }
    private void goToDeliveriesActivity() { Intent intent = new Intent(this, DeliveriesActivity.class); startActivity(intent); finish(); }
    private void showErrorDialog(String title, String message) { new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton(getString(R.string.ok), null).show(); }
    @Override protected void onDestroy() { super.onDestroy(); if (executor != null) { executor.shutdown(); } }
}