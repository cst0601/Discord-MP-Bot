package com.alchemist.service;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alchemist.ArgParser;
import com.alchemist.HoloMember;


public class BonkListener extends ListenerAdapter implements Service {
	
	private Logger logger = LoggerFactory.getLogger(BonkListener.class);
	private final long bonkEmoteId;	// TODO: remove this literal constant
	private Emote emote = null;
	
	public BonkListener() {
		long emote_id = 0;	// if exception, emote id shall be 0
		try {
			Scanner scanner = new Scanner(new File("config/bonk.json"));
			scanner.useDelimiter("\\Z");
			JSONObject json = new JSONObject(scanner.next());
			scanner.close();
			
			emote_id = json.getLong("bonk_emote_id");
		} catch (FileNotFoundException e) {
			logger.error("config/bonk.json is missing.");
		}
		bonkEmoteId = emote_id;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		
		String msg = message.getContentDisplay();
		
		if (event.isFromType(ChannelType.TEXT)) {
			ArgParser parser = new ArgParser(msg);
			
			if (parser.getCommand().equals(">bonk")) {
				
				emote = message.getGuild().getEmoteById(bonkEmoteId);
				
				if (emote != null) {
					List<Member> mentionedMember = new LinkedList<>(message.getMentionedMembers());
					getTargetMessage(channel, message.getId(), mentionedMember);
				}
				else {
					logger.warn(String.format(
							"Requested from guild: %s, does not contain designated emote.",
							message.getGuild().getId()));
					channel.sendMessage("Error: This sever does not contain the designated bonk emote.").queue();
				}
				
			}
		}
	}
	
	private void getTargetMessage(
			MessageChannel channel,
			String messageId,
			List<Member> targetMember)
	{	
		// this is a rest action that waits for completion, might make this method to thread
		MessageHistory history = MessageHistory.getHistoryBefore(channel, messageId)
				.limit(10).complete();
		logger.warn("Message retrieve done!");
		for(Message message: history.getRetrievedHistory()) {
			logger.warn("searching...: " + message.getContentDisplay());
			if (containMember(targetMember, message.getMember())) {
				logger.info("target aquired");
				targetMember.remove(message.getMember());
				message.addReaction(emote).queue(
						null,
						new ErrorHandler().handle(
								EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
								(ex) -> channel.sendMessage("Action failed due to missing permission.")
							)
						);
			}
		}
	}
	
	private boolean containMember(List<Member> targets, Member member) {
		for (Member target: targets)
			if (target.getUser().getName().equals(
					member.getUser().getName()))
				return true;
		
		return false;
	}

	@Override
	public String getServiceName() {
		return "bonk";
	}

	@Override
	public String getServiceMan() {
		return 
			"# NAME\n"
			+ "    bonk - bonk a member!\n\n"
			+ "# SYNOPSIS\n"
			+ "    bonk @member\n";
	}

}
