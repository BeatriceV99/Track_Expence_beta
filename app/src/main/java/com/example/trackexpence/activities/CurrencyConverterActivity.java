package com.example.trackexpence.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.trackexpence.R;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.models.MenuManager;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.api.API_CurrencyConverter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Locale;

public class CurrencyConverterActivity extends AppCompatActivity {
    private static final String TAG = CurrencyConverterActivity.class.getCanonicalName();
    Context PMContext = this;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private MenuManager menu_manager = new MenuManager();

    double xRate = 0.0;
    double xAmount = 0.0;
    String xCurrencyStart = "";
    String xCurrencyEnd = "";

    EditText start_amount;
    TextView end_amount;
    Spinner spinner_start;
    Spinner spinner_end;

    NavigationView menu_main;
    FirebaseWrapper.Auth mAuth = new FirebaseWrapper.Auth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        /* Menu Start */
        menu_main = (NavigationView) findViewById(R.id.menu_main_currency);

        menu_main.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                menu_manager.ChangeActivity(CurrencyConverterActivity.this, item);
                return true;
            }
        });

        drawerLayout =findViewById(R.id.menu_currency);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /* Menu End */

        String[] displayText_start = {"Euro", "US Dollar", "Japanese Yen", "Bulgarian Lev", "Czech Republic Koruna",
                "Danish Krone", "British Pound Sterling", "Hungarian Forint", "Polish Zloty", "Romanian Leu",
                "Swedish Krona", "Swiss Franc", "Icelandic Króna", "Norwegian Krone", "Croatian Kuna", "Russian Ruble",
                "Turkish Lira", "Australian Dollar", "Brazilian Real", "Canadian Dollar", "Chinese Yuan", "Hong Kong Dollar",
                "Indonesian Rupiah", "Israeli New Sheqel", "Indian Rupee", "South Korean Won", "Mexican Peso", "Malaysian Ringgit",
                "New Zealand Dollar", "Philippine Peso", "Singapore Dollar", "Thai Baht", "South African Rand"};
        String[] values_start = {"EUR", "USD", "JPY", "BGN", "CZK", "DKK", "GBP", "HUF", "PLN", "RON",
                "SEK", "CHF", "ISK", "NOK", "HRK", "RUB", "TRY", "AUD", "BRL", "CAD", "CNY", "HKD",
                "IDR", "ILS", "INR", "KRW", "MXN", "MYR", "NZD", "PHP", "SGD", "THB", "ZAR"};
        String[] displayText_end = {"US Dollar", "Euro", "Japanese Yen", "Bulgarian Lev", "Czech Republic Koruna",
                "Danish Krone", "British Pound Sterling", "Hungarian Forint", "Polish Zloty", "Romanian Leu",
                "Swedish Krona", "Swiss Franc", "Icelandic Króna", "Norwegian Krone", "Croatian Kuna", "Russian Ruble",
                "Turkish Lira", "Australian Dollar", "Brazilian Real", "Canadian Dollar", "Chinese Yuan", "Hong Kong Dollar",
                "Indonesian Rupiah", "Israeli New Sheqel", "Indian Rupee", "South Korean Won", "Mexican Peso", "Malaysian Ringgit",
                "New Zealand Dollar", "Philippine Peso", "Singapore Dollar", "Thai Baht", "South African Rand"};
        String[] values_end = {"USD", "EUR", "JPY", "BGN", "CZK", "DKK", "GBP", "HUF", "PLN", "RON",
                "SEK", "CHF", "ISK", "NOK", "HRK", "RUB", "TRY", "AUD", "BRL", "CAD", "CNY", "HKD",
                "IDR", "ILS", "INR", "KRW", "MXN", "MYR", "NZD", "PHP", "SGD", "THB", "ZAR"};

        // populate spinners
        spinner_start = findViewById(R.id.start_converter);
        spinner_end = findViewById(R.id.end_converter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayText_start);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_start.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayText_end);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_end.setAdapter(adapter2);

        start_amount = findViewById(R.id.start_amount);
        end_amount = findViewById(R.id.end_amount);

        start_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    String xInsertedAmount = charSequence.toString();
                    if (!xInsertedAmount.isEmpty()) {
                        xAmount = Double.parseDouble(xInsertedAmount);
                    }
                    else {
                        xAmount = 0.0;
                    }
                    showConvertedAmount();
                } catch (Exception e) {
                    Log.e(TAG, "start_amount-onTextChanged---Error: " + e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        spinner_start.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String selectedValue_start = values_start[position];
                    xCurrencyStart = selectedValue_start;
                    getRate();
                    showConvertedAmount();
                }
                catch (Exception e){
                    Log.e(TAG, "spinner_start-onItemSelected---Error: "+e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner_end.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String selectedValue_end = values_end[position];
                    xCurrencyEnd = selectedValue_end;
                    getRate();
                    showConvertedAmount();
                }
                catch (Exception e) {
                    Log.e(TAG, "spinner_end-onItemSelected---Error: "+e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getUserInfo();
    }

    private void getRate() {
        try {
            API_CurrencyConverter request = new API_CurrencyConverter(PMContext);
            String xParameters[]= {xCurrencyStart, xCurrencyEnd};

            String xJSON = request.execute(xParameters).get();

            if (xJSON != null) {
                Log.d(TAG, "CurrencyConverterActivity---JSON Response from API_CurrencyConverter: "+xJSON);

                //parse json response
                JsonObject jsonObject = JsonParser.parseString(xJSON).getAsJsonObject();
                JsonObject data = jsonObject.getAsJsonObject("data");

                String xxCurrencyEnd = data.entrySet().iterator().next().getKey();
                xRate = data.get(xxCurrencyEnd).getAsDouble();

                Log.d(TAG, "CurrencyConverterActivity---CurrencyRate: "+xRate+". Converted amount: "+convertAmount());
            }
        }
        catch (Exception e) {
            Log.e(TAG, "CurrencyConverterActivity---Error recovering rates for amount conversion. Error: "+e);
            xRate=0.0;
        }
    }

    private double convertAmount(){
        try {
            String formattedNumber = String.format(Locale.US, "%.2f",xAmount*xRate);
            return Double.parseDouble(formattedNumber);
        }
        catch (Exception e){
            Log.e(TAG, "convertAmount---Error: "+e);
        }
        return 0.0;
    }

    private void showConvertedAmount(){
        try {
            end_amount.setText(Double.toString(convertAmount()));
        }
        catch (Exception e) {
            Log.e(TAG, "showConvertedAmount---Error: "+e);
        }
    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getUserInfo() {
        try{
            StorageReference storageReferenceRead = FirebaseStorage.getInstance().getReference().child("images/"+mAuth.getUid()+"/Profile_photo.jpg");
            ProgressDialog progressDialog = new ProgressDialog(this);

            ImageView imageView = menu_main.getHeaderView(0).findViewById(R.id.imageProfileMenu);

            storageReferenceRead.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .into(imageView);
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "getUserInfo---Error: "+exception.getMessage());
            });

        }
        catch(Exception e){
            Log.e(TAG, "getUserInfo---Error: "+e);
        }

        TextView nameProfile = menu_main.getHeaderView(0).findViewById(R.id.nameMenu);

        try {
            //get current user id
            String UserID=mAuth.getUid();

            //get url of database
            String DBUrl = DBManager.GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

            DatabaseReference reference=database.getReference("users");

            reference.child(UserID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {

                    if(task.isSuccessful()){
                        DataSnapshot dataSnapshot= task.getResult();
                        String xFirstName=String.valueOf(dataSnapshot.child("firstname").getValue());
                        String xLastName=String.valueOf(dataSnapshot.child("lastname").getValue());
                        nameProfile.setText(xFirstName + " " + xLastName);
                    }
                }
            });
        }
        catch(Exception e) {
            Log.e(TAG, "Error reading data from user profile." +e);
        }

    }
}
