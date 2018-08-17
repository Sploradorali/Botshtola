package xivdb;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import core.Responder;
import database.DBInteraction;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class XIVDBCommandCenter {

    private static final String SEARCH_URL = "https://api.xivdb.com/search?string=";
    private static final String API_URL = "https://api.xivdb.com/";
    private static final String SITE_URL = "https://xivdb.com/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) "
            + "AppleWebKit/537.11 (KHTML, like Gecko) "
            + "Chrome/23.0.1271.95 Safari/537.11";

    /**
     * Generates an HttpURLConnection object for the XIVDB API
     *
     * @param query
     * @return
     * @throws IOException
     */
    private static HttpURLConnection establish(String query) throws IOException {
        query = query.replace(" ", "+");
        HttpURLConnection con = (HttpURLConnection) (new URL(SEARCH_URL + query)).openConnection();
        con.addRequestProperty("User-Agent", USER_AGENT);
        return con;
    }

    /**
     * Retrieves the JSON array against which the search result will be checked
     *
     * @param in
     * @param category
     * @return
     * @throws IOException
     */
    private static JSONArray getJSONArray(InputStream in, String category) throws IOException {
        JSONTokener tokener = new JSONTokener(in);
        JSONObject root = new JSONObject(tokener);
        return root.getJSONObject(category).getJSONArray("results");
    }

    /**
     * Searches XIVDB for a character, specified by optional server name following a hash symbol.
     *
     * @param query
     * @param ch
     */
    public static HttpURLConnection charSearch(String query, MessageChannel ch) {

        String serverSelect = null;
        if (query.split(" ").length > 1 && query.split(" ")[0].startsWith("#")) {
            serverSelect = query.split(" ")[0].substring(1, 2).toUpperCase() + query.split(" ")[0].substring(2);
            query = query.substring(query.indexOf(" ") + 1);
            System.out.println(serverSelect);
            System.out.println(query);
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "characters");

            if (root.length() == 1) {
                int id = root.getJSONObject(0).getInt("id");
                URL singleURL = new URL(API_URL + "character/" + id);
                HttpURLConnection singleCon = (HttpURLConnection) singleURL.openConnection();
                singleCon.addRequestProperty("User-Agent", USER_AGENT);

                return singleCon;

            } else if (root.length() > 1) {
                System.out.println("mult found");
                if (serverSelect != null) {
                    System.out.println("server selected");
                    for (int i = 0; i < root.length(); i++) {
                        JSONObject ob = root.getJSONObject(i);
                        if (serverSelect.equals(ob.getString("server"))) {
                            charSearch(String.valueOf(ob.getInt("id")), ch);
                            System.out.println(String.valueOf(ob.getInt("id")));
                            break;
                        }
                    }
                } else {
                    ch.sendMessage("Looks like a common name... Let's be a little specific.\n"
                            + "```Tip: Add your server name after # "
                            + "with your full character name!\n"
                            + "Example: " + Responder.getPrefix() + "char #behemoth douran cul'hwali```")
                            .queue();
                }
            }
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }
        return null;
    }

    /**
     * Retrieves character details and sends as an Embed.
     *
     * @param query
     * @param ch
     */
    public static void getCharacterSummary(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "char #OPTIONAL-SERVER FIRST-NAME LAST-NAME```")
                    .queue();
            return;
        }

        HttpURLConnection con = charSearch(query, ch);

        if (con == null) return;

        try (InputStream singleIn = con.getInputStream()) {
            JSONTokener singleTokener = new JSONTokener(singleIn);
            JSONObject character = new JSONObject(singleTokener);

            int id = character.getInt("lodestone_id");
            String name = character.getString("name");
            String server = character.getString("server");
            String title = character.getJSONObject("data").getString("title");
            String gc = character.getJSONObject("data").getJSONObject("grand_company").getString("name");
            String icon = character.getJSONObject("data").getJSONObject("grand_company").getString("icon");
            String gender = character.getJSONObject("data").getString("gender");
            String race = character.getJSONObject("data").getString("race");
            String clan = character.getJSONObject("data").getString("clan");
            String image = character.getString("portrait");
            String url = "https://xivdb.com/character/" + id;


            XIVDBEmbedManager.sendCharacterSearchEmbed(name, title, image, server,
                    gc, race, clan, gender, url, icon, url, id, ch);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void getCharacterClasses(String query, MessageReceivedEvent ev) {
        if (query.isEmpty()) {
            ev.getTextChannel().sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "classes CHARACTER_ID```")
                    .queue();
            return;
        } else if (query.equals("#me")) {
            query = String.valueOf(DBInteraction.getUserCharacter(ev.getAuthor().getIdLong()));
        }

        HttpURLConnection con = charSearch(query, ev.getTextChannel());
        if (con == null) return;

        try (InputStream singleIn = con.getInputStream()) {
            JSONTokener singleTokener = new JSONTokener(singleIn);
            JSONObject character = new JSONObject(singleTokener);

            String name = character.getString("name");
            String server = character.getString("server");

            JSONObject classjobs = character.getJSONObject("data").getJSONObject("classjobs");

            LinkedHashMap<String, Integer> jobs = new LinkedHashMap<>();


            // Figure out how to dynamically iterate
            for (int i = 1;i < 40; i++) {
                try {
                    JSONObject job = classjobs.getJSONObject(String.valueOf(i));
                    jobs.put(job.getJSONObject("data").getString("abbr"), job.getInt("level"));
                } catch (JSONException jex) {}
            }

            XIVDBEmbedManager.sendCharacterClassesEmbed(name, server,
                    character.getJSONObject("data")
                            .getJSONObject("active_class")
                            .getJSONObject("role")
                            .getString("name"), jobs, ev.getTextChannel());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Searches for an item on XIVDB
     *
     * @param query
     * @param ch
     */
    public static void itemSearch(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "item ruby tide astrometer```")
                    .queue();
            return;
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "items");
            JSONObject result = root.getJSONObject(0);

            long id = result.getLong("id");
            String name = result.getString("name");
            int rarity = result.getInt("rarity");
            String category = result.getString("category_name");
            int itemLevel = result.getInt("level_item");
            int equipLevel = result.getInt("level_equip");
            String icon = result.getString("icon");

            String url = SITE_URL + "item/" + result.getLong("id");
            String url2 = SITE_URL + "?search=" + query;

            XIVDBEmbedManager.sendItemSearchEmbed(id, name, rarity, category, itemLevel,
                    equipLevel, icon, url, url2, ch);
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }

    }

    /**
     * Searches for a quest on XIVDB
     *
     * @param query
     * @param ch
     */
    public static void questSearch(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "quest heavensward```")
                    .queue();
            return;
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "quests");
            JSONObject result = root.getJSONObject(0);

            long id = result.getLong("id");
            String name = result.getString("name");
            String genre = result.getString("genre_name");
            int level = result.getInt("class_level_1");
            String category = result.getString("classjob_category_1");
            String icon = result.getString("icon");

            String url = SITE_URL + "quest/" + result.getLong("id");
            String url2 = SITE_URL + "?search=" + query;

            XIVDBEmbedManager.sendQuestSearchEmbed(id, name, genre, level, category,
                    icon, url, url2, ch);
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }
    }

    /**
     * Searches for a FATE on XIVDB
     *
     * @param query
     * @param ch
     */
    public static void fateSearch(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "quest steel reign```")
                    .queue();
            return;
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "fates");
            JSONObject result = root.getJSONObject(0);

            long id = result.getLong("id");
            String name = result.getString("name");
            int level = result.getInt("class_level");
            String icon = result.getString("icon");

            String url = SITE_URL + "fate/" + result.getLong("id");
            String url2 = SITE_URL + "?search=" + query;

            XIVDBEmbedManager.sendFateSearchEmbed(id, name, level, icon, url, url2, ch);
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }
    }

    /**
     * Searches for an achievement on XIVDB
     *
     * @param query
     * @param ch
     */
    public static void achievementSearch(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "quest i got the magic stick iv```")
                    .queue();
            return;
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "achievements");
            JSONObject result = root.getJSONObject(0);

            long id = result.getLong("id");
            String name = result.getString("name");
            String category = result.getString("category_name");
            String icon = result.getString("icon");

            String url = SITE_URL + "achievement/" + result.getLong("id");
            String url2 = SITE_URL + "?search=" + query;

            XIVDBEmbedManager.sendAchievementSearchEmbed(id, name, category, icon, url, url2, ch);
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }
    }

    /**
     * Searches for an instance, such as a dungeon, raid, or trial, on XIVDB
     *
     * @param query
     * @param ch
     */
    public static void instanceSearch(String query, MessageChannel ch) {
        if (query.isEmpty()) {
            ch.sendMessage("Please tell me what to look for!\n"
                    + "```Example: " + Responder.getPrefix() + "instance sastasha```")
                    .queue();
            return;
        }
        query = query.replace(" ", "+");

        try (InputStream in = establish(query).getInputStream()) {
            JSONArray root = getJSONArray(in, "instances");
            JSONObject result = root.getJSONObject(0);

            long id = result.getLong("id");
            String name = result.getString("name");
            int level = result.getInt("level");
            String category = String.valueOf(result.get("content_name"));
            String icon = result.getString("icon");

            String url = SITE_URL + "instance/" + result.getLong("id");
            String url2 = SITE_URL + "?search=" + query;

            XIVDBEmbedManager.sendInstanceSearchEmbed(id, name, category, level, icon, url, url2, ch);
        } catch (IOException ex) {
            System.out.print("IOException! " + ex.getMessage());
        } catch (JSONException jex) {
            System.out.print("JSONException! " + jex.getMessage());
        }
    }

    /**
     * Sets a member's own character
     *
     * @param characterId
     * @param memberId
     * @param member
     * @param ch
     */
    public static void setOwnCharacter(long characterId, long memberId, Member member, MessageChannel ch) {
        int update = DBInteraction.mapUserToChar(memberId, characterId);
        if (update == 0) {
            ch.sendMessage(member.getNickname() + " changed their character!").queue();
        } else if (update == 1) {
            ch.sendMessage(member.getNickname() + " set their character!").queue();
        } else {
            ch.sendMessage(member.getNickname() + ", I wasn't able to locate that character...").queue();
        }
    }

    /**
     * Gets a member's own character and prints an embed
     *
     * @param memberId
     * @param ch
     */
    public static void getCharacter(long memberId, Member member, MessageChannel ch) {
        long characterId = DBInteraction.getUserCharacter(memberId);
        if (characterId > 0) {
            getCharacterSummary(String.valueOf(characterId), ch);
        } else {
            ch.sendMessage("Sorry, " + member.getNickname() +
                    ", I couldn't find a character that belongs to you.")
                    .queue();
        }
    }


    /**
     * Clears a user's character profile from the database
     *
     * @param member
     * @param ch
     */
    public static void clearOwnCharacter(Member member, MessageChannel ch) {
        DBInteraction.deleteUserCharacter(member.getUser().getIdLong());
        ch.sendMessage(member.getNickname() + ", I got rid of your character information.")
                .queue();
    }
}
