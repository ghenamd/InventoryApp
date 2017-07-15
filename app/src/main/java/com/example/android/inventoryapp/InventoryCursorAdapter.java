package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Ghena on 11/07/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    private TextView productQuantity;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        TextView productName = (TextView) view.findViewById(R.id.product_name);
        TextView productPrice = (TextView) view.findViewById(R.id.price);
        TextView productSupplier = (TextView) view.findViewById(R.id.supplier);
        productQuantity = (TextView) view.findViewById(R.id.quantity);
        ImageView productImage = (ImageView) view.findViewById(R.id.product_image);
        int productIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        final int productID = cursor.getInt(productIndex);

        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
        String supplier = cursor.getString(cursor.getColumnIndexOrThrow("supplier"));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow("quantity"));
        Uri image = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("picture")));

        Button sale = (Button) view.findViewById(R.id.sale);
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                productQuantity = (TextView) view.findViewById(R.id.quantity);
                int quantity = Integer.parseInt(productQuantity.getText().toString());
                if (quantity == 0) {
                    return;
                } else {
                    quantity = quantity - 1;
                }
                Log.v("Quantity ", String.valueOf(quantity));
                Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, productID);
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                context.getContentResolver().update(uri, values, null, null);
                productQuantity.setText(String.valueOf(quantity));


            }
        });
        // If there is no details about  name price, quantity or supplier we set them as Unknown
        if (TextUtils.isEmpty(name)) {
            productName.setText(R.string.unknown_name);
        } else {
            productName.setText("Name: " + name);
        }

        if (TextUtils.isEmpty(price)) {
            productPrice.setText(R.string.unknown_price);
        } else {
            productPrice.setText("Price: " + price);
        }

        if (TextUtils.isEmpty(supplier)) {
            productSupplier.setText(R.string.unknown_supplier);
        } else {
            productSupplier.setText("Supplier: " + supplier);
        }

        if (TextUtils.isEmpty(quantity)) {
            productQuantity.setText(R.string.unknown_quantity);
        } else {
            productQuantity.setText(quantity);
        }

        productImage.setImageURI(image);
        Picasso.with(context)
                .load(image)
                .placeholder(R.drawable.placeholder)// This is the image holder if the image is missing
                .fit()
                .into(productImage);
    }
}
