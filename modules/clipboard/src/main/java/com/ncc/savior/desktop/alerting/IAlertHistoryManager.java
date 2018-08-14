package com.ncc.savior.desktop.alerting;

import java.io.IOException;

public interface IAlertHistoryManager {

	void storeAlert(BaseAlertMessage alertMessage) throws IOException;
	
}
