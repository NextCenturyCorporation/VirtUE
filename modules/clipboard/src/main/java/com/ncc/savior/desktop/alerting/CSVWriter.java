package com.ncc.savior.desktop.alerting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVWriter {
    private static final String SAMPLE_CSV_FILE = "./alerts.csv";
	private static BufferedWriter writer;
	private static CSVPrinter csvPrinter;
	    
    public CSVWriter() throws IOException {
    	 writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));

         csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Message", "Time"));
    }
    
    public static void write(BaseAlertMessage alertMessage) throws IOException {
    	 csvPrinter.printRecord(alertMessage.getPlainTextMessage(), alertMessage.getDate());
    	 
         csvPrinter.flush();    
    }
}