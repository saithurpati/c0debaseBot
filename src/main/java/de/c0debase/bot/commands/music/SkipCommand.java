package de.c0debase.bot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.c0debase.bot.CodebaseBot;
import de.c0debase.bot.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.concurrent.TimeUnit;

/**
 * @author Biosphere
 * @date 23.01.18
 */
public class SkipCommand extends Command {

    public SkipCommand() {
        super("skip", "Überspringe einen Track", Category.MUSIC, "next");
    }

    @Override
    public void execute(String[] args, Message msg) {
        EmbedBuilder embedBuilder = getEmbed(msg.getGuild(), msg.getAuthor());

        if (msg.getMember().getVoiceState().inVoiceChannel() && msg.getMember().getVoiceState().getChannel().getMembers().contains(msg.getGuild().getSelfMember())) {
            CodebaseBot.getInstance().getMusicManager().skip(msg.getGuild());
            if (CodebaseBot.getInstance().getMusicManager().getPlayingTrack(msg.getGuild()) != null) {
                AudioTrackInfo trackInfo = CodebaseBot.getInstance().getMusicManager().getPlayingTrack(msg.getGuild()).getInfo();
                String length;
                if (TimeUnit.MILLISECONDS.toHours(trackInfo.length) >= 24) {
                    length = String.format("%dd %02d:%02d:%02d", TimeUnit.MILLISECONDS.toDays(trackInfo.length), TimeUnit.MILLISECONDS.toHours(trackInfo.length) % 24, TimeUnit.MILLISECONDS.toMinutes(trackInfo.length) % 60, TimeUnit.MILLISECONDS.toSeconds(trackInfo.length) % 60);
                } else {
                    length = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(trackInfo.length) % 24, TimeUnit.MILLISECONDS.toMinutes(trackInfo.length) % 60, TimeUnit.MILLISECONDS.toSeconds(trackInfo.length) % 60);
                }
                embedBuilder.addField(trackInfo.title, "`" + trackInfo.author + " - " + (trackInfo.isStream ? "STREAM" : length) + "`", false);
            } else {
                embedBuilder.setDescription("Es gibt kein weiteres Lied in der Warteschlange");
            }
        } else {
            embedBuilder.setDescription("Du bist in keinem Voicechannel mit dem Bot");
        }
        msg.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}
