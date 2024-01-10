package firebase.classes;

import android.widget.EditText;
import android.widget.TextView;

import com.example.stayalert.env.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.IdentityHashMap;
import java.util.Map;

public class FirebaseDatabase {
    private static final Logger LOGGER = new Logger();
    FirebaseAuth mAuth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    CollectionReference collection;
    FirebaseFirestore db;
    DocumentReference ref;
    String userID;
    Map<String, Object> userData=null;

    public FirebaseDatabase(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getUid();
    }


    public String onFailureDialog(Exception e) {
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

    public Map<String, Object>  getUserInfo(){
        userData= new IdentityHashMap<>();
        ref = db.collection("users").document(userID);
        System.out.println("id "+userID);
        ref.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                System.out.println("exist");
                userData = documentSnapshot.getData();
            }else{

            }
        }).addOnFailureListener(e -> {
            System.out.println("Error" +onFailureDialog(e));
        });
        return userData;
    }

    public boolean insertDB(String collection){
        return false;
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
                errorMessage="Wrong email or password";
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
