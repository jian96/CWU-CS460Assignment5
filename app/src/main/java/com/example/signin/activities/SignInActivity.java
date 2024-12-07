package com.example.signin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.signin.databinding.ActivitySignInBinding;
import com.example.signin.utilities.Constants;
import com.example.signin.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * This is called on app creation and orientation changes to bind Views and call listeners
     * Uses view binding to automatically associate with Views
     *
     * @param savedInstanceState param for orientation changes
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    /**
     * Sets listeners for when user clicks on the "Sign in" or "Create new account" button
     * on the sign in page and executes functions acoordingly
     * If the create new account button is pressed, the user is redirected to the sign up activity
     * If the sign in button is pressed, email input is checked if they're an email pattern or was entered
     * and if the password was entered
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidateSignInDetails()) {
                SignIn();
            }
        });
    }

    /**
     * Helper function to display toasts
     *
     * @param message a string to display as a Toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Function to handle sign in logic, after user input has been validated by another function
     * When user input of email is confirmed to be an email pattern and exists, and the password exists
     * First hides the "sign in" button and replaces it with a progress bar
     * Then the function retrieves an instance of the project's Firebase Auth
     * Then the e-mail and password from the user input is checked if exists and is correct
     * using signInWithEmailAndPassword(). The user data is then retrieved using getCurrentUser()
     * If a match is found then the user is directed to the main activity and details such as
     * sign in status, userid, name, and image are saved into a Preference object
     * If a match isn't found then a Toast pops up saying unable to login
     */
    private void SignIn() {
        loading(true);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                            DocumentSnapshot documentSnapshot = userTask.getResult();
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_USERID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                                            showToast("Login successful!");
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            loading(false);
                                            showToast("User data retrieval failed.");
                                        }
                                    });
                        }
                    } else {
                        loading(false);
                        showToast("unable to login");
                    }
                });
    }

    /**
     * Helper function used to hide or display the sign in button and progress bar
     *
     * @param isLoading signals if it's a state where the user clicks the button or the UI finished
     *                  loading
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This function retrieves the text in the e-mail and password TextEdit Views and checks if
     * the email is valid (using a pattern checker) or exists, and if the password exists
     *
     * @return signals if the user input is valid or not
     */
    private boolean isValidateSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your e-mail");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter a valid e-mail");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password");
            return false;
        } else {
            return true;
        }
    }

}
