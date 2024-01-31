package helper.classes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayalert.CameraActivity;
import com.example.stayalert.R;

public class DialogHelper {
    private Dialog dialog;
    private TextView dialogTitle;
    private TextView dialogInfo,dialogOkay, dialogAction;

    public DialogHelper(Context context) {
        instantiate(context);
    }

    public interface DialogClickListener {
        void onOkayClicked();
        void onActionClicked();
    }

    private DialogClickListener dialogClickListener;

    public DialogHelper(Context context, DialogClickListener listener) {
        instantiate(context);
        // Set the listener for the cancel button
        dialogOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onOkayClicked();
                }
                dialog.dismiss();
                normalDialog();
            }
        });

        dialogAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onActionClicked();
                }
                dialog.dismiss();
                normalDialog();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (dialogClickListener != null) {
                    normalDialog();
                }
            }
        });

        this.dialogClickListener = listener;
    }

    public void instantiate(Context context){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.pop_up_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        dialogTitle = dialog.findViewById(R.id.TVTitle);
        dialogInfo = dialog.findViewById(R.id.TVInfo);
        dialogOkay = dialog.findViewById(R.id.TVCancel);
        dialogAction =  dialog.findViewById(R.id.TVAction);
    }

    public void showDialog(String title, String info) {
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void showTestImage(Bitmap bitmap){
        ImageView imageView= dialog.findViewById(R.id.testImage);
        imageView.setImageBitmap(bitmap);
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void logoutDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Logout");
    }

    public void signUpDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Sign up");
    }

    public void signInDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Sign in");
    }

    public void discardDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Discard");
    }


    public void normalDialog(){

        dialogAction.setVisibility(View.GONE);
        dialogOkay.setText("Okay");
    }
}
