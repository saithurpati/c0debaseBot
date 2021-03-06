package de.c0debase.bot.commands.general;

import de.c0debase.bot.commands.Command;
import de.c0debase.bot.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.time.format.DateTimeFormatter;

/**
 * @author Biosphere
 * @date 17.03.18
 */
public class UserinfoCommand extends Command {

    public UserinfoCommand() {
        super("userinfo", "Zeigt ein paar Infos über einen Nutzer", Category.GENERAL);
    }

    @Override
    public void execute(String[] args, Message message) {
        Member member = args.length == 0 ? message.getMember() : searchMember(args[0], message.getMember());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setThumbnail(member.getUser().getAvatarUrl());
        embedBuilder.addField("Name", StringUtils.replaceCharacter(member.getUser().getName()), true);
        embedBuilder.addField("Nickname", member.getNickname() == null ? StringUtils.replaceCharacter(member.getUser().getName()) : StringUtils.replaceCharacter(member.getNickname()), true);
        embedBuilder.addField("Status", member.getOnlineStatus().getKey(), true);
        embedBuilder.addField("Spiel", member.getGame() != null ? member.getGame().getName() : "---", true);
        embedBuilder.addField("Rollen", String.valueOf(member.getRoles().size()), true);
        embedBuilder.addField("Beitritt", member.getJoinDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), true);
        embedBuilder.addField("Erstelldatum: ", member.getUser().getCreationTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), true);
        embedBuilder.addField("Standart Avatar: ", String.valueOf(member.getUser().getAvatarUrl() == null), true);


        message.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}
