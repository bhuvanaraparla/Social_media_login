package com.example.social_media_login;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {
    Button logbtn;
    EditText etnn, etnp, etne;
    ImageView google, facebook;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity2";

    private GoogleSignInClient mGoogleSignInClient;
    String name, email;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

                        if (isLoggedIn) {
                            String facebookUserId = accessToken.getUserId();
                            navigateToMainActivity3(facebookUserId);
                        } else {
                            Toast.makeText(MainActivity2.this, "Facebook Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
        logbtn = findViewById(R.id.logbtn);
        etnn = findViewById(R.id.etnn);
        etne = findViewById(R.id.etne);
        etnp = findViewById(R.id.etnp);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("582980066441-2sm3113s85svndlp4v4fk1ar05dpd8ln.apps.googleusercontent.com") // Ensure you have this line
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_name = etnn.getText().toString().trim();
                String txt_email = etne.getText().toString().trim();
                String txt_password = etnp.getText().toString().trim();

                // Validate input fields
                if (!isValidName(txt_name)) {
                    etnn.setError("Enter a valid name (contains alphabets only without spacing)");
                    return;
                }
                if (!isValidPassword(txt_password)) {
                    etnp.setError("Enter a valid password (contains at least 6 characters)");
                    return;
                }
                if (!isValidEmail(txt_email)) {
                    etne.setError("Enter a valid Email");
                    return;
                }

                if (isValidPassword(txt_password) && isValidEmail(txt_email) && isValidName(txt_name)) {
                    registeredUser(txt_email, txt_password);
                }
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //login to facebook
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity2.this, Arrays.asList("public_profile"));
                } catch (Exception e) {
                    e.printStackTrace();
                    // Log or display an error message
                    Toast.makeText(MainActivity2.this, "Error logging in with Facebook", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void navigateToMainActivity3(String facebookUserId) {
        name = etnn.getText().toString();
        email = etne.getText().toString();

        // Open the Facebook profile in the Facebook app or browser
        String facebookProfileUrl = "https://www.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookProfileUrl));
        startActivity(intent);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == 2000) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            // Handle error
            Log.w(TAG, "Google sign in failed", e);
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            showLoginDialog();

                            // Redirect to MainActivity3 upon successful sign-in
                            redirectToMainActivity3();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity2.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void redirectToMainActivity3() {
        name = etnn.getText().toString();
        email = etne.getText().toString();

        Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
        intent.putExtra("EXTRA_NAME", name);
        intent.putExtra("EXTRA_EMAIL", email);
        startActivity(intent);
    }

    private void registeredUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity2.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                showLoginDialog();
            } else {
                String errorMessage = "Registration Failed";
                if (task.getException() != null) {
                    errorMessage = "Registration Failed: " + task.getException().getMessage();
                }
                Toast.makeText(MainActivity2.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String txt_email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailPattern, txt_email);
    }

    private boolean isValidName(String txt_name) {
        String namePattern = "^[A-Za-z ]+$";
        return Pattern.matches(namePattern, txt_name);
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^.{6,}$";
        return Pattern.matches(passwordPattern, password);
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login");
        builder.setMessage("Welcome");
        builder.setPositiveButton("Ok", (dialog, which) -> {
            name = etnn.getText().toString();
            email = etne.getText().toString();
            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
            intent.putExtra("EXTRA_NAME", name);
            intent.putExtra("EXTRA_EMAIL", email);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(MainActivity2.this, "Login failed,Please try again", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}
