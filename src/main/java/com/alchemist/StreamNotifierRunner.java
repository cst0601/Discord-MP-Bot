package com.alchemist;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

/**
 * Workhorse of stream notification
 * @author greg8
 *
 */
public class StreamNotifierRunner extends Thread {
	public StreamNotifierRunner(JDA jda, String memberName,
			long messageChannelId, long pingId) {
		logger = LoggerFactory.getLogger(StreamNotifierRunner.class);
		api = new HoloToolsApi();
		upcommingStreams = new LinkedList<UpcommingStream>();
		targetChannel = jda.getTextChannelById(messageChannelId);
		pingRole = jda.getRoleById(pingId);
		this.memberName = memberName;
	}
	
	public void run() {
		updateUpcomingStreams();	// init upcoming stream list
		notifyUpcomingStreams(false);
		
		while (true) {
			String message;
			if ((message = messageBox.poll()) != null) {
				if (message.equals("stop")) {
					logger.info("Terminaing stream notifier runner...");
					break;
				}
			}
			else {
				updateUpcomingStreams();
				notifyUpcomingStreams(true);
				try {
					sleep(60000);	// 1 min
				} catch (InterruptedException e) {
					logger.info("Runner sleep interrupted");
				}
			}
		}
	}
	
	public void sendMessage(String message) throws InterruptedException {
		messageBox.put(message);
	}
	
	private int containsUpcomingStream(UpcommingStream newStream) {
		for (int i = 0; i < upcommingStreams.size(); ++i) {
			if (upcommingStreams.get(i).getStreamUrl().equals(newStream.getStreamUrl()))
				return i;
		}
		return -1;
	}
	
	private void updateUpcomingStreams() {
		try {
			for (LiveStream stream: api.getStreamOfMember(memberName, "upcoming")) {
				UpcommingStream upcommingStream = new UpcommingStream(stream, pingRole);
				
				int streamIndex = containsUpcomingStream(upcommingStream);
				if (streamIndex != -1) {	// do possible update to existing streams
					upcommingStreams.get(streamIndex).updateStreamStartTime(stream);
				}
				else {	// add new stream
					upcommingStreams.add(upcommingStream);
					logger.info("New upcoming stream " + upcommingStream.getStreamUrl());
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("Failed to update upomming streams.");
		}
	}
		
	private void notifyUpcomingStreams(boolean sendMessage) {
		ListIterator<UpcommingStream> iter = upcommingStreams.listIterator();
		while(iter.hasNext()) {
			UpcommingStream stream = iter.next();
			Message message = stream.broadcast();
			if (message != null && sendMessage) {
				targetChannel.sendMessage(message).queue();
				logger.info("Notified stream: " + stream.getStreamUrl());	
			}
			if (stream.hasStarted()) {
				logger.info("Remove started stream: " + stream.getStreamUrl());
				iter.remove();
			}
		}
	}
	
	private Logger logger;
	private BlockingQueue<String> messageBox = new LinkedBlockingQueue<String>();
	private HoloToolsApi api;
	private String memberName;
	private MessageChannel targetChannel;
	private Role pingRole;
	private List<UpcommingStream> upcommingStreams;
}
