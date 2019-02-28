package com.example.shoppinglisthq;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    ShoppingMemoDataSource dataSource;
    private boolean isButtonCklick = true;
    ListView mShoppingMemosListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ShoppingMemo testMemo = new ShoppingMemo("Birnen", 5, 100);
//        Log.d(TAG, "onCreate: TestMemo: " + testMemo.toString());

        dataSource = new ShoppingMemoDataSource(this);


        initializeShoppingMemoListView();

        activateAddButton();
        initializeContectualActionBar();
//        Log.d(TAG, "onCreate: Datenquelle wird geöffnet");
//        dataSource.open();

//        ShoppingMemo shoppingMemo = dataSource.createShoppingMemo("Testprodukt", 3);
//        Log.d(TAG, "onCreate: Es wurde folgender EIntrag in die Datenbank geschrieben: ");
//        Log.d(TAG, "onCreate: ID: " + shoppingMemo.getId() + " ,Inhalt: " + shoppingMemo.toString());
//
//        showAllListEntries();

//        Log.d(TAG, "onCreate: Datenquelle wird geschlossen.");
//        dataSource.close();
    }

    private void initializeShoppingMemoListView() {
        List<ShoppingMemo> emtyListForInit = new ArrayList<>();
        mShoppingMemosListView = findViewById(R.id.lv_shopping_memo);
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter =
                new ArrayAdapter<ShoppingMemo>(this, android.R.layout.simple_list_item_multiple_choice, emtyListForInit) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        //TODO
//                        CheckedTextView checkedTextView = (CheckedTextView) view;
//                        Drawable drawable = checkedTextView.getCheckMarkDrawable();
//                        drawable.setVisible(false, false);

                        ShoppingMemo memo = (ShoppingMemo) mShoppingMemosListView.getItemAtPosition(position);
                        if (memo.isChecked()) {
                            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            textView.setTextColor(Color.rgb(175, 175, 175));
                        } else {
                            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            textView.setTextColor(Color.GRAY);
                        }
                        return view;
                    }
                };
        mShoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);

        //mit lambda
        mShoppingMemosListView.setOnItemClickListener((parent, view, position, id) -> {
            ShoppingMemo memo = (ShoppingMemo) parent.getItemAtPosition(position);
            ShoppingMemo updatedMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
            Log.d(TAG, "initializeShoppingMemoListView: Checked-Status von Eintrag: " + updatedMemo.toString() + " ist: " + updatedMemo.isChecked());
            showAllListEntries();
        });

//      the same with anonymos
//        mShoppingMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                ShoppingMemo memo = (ShoppingMemo) adapterView.getItemAtPosition(position);
//
//                // Hier den checked-Wert des Memo-Objekts umkehren, bspw. von true auf false
//                // Dann ListView neu zeichnen mit showAllListEntries()
//                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
//                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedShoppingMemo.toString() + " ist: " + updatedShoppingMemo.isChecked());
//                showAllListEntries();
//            }
//        });
    }

    private void activateAddButton() {
        final EditText editTextQuantity = findViewById(R.id.editText_quantity);
        final EditText editTextProduct = findViewById(R.id.editText_product);
        Button buttonAddProduct = findViewById(R.id.button_add_product);
        buttonAddProduct.setOnClickListener(v -> {
            String quantityString = editTextQuantity.getText().toString();
            String product = editTextProduct.getText().toString();
            if (TextUtils.isEmpty(quantityString)) {
                editTextQuantity.setError(getString(R.string.editText_errorMessage));
                return;
            }
            if (TextUtils.isEmpty(product)) {
                editTextProduct.setError(getString(R.string.editText_errorMessage));
                return;
            }

            int quantity = Integer.parseInt(quantityString);
            editTextProduct.setText("");
            editTextQuantity.setText("");

            dataSource.createShoppingMemo(product, quantity);

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null && isButtonCklick) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            showAllListEntries();
        });

        editTextProduct.setOnEditorActionListener((textView, pos, keyEvent) -> {
            isButtonCklick = false;
            buttonAddProduct.performClick();
            editTextQuantity.requestFocus();
            isButtonCklick = true;
            return true;

        });
    }

    private void initializeContectualActionBar() {
        final ListView shoppingMemoListView = findViewById(R.id.lv_shopping_memo);
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }

                String cabTitel = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitel);
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedShoppingMemoPosition = shoppingMemoListView.getCheckedItemPositions();
                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedShoppingMemoPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemoPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemoPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);
                                Log.d(TAG, "onActionItemClicked: Position im ListView: " + positionInListView);
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }
                        showAllListEntries();
                        mode.finish();
                        break;
                    case R.id.cab_change:
                        for (int i = 0; i < touchedShoppingMemoPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemoPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemoPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);
                                Log.d(TAG, "onActionItemClicked: Position im ListView: " + positionInListView);
                                AlertDialog editShoppingMemoDialog = createShoppingMemoDialog(shoppingMemo);
                                editShoppingMemoDialog.show();
//                                View forKeyboard = editShoppingMemoDialog.getCurrentFocus();
//                                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                                imm.showSoftInput(getCurrentFocus(),InputMethodManager.SHOW_FORCED);
                            }
                        }
                        mode.finish();
                        break;
                    default:
                        returnValue = false;
                }

                return returnValue;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;

            }
        });
    }

    private AlertDialog createShoppingMemoDialog(final ShoppingMemo shoppingMemo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_shopping_memo, null);

        final EditText editTextNewQauntity = dialogView.findViewById(R.id.editText_new_quantity);
        editTextNewQauntity.setText(String.valueOf(shoppingMemo.getQuantity()));

        final EditText editTextNewProduct = dialogView.findViewById(R.id.editText_new_product);
        editTextNewProduct.setText(shoppingMemo.getProduct());

        builder.setView(dialogView)
                .setTitle(R.string.dialog_titel)
                .setPositiveButton(R.string.dialog_button_positiv, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String quantityString = editTextNewQauntity.getText().toString();
                        String productString = editTextNewProduct.getText().toString();
                        if (TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(productString)) {
                            Toast.makeText(MainActivity.this, "Felder dürfen nicht leer sein", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int quantityInt = Integer.parseInt(quantityString);
                        ShoppingMemo sMemo = dataSource.updateShoppingMemo(shoppingMemo.getId(), productString, quantityInt, shoppingMemo.isChecked());

                        Log.d(TAG, "onClick: Alter Eintrag - ID: " + shoppingMemo.getId() + " INhalt: " + shoppingMemo.toString());
                        Log.d(TAG, "onClick: Neuer Eintrag - ID: " + sMemo.getId() + " INhalt: " + sMemo.toString());

                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negativ, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Datenquelle wird geöffnet");
        dataSource.open();
        showAllListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Datenquelle wird geschlossen.");
        dataSource.close();
    }

    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();
        ArrayAdapter<ShoppingMemo> adapter = (ArrayAdapter<ShoppingMemo>) mShoppingMemosListView.getAdapter();
        adapter.clear();
        adapter.addAll(shoppingMemoList);
        adapter.notifyDataSetChanged();
    }
//    private void showAllListEntries() {
//        List<ShoppingMemo> shoppingMemos = dataSource.getAllShoppingMemos();
//        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_multiple_choice, shoppingMemos);
//        ListView shoppingMemoListView = findViewById(R.id.lv_shopping_memo);
//        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Settings wurde gedrückt", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startSacan(View view) {

        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Scanner nicht installiert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            TextView product = findViewById(R.id.editText_product);
            product.setText(getProductName(data.getStringExtra("SCAN_RESULT")));
            TextView quantity = findViewById(R.id.editText_quantity);
            quantity.requestFocus();
        }else {
            Toast.makeText(this, "Scan nicht möglich", Toast.LENGTH_SHORT).show();
        }
    }

    private String getProductName(String scanResult) {
        HoleDatenTask task = new HoleDatenTask();
        String result = null;
        try {
            result = task.execute(scanResult).get();
            JSONObject rootObject = new JSONObject(result);
            Log.d(TAG, "getProductName: " + rootObject.toString(2));
            if (rootObject.has("product")) {
                JSONObject productObject = rootObject.getJSONObject("product");
                if (productObject.has("product_name")) {
                    return productObject.getString("product_name");
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "", e);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return "Artikel nicht gefunden";
    }


    public class HoleDatenTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            final String baseUrl = "https://world.openfoodfacts.org/api/v0/product/";
            final String requestUrl = baseUrl + strings[0] + ".json";
            Log.d(TAG, "doInBackground: " + requestUrl);
            StringBuilder result = new StringBuilder();
            URL url = null;

            try {
                url = new URL(requestUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "", e);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {

            }
            Log.d(TAG, "doInBackground: " + result.toString());
            return result.toString();
        }
    }
}
