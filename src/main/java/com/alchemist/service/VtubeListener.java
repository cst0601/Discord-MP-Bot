package com.alchemist.service;

import java.awt.Color;
import java.util.ArrayList;

import com.alchemist.ContentFactory;
import com.alchemist.LiveStream;
import com.alchemist.Schedule;
import com.alchemist.YoutubeApi;
import com.alchemist.HoloApi;
import com.alchemist.jsonResponse.JsonResponse;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * VtubeListener
 * Message commands related to vtube
 * @author greg8
 *
 */
public class VtubeListener extends ListenerAdapter implements Service {
	private YoutubeApi api;
	private HoloApi holoApi;
	
	public VtubeListener(String key) {
		api = new YoutubeApi(key);
		holoApi = new HoloApi();
	}
	
	public String contentFormat() {
		return "";
	}
	
	/**
	 * Parse the command by splitting it by space or endings
	 * @param command
	 * @return parsed arguments
	 */
	public String[] parseArgv (String command) {
		return command.split("\\s+");
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// JDA jda = event.getJDA();	// core stuff jda
		
		// User author = event.getAuthor();
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		
		String msg = message.getContentDisplay();
		// boolean isBot = author.isBot();
		
		if (event.isFromType(ChannelType.TEXT)) {
			// Guild guild = event.getGuild();	// Not used in this case
			// TextChannel textChannel = event.getTextChannel();
			
			String [] commandVector = parseArgv(msg);
			if (commandVector[0].equals(">holo")) {
				if (commandVector.length < 2) {
					channel.sendMessage(new MessageBuilder("Error: Usage: ")
							.append(">holo <member>", MessageBuilder.Formatting.BLOCK)
							.append(".\nUse ")
							.append(">holo list", MessageBuilder.Formatting.BLOCK)
							.append(" to get a full list of available members.\n")
							.append(">holo schedules", MessageBuilder.Formatting.BLOCK)
							.append(" to get schedules of today.")
							.build()).queue();
					return;
				}
				
				try {
					JsonResponse response = api.request(commandVector[1]);
					if (commandVector[1].equals("list")) {
						
						String members = "";
						for (String name: api.getAvailableMembers())
							members += " - " + name + "\n";
						
						channel.sendMessage(new EmbedBuilder()
							.setTitle(">holo list")
							.setColor(Color.red)
							.addField("List of availble members", members, false)
							.setFooter("35P | Chikuma", "https://i.imgur.com/DOb1GZ1.png")
							.build()).queue();
					}
					
					else if (commandVector[1].equals("schedules")) {
						try {
							ArrayList<Schedule> schedules = holoApi.request();
							String scheduleStr = "";
							for (Schedule schedule: schedules)
								scheduleStr += " - " + schedule.toString() + "\n";
							
							channel.sendMessage(new EmbedBuilder()
									.setTitle(">holo schedule")
									.setColor(Color.red)
									.addField("Schedules of " + schedules.get(0).getDate(),
											scheduleStr ,false)
									.setFooter("updated@" + holoApi.getUpdateTime() +
											" | All showed time are in JST")
									.build()).queue();
						} catch (Exception e) {
							channel.sendMessage("Looks like schedule api went on vacation.  :((\n"
									+ "Contact admin to get help.").queue();
						}
					}
					
					else if (response != null) {	// if arg member name not avaliable
						LiveStream liveStream = new ContentFactory().createLiveStream(api.request(commandVector[1]).getBody());
					
						if (liveStream == null) 
							channel.sendMessage("目前並沒有直播 :(").queue();
						
						else
							channel.sendMessage("目前的直播：\n" + liveStream.getTitle() + "\n" + liveStream.toString()).queue();
					}
					
					else {
						channel.sendMessage(new MessageBuilder("Error: member not found.\n")
							.append("Use ")
							.append(">holo list", MessageBuilder.Formatting.BLOCK)
							.append(" to get a full list of available members.")
							.build()).queue();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	public String getServiceName() {
		return "holo";
	}

	@Override
	public String getServiceMan() {
		return
			"# NAME\n"
			+ "    holo - Hololive event tracker\n\n"
			+ "# SYNOPSIS\n"
			+ "    holo <command> [args]\n\n"
			+ "# COMMANDS\n"
			+ "    * list: List all available hololive members.\n"
			+ "    * <member_name>: Fetch streams currently going on.\n"
			+ "    * schedules: Get all schedules of today (JST).\n";
	}
}
