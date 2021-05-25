package com.project.android.transtalk.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountsFragment extends Fragment {

    //User Interface
    private CircleImageView mDisplayImage;
    private TextInputLayout mDisplayName;
    private TextInputLayout mStatus;
    private Button mSave;

    //Firebase
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mImageStorage;

    private static final int GALLERY_PICK = 1;
    private String mCurrentUserId;

    public AccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mMainView = inflater.inflate(R.layout.fragment_accounts, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mDisplayImage= mMainView.findViewById(R.id.accounts_image);
        mDisplayName= mMainView.findViewById(R.id.accounts_display_name_layout);
        mStatus= mMainView.findViewById(R.id.accounts_status_layout);
        mSave= mMainView.findViewById(R.id.accounts_save);
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(mCurrentUserId).child("Name").getValue().toString();
                final String image = dataSnapshot.child(mCurrentUserId).child("ImageUrl").getValue().toString();
                String status = dataSnapshot.child(mCurrentUserId).child("Status").getValue().toString();

                mDisplayName.getEditText().setText(name);
                mStatus.getEditText().setText(status);
                if (!image.equals("Default")) {

                    Picasso.get()
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.contact_image)
                            .into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception ex) {
                                    Picasso.get()
                                            .load(image)
                                            .placeholder(R.drawable.contact_image)
                                            .into(mDisplayImage);

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String displayName = mDisplayName.getEditText().getText().toString();
                String status = mStatus.getEditText().getText().toString();
                mUserDatabase.child(mCurrentUserId).child("Name").setValue(displayName);
                mUserDatabase.child(mCurrentUserId).child("Status").setValue(status);

            }
        });

        mDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(),this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                File thumb_filepath = new File(resultUri.getPath());
                Bitmap compressedImageBitmap;
                try {
                    compressedImageBitmap =  BitmapFactory.decodeFile(thumb_filepath.toString());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG,75,byteArrayOutputStream);
                    final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                    final StorageReference filepath = mImageStorage.child("profile_images").child(mCurrentUserId + ".jpeg");
                    final StorageReference thumb_file = mImageStorage.child("profile_images").child("thumbs").child(mCurrentUserId+".jpeg");
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                UploadTask uploadTask = thumb_file.putBytes(thumb_byte);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        thumb_file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                mUserDatabase.child(mCurrentUserId).child("ThumbUrl").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            Toast.makeText(getContext(), "Successfully Uploaded Thumb Image", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        mUserDatabase.child(mCurrentUserId).child("ImageUrl").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(getContext(), "Successfully Uploaded the Image", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    }
                                });
                            } else {

                                Toast.makeText(getContext(), "Error in uploading", Toast.LENGTH_SHORT).show();

                            }
                        }

                    });
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(getContext(),"Error="+error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("Fragment","AccountsFragment");
        super.onSaveInstanceState(savedInstanceState);
    }
}
