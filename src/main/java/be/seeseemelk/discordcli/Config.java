package be.seeseemelk.discordcli;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class Config
{
	private String token;
	private String channelId;
	
	public Config(File file) throws InvalidFileFormatException, IOException
	{
		Ini ini = new Ini(file);
		token = ini.get("Discord", "token");
		channelId = ini.get("Discord", "channel_id");
		
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
}
