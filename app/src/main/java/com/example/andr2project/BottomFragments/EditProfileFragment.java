package com.example.andr2project.BottomFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.andr2project.R;
import com.example.andr2project.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    ImageView backBtn;
    TextInputEditText nameEt, emailEt, passwordEt;
    CircleImageView imageProfileEdit;
    Spinner teamEdit;
    MaterialButton btnUpdate;
    FirebaseAuth firebaseAuth;
    private CollectionReference db;
    private StorageReference mStorageRef;
    Uri selectedImageUri;
    ProgressDialog loader;
    String oldPassword;
    String selectedTeam;
    private static final String TAG = "EditProfileFragment";

    public EditProfileFragment() {
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference("UserPictures");

        loader = new ProgressDialog(getActivity());
        loader.setTitle("Updating");
        loader.setMessage("Updating your credentials! Please wait...");
        loader.setCanceledOnTouchOutside(false);

        nameEt = view.findViewById(R.id.nameEditId);
        emailEt = view.findViewById(R.id.emailEditId);
        passwordEt = view.findViewById(R.id.passwordEditId);
        teamEdit = view.findViewById(R.id.teamEditId);
        btnUpdate = view.findViewById(R.id.updateEditId);
        imageProfileEdit = view.findViewById(R.id.imageProfileEditId);

        backBtn = view.findViewById(R.id.backBtnId);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        db.document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            nameEt.setText(user.getName());
                            emailEt.setText(user.getEmail());
                            passwordEt.setText(user.getPassword());
                            Glide.with(getActivity()).load(user.getImgUrl())
                                    .placeholder(R.drawable.ic_baseline_image_24)
                                    .into(imageProfileEdit);

                            ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                                    R.array.teams, android.R.layout.simple_list_item_activated_1);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                            teamEdit.setAdapter(arrayAdapter);
                            teamEdit.setSelection(arrayAdapter.getPosition(user.getTeam().name()));
                            teamEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedTeam = parent.getItemAtPosition(position).toString();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                            btnUpdate.setEnabled(true);
                            oldPassword = user.getPassword();
                        } else {
                            Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.getMessage());
                        Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    System.out.print("Current data: null");
                    Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageProfileEdit.setOnClickListener(v -> {
            imageChooser();
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loader.show();

                if (selectedImageUri != null) {
                    StorageReference fileReference = mStorageRef.child(firebaseAuth.getCurrentUser().getUid() + "." + getFileExtension(selectedImageUri));
                    fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(uri -> {
                            String img = uri.toString();
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", nameEt.getText().toString());
                            map.put("email", emailEt.getText().toString());
                            map.put("team", selectedTeam);
                            map.put("imgUrl", img);

                            try {
                                //password update if uri is selected
                                if (!oldPassword.equals(passwordEt.getText().toString())) {
                                    AuthCredential credential = EmailAuthProvider.getCredential(emailEt.getText().toString(), oldPassword);
                                    firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                firebaseAuth.getCurrentUser().updatePassword(passwordEt.getText().toString()).addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("password", passwordEt.getText().toString());
                                                        db.document(firebaseAuth.getCurrentUser().getUid()).update(map);
                                                        Log.d(TAG, "onComplete: Password Updated!");
                                                    } else {
                                                        Log.e(TAG, "Password not update.. 1 " + task.getException().getMessage());
                                                    }
                                                });
                                            } else {
                                                Log.e(TAG, "Password not update.. 2" + task.getException().getMessage());
                                            }
                                        }
                                    });
                                }

                                //other record update if uri is selected
                                db.document(firebaseAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            loader.dismiss();
                                            Toast.makeText(getActivity(), "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            loader.dismiss();
                                            Toast.makeText(getActivity(), "Failed to update user! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                            } catch (Exception e) {
                                loader.dismiss();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "editProfileFragment: " + e.getMessage());
                            }
                        });
                    }).addOnFailureListener(e -> {
                        loader.dismiss();
                        Log.e(TAG, "editProfileFragment: " + e.getMessage());
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", nameEt.getText().toString());
                    map.put("email", emailEt.getText().toString());
                    map.put("team", selectedTeam);

                    try {
                        //password update if uri is null
                        if (!oldPassword.equals(passwordEt.getText().toString())) {
                            AuthCredential credential = EmailAuthProvider.getCredential(emailEt.getText().toString(), oldPassword);
                            firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseAuth.getCurrentUser().updatePassword(passwordEt.getText().toString()).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("password", passwordEt.getText().toString());
                                                db.document(firebaseAuth.getCurrentUser().getUid()).update(map);
                                                Log.d(TAG, "onComplete: Password Updated!");
                                            } else {
                                                Log.e(TAG, "Password not update.. 1 " + task.getException().getMessage());
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "Password not update.. 2" + task.getException().getMessage());
                                    }
                                }
                            });
                        }

                        //other record update if uri is null
                        db.document(firebaseAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loader.dismiss();
                                    Toast.makeText(getActivity(), "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    loader.dismiss();
                                    Toast.makeText(getActivity(), "Failed to update user! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        loader.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "editProfileFragment: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap;
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                            imageProfileEdit.setImageBitmap(selectedImageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
