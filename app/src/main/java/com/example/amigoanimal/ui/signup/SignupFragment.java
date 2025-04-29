package com.example.amigoanimal.ui.signup;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.amigoanimal.R;
import com.example.amigoanimal.databinding.FragmentSignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;

    private EditText edit_name, edit_email, edit_password;
    private Button button;
    private String usuarioID;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SignupViewModel signupViewModel =
                new ViewModelProvider(this).get(SignupViewModel.class);

        binding = FragmentSignupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        IniciarComponentes();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = edit_name.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_password.getText().toString();

                if(nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(v, "Preencha todos os campos!", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    CadastrarUsuario(nome,email,senha,v);
                }
            }
        });

        final boolean[] isPasswordVisible = {false};

        edit_password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = edit_password.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    if (event.getRawX() >= (edit_password.getRight() - drawableWidth - edit_password.getPaddingRight())) {

                        // Alterna a visibilidade da senha
                        isPasswordVisible[0] = !isPasswordVisible[0];

                        if (isPasswordVisible[0]) {
                            edit_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            edit_password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        } else {
                            edit_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            edit_password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                        }

                        // Coloca o cursor no final
                        edit_password.setSelection(edit_password.getText().length());

                        // Acessibilidade
                        v.performClick();
                        return true;
                    }
                }
            }
            return false;
        });

        return root;
    }

    private void CadastrarUsuario(String nome, String email, String senha, View view){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    SalvarDadosUsuario(nome);

                    Snackbar snackbar = Snackbar.make(view, "Sucesso ao cadastrar novo usuário!", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                    String erro;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        erro = "Cadastre uma senha com pelo menos 6 caracteres!";
                    } catch (FirebaseAuthUserCollisionException e){
                        erro = "Email já cadastrado! Use outro email!";
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        erro = "Email inválido!";
                    } catch (Exception e) {
                        erro = "Erro ao cadastrar usuário!";
                    }

                    Snackbar snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });

    }

    private void SalvarDadosUsuario(String nome){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> usuarios = new HashMap<>();
        usuarios.put("nome",nome);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("db","sucesso ao salvar!");
            }
        })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("db_error","erro ao salvar: " + e.toString());
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void IniciarComponentes() {
        edit_name = binding.editName;
        edit_email = binding.editEmail;
        edit_password = binding.editPassword;
        button = binding.button;
    }
}