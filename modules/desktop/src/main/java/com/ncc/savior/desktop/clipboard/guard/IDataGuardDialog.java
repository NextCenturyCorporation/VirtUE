package com.ncc.savior.desktop.clipboard.guard;

import com.ncc.savior.desktop.clipboard.guard.CopyPasteDialog.IDialogListener;

public interface IDataGuardDialog {

	public void show(String source, String destination);

	public void setLoginEventListener(IDialogListener listener);

}