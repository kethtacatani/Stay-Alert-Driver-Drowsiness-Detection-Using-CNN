package firebase.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.stayalert.CameraActivity;
import com.example.stayalert.env.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class FirebaseDatabase {
    private static final String TAG = "FirebaseDatabase";
    FirebaseAuth mAuth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    CollectionReference collection;
    FirebaseFirestore db;
    DocumentReference ref;
    String userID;
    Map<String, Object> userData=new HashMap<>();
    Exception exception =null;
    FirebaseStorage storage;

    public FirebaseDatabase(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getUid();
        storage= FirebaseStorage.getInstance();
    }




    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DocumentSnapshot documentSnapshot);
        void onStart();
        void onFailure(Exception e);
    }

    public void readData(String collection, String document, String source, final OnGetDataListener listener) {

        Source sourceData;
        if(source.equals("cache")){
            sourceData= Source.CACHE;
        }else if(source.equals("server")){
            sourceData=Source.SERVER;
        }else{
            sourceData=Source.DEFAULT;
        }

        listener.onStart();

        db.collection(collection)
                .document(document)
                .get(sourceData)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        listener.onSuccess(documentSnapshot);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e);
                    }
                });
    }


    public String onFailureDialog(Exception e) {
        if(e==null){
            exception=null;
            return "success";
        }
        System.err.println("error "+e.getMessage());
        return "No connection to the database";

    }

    public String failureDialog(Task task){
        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
        String error= new FirebaseDatabase().getAuthMessage(errorCode);
        if (!error.contains("Fatal")){
            return error;
        }else{
            System.err.println(error);
            return "There is an error with the database";

        }
    }

    public Map<String, Object>  getUserInfo(String source){

        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get((source =="server" || source =="SERVER")?Source.SERVER:Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        userData=document.getData();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return userData;
    }

    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    public void writeUserInfo(Map userData, String collection,String document, TaskCallback<Void> callback) {

        db.collection(collection).document(document).set(userData)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null); // Passing null as there is no specific result for success
                }).addOnFailureListener(e -> {
                    Log.e("WriteUserInfo", "Err " + e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteDocument(String collection,String document, TaskCallback<Void> callback) {
        db.collection(collection).document(document).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(null); // Passing null as there is no specific result for success
                } else {
                    // Handle errors here
                    Exception e = task.getException();
                    if (e != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            }
        });
    }
    public void updateUserInfo(Map userData, TaskCallback<Void> callback) {
        db.collection("users").document(userID).update(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null); // Passing null as there is no specific result for success
                    }
                }).addOnFailureListener(e -> {
                    Log.e("UpdateUserInfo", "Error " + e);
                    callback.onFailure(e.getMessage());
                });
    }





    public interface OnInterfaceListener {
        void onInterfaceCheckResult(boolean isTrue,String message);
    }

    public boolean isContactUnique(String contactNumber,OnInterfaceListener listener){
        DocumentReference docRef = db.collection("contact_numbers").document(contactNumber);
        docRef.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        listener.onInterfaceCheckResult(true,null);
                    } else {
                        listener.onInterfaceCheckResult(false,null);
                    }
                } else {
                    Log.d(TAG, "get failed with "+ task.getException().getLocalizedMessage());
                    listener.onInterfaceCheckResult(false,task.getException().getLocalizedMessage());
                }
            }
        });
        return false;
    }

    public String[] saveImageToLocal(Context mcoContext, String nameExtension, Bitmap bitmap, String folder){
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String path=mAuth.getUid()+"/"+folder+"/"+time.substring(0, Math.min(time.length(), 8));
        File dir = new File(mcoContext.getFilesDir(), path);
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            String fileName=(nameExtension.contains("local_"))?nameExtension:"local_"+time +"_"+nameExtension+".jpg";
            File imageFile = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            compressImage(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // Close the FileOutputStream
            fos.flush();
            fos.close();

            return new String[]{path,fileName};
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void saveFileInfoToFirestore(Map fileInfo, String folder) {
        db.collection("users/"+mAuth.getUid()+"/"+folder).document(userData.get("file_name").toString()).set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //display the image in ui
                        //save the image in storage calling syncFunction
                    }
                }).addOnFailureListener(e -> {
                    Log.e("UpdateUserInfo", "Error " + e);
                });
    }

    public void uploadFileToStorage(Map fileinfo, TaskCallback<Void> callback) {

    }

    public String isImageUploaded(Bitmap bitmap, String timestamp,OnInterfaceListener listener){

        StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child(timestamp+ ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressImage(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                listener.onInterfaceCheckResult(true,null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Excep "+exception.getLocalizedMessage());
                listener.onInterfaceCheckResult(false,exception.getLocalizedMessage());
            }
        });
        return null;
    }
    private String uploadFile(String storagePath, String localPath) {
        // Implement file upload logic using Java libraries or Android SDK
        // Make sure to return an UploadTask instance
        return null;
    }

    public Bitmap compressImage(Bitmap imageBitmap){
        Bitmap bitmap = imageBitmap;
        int maxSizeInBytes = 100 * 1024; // 100 KB

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100; // initial quality

        do {
            // Compress the Bitmap with the current quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            // Check the size of the compressed image
            int compressedSize = baos.toByteArray().length;

            // If the compressed size is greater than the maximum size, reduce the quality
            if (compressedSize > maxSizeInBytes) {
                baos.reset(); // Reset the ByteArrayOutputStream

                // Reduce the quality by a certain factor (e.g., 10% reduction)
                quality -= 10;

                // Ensure the quality doesn't go below 0
                if (quality < 0) {
                    quality = 0;
                }
            }
        } while (baos.toByteArray().length > maxSizeInBytes);
        return bitmap;
    }

    public String getAuthMessage(String errorCode){
        String errorMessage="";

        switch (errorCode) {
            case "ERROR_INVALID_CUSTOM_TOKEN":
                errorMessage="Fatal: The custom token format is incorrect. Please check the documentation.";
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                errorMessage="Fatal: The custom token corresponds to a different audience.";
                break;

            case "ERROR_INVALID_CREDENTIAL":
                errorMessage="Account does not exist or wrong email or password";
                break;

            case "ERROR_INVALID_EMAIL":
                errorMessage="Invalid email address";
                break;

            case "ERROR_WRONG_PASSWORD":
                errorMessage="Wrong email or password";
                break;

            case "ERROR_USER_MISMATCH":
                errorMessage="Fatal: The supplied credentials do not correspond to the previously signed in user.";
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                errorMessage="Fatal: This operation is sensitive and requires recent authentication. Log in again before retrying this request.";
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                errorMessage="Email already used in different sign-in method";
//                errorMessage="Fatal: An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.";
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                errorMessage="Account already exists";
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                errorMessage="Credentials already in used by different account";
                break;

            case "ERROR_USER_DISABLED":
                // Handle ERROR_USER_DISABLED
                errorMessage="Account is disabled";
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                errorMessage="Token expires, please sign-in again";
                // Handle ERROR_USER_TOKEN_EXPIRED
                break;

            case "ERROR_USER_NOT_FOUND":
                errorMessage="User not found";
                break;

            case "ERROR_INVALID_USER_TOKEN":
                errorMessage="Invalid user token, please sign-in again";
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                // Handle ERROR_OPERATION_NOT_ALLOWED
                errorMessage="Fatal: This operation is not allowed. You must enable this service in the console.";
                break;

            case "ERROR_WEAK_PASSWORD":
                errorMessage="Weak password";
                break;
            case "ERROR_MISSING_EMAIL":
                errorMessage="Please provide an email";
            default:
                errorMessage="Fatal: Unknown error";
        }


        return errorMessage;
    }
}
