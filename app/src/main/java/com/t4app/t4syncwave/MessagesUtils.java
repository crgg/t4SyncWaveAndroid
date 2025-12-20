package com.t4app.t4syncwave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.t4app.t4syncwave.conection.model.AddMemberResponse;

import java.util.Objects;

public class MessagesUtils {
    private static boolean isDialogShowing = false;
    private static AlertDialog currentDialog;

    public static void showErrorDialog(Context context, String errorMessage) {
        if (errorMessage == null) {
            return;
        }
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            Activity activity = (Activity) context;
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.normal_error_layout, null);
            builder.setView(dialogView)
                    .setCancelable(false);

            currentDialog = builder.create();
            Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
            Button ok_btn = dialogView.findViewById(R.id.ok_btn);

            if (errorMessage.contains("413 Request Entity Too Large")) {
                errorMessage = "Audio Too heavy";
            }
            tvMessage.setText(errorMessage);

            ok_btn.setOnClickListener(view -> {
                currentDialog.dismiss();
                isDialogShowing = false;
            });

            currentDialog.show();
        } catch (Exception e) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            isDialogShowing = false;
        }
    }

    public static void showSuccessDialog(Context context, String successMessage) {
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            Activity activity = (Activity) context;
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.success_message_layout, null);
            builder.setView(dialogView)
                    .setCancelable(false);

            currentDialog = builder.create();
            Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
            Button ok_btn = dialogView.findViewById(R.id.ok_btn);

            tvMessage.setText(successMessage);

            ok_btn.setOnClickListener(view -> {
                currentDialog.dismiss();
                isDialogShowing = false;
            });

            currentDialog.show();
        } catch (Exception e) {
            Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show();
            isDialogShowing = false;
        }
    }


    public static void showAddGroupLayout(Context context, ListenersUtils.AddGroupListener listener) {
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            Activity activity = (Activity) context;
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_group_layout, null);
            builder.setView(dialogView).setCancelable(false);

            currentDialog = builder.create();
            Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextInputLayout groupNameLayout = dialogView.findViewById(R.id.groupNameLayout);
            TextInputEditText groupNameValue = dialogView.findViewById(R.id.groupNameValue);
            MaterialButton addGroupBtn = dialogView.findViewById(R.id.btnAdd);
            MaterialButton cancelBtn = dialogView.findViewById(R.id.btnCancel);

            addGroupBtn.setOnClickListener(view -> {
                String nameGroup = groupNameValue.getText().toString();
                if (nameGroup.isEmpty()){
                    groupNameLayout.setError("Name Is Required");
                    groupNameValue.requestFocus();
                }else{
                    listener.onAddGroup(nameGroup);
                    currentDialog.dismiss();
                    isDialogShowing = false;
                }
            });

            cancelBtn.setOnClickListener(view -> {
                currentDialog.dismiss();
                isDialogShowing = false;
            });

            currentDialog.show();
        } catch (Exception e) {
            isDialogShowing = false;
        }
    }


    public static void showAddMemberLayout(Context context, ListenersUtils.AddMemberListener listener) {
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            Activity activity = (Activity) context;
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_member_layout, null);
            builder.setView(dialogView).setCancelable(false);

            currentDialog = builder.create();
            Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextInputLayout memberEmailLayout = dialogView.findViewById(R.id.memberEmailLayout);
            TextInputEditText memberEmailValue = dialogView.findViewById(R.id.memberEmailValue);
            MaterialButton addGroupBtn = dialogView.findViewById(R.id.btnAdd);
            MaterialButton cancelBtn = dialogView.findViewById(R.id.btnCancel);

            addGroupBtn.setOnClickListener(view -> {
                String nameGroup = memberEmailValue.getText().toString();
                if (nameGroup.isEmpty()){
                    memberEmailLayout.setError("Name Is Required");
                    memberEmailValue.requestFocus();
                }else{
                    listener.onAddMember(nameGroup);
                    currentDialog.dismiss();
                    isDialogShowing = false;
                }
            });

            cancelBtn.setOnClickListener(view -> {
                currentDialog.dismiss();
                isDialogShowing = false;
            });

            currentDialog.show();
        } catch (Exception e) {
            isDialogShowing = false;
        }
    }





}
