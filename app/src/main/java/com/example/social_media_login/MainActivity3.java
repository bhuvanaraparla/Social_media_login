package com.example.social_media_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

// ... (imports and package declaration)

public class MainActivity3 extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    TextView username, usermail, hello;
    Button signout;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        username = findViewById(R.id.username);
        usermail = findViewById(R.id.usermail);
        image = findViewById(R.id.image);
        signout = findViewById(R.id.signout);
        hello = findViewById(R.id.hello);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("EXTRA_NAME");
            String email = intent.getStringExtra("EXTRA_EMAIL");
            if (name != null) {
                username.setText(name);
            }

            if (email != null) {
                usermail.setText(email);
            }
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            // If the Facebook access token is not null and not expired,
            // you may want to handle this case accordingly.
            // For now, I'm not starting MainActivity2 in this case.
        } else {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            try {
                                String name = null; // Declare the name variable here
                                if (object != null && object.has("name")) {
                                    name = object.getString("name");
                                    username.setText(name);
                                } else {
                                    Log.e("ProfileData", "Name not found in JSON response");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ProfileData", "Error parsing JSON response" + object.toString());
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,picture.type(large)");
            request.setParameters(parameters);
            request.setGraphPath("me");
            request.executeAsync();
        }

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity3.this);
        if (account != null) {
            String name = account.getDisplayName();
            String email = account.getEmail();
            username.setText(name);
            usermail.setText(email);
        }

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    void signOut() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    redirectToSignInScreen();
                }
            });
        } else {
            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
            if (facebookAccessToken != null) {
                LoginManager.getInstance().logOut();
                redirectToSignInScreen();
            }
        }
    }

    private void redirectToSignInScreen() {
        finish();
        startActivity(new Intent(MainActivity3.this, MainActivity2.class));
    }
}
