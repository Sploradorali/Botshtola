# Botshtola
FFXIV utility Discord bot using the JDA library

This bot was initially designed for the sole use of a specific Free Company Discord, but is undergoing continuous changes to improve
public scalability. Feel free to request a pull if you'd like to try and help, or if you'd like to try using it for your own server. 
Some functionality will not work without extra DIY. For example, the instance of this bot on the original Discord server utilizes
a JavaDB/Apache Derby embedded database, which is not included in the repository. A configuration (config.txt) is also required. 
Because of this, for anyone looking for a full-featured FFXIV utility bot, there are many great public bots out there!

<h2> August 15, 2018 UPDATE - Y’shtola (1.2.0)</h2>

<h3>Summary</h3>

Basic scheduling functionality, crafting and gathering roles, FFXIV server status check functionality, a FFXIV weekly reset timer, 
extra XIVDB commands, and a couple of bug fixes. Also, this is actually a complete rewrite of the original bot and the source code 
is now available here on Github. 

<h3>New Commands and Command Updates</h3>

* Y’shtola will now notify the Discord server when Behemoth goes down and comes back up, as well as display the server status live on 
her “Playing” tag.
* Crafting and gathering roles are now active with the role functions. You can use !addjob with the appropriate acronym as before 
(ex. MIN for Miner).
* !seejobs *CHARACTER_ID* - Retrieves an embed of a character’s classes and levels.
* !myjobs - Retrieves an embed of a user’s own character.
* !achievement – Searches XIVDB for an achievement.
* !instance – Searches XIVDB for an instance (i.e. dungeon, raid, etc.).
* !serverstatus – Checks the status of an FFXIV server (default is Behemoth).
* !timetoreset – Checks the time to the next weekly reset.

<h3>Scheduling</h3>

* I created a new section of the bot dedicated to creating timed events. The new FFXIV companion app will have a calendar 
as well, but I decided to proceed with this in case it’s easier for some people to plan things right on Discord. If you 
join an event, Y’shtola will send you a notification 30 minutes prior to the event time in your direct messages. It’s a 
bit barebones, but I will continue working on it if people still want to use it despite the companion app’s release.
* !newevent – Creates a new event; type by itself for a small tutorial.
* !event *EVENT ID* - Looks up information about an event.
* !cancelevent *EVENT ID* – Cancels an event.
* !joinevent *EVENT ID* - Joins an event’s roster.
* !leaveevent *EVENT ID* - Leaves an event’s roster.
* !myevents – Lists events you’re participating in.

<h3>Fixes and Miscellaneous</h3>

* The welcome was slightly altered to encourage newbies to use initial role commands in #bot-center.

<h3>Plans and WIP</h3>

* Some commands, like the newevent command, appear a little clunky. I’m actively taking suggestions on how to improve things like 
this, unless I’m the only one bothered by how cluttered it looks.
* I’ve gotten requests for maintenance and event timers. Unfortunately, those are a little more involved to implement, as I haven’t 
been able to find a publicly updated database to easily interface to – unlike weekly reset, maintenance days are often sporadic and 
irregular. The easiest solution, in terms of coding, is to make a command for admins that allow them to manually enter maintenance 
days. Once I make sure there isn’t a more dynamic, reliable solution, I’ll start working on it.
* Audio features are on the eventual to-do list, but Rythm is great already, so not really on my short-term radar.
* The bot isn’t reliably scalable to other servers, like KupoBot. I want to kind of work on this eventually, but KupoBot and SerAymeric 
already provide extensible, public solutions for FFXIV servers, so this is low on my priority list. 
