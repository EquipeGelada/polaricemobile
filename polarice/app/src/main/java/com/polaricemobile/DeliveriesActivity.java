package com.polaricemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeliveriesActivity extends AppCompatActivity implements DeliveriesAdapter.OnDeliveryCompleteListener {
    
    private MaterialToolbar toolbar;
    private MaterialButton refreshButton;
    private MaterialButton logoutButton;
    private RecyclerView deliveriesRecycler;
    private LinearLayout emptyStateLayout;
    private FrameLayout loadingOverlay;
    
    private DeliveriesAdapter adapter;
    private List<SupabaseConfig.Delivery> deliveries;
    private ExecutorService executor;
    private SharedPreferences preferences;
    
    private String accessToken;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries);
        
        // Inicializar componentes
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        
        executor = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("polar_ice_prefs", MODE_PRIVATE);
        
        // Recuperar dados do usuário
        getUserData();
        
        // Configurar listeners
        refreshButton.setOnClickListener(v -> loadDeliveries());
        logoutButton.setOnClickListener(v -> handleLogout());
        
        // Carregar entregas inicial
        loadDeliveries();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        refreshButton = findViewById(R.id.refreshButton);
        logoutButton = findViewById(R.id.logoutButton);
        deliveriesRecycler = findViewById(R.id.deliveriesRecycler);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.deliveries_title));
        }
    }
    
    private void setupRecyclerView() {
        deliveries = new ArrayList<>();
        adapter = new DeliveriesAdapter(deliveries, this);
        deliveriesRecycler.setLayoutManager(new LinearLayoutManager(this));
        deliveriesRecycler.setAdapter(adapter);
    }
    
    private void getUserData() {
        accessToken = preferences.getString("access_token", null);
        userId = preferences.getString("user_id", null);
        
        if (accessToken == null || userId == null) {
            // Não está logado, voltar para login
            goToLogin();
        }
    }
    
    private void loadDeliveries() {
        setLoadingState(true);
        
        executor.execute(() -> {
            try {
                SupabaseConfig.Delivery[] deliveriesArray = SupabaseConfig.getPendingDeliveries(userId, accessToken);
                List<SupabaseConfig.Delivery> deliveriesList = Arrays.asList(deliveriesArray);
                
                runOnUiThread(() -> {
                    setLoadingState(false);
                    updateDeliveriesList(deliveriesList);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showErrorDialog(getString(R.string.error), getString(R.string.network_error));
                });
            }
        });
    }
    
    private void updateDeliveriesList(List<SupabaseConfig.Delivery> newDeliveries) {
        deliveries.clear();
        deliveries.addAll(newDeliveries);
        adapter.notifyDataSetChanged();
        
        // Mostrar/esconder empty state
        if (deliveries.isEmpty()) {
            deliveriesRecycler.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            deliveriesRecycler.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onDeliveryComplete(SupabaseConfig.Delivery delivery, int position) {
        // Mostrar diálogo de confirmação
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_complete))
                .setMessage(getString(R.string.confirm_complete_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    completeDelivery(delivery, position);
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
    
    private void completeDelivery(SupabaseConfig.Delivery delivery, int position) {
        setLoadingState(true);
        
        executor.execute(() -> {
            try {
                boolean success = SupabaseConfig.completeDelivery(delivery.id, accessToken);
                
                runOnUiThread(() -> {
                    setLoadingState(false);
                    
                    if (success) {
                        // Remover da lista
                        adapter.removeDelivery(position);
                        
                        // Mostrar mensagem de sucesso
                        Toast.makeText(this, getString(R.string.delivery_completed), Toast.LENGTH_SHORT).show();
                        
                        // Atualizar empty state se necessário
                        if (deliveries.isEmpty()) {
                            deliveriesRecycler.setVisibility(View.GONE);
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showErrorDialog(getString(R.string.error), "Não foi possível completar a entrega. Tente novamente.");
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
    
    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage("Tem certeza que deseja sair?")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
    
    private void performLogout() {
        setLoadingState(true);
        
        executor.execute(() -> {
            try {
                // Tentar fazer logout no Supabase
                SupabaseConfig.signOut(accessToken);
            } catch (Exception e) {
                // Ignorar erros de logout remoto
            }
            
            runOnUiThread(() -> {
                // Limpar dados locais
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                
                // Ir para tela de login
                goToLogin();
            });
        });
    }
    
    private void setLoadingState(boolean loading) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        refreshButton.setEnabled(!loading);
        logoutButton.setEnabled(!loading);
    }
    
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar entregas quando voltar para a tela
        loadDeliveries();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}