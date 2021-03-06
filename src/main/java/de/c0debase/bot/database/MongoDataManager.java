package de.c0debase.bot.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.c0debase.bot.CodebaseBot;
import de.c0debase.bot.database.data.Activity;
import de.c0debase.bot.database.data.LevelUser;
import de.c0debase.bot.utils.Constants;
import de.c0debase.bot.utils.Pagination;
import net.jodah.expiringmap.ExpiringMap;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Biosphere
 * @date 27.04.18
 */
public class MongoDataManager {

    private final ExecutorService executorService;
    private final MongoDatabaseManager mongoDatabaseManager;
    private final Map<String, LevelUser> userCache;
    private final Map<String, Pagination> leaderboardCache;
    private final JsonWriterSettings jsonWriterSettings;

    public MongoDataManager() {
        this.executorService = Executors.newCachedThreadPool();
        jsonWriterSettings = JsonWriterSettings.builder()
                .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
                .build();
        mongoDatabaseManager = new MongoDatabaseManager(System.getenv("MONGO-HOST"), System.getenv("MONGO-PORT") == null ? 27017 : Integer.valueOf(System.getenv("MONGO-PORT")), null, null);
        final ExpiringMap.Builder<Object, Object> mapBuilder = ExpiringMap.builder();
        mapBuilder.maxSize(123).expiration(1, TimeUnit.MINUTES).build();
        leaderboardCache = mapBuilder.build();
        userCache = mapBuilder.build();
    }


    public void getLevelUser(final String guildID, final String userID, final Consumer<LevelUser> consumer) {
        if (userCache.containsKey(guildID + "-" + userID)) {
            consumer.accept(userCache.get(guildID + "-" + userID));
            return;
        }
        executorService.execute(() -> {
            final Document document = mongoDatabaseManager.getUsers().find(Filters.and(Filters.eq("guildID", guildID), Filters.eq("userID", userID))).first();
            final LevelUser levelUser;
            if (document == null) {
                levelUser = new LevelUser();
                levelUser.setGuildID(guildID);
                levelUser.setLevel(0);
                levelUser.setCoins(0.0);
                levelUser.addXP(50);
                levelUser.setCoins(levelUser.getXp() * 0.05);
                levelUser.setLastMessage(System.currentTimeMillis());
                levelUser.setUserID(userID);
                levelUser.setRoles(new ArrayList<>());
                mongoDatabaseManager.getUsers().insertOne(Document.parse(Constants.GSON.toJson(levelUser)));
            } else {
                levelUser = Constants.GSON.fromJson(document.toJson(jsonWriterSettings), LevelUser.class);
                if(levelUser.getCoins() == null){
                    levelUser.setCoins(levelUser.getXp() * 0.01);
                }
            }
            userCache.put(guildID + "-" + userID, levelUser);
            consumer.accept(levelUser);
        });
    }

    public void updateLevelUser(final LevelUser levelUser) {
        executorService.execute(() -> {
            mongoDatabaseManager.getUsers().replaceOne(Filters.and(Filters.eq("guildID", levelUser.getGuildID()), Filters.eq("userID", levelUser.getUserID())), Document.parse(Constants.GSON.toJson(levelUser)));
            userCache.put(levelUser.getGuildID() + "-" + levelUser.getUserID(), levelUser);
        });
    }

    public void getLeaderboard(final String guildID, final Consumer<Pagination> consumer) {
        if (leaderboardCache.containsKey(guildID)) {
            consumer.accept(leaderboardCache.get(guildID));
            return;
        }
        executorService.execute(() -> {
            List<LevelUser> levelUsers = new ArrayList<>();
            FindIterable<Document> document = mongoDatabaseManager.getUsers()
                    .find(Filters.eq("guildID", guildID))
                    .sort(Sorts.descending("level", "xp"));

            for (Document aDocument : document) {
                levelUsers.add(Constants.GSON.fromJson(aDocument.toJson(jsonWriterSettings), LevelUser.class));
            }
            levelUsers.sort((o1, o2) -> {
                if (o1.getLevel() != o2.getLevel()) {
                    return Double.compare(o2.getLevel(), o1.getLevel());
                } else {
                    return Double.compare((o2.getXp() + o2.getLevel() + (1000 * o2.getLevel() * 1.2)), (o1.getXp() + o1.getLevel() + (1000 * o1.getLevel() * 1.2)));
                }
            });

            Pagination pagination = new Pagination(levelUsers, 10);
            leaderboardCache.put(guildID, pagination);
            consumer.accept(pagination);
        });
    }

    public void getActivity(int day, int year,  String guildID, Consumer<Activity> consumer){
        executorService.execute(() -> {
            Document document = mongoDatabaseManager.getActivity().find(Filters.and(Filters.eq("guildID", guildID), Filters.eq("day", day), Filters.eq("year", year))).first();
            Activity activity;
            if(document == null){
                activity = new Activity();
                activity.setDay(day);
                activity.setYear(year);
                activity.setGuildID(guildID);
                activity.setMessages(0);
                activity.setUsers(new ArrayList<>());
                activity.setChannel(new HashMap<>());
                if(CodebaseBot.getInstance().getJda().getGuildById("361448651748540426") != null){
                    activity.setMembers(CodebaseBot.getInstance().getJda().getGuildById("361448651748540426").getMembers().size());
                } else {
                    activity.setMembers(0);
                }
                mongoDatabaseManager.getActivity().insertOne(Document.parse(Constants.GSON.toJson(activity)));
            } else {
                activity = Constants.GSON.fromJson(document.toJson(jsonWriterSettings), Activity.class);
                if(activity.getMembers() == null){
                    if(CodebaseBot.getInstance().getJda().getGuildById("361448651748540426") != null){
                        activity.setMembers(CodebaseBot.getInstance().getJda().getGuildById("361448651748540426").getMembers().size());
                    } else {
                        activity.setMembers(0);
                    }
                }
            }
            consumer.accept(activity);
        });
    }

    public void updateActivity(Activity activity) {
        executorService.execute(() -> mongoDatabaseManager.getActivity().replaceOne(Filters.and(Filters.eq("guildID", activity.getGuildID()), Filters.eq("day", activity.getDay()), Filters.eq("year", activity.getYear())), Document.parse(Constants.GSON.toJson(activity))));
    }

}
