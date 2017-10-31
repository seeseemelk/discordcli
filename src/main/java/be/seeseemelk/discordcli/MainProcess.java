package be.seeseemelk.discordcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
	private Thread thread;

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

			ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
			
			// Reading thread
			// Reads lines from the process and pushes it to lineBuffer
			Runnable runnable = () ->
			{
				try
				{
					try (BufferedReader input = new BufferedReader(new InputStreamReader(stdout));)
					{
						while (process.isAlive())
						{
							while (!input.ready() && process.isAlive())
								Thread.sleep(30);
							
							if (process.isAlive())
							{
								String line = input.readLine();
								lineBuffer.put(line);
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				catch (IllegalStateException | InterruptedException e)
				{
				}
				finally
				{
					System.out.println("Stopping process");
					pool.shutdown();
					
					if (process.isAlive())
					{
						process.destroyForcibly();
					}
					else
					{
						// Program probably exited unexpectedly.
						try
						{
							start();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			};
			
			thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.start();
			
			// Post thread
			// Will periodically call the callbacks with the new messages.
			Runnable prunnable = () -> {
				if (!lineBuffer.isEmpty())
				{
					StringBuilder builder = new StringBuilder();
					
					while (!lineBuffer.isEmpty() && builder.length() < 500)
					{
						String line = lineBuffer.peek();
						if (builder.length() + line.length() + 1 < 500)
						{
							line = lineBuffer.poll();
							builder.append(line).append('\n');
						}
					}
					
					receivedLine(builder.toString());
				}
			};
			
			pool.scheduleWithFixedDelay(prunnable, 100L, 100L, TimeUnit.MILLISECONDS);
		}
	}
	
	public void stop()
	{
		thread.interrupt();
		
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		process = null;
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
