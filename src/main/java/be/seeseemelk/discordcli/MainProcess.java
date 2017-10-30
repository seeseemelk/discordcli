package be.seeseemelk.discordcli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MainProcess
{
	private ProcessBuilder builder;
	private Process process;
	private InputStream stdout;
	private OutputStream stdin;
	private List<Consumer<String>> callbacks = new ArrayList<>();
	private BlockingQueue<String> lineBuffer = new LinkedBlockingQueue<>();

	public MainProcess(String processName)
	{
		builder = new ProcessBuilder(processName);
		builder.redirectErrorStream(true);
	}

	/**
	 * Starts the process.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException
	{
		if (process == null || !process.isAlive())
		{
			process = builder.start();
			stdout = process.getInputStream();
			stdin = process.getOutputStream();

			// Reading thread
			// Reads lines from the process and pushes it to lineBuffer
			Runnable runnable = () ->
			{
				try (Scanner input = new Scanner(stdout);)
				{
					while (process.isAlive())
					{
						String line = input.nextLine();
						
						try
						{
							lineBuffer.put(line);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			};
			
			Thread thread = new Thread(runnable);
			thread.start();
			
			// Post thread
			// Will periodically call the callbacks with the new messages.
			Runnable prunnable = () -> {
				if (!lineBuffer.isEmpty())
				{
					StringBuilder builder = new StringBuilder();
					
					int lines = 0;
					while (!lineBuffer.isEmpty() && lines < 50)
					{
						String line = lineBuffer.poll();
						builder.append(line).append('\n');
						lines++;
					}
					
					receivedLine(builder.toString());
				}
			};
			
			ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
			pool.scheduleWithFixedDelay(prunnable, 100L, 100L, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Adds a listener that gets executed every time a new line is read from the
	 * process output.
	 * 
	 * @param consumer The consumer that will be executed.
	 */
	public void addListener(Consumer<String> consumer)
	{
		callbacks.add(consumer);
	}

	/**
	 * Executed when a line of output becomes available from the process.
	 * 
	 * @param line The line that has been read.
	 */
	private void receivedLine(String line)
	{
		for (Consumer<String> consumer : callbacks)
		{
			consumer.accept(line);
		}
	}
	
	/**
	 * Writes a string to the program input.
	 * @param str The string to write.
	 */
	public void write(String str)
	{
		try
		{
			stdin.write(str.getBytes());
			stdin.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
