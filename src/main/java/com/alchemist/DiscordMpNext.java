package com.alchemist;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

import com.alchemist.service.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class DiscordMpNext {
	private JDA jda;
	private Logger logger;
	
	public static void main(String[] args) {
		try {
			new DiscordMpNext().startUp(args);
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads some confidential informations [Discord bot token / youtube api
	 * key] and creates JDA
	 * @throws LoginException
	 */
	private void startUp (String args[]) throws LoginException {
		logger = LoggerFactory.getLogger(DiscordMpNext.class);
		// load token from config
		Properties properties = new Properties();
		try {
			properties.load(DiscordMpNext.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to read from config.properties, please" +
					     "check if the file exists.");
			return;
		}
		
		String token = properties.getProperty("token", null);
		String ytKey = properties.getProperty("yt_key", null);
		
		try {
			JDABuilder builder = JDABuilder.createDefault(token)
					.addEventListeners(new PingListener())
					.addEventListeners(new RollListener())
					.addEventListeners(new ManualListener())
					.addEventListeners(new VtubeListener(ytKey))
					.addEventListeners(new AboutListener())
					.addEventListeners(new BonkListener())
					.addEventListeners(new CountDownListener())	// special event
					.addEventListeners(new StreamNotifierService())
					.addEventListeners(new TsunomakiListener())
					.setActivity(Activity.of(Activity.ActivityType.DEFAULT,
								 			 "Say >man to seek help!"));
			
			builder = buildTwitterBroadcaster(builder, args);				
			
			jda = builder.build();
			jda.awaitReady();
			
			logger.info("Finish building JDA!");
		} catch (InterruptedException e) {
			// await is a blocking method, if interrupted
			e.printStackTrace();
		} catch (LoginException e) {
			// things go wrong in authentication
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks twitter mode and add twitter listener to JDA if mode is true
	 * @param builder
	 * @param args
	 * @return
	 */
	private JDABuilder buildTwitterBroadcaster(JDABuilder builder, String args[]) {
		boolean twitterMode = true;
		
		for (String arg: args) {
			if (arg.equals("noTwitter")) {
				logger.info("Twitter mode is false");
				twitterMode = false;
				break;
			}
		}
		
		if (twitterMode) {
			builder.addEventListeners(new TwitterBroadcastService());
		}
		
		return builder;
	}
	
}
