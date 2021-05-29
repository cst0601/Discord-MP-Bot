package com.alchemist.service;

import java.awt.Color;

import com.alchemist.ArgParser;
import com.alchemist.exceptions.ArgumentParseException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Tsunomaki Jyanken game, there's no way you could win.
 * @author greg8
 *
 */
public class TsunomakiListener extends ListenerAdapter implements Service {

	private ArgParser parser = null;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		
		String msg = message.getContentDisplay();	// get readable version of the message
		
		if (event.isFromType(ChannelType.TEXT)) {
			parser = new ArgParser(msg);
			
			if (parser.getCommand().equals(">tsunomaki")) {
				try {
					parser.parse();
				} catch (ArgumentParseException e) {
					channel.sendMessage("Command format error.\n" + e.getMessage()).queue();
					return;
				}
				
				if (parser.getCommandSize() < 2) {
					channel.sendMessage("Error: Missing argument.\n"
							+ "Choose one from: :scissors: :rock: :roll_of_paper:").queue();
				}
				else {
					String decision = getTsunomakiDecision(parser.getCommand(1));
					if (decision == null)
						channel.sendMessage("Error: Not a vaild argument.").queue();
					else
						channel.sendMessage(new EmbedBuilder()
								.setTitle("角巻じゃんけんだ！", null)
								.setColor(Color.red)
								.setDescription("また負けたにぇ...")
								.addField("Result:", decision, false)
								.addBlankField(false)
								.setAuthor("角巻わため", null, "https://i.imgur.com/C8TPYb9.jpg")
								.setFooter("わためは悪くないよね～！", null)
								.build()).queue();
				}
			}
		}
	}
	
	private String getTsunomakiDecision(String playerDecision) {
		if (playerDecision.equals("✂️")) { return ":rock:"; }
		else if (playerDecision.equals("🪨")) { return ":roll_of_paper:"; }
		else if (playerDecision.equals("🧻")) { return ":scissors:"; }
		return null;
	}
	
	@Override
	public String getServiceName() {
		return "tsunomaki";
	}

	@Override
	public String getServiceMan() {
		return
				"# NAME\n"
				+ "    tsunomaki - 角巻じゃんけんだ！!\n\n";
	}
}
