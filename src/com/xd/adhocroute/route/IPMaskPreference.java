package com.xd.adhocroute.route;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class IPMaskPreference extends EditTextPreference {
	public IPMaskPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public IPMaskPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public IPMaskPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		editText.setKeyListener(DigitsKeyListener.getInstance(" 0123456789."));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)"
                    + " "
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            		+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
	public  boolean validate(String addr) {
		return ipCheck(addr);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			// verify now that it's an IP
			String addr = getEditText().getText().toString();
			if (!addr.equals("") && !validate(addr)) {
				Toast.makeText(getContext(), "格式不正确", Toast.LENGTH_LONG).show();
				positiveResult = false;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}

