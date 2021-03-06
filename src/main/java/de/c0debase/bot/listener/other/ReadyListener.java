package de.c0debase.bot.listener.other;

import de.c0debase.bot.CodebaseBot;
import de.c0debase.bot.tempchannel.Tempchannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @author Biosphere
 * @date 23.01.18
 */

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Set<Class<? extends ListenerAdapter>> classes = new Reflections("de.c0debase.bot.listener").getSubTypesOf(ListenerAdapter.class);
        classes.forEach(listenerClass -> {
            if(listenerClass != ReadyListener.class){
                try {
                    event.getJDA().addEventListener(listenerClass.newInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        if(!event.getJDA().getGuildsByName("c0debase", true).isEmpty()){
            for (VoiceChannel voiceChannel : event.getJDA().getGuildsByName("c0debase", true).get(0).getVoiceChannels()) {
                String name = ("temp-" + voiceChannel.getName().toLowerCase()).replaceAll("\\s+", "-");
                final TextChannel textChannel = voiceChannel.getGuild().getTextChannelsByName(name, true).isEmpty() ? null : voiceChannel.getGuild().getTextChannelsByName(name, true).get(0);
                if (textChannel == null) {
                    CodebaseBot.getInstance().getTempchannels().put(voiceChannel.getId(), new Tempchannel());
                } else {
                    Tempchannel tempchannel = new Tempchannel(textChannel);
                    tempchannel.onLoad(textChannel, voiceChannel);
                    CodebaseBot.getInstance().getTempchannels().put(voiceChannel.getId(), tempchannel);
                }
            }
        }
    }
}