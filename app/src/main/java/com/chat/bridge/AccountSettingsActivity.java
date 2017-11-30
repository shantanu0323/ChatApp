package com.chat.bridge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AccountSettingsActivity";
    private static final int GALLERY_REQUEST = 10;
    private static final int TAKE_PICTURE = 20;
    private CircularImageView profilepic;
    private FrameLayout viewProfilePic;
    private EditText etDisplayName;
    private EditText etStatus;
    private TextView tvEmail;
    private FloatingActionButton bProfilepic;
    private Button bEdit;
    private ImageButton bCancel;
    private ProgressDialog progressDialog;
    private ImageView expandedProfilePic;
    private Uri uri = null;
    private StorageReference mStorageRef;
    private DatabaseReference currentUserRef;
    private FirebaseAuth mAuth;

    private void findViews() {
        profilepic = (CircularImageView) findViewById(R.id.profilepic);
        etDisplayName = (EditText) findViewById(R.id.etDisplayName);
        etStatus = (EditText) findViewById(R.id.etStatus);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        bProfilepic = (FloatingActionButton) findViewById(R.id.bProfilepic);
        bEdit = (Button) findViewById(R.id.bEdit);
        viewProfilePic = (FrameLayout) findViewById(R.id.viewProfilePic);
        bCancel = (ImageButton) findViewById(R.id.bCancel);
        expandedProfilePic = (ImageView) findViewById(R.id.expandedProfilePic);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profilepic");

        progressDialog = new ProgressDialog(this);
        bProfilepic.setOnClickListener(this);
        bEdit.setOnClickListener(this);
        bCancel.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mAuth = FirebaseAuth.getInstance();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());
        findViews();
        progressDialog.setIcon(getResources().getDrawable(R.drawable.ic_display_name));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Just a sec... while we prepare your profile...");
        progressDialog.setTitle("Preparing Profile...");
        progressDialog.show();

        populateViewsWithData();
    }

    private void populateViewsWithData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabase.keepSynced(true);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    HashMap<String, String> currentUser = (HashMap<String, String>) dataSnapshot.child(userId).getValue();
                    etDisplayName.setText(currentUser.get("name"));
                    etStatus.setText(currentUser.get("status"));
                    tvEmail.setText(currentUser.get("email"));
                    final String imageUrl = currentUser.get("image");
                    if (!imageUrl.equalsIgnoreCase("default")) {
                        Picasso.with(AccountSettingsActivity.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_image).into(profilepic, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded offline successfully
                            }

                            @Override
                            public void onError() {
                                Picasso.with(AccountSettingsActivity.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.default_image).into(profilepic);
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Log.e(TAG, "onCancelled: Retreiving Failed : " + databaseError.getMessage());
                Snackbar.make(getCurrentFocus(), "Sorry!!! I could not load your profile right now... :\n" + databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        uri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: REQUESTcODE");
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = uri;
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity(selectedImage)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }
        } else {
            Log.e(TAG, "onActivityResult: UNSUCESSFULL : resultCode = " + resultCode);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                uri = resultUri;
                uploadImage(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "CROPPING UNSUCCESSFULL : " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.bProfilepic:
                PopupMenu popupMenu = new PopupMenu(AccountSettingsActivity.this, bProfilepic, Gravity.LEFT);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_view:
//                                //Convert Bitmap to Byte Array:-
//                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                                byte[] byteArray = stream.toByteArray();
//                                //Pass byte array into intent:-
//
//                                Intent viewProfilePicIntent = new Intent(getApplicationContext(), ViewProfilePicIntent.class);
//                                viewProfilePicIntent.putExtra("picture", byteArray);
//                                startActivity(viewProfilePicIntent);
//                                //Get Byte Array from Bundle and Convert into Bitmap Image:-
//
//                                Bundle extras = getIntent().getExtras();
//                                byte[] byteArray = extras.getByteArray("picture");
//
//                                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//                                ImageView image = (ImageView) findViewById(R.id.imageView1);
//
//                                image.setImageBitmap(bmp);
                                viewProfilePic.setVisibility(View.VISIBLE);
                                break;
                            case R.id.action_camera:
                                takePhoto();
                                break;
                            case R.id.action_gallery:
                                pickUserImage();
                                break;
                        }
                        return true;
                    }
                });
                try {
                    popupMenu.show();
                } catch (Exception e) {
                    Log.e(TAG, "onClick: PopupMenu ", e);
                }
                break;
            case R.id.bEdit:
                Intent editIntent = new Intent(getApplicationContext(), EditActivity.class);
                editIntent.putExtra("name", etDisplayName.getText().toString());
                editIntent.putExtra("status", etStatus.getText().toString());
                startActivity(editIntent);
                break;
            case R.id.bCancel:
                viewProfilePic.setVisibility(View.GONE);
        }
    }

    public void uploadImage(final Uri uri) {
        progressDialog.setIcon(getResources().getDrawable(R.drawable.ic_upload));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Just a sec... while we update your profile pic...");
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        File thumbnailPath = new File(uri.getPath());
        Bitmap thumbnailBitmap = null;
        try {
            thumbnailBitmap = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(75)
                    .compressToBitmap(thumbnailPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bAOS);
        final byte[] thumbnailByte = bAOS.toByteArray();

        StorageReference profilepicStorage = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        final StorageReference thumbnailStorage = mStorageRef.child("thumbnails").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

        profilepicStorage.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        UploadTask uploadTask = thumbnailStorage.putBytes(thumbnailByte);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadThumbnailUrl = taskSnapshot.getDownloadUrl();
                                DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                                userDatabase.child("image").setValue(downloadUrl.toString());
                                userDatabase.child("thumbnail").setValue(downloadThumbnailUrl.toString());
                                profilepic.setImageURI(uri);
                                expandedProfilePic.setImageURI(uri);
                                progressDialog.dismiss();


                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        progressDialog.dismiss();
                        Snackbar.make(getCurrentFocus(), "Uploading failed : " + exception.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (viewProfilePic.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            viewProfilePic.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: CALLED");
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: CALLED");
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: CALLED");
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: CALLED");
        currentUserRef.child("online").setValue("false");
    }

}
