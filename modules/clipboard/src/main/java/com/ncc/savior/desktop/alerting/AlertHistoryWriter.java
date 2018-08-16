package com.ncc.savior.desktop.alerting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class AlertHistoryWriter implements IAlertHistoryManager {
	private static final Logger logger = LoggerFactory.getLogger(AlertHistoryWriter.class);
	private static ImageIcon saviorIcon = new ImageIcon(AlertHistoryWriter.class.getResource("/images/saviorLogo.png"));
	
    private static final String CSV_FILE_PATH = "./alerts.csv";
	private BufferedWriter writer;
	private CSVPrinter csvPrinter;
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("M/d/yyyy    hh:mm:ss");
	
	private JDialog dialog;
	    
    public AlertHistoryWriter() throws IOException {
    	setupDialog();
       
    	if (isFileOpen()) {
        	displayDialog();
        	while (isFileOpen()) {
        	}
        	dialog.dispose();
        }

    	writer = Files.newBufferedWriter(Paths.get(CSV_FILE_PATH));
	 
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Message", "Time"));
    }
    
    public void setupDialog() {
    	dialog = new JDialog();
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        		
        JLabel prompt = new JLabel("Please close the alerts.csv file", JLabel.CENTER);
        prompt.setVerticalAlignment(SwingConstants.CENTER);
        container.add(prompt, BorderLayout.CENTER);
        dialog.add(container);
        
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (isFileOpen()) {
                	displayDialog();
                }
            }
        });
        	        		
        container.setBorder(new LineBorder(Color.BLACK, 2));
        
        dialog.setIconImage(saviorIcon.getImage());
       	dialog.setAlwaysOnTop(true);
       	dialog.setSize(new Dimension(300, 100));
       	dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public void displayDialog() {
    	dialog.setVisible(true);
    }
    
    public boolean isFileOpen() {
    	try {
    		Files.newBufferedWriter(Paths.get(CSV_FILE_PATH));
    		return false;
    	} catch (Exception e) {
    		return true;
    	}
    }

	@Override
	public void storeAlert(BaseAlertMessage alertMessage) {
		try {
			
			Date date = new Date(alertMessage.getTime());
			String formattedDate = formatter.format(date);
			csvPrinter.printRecord(alertMessage.getPlainTextMessage(), formattedDate);
			csvPrinter.flush();
		} catch (Exception e) {
			logger.error("Error writing to alerts csv", e);
		}
	}
}