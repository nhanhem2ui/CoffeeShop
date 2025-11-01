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
import com.example.coffeeshop.R;
import com.example.coffeeshop.models.Product;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvDescription.setText(product.getDescription());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

        // Set image based on product name/imageUrl
        int imageResource = getImageResource(product.getImageUrl());
        holder.ivProduct.setImageResource(imageResource);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    private int getImageResource(String imageName) {
        // Map image names to drawable resources
        // You'll need to add coffee images to res/drawable
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvDescription, tvPrice;
        ImageButton btnEdit, btnDelete, btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvDescription = itemView.findViewById(R.id.tv_product_description);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}