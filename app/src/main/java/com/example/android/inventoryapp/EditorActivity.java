package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ghena on 05/07/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int mQuantity;
    private EditText mProductName;
    private EditText mPrice;
    private EditText mSupplier;
    private EditText quantity;
    private ImageView mProductImage;
    private Button mPictureButton;
    private Uri mUri;
    private Uri mCurrentProductUri;
    private static final String STATE_URI = "STATE_URI";
    private static final int SEND_MAIL_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int INVENTORY_LOADER = 0;
    private boolean mInventoryHasChanged = false;
    private boolean infoFilled = true;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        final Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle("Add a Product");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Product");
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }
        quantity = (EditText) findViewById(R.id.product_quantity_editor);
        mProductName = (EditText) findViewById(R.id.product_name_editor);
        mProductName.setOnTouchListener(mTouchListener);
        mPrice = (EditText) findViewById(R.id.product_price_editor);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplier = (EditText) findViewById(R.id.product_supplier_editor);
        mSupplier.setOnTouchListener(mTouchListener);
        mProductImage = (ImageView) findViewById(R.id.product_image_editor);
        mPictureButton = (Button) findViewById(R.id.new_picture_button);
        mPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to pick up a new image
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select your picture"
                ), PICK_IMAGE_REQUEST);
            }
        });

        final Button decrement = (Button) findViewById(R.id.decrement);
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });
        Button increment = (Button) findViewById(R.id.increment);
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });
        Button order = (Button) findViewById(R.id.order);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Product name: ");
                emailIntent.putExtra(Intent.EXTRA_TEXT, " Dear ");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });
    }

    private void decrement() {
        String quantityEdit = quantity.getText().toString().trim();
        if (quantityEdit.isEmpty()) {
            quantityEdit = "0";
        }
        mQuantity = Integer.parseInt(quantityEdit);
        mQuantity -= 1;
        if (mQuantity <= 0) {
            Toast.makeText(EditorActivity.this, R.string.quantity_zero, Toast.LENGTH_SHORT).show();
            mQuantity = 0;
        }
        quantity.setText(String.valueOf(mQuantity));
    }

    private void increment() {

        String quantityEdit = quantity.getText().toString().trim();
        if (quantityEdit.isEmpty()) {
            quantityEdit = "0";
        }
        mQuantity = Integer.parseInt(quantityEdit);
        mQuantity += 1;
        Log.v("This is QUANTITY:", String.valueOf(mQuantity));
        quantity.setText(String.valueOf(mQuantity));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUri != null)
            outState.putString(STATE_URI, mUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            ViewTreeObserver viewTreeObserver = mProductImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mProductImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mProductImage.setImageBitmap(getBitmapFromUri(mUri));
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            if (resultData != null) {
                mUri = resultData.getData();
                mProductImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;
        // Get the dimensions of the View
        int targetW = mProductImage.getWidth();
        int targetH = mProductImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {

            return null;
        } catch (Exception e) {

            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void saveProduct() {
        String productName = mProductName.getText().toString().trim();
        String productPrice = mPrice.getText().toString().trim();
        String productSupplier = mSupplier.getText().toString().trim();
        String productQuantity = quantity.getText().toString().trim();
        ContentValues values = new ContentValues();

        //Check if there was a name entered if not show a Toast message
        if (TextUtils.isEmpty(productName) || (TextUtils.isEmpty(productPrice)) || (TextUtils.isEmpty(productSupplier)) ||
                (TextUtils.isEmpty(productQuantity)) || (mUri == null || TextUtils.isEmpty(mUri.toString()))) {
            infoFilled = false;
            Toast.makeText(EditorActivity.this, R.string.missing_info, Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, productName);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(productPrice));
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER, productSupplier);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(productQuantity));
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE, mUri.toString());
            infoFilled = true;
        }

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.error_saving_product, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
            }
        } else

        {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.update_failed,
                        Toast.LENGTH_SHORT).show();
            } else {// Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.update_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        //Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int mRowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (mRowsDeleted == 0) {
                Toast.makeText(this, R.string.error_deleting_product, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save a new product in the database
                saveProduct();
                if (infoFilled) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            mUri = Uri.parse(cursor.getString(pictureColumnIndex));

            mProductName.setText(name);
            mPrice.setText(price);
            mSupplier.setText(supplier);
            quantity.setText(Integer.toString(mQuantity));
            mProductImage.setImageURI(mUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mPrice.setText("");
        mSupplier.setText("");
        quantity.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_quit_dialog);
        builder.setPositiveButton(R.string.discard_dialog, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
