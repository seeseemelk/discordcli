package be.seeseemelk.discordcli;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class Config
{
	private String token;
	private String channelId;
	private String program;
	private String role;
	
	public Config(File file) throws InvalidFileFormatException, IOException
	{
		Ini ini = new Ini(file);
		token = ini.get("Discord", "token");
		channelId = ini.get("Discord", "channel_id");
		program = ini.get("Discord", "program");
		role = ini.get("Discord", "role");
		
		if (token == null)
		{
			throw new NullPointerException("Could not find [Discord].token value");
		}
		else if (channelId == null)
		{
			throw new NullPointerException("Could not find [Discord].channel_id value");
		}
	}
	
	/**
	 * Get the login token that is used to login to the bot.
	 * @return The token to use.
	 */
	public String getToken()
	{
		return token;
	}
	
	/**
	 * The id of the channel that will be used for I/O. 
	 * @return The id of the text channel.
	 */
	public String getTextChannelId()
	{
		return channelId;
	}
	
	/**
	 * The name of the program to run.
	 * @return The program to run.
	 */
	public String getProgram()
	{
		return program;
	}
	
	/**
	 * The required role to interact with the bot.
	 * @return The role required to interact with the bot.
	 */
	public String getRole()
	{
		return role;
	}
}
