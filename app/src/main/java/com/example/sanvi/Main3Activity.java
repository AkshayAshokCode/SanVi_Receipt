package com.example.sanvi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Main3Activity extends AppCompatActivity {
    Button next;
    Button back;
    EditText otp;
    FirebaseAuth mAuth;
    String codeSent;
    FirebaseFirestore fStore;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        next=findViewById(R.id.next2);
        back=findViewById(R.id.back2);
        otp=findViewById(R.id.editText8);





        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code=otp.getText().toString().trim();
                if(code.isEmpty() || code.length()!=6){
                    otp.setError("Enter valid Code");
                    otp.requestFocus();
                    return;
                }



                verifySignInCode(code);
                }
        });

     back.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Main3Activity.this, Main2Activity.class);
            startActivity(i);

        }
    });
        Intent i=getIntent();
        Bundle b=i.getExtras();
       String phoneNumber=(String) b.get("number");
        sendVerificationCode(phoneNumber);
}
    private void sendVerificationCode(String phoneNumber){

        if(phoneNumber.length()!=13) {
            Toast.makeText(getApplicationContext(), "Please Enter a Valid Number", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks);

    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent=s;
        }
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        String code=phoneAuthCredential.getSmsCode();
            if(code.length()==6) {
                verifySignInCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    };
    private void verifySignInCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent b=getIntent();
                            HashMap<String, String> user = (HashMap<String, String>)b.getSerializableExtra("data");

                            userID=mAuth.getCurrentUser().getUid();
                            Toast.makeText(getApplicationContext(),"Database stored", Toast.LENGTH_SHORT);
                            DocumentReference documentReference= fStore.collection("Users").document(userID);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","Successful in adding Data");
                                }
                            });
                            Intent i=new Intent(Main3Activity.this,Main3bActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.putExtra("userID",userID);

                            startActivity(i);

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),"Invalid OTP entered", Toast.LENGTH_SHORT);
                            }

                            }
                    }
                });
    }
}
