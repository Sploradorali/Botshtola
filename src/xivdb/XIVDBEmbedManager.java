package xivdb;

import function.Job;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.awt.*;
import java.util.Map;

public class XIVDBEmbedManager {

    private XIVDBEmbedManager() {
    }

    /**
     * Builds and sends an embed reflecting character information
     *
     * @param name
     * @param title
     * @param image
     * @param server
     * @param gc
     * @param race
     * @param clan
     * @param gender
     * @param url
     * @param icon
     * @param url2
     * @param id
     * @param channel
     */
    protected static void sendCharacterSearchEmbed(String name, String title,
                                                   String image, String server, String gc, String race, String clan,
                                                   String gender, String url, String icon, String url2, long id,
                                                   MessageChannel channel) {
        channel.sendMessage(
                new EmbedBuilder()
                        .setTitle(title.equals(" ") ? name : name + " | " + title)
                        .setColor(Color.LIGHT_GRAY)
                        .setThumbnail(icon)
                        .setImage(image)
                        .setDescription(
                                server + " | " + gc + "\n"
                                        + race + " " + clan + " (" + gender + ")\n\n"
                                        + "[View on XIVDB](" + url + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }

    /**
     * Sends all character jobs as an Embed.
     *
     * @param name
     * @param server
     * @param currentJob
     * @param classes
     * @param ch
     */
    protected static void sendCharacterClassesEmbed(String name, String server, String currentJob,
                                                    Map<String, Integer> classes, MessageChannel ch) {
        String iconUrl = "https://ffxiv.gamerescape.com/w/images/7/71/Main_Command_3_Icon.png";
        for (Job j : Job.values()) {
            if (j.getName().equals(currentJob)) {
                iconUrl = j.getImgUrl();
                break;
            }
        }


        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(name + " | " + server);
        builder.setThumbnail(iconUrl);
        builder.addField("Current Job", currentJob, false);

        Object[] classAbbrs = classes.keySet().toArray();

        for (int i = 0; i < classes.size(); i++) {

            String key = String.valueOf(classAbbrs[i]);
            int value = classes.get(classAbbrs[i]);
            String valueStr = value == 0 ? "N/A" : "Lv. " + String.valueOf(value);

            builder.addField(key, valueStr, true);
        }

        ch.sendMessage(builder.build()).queue();

    }

    /**
     * Builds and sends an embed reflecting item information
     *
     * @param id
     * @param name
     * @param rarity
     * @param category
     * @param itemLevel
     * @param equipLevel
     * @param icon
     * @param url
     * @param url2
     * @param channel
     */
    protected static void sendItemSearchEmbed(
            long id, String name, int rarity, String category, int itemLevel,
            int equipLevel, String icon, String url, String url2,
            MessageChannel channel
    ) {
        StringBuilder raritySB = new StringBuilder();
        for (int i = 0; i < rarity; i++) {
            raritySB.append(":star:");
        }

        String rarityStars = raritySB.toString();

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setThumbnail(icon)
                        .setTitle(name)
                        .setDescription(
                                rarityStars + "\n\n"
                                        + category + "\n"
                                        + "Item level " + itemLevel + " | "
                                        + "Equip level " + equipLevel + "\n\n"
                                        + "[View on XIVDB](" + url + ") | "
                                        + "[More results](" + url2 + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }

    /**
     * Builds and sends an embed reflecting quest information
     *
     * @param id
     * @param name
     * @param genre
     * @param level
     * @param category
     * @param icon
     * @param url
     * @param url2
     * @param channel
     */
    protected static void sendQuestSearchEmbed(
            long id, String name, String genre, int level,
            String category, String icon, String url, String url2,
            MessageChannel channel
    ) {

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.YELLOW)
                        .setThumbnail(icon)
                        .setTitle(name)
                        .setDescription(
                                genre + " | Level " + level + "\n\n"
                                        + category + "\n\n"
                                        + "[View on XIVDB](" + url + ") | "
                                        + "[More results](" + url2 + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }

    /**
     * Builds and sends an embed reflecting FATE information
     *
     * @param id
     * @param name
     * @param level
     * @param icon
     * @param url
     * @param url2
     * @param channel
     */
    protected static void sendFateSearchEmbed(
            long id, String name, int level, String icon, String url, String url2,
            MessageChannel channel
    ) {

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.MAGENTA)
                        .setThumbnail(icon)
                        .setTitle(name)
                        .setDescription(
                                "Level " + level + "\n\n"
                                        + "[View on XIVDB](" + url + ") | "
                                        + "[More results](" + url2 + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }

    protected static void sendAchievementSearchEmbed(
            long id, String name, String category, String icon, String url, String url2,
            MessageChannel channel
    ) {

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setThumbnail(icon)
                        .setTitle(name)
                        .setDescription(
                                category + "\n\n"
                                        + "[View on XIVDB](" + url + ") | "
                                        + "[More results](" + url2 + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }

    /**
     * Builds and sends an Embed reflecting instance information
     *
     * @param id
     * @param name
     * @param category
     * @param level
     * @param icon
     * @param url
     * @param url2
     * @param channel
     */
    protected static void sendInstanceSearchEmbed(
            long id, String name, String category, int level, String icon, String url, String url2, MessageChannel channel
    ) {
        String levelStr = String.valueOf(level);
        if (level == 0) levelStr = "N/A";

        category = (category.equals("null") ? "Misc" : category);

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.DARK_GRAY)
                        .setThumbnail(icon)
                        .setTitle(name)
                        .setDescription(
                                category
                                        + " | Level: " + levelStr + "\n\n"
                                        + "[View on XIVDB](" + url + ") | "
                                        + "[More results](" + url2 + ")"
                        )
                        .setFooter("www.xivdb.com - ID: " + id, null)
                        .build()
        ).queue();
    }
}
