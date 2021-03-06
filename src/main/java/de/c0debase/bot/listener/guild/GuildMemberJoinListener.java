package de.c0debase.bot.listener.guild;

import de.c0debase.bot.CodebaseBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Biosphere
 * @date 23.01.18
 */
public class GuildMemberJoinListener extends ListenerAdapter {


    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setFooter("@" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator(), event.getMember().getUser().getEffectiveAvatarUrl());
            embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
            embedBuilder.setThumbnail(event.getMember().getUser().getEffectiveAvatarUrl());
            embedBuilder.appendDescription("Willkommen auf c0debase " + event.getMember().getAsMention() + "\n");
            embedBuilder.appendDescription("— Weise dir eine Rolle mit !role zu\n");
            embedBuilder.appendDescription("— Schaue dir die Regeln in #rules an");


           event.getGuild().getTextChannelById(System.getenv("BOTCHANNEL")).sendMessage(embedBuilder.build()).queue();

            event.getGuild().getTextChannelsByName("log", true).forEach(channel -> {
                EmbedBuilder logBuilder = new EmbedBuilder();
                logBuilder.setFooter("@" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator(), event.getMember().getUser().getEffectiveAvatarUrl());
                logBuilder.setThumbnail(event.getMember().getUser().getEffectiveAvatarUrl());
                logBuilder.appendDescription("Erstelldatum: " + event.getUser().getCreationTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + "\n");
                logBuilder.appendDescription("Standart Avatar: " + (event.getMember().getUser().getAvatarUrl() == null) + "\n");
                channel.sendMessage(logBuilder.build()).queue();
            });
        CodebaseBot.getInstance().getMongoDataManager().getLevelUser(event.getGuild().getId(), event.getUser().getId(), levelUser -> {
            List<Role> roles = new ArrayList<>();
            levelUser.getRoles().forEach(roleName -> {
                Role role = event.getGuild().getRoleById(roleName);
                if(role != null && PermissionUtil.canInteract(event.getGuild().getSelfMember(), role)){
                    roles.add(role);
                }
            });
            event.getGuild().getController().addRolesToMember(event.getMember(), roles).reason("Autorole").queue();
        });
        CodebaseBot.getInstance().getMongoDataManager().getActivity(LocalDateTime.now().getDayOfMonth(), LocalDateTime.now().getYear(), event.getGuild().getId(), activity -> {
            activity.setMembers(event.getGuild().getMembers().size());
            CodebaseBot.getInstance().getMongoDataManager().updateActivity(activity);
        });
    }
}
