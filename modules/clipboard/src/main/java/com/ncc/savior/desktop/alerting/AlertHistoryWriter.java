package com.ncc.savior.desktop.alerting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class AlertHistoryWriter implements IAlertHistoryManager {
	private static final Logger logger = LoggerFactory.getLogger(AlertHistoryWriter.class);
	
    private static final String SAMPLE_CSV_FILE = "./alerts.csv";
	private BufferedWriter writer;
	private CSVPrinter csvPrinter;
	    
    public AlertHistoryWriter() throws IOException {
    	while(true) {
    		try {
        		writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));
        		break;
        	} catch (FileSystemException e) {
        		JDialog dialog = new JDialog();
        		JPanel container = new JPanel(new BorderLayout());
        		container.setBackground(Color.WHITE);
        		
        		JLabel prompt = new JLabel("Please close the alerts.csv file", JLabel.CENTER);
        		prompt.setVerticalAlignment(SwingConstants.CENTER);
        		container.add(prompt, BorderLayout.CENTER);
        		dialog.add(container);
        		
        		JButton ok = new JButton("Ok");
        		ok.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						dialog.dispose();
					}
        			
        		});
        		
        		container.add(ok, BorderLayout.SOUTH);
        		
        		dialog.setAlwaysOnTop(true);
        		dialog.setSize(new Dimension(300, 100));
        		dialog.setLocationRelativeTo(null);
        	    dialog.setModal(true);
        	    dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        	    dialog.setVisible(true);
        	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        	}
    	}
 
         csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Message", "Time"));
    }

	@Override
	public void storeAlert(BaseAlertMessage alertMessage) {
		try {
			csvPrinter.printRecord(alertMessage.getPlainTextMessage(), alertMessage.getDate());
			csvPrinter.flush();
		} catch (IOException e) {
			logger.error("Error writing to alerts csv", e);
		}
	}
}