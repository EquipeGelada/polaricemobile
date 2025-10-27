package com.polaricemobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder> {
    
    private List<SupabaseConfig.Delivery> deliveries;
    private OnDeliveryCompleteListener listener;
    
    public interface OnDeliveryCompleteListener {
        void onDeliveryComplete(SupabaseConfig.Delivery delivery, int position);
    }
    
    public DeliveriesAdapter(List<SupabaseConfig.Delivery> deliveries, OnDeliveryCompleteListener listener) {
        this.deliveries = deliveries;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        SupabaseConfig.Delivery delivery = deliveries.get(position);
        holder.bind(delivery, position);
    }
    
    @Override
    public int getItemCount() {
        return deliveries.size();
    }
    
    public void updateDeliveries(List<SupabaseConfig.Delivery> newDeliveries) {
        this.deliveries = newDeliveries;
        notifyDataSetChanged();
    }
    
    public void removeDelivery(int position) {
        if (position >= 0 && position < deliveries.size()) {
            deliveries.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, deliveries.size());
        }
    }
    
    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private TextView clientNameText;
        private TextView addressText;
        private MaterialButton completeButton;
        
        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            addressText = itemView.findViewById(R.id.addressText);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
        
        public void bind(SupabaseConfig.Delivery delivery, int position) {
            // Definir textos
            clientNameText.setText(delivery.cliente_nome != null ? 
                delivery.cliente_nome : "Cliente #" + delivery.cliente_id);
            addressText.setText(delivery.endereco_entrega != null ? 
                delivery.endereco_entrega : "Endereço não informado");
            
            // Configurar botão de completar
            completeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeliveryComplete(delivery, position);
                }
            });
        }
    }
}