package be.seeseemelk.discordcli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ini4j.InvalidFileFormatException;

public class DiscordCLI
{
	private Config config;
	private Discord discord;
	private MainProcess process;

	public DiscordCLI(File configFile) throws InvalidFileFormatException, IOException
	{
		config = new Config(configFile);
		
		process = new MainProcess("sh");
		process.start();
		
		discord = new Discord(config.getToken(), config.getRole());
		discord.setTextChannel(config.getTextChannelId());
		
		discord.addMessageListener(e ->
		{
			String message = e.getMessage().getContent();
			if (message.equals("!"))
			{
				try
				{
					process.stop();
					process.start();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
			else
			{
				message += "\n";
				process.write(message);
			}
		});
		
		process.addListener(line -> {
			if (line.isEmpty())
			{
				discord.postMessage(" ");
			}
			else
			{
				discord.postMessage("```" + line + "```");
			}
		});
	}

	public static void main(String[] args)
	{
		final Options options = new Options();
		options.addRequiredOption("c", "config", true, "The config file to use.");

		CommandLineParser parser = new DefaultParser();
		try
		{
			CommandLine cl = parser.parse(options, args);
			String filename = cl.getOptionValue('c');
			File file = new File(filename);
			if (file.isFile())
			{
				try
				{
					new DiscordCLI(file);
				}
				catch (InvalidFileFormatException e)
				{
					System.err.println("Failed to parse configuration file: " + e.getMessage());
				}
				catch (IOException e)
				{
					System.err.println("Could not read configuration file");
					e.printStackTrace();
				}
			}
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

}
