
package helper.classes;

        import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.stayalert.CameraActivity;
        import com.example.stayalert.DetectionLogsInfo;
        import com.example.stayalert.R;

public class ViewDetectedImageHelper {
    Dialog dialog;
    TextView type, timestamp, accuracy, location, inference, title;
    private ImageButton closeBtn;
    private DetectionLogsInfo info;

    public ViewDetectedImageHelper(Context context) {
        instantiate(context);

    }


    public interface DialogClickListener {
        void onOkayClicked();
    }

    private DialogClickListener dialogClickListener;

    public ViewDetectedImageHelper(Context context, DialogClickListener listener) {


        instantiate(context);
        // Set the listener for the cancel button
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onOkayClicked();
                }
                dialog.dismiss();
                dismissDialog();
            }
        });


        this.dialogClickListener = listener;



    }
    public void instantiate(Context context){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.fragment_view_detection_image);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        closeBtn=dialog.findViewById(R.id.detectionViewCloseBtn);
        type = dialog.findViewById(R.id.detectionLogsType);
        timestamp= dialog.findViewById(R.id.detectionLogsDate);
        accuracy= dialog.findViewById(R.id.detectionLogsAccuracy);
        location= dialog.findViewById(R.id.detectionLogsLocation);
        inference= dialog.findViewById(R.id.detectionLogsInference);
        title=dialog.findViewById(R.id.detectionLogsTitle);
    }

    public void showDialog(DetectionLogsInfo info){
        type.setText(info.getDetectionType());
        timestamp.setText(info.getTimestamp());
        location.setText(info.getLocation());
        accuracy.setText(info.getAccuracy());
        inference.setText(info.getInference());
        title.setText(info.getTitle());

        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}