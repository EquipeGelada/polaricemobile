package com.equipegelada.polaricemobile;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class DeliveriesActivity extends AppCompatActivity {
    ListView deliveriesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries);
        deliveriesList = findViewById(R.id.deliveriesList);
        // Exemplo simples. Substitua por dados da API de entregas do backend.
        String[] dummyDeliveries = {"Entrega 001 - Cliente A", "Entrega 002 - Cliente B"};
        deliveriesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dummyDeliveries));
    }
}
