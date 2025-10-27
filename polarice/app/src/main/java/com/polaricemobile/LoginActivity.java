package com.polaricemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private MaterialButton loginButton;
    private ProgressBar loadingIndicator;
    private ExecutorService executor;
    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Inicializar componentes
        initializeViews();
        executor = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("polar_ice_prefs", MODE_PRIVATE);
        
        // Verificar se já está logado
        checkExistingLogin();
        
        // Configurar listener do botão de login
        loginButton.setOnClickListener(this::handleLogin);
    }
    
    private void initializeViews() {
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }
    
    private void checkExistingLogin() {
        String accessToken = preferences.getString("access_token", null);
        String userId = preferences.getString("user_id", null);
        
        if (accessToken != null && userId != null) {
            // Já está logado, ir para tela de entregas
            goToDeliveriesActivity();
        }
    }
    
    private void handleLogin(View view) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        
        // Validar campos
        if (email.isEmpty()) {
            emailField.setError(getString(R.string.email_hint));
            emailField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            passwordField.setError(getString(R.string.password_hint));
            passwordField.requestFocus();
            return;
        }
        
        // Mostrar loading
        setLoadingState(true);
        
        // Executar login em background thread
        executor.execute(() -> {
            try {
                SupabaseConfig.AuthResponse response = SupabaseConfig.signIn(email, password);
                
                runOnUiThread(() -> {
                    setLoadingState(false);
                    
                    if (response.error != null) {
                        // Erro na autenticação
                        showErrorDialog(getString(R.string.login_error), response.error_description != null ? 
                            response.error_description : getString(R.string.invalid_credentials));
                    } else if (response.user != null && response.access_token != null) {
                        // Login bem-sucedido
                        saveUserData(response);
                        goToDeliveriesActivity();
                    } else {
                        // Resposta inesperada
                        showErrorDialog(getString(R.string.login_error), getString(R.string.invalid_credentials));
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showErrorDialog(getString(R.string.error), getString(R.string.network_error));
                });
            }
        });
    }
    
    private void setLoadingState(boolean loading) {
        loginButton.setEnabled(!loading);
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        emailField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
    }
    
    private void saveUserData(SupabaseConfig.AuthResponse response) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("access_token", response.access_token);
        editor.putString("refresh_token", response.refresh_token);
        editor.putString("user_id", response.user.id);
        editor.putString("user_email", response.user.email);
        editor.apply();
    }
    
    private void goToDeliveriesActivity() {
        Intent intent = new Intent(this, DeliveriesActivity.class);
        startActivity(intent);
        finish(); // Remover LoginActivity do stack
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}