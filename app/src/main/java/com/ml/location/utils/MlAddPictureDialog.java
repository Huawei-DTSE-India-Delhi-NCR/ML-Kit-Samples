package com.ml.location.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ml.location.R;

public class MlAddPictureDialog extends Dialog implements View.OnClickListener {
    private TextView tvTakePicture;
    private TextView tvSelectImage;
    private TextView tvExtend;
    private Context context;
    private ClickListener clickListener;

    public interface ClickListener {
        /**
         * Take picture
         */
        void takePicture();

        /**
         * Select picture from local
         */
        void selectImage();

        /**
         * Extension method
         */
        void doExtend();
    }

    public MlAddPictureDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initViews();
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.dialog_add_picture, null);
        this.setContentView(view);

        this.tvTakePicture = view.findViewById(R.id.take_photo);
        this.tvSelectImage = view.findViewById(R.id.select_image);
        this.tvExtend = view.findViewById(R.id.extend);
        this.tvTakePicture.setOnClickListener(this);
        this.tvSelectImage.setOnClickListener(this);
        this.tvExtend.setOnClickListener(this);

        this.setCanceledOnTouchOutside(true);
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialogWindow.setAttributes(layoutParams);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
        if (this.clickListener == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.take_photo) {
            this.clickListener.takePicture();
        } else if (id == R.id.select_image) {
            this.clickListener.selectImage();
        } else if (id == R.id.extend) {
            this.clickListener.doExtend();
        }
    }
}
