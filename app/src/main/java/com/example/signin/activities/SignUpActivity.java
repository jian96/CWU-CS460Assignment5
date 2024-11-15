package com.example.signin.activities;

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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.signin.MainActivity;
import com.example.signin.R;
import com.example.signin.databinding.ActivitySignInBinding;
import com.example.signin.databinding.ActivitySignUpBinding;
import com.example.signin.utilities.Constants;
import com.example.signin.utilities.PreferenceManager;
import com.google.firebase.FirebaseKt;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private PreferenceManager preferenceManager;
    private String encodeImage;

    /**
     * This is called on app creation and orientation changes to bind Views and call listeners
     * Uses view binding to automatically associate with Views
     * @param savedInstanceState param for orientation changes
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    /**
     * Sets listeners for when user clicks on the "Sign up" button
     * on the sign up page
     * If the user clicks the sign in button, they are redirected to sign in activity
     * If the user clicks on the "add image" button, they are directed to a System dialog used to
     * output an image to the app
     * If the user clicks on sign up, the sign up detail validation function is called
     */
    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        binding.buttonSignUp.setOnClickListener(v ->{
            if(isValidateSignUpDetails()){
                SignUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    /**
     * Helper function to display toasts
     * @param message a string to display as a Toast
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is the main sign up function, called after the sign up detail has been checked to be valid
     * When the user clicks the sign up button, it is hidden and the progress bar appears to show the system is working
     * The function first retrieves an instance of the firestore database
     * The sign up detail are all put into a hashmap, since all of the inputs are strings, including the image
     * which is encoded into Base64
     * Then the hashmap is added to the cloud database, and the user is redirected to the main activity
     * Preliminary login activities such as saving the login metadata into a Preference object also occurs since
     * signing up logs the user in
     * If the sign up fails then a Toast will display the exception message
     */
    private void SignUp(){

        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }).addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
        });
    }

    /**
     * Function used to encode an image into Base 64 string
     * Encoding algorithm is not implemented here
     * The image is also scaled appropiately and compressed
     * as a JPEG before being encoded to ensure standardization of avatars
     * The image is converted to raw bytes and then encoded using Base64.encodeToString()
     * @param bitmap The avatar the user uploads
     * @return A string representing the Base 64 encoding of the original bitmap
     */
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()*previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayInputStream);

        byte[] bytes = byteArrayInputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * This function is used to call the system to access the Android device's gallery
     * in order to choose an image.
     * After an image is chosen, it is displayed on the imageProfile View on the sign up page
     * and also removes the "add image" text in the middle of imageProfile
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK){
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * This function ensures every sign up detail is inputted by the user
     * It also uses a pattern checker if the e-mail is a valid format.
     * Password confirmation is also checked here
     * @return Signals if the sign up details are valid
     */
    private Boolean isValidateSignUpDetails(){
        if (encodeImage == null){
            showToast("Please choose an avatar");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please enter your e-mail");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Please enter a valid e-mail");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please enter your password");
            return false;
        }  else if (binding.inputConFirmPassword.getText().toString().trim().isEmpty()){
            showToast("Please confirm your password");
            return false;
        }  else if (!binding.inputPassword.getText().toString().equals(binding.inputConFirmPassword.getText().toString())){
            showToast("Password does not match password confirmation");
            return false;
        }  else {
            return true;
        }


    }

    /**
     * Helper function used to hide or display the sign in button and progress bar
     * @param isLoading signals if it's a state where the user clicks the button or the UI finished
     *                  loading
     */
    private void loading (Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}