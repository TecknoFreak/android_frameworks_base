/*
 * Copyright (C) 2012 The Android Open Source Project
 * This code has been modified. Portions copyright (C) 2013, ParanoidAndroid Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keyguard;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView.OnEditorActionListener;
import android.provider.Settings;

/**
 * Displays a PIN pad for unlocking.
 */
public class KeyguardPINView extends KeyguardAbsKeyInputView
        implements KeyguardSecurityView, OnEditorActionListener, TextWatcher {

    private boolean mQuickUnlock;

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void resetState() {
        if (KeyguardUpdateMonitor.getInstance(mContext).getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_pin_instructions, false);
        }
        mPasswordEntry.setEnabled(true);
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.pinEntry;
    }

    @Override
    protected boolean getQuickUnlockAllowed() {
        return mQuickUnlock;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

		mQuickUnlock = (Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.PSX_LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
		
        final View ok = findViewById(R.id.key_enter);
        if (ok != null) {
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            ok.setOnHoverListener(new LiftToActivateListener(getContext()));
        }

        // The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
        // not a separate view
        View pinDelete = findViewById(R.id.delete_button);
        if (pinDelete != null) {
            pinDelete.setVisibility(View.VISIBLE);
            pinDelete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        CharSequence str = mPasswordEntry.getText();
                        if (str.length() > 0) {
                            mPasswordEntry.setText(str.subSequence(0, str.length()-1));
                        }
                    }
                    doHapticKeyClick();
                }
            });
            pinDelete.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        mPasswordEntry.setText("");
                    }
                    doHapticKeyClick();
                    return true;
                }
            });
        }

        mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
        mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mPasswordEntry.requestFocus();
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_pin;
    }
}
