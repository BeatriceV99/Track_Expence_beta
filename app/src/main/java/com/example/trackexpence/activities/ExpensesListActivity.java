package com.example.trackexpence.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trackexpence.fragments.ExpenseListFragment;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.models.MenuManager;
import com.example.trackexpence.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ExpensesListActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private final static String TAG = ExpensesListActivity.class.getCanonicalName();

    NavigationView menu_main;
    FirebaseWrapper.Auth mAuth=new FirebaseWrapper.Auth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        try{
            /*---Menu Start--*/
            menu_main=(NavigationView)findViewById(R.id.menu_main_nview);
            MenuManager menu_manager=new MenuManager();
            drawerLayout = findViewById(R.id.menu_main);
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            menu_main.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    menu_manager.ChangeActivity(ExpensesListActivity.this, menuItem);

                    return true;
                }
            });
            /*---Menu End--*/

            Bundle bundle=new Bundle();
            String xBContent="ExpensesListActivity";
            bundle.putString("caller", xBContent);
            Fragment mFragment = null;
            mFragment = new ExpenseListFragment();
            mFragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, mFragment, "ExpenseListFragment").commit();
        }
        catch(Exception e) {
            Log.e(TAG, "onCreate---Error: "+e);
        }

        getUserInfo();

    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addExpense(View view) {
        try{
            MenuManager menu_manager=new MenuManager();
            Bundle bundle=new Bundle();
            String xBContent="ExpensesListActivity";
            bundle.putString("caller", xBContent);
            menu_manager.goToActivity(ExpensesListActivity.this, ExpenseActivity.class, bundle);
        }
        catch (Exception e) {
            Log.e(TAG, "addExpense---Error: "+e);
        }
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