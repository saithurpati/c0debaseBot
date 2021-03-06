package de.c0debase.bot.commands.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.c0debase.bot.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

/**
 * @author Biosphere
 * @date 16.04.18
 */
public class GithubCommand extends Command {

    public GithubCommand() {
        super("github", "Zeigt einige Informationen über das Repo des Bots", Category.GENERAL, "code", "source");
    }

    @Override
    public void execute(String[] args, Message msg) {
        final StringWriter writer = new StringWriter();
        try {
            URL url = new URL("https://api.github.com/repos/Biospheere/c0debaseBot");
            try (final InputStream inputStream = url.openConnection().getInputStream()) {
                IOUtils.copy(inputStream, writer, "UTF-8");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JsonObject jsonObject = new JsonParser().parse(writer.toString()).getAsJsonObject();

        EmbedBuilder embedBuilder = getEmbed(msg.getGuild(), msg.getAuthor());
        embedBuilder.setTitle("c0debaseBot", "https://github.com/Biospheere/c0debaseBot");
        embedBuilder.addField("Sprache", jsonObject.get("language").getAsString(), false);
        embedBuilder.addField("Stars", String.valueOf(jsonObject.get("stargazers_count").getAsInt()), false);
        embedBuilder.addField("Forks", String.valueOf(jsonObject.get("forks_count").getAsInt()), false);
        embedBuilder.addField("Issues", String.valueOf(jsonObject.get("open_issues_count").getAsInt()), false);
        embedBuilder.addField("Clonen", "`git clone " + jsonObject.get("ssh_url").getAsString() + "`", false);
        msg.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}
