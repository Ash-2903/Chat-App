package com.example.chatapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivitySettingsBinding;
import com.example.chatapplication.models.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // display username and password always
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null) {
            // display username
            binding.uName.setText(currentUser.getDisplayName());
            // display user email
            binding.uEmail.setText(currentUser.getEmail());
        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        database.getReference().child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Uri pfpUri = Uri.parse(snapshot.child("profilePic").getValue(String.class));
                Picasso.get().load(pfpUri).placeholder(R.drawable.profile).into(binding.userPfp);
                String bio = snapshot.child("bio").getValue(String.class);
                binding.uName.setText(snapshot.child("username").getValue(String.class));
                if(bio!=null) {
                    binding.bioDisplay.setText(bio);
                    binding.bioEdit.setText(bio);
                }
                String dob = snapshot.child("dob").getValue(String.class);
                if(dob!=null) {
                    binding.dobDisplay.setText(dob);
                    binding.dobDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                }
                String gender = snapshot.child("gender").getValue(String.class);
                if(gender!=null) {
                    binding.genderDisplay.setText(gender);
                    binding.genderDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                }
                String location = snapshot.child("location").getValue(String.class);
                if(location!=null) {
                    binding.locationDisplay.setText(location);
                    binding.locationDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                    binding.locationInput.setText(location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.changePfp.setOnClickListener(v -> activityResultLauncher.launch("image/*"));


        // handle clicks on edit and submit icons
        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // button
                binding.editBtn.setVisibility(View.GONE);
                binding.setBtn.setVisibility(View.VISIBLE);
                // profile pic
                binding.changePfp.setVisibility(View.VISIBLE);
                // username
                binding.uNameEdit.setVisibility(View.VISIBLE);
                binding.uName.setVisibility(View.GONE);
                // bio
                binding.bioDisplay.setVisibility(View.GONE);
                binding.bioEdit.setVisibility(View.VISIBLE);
                // dob
                binding.dobDisplay.setVisibility(View.GONE);
                binding.dobInput.setVisibility(View.VISIBLE);
                // gender
                binding.genderDisplay.setVisibility(View.GONE);
                binding.genderInput.setVisibility(View.VISIBLE);
                // location
                binding.locationDisplay.setVisibility(View.GONE);
                binding.locationInput.setVisibility(View.VISIBLE);
            }
        });

        binding.setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = binding.uNameEdit.getText().toString();
                database.getReference().child("Users").child(currentUser.getUid()).child("username").setValue(username);

                String bio = binding.bioEdit.getText().toString();
                if(!bio.equals("Hello there ! Chatting my way through Chatterly.") && !bio.equals("")) {
                    database.getReference().child("Users").child(currentUser.getUid()).child("bio").setValue(bio);
                }

                String gender = binding.genderInput.getSelectedItem().toString();
                if(!gender.equals("Prefer not to say"))
                    database.getReference().child("Users").child(currentUser.getUid()).child("gender").setValue(gender);
//              else
//                  database.getReference().child("Users").child(currentUser.getUid()).child("gender").setValue(null);


                String location = binding.locationInput.getText().toString();
                if(!location.equals("")) {
                    database.getReference().child("Users").child(currentUser.getUid()).child("location").setValue(location);
                }

                // button
                binding.setBtn.setVisibility(View.GONE);
                binding.editBtn.setVisibility(View.VISIBLE);
                // profile pic
                binding.changePfp.setVisibility(View.GONE);
                // username
                binding.uName.setVisibility(View.VISIBLE);
                binding.uNameEdit.setVisibility(View.GONE);
                // bio
                binding.bioDisplay.setVisibility(View.VISIBLE);
                binding.bioEdit.setVisibility(View.GONE);
                // dob
                binding.dobDisplay.setVisibility(View.VISIBLE);
                binding.dobInput.setVisibility(View.GONE);
                // gender
                binding.genderDisplay.setVisibility(View.VISIBLE);
                binding.genderInput.setVisibility(View.GONE);
                // location
                binding.locationDisplay.setVisibility(View.VISIBLE);
                binding.locationInput.setVisibility(View.GONE);
            }
        });

        binding.dobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"Date of Birth");
            }
        });

        SwitchMaterial switchButton = binding.switchThemes;
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putInt("SelectedTheme", AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putInt("SelectedTheme", AppCompatDelegate.MODE_NIGHT_NO);
                }
                editor.apply();
            }
        });

        // Check if the app is in dark mode
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Set the switch to the checked state (for dark mode)
            switchButton.setChecked(true);
        } else {
            // Set the switch to the unchecked state (for light mode)
            switchButton.setChecked(false);
        }
        boolean nightModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        binding.switchThemes.setChecked(nightModeOn);

    }


    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    binding.userPfp.setImageURI(result);
                    final StorageReference reference = storage.getReference().child("profilePic").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                    reference.putFile(result).addOnSuccessListener(taskSnapshot -> {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilePic").setValue(uri.toString());
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DATE,dayOfMonth);
        //String dob = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dob = sdf.format(calendar.getTime());
        Toast.makeText(this, "dob : " + dob, Toast.LENGTH_SHORT).show();
        Log.d("dob", "onDateSet: " + dob);
        TextView dobTextView = findViewById(R.id.dobDisplay);
        dobTextView.setText(dob);
        dobTextView.setTextColor(getResources().getColor(R.color.darker_blue));
        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("dob").setValue(dob);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it.
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Handle the date set by the user.
            ((SettingsActivity) requireActivity()).onDateSet(view, year, month, day);
        }
    }

}


