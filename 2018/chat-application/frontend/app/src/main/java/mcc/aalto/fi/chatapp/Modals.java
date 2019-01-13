package mcc.aalto.fi.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

import java.util.function.Consumer;

public abstract class Modals {
    public static void showPrompt(Context context, String title, Consumer<String> callback) {
        showPrompt(context, title, context.getResources().getString(R.string.modal_ok), callback);
    }

    public static void showPrompt(Context context, String message, String proceed, Consumer<String> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        // Input field
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // buttons
        builder.setPositiveButton(proceed, (dialog, which) -> callback.accept(input.getText().toString()));
        builder.setNegativeButton(context.getResources().getText(R.string.modal_cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static void showQuestion(Context context, String message, Consumer<Boolean> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        // buttons
        builder.setPositiveButton(context.getResources().getText(R.string.modal_ok), (dialog, which) -> callback.accept(true));
        builder.setNegativeButton(context.getResources().getText(R.string.modal_cancel), (dialog, which) -> callback.accept(false));

        builder.show();
    }

    public static void showMessage(Context context, String message) {
        showMessage(context, message, context.getResources().getString(R.string.modal_ok));
    }

    public static void showMessage(Context context, String message, String proceed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        // buttons
        builder.setPositiveButton(proceed, ((dialog, i) -> dialog.dismiss()));

        builder.show();

    }
}
