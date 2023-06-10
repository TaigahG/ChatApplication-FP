package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignInBinding;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners(){
        binding.backSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValid()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    //Toast is for the small feedback in a small pop up
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    //Sign up method
    //using hash map to store items with identifiers
    private void signUp(){
        Loading(true);
        FirebaseFirestore DB = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        //add the 'user' to the user collection and store it to the database
        DB.collection(Constants.KEY_COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    Loading(false);
                    preferenceManager.setBoolean(Constants.KEY_SIGNED_IN, true);
                    //Stores the ID of the newly created document in the user's preferences.
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    //Stores the user's name in the preferences.
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    //Stores the user's encoded image in the preferences.
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    showToast("Account has successfully created");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }).addOnFailureListener(exception -> {
                    Loading(false);
                    showToast(exception.getMessage());

                });
    }

    //getting image from user for the profile
    private String encodedImage(Bitmap bitmap){
        int prevWidth = 150;
        int prevHeight = bitmap.getHeight() * prevWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, prevWidth, prevHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri uriImage = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(uriImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imgProfile.setImageBitmap(bitmap);
                            binding.txtImg.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    //checking if user input all of the data needed
    //if one of the column hasn't filled by the user
    //the user will get small pop up warning
    private boolean isValid(){

        //if the user hasn't input the image
        if(encodedImage == null){
            showToast("Select an image for your profile");
            return false;
        }
        //if the user hasn't input the name
        else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Input your name");
            return false;
        }
        //if the user hasn't input the email
        else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Input your email");
            return false;
        }
        //if the email format is wrong
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Email is not valid");
            return false;
        }
        //if the user hasn't input the password
        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Input your password");
            return false;
        }
        //if the user hasn't input confirm password
        else if(binding.inputConfPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }
        //if the password and the confirm password is not similar
        else if(!binding.inputConfPassword.getText().toString().equals(binding.inputPassword.getText().toString())){
            showToast("Password is not match");
            return false;
        }
        else{
            return true;
        }

    }

    //loading animation
    private void Loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}