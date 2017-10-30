package be.seeseemelk.discordcli;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

public class Discord
{
	private JDA jda;
	private TextChannel channel;
	private List<Consumer<GuildMessageReceivedEvent>> callbacks = new ArrayList<>();
	private String rolename;
	
	public Discord(String token, String rolename)
	{
		try
		{
			this.rolename = rolename;
			
			jda = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
			AnnotatedEventManager manager = new AnnotatedEventManager();
			manager.register(this);
			jda.setEventManager(manager);
			
			String url = jda.asBot().getInviteUrl(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE);
			System.out.format("Invite url = %s%n", url);
		}
		catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e)
		{
			System.err.println("Failed to login");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set the text channel that the bot will listen to.
	 * @param channelId The id of the channel the bot will listen to.
	 */
	public void setTextChannel(String channelId)
	{
		channel = jda.getTextChannelById(channelId);
	}
	
	/**
	 * Adds a callback to the message receiver.
	 * @param consumer The function that will be called when a message is received.
	 */
	public void addMessageListener(Consumer<GuildMessageReceivedEvent> consumer)
	{
		callbacks.add(consumer);
	}
	
	/**
	 * Post a message to the text channel.
	 * @param message The message to write.
	 */
	public void postMessage(String message)
	{
		channel.sendMessage(message).submit();
	}
	
	/**
	 * Checks if the certain user may execute a command.
	 * @return {@code true} if the user may execute the command, {@code false}
	 */
	public boolean mayExecute(Member member)
	{
		for (Role role : member.getRoles())
		{
			if (role.getName().equalsIgnoreCase(rolename))
			{
				return true;
			}
		}
		return false;
	}
	
	@SubscribeEvent
	public void onMessageReceived(GuildMessageReceivedEvent event)
	{
		if (event.getChannel().equals(channel) && !event.getAuthor().equals(jda.getSelfUser()))
		{
			Member member = event.getMember();
			if (mayExecute(member))
			{
				for (Consumer<GuildMessageReceivedEvent> consumer : callbacks)
				{
					consumer.accept(event);
				}
			}
			else
			{
				event.getMessage().delete().submit();
			}
		}
	}
}














