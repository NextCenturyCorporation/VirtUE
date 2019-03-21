package com.ncc.savior.desktop.windows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.ncc.savior.util.JavaUtil;

public class WindowsApplicationLauncher {
	public static void main(String[] args) throws IOException, InterruptedException {
		String file = "c:\\virtue\\mount.txt";
		runCommandsFromFile(file, "","");
		file = "c:\\virtue\\app.txt";
		runCommandsFromFile(file, "","");
		// JavaUtil.sleepAndLogInterruption(2000);

//		Runtime.getRuntime().exec("cmd.exe /c start");

		// runApp("C:\\Windows\\System32\\cmd.exe","start","notepad.exe");
	}

	private static void runCommandsFromFile(String file, String cmdPrefix, String cmdSuffix) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			write("Running file: " +file);
			String line = null;
			while ((line = reader.readLine()) != null) {
				write("Running line: " +line);
				if (!JavaUtil.isNotEmpty(line) || line.startsWith("#")) {
					write("Skipping line");
					continue;
				}
				final String cmd = line;
				Runnable r = () -> {
					runApp(cmdPrefix+cmd+cmdSuffix);
				};
				new Thread(r).start();
			}

		} catch (Throwable t) {
			writeException(t);
		}
	}

	private static void runApp(String ...cmds) {
		runAppRuntime(cmds);
	}

	private static void writeException(Throwable t) {
		write("Error: " + t.getClass().getCanonicalName() + ":" + t.getLocalizedMessage());
	}

	private static void runAppRuntime(String... cmds) {
		try {
			String cmd = Arrays.stream(cmds).collect(Collectors.joining(" "));
			write("running command"+cmd);
//			cmd="cmd.exe /c start "+cmd;
			Process p=Runtime.getRuntime().exec(cmd);
		} catch (Throwable t) {
			writeException(t);
		}
	}

	private static void runAppProcessBuilder(String... cmds) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmds);
			processBuilder.redirectErrorStream(true);
			write("Starting process " + cmds.toString());
			Process proc = processBuilder.start();
			InputStream in = proc.getInputStream();
			BufferedReader output = new BufferedReader(new InputStreamReader(in));
			write("Starting waiting for process");
			// proc.waitFor();
			write("finished process");
			String line;
			while (null != (line = output.readLine())) {
				System.out.println(line);
				write("output: " + line);
			}
		} catch (Throwable t) {

			writeException(t);
		}
	}

	private static void write(String string) {
		try (FileWriter writer = new FileWriter("c:\\virtue\\virtue-apptest.log", true)) {
			writer.write(string);
			writer.write(System.lineSeparator());
		} catch (Throwable t) {
			System.err.println("Error writing " + t.getClass().getCanonicalName() + " " + t.getLocalizedMessage());
		}
	}
}
