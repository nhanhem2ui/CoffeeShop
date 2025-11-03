package com.example.coffeeshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.coffeeshop.R;
import com.example.coffeeshop.models.CartItem;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveClick(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getProduct().getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getProduct().getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", item.getSubtotal()));

        // Load image
        String imageUrl = item.getProduct().getImageUrl();
        if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.coffee_default)
                    .error(R.drawable.coffee_default)
                    .centerCrop()
                    .into(holder.ivProduct);
        } else {
            int imageResource = getImageResource(imageUrl);
            holder.ivProduct.setImageResource(imageResource);
        }

        // FIXED: Use final reference to current item for click listeners
        final CartItem currentItem = item;

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChanged(currentItem, currentItem.getQuantity() + 1);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null && currentItem.getQuantity() > 1) {
                listener.onQuantityChanged(currentItem, currentItem.getQuantity() - 1);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    @Override
    public long getItemId(int position) {
        return cartItems.get(position).getCartId();
    }

    // Remove getItemViewType override - not needed for stable IDs

    private int getImageResource(String imageName) {
        switch (imageName != null ? imageName.toLowerCase() : "") {
            case "espresso":
                return R.drawable.espresso;
            case "cappuccino":
                return R.drawable.cappuccino;
            case "latte":
                return R.drawable.latte;
            case "americano":
                return R.drawable.americano;
            case "mocha":
                return R.drawable.mocha;
            case "macchiato":
                return R.drawable.macchiato;
            case "coldbrew":
                return R.drawable.coldbrew;
            case "flatwhite":
                return R.drawable.flatwhite;
            default:
                return R.drawable.coffee_default;
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvSubtotal;
        ImageButton btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_cart_product);
            tvName = itemView.findViewById(R.id.tv_cart_product_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_cart_subtotal);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}