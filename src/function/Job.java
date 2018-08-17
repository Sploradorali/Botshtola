package function;

public enum Job {
	AST("Astrologian", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/f/fb/Astrologian_Icon_3.png", "457637"),
    BLM("Black Mage", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/5/51/Black_Mage_Icon_3.png", "815958"),
    BRD("Bard", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/b/b3/Bard_Icon_3.png", "815958"),
    DRG("Dragoon", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/2/21/Dragoon_Icon_3.png", "815958"),
    DRK("Dark Knight", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/b/bd/Dark_Knight_Icon_3.png", "455ccb"),
    MCH("Machinist", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/9/99/Machinist_Icon_3.png", "815958"),
    MNK("Monk", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/f/f9/Monk_Icon_3.png", "815958"),
    NIN("Ninja", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/c/c8/Ninja_Icon_3.png", "815958"),
    PLD("Paladin", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/6/66/Paladin_Icon_3.png", "455ccb"),
    RDM("Red Mage", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/6/64/Red_Mage_Icon_3.png", "815958"),
    SAM("Samurai", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/9/98/Samurai_Icon_3.png", "815958"),
    SCH("Scholar", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/e/e0/Scholar_Icon_3.png", "457637"),
    SMN("Summoner", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/f/f6/Summoner_Icon_3.png", "815958"),
    WAR("Warrior", JobType.DOW, "https://ffxiv.gamerescape.com/w/images/1/16/Warrior_Icon_3.png", "455ccb"),
    WHM("White Mage", JobType.DOM, "https://ffxiv.gamerescape.com/w/images/d/db/White_Mage_Icon_3.png", "457637"),
    ALC("Alchemist", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/d/db/Alchemist_Icon_9.png", "424242"),
    ARM("Armorer", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/4/41/Armorer_Icon_9.png", "424242"),
    BOT("Botanist", JobType.DOL, "https://ffxiv.gamerescape.com/w/images/2/24/Botanist_Icon_9.png", "424242"),
    BSM("Blacksmith", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/e/e1/Blacksmith_Icon_9.png", "424242"),
    CRP("Carpenter", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/7/73/Carpenter_Icon_9.png", "424242"),
    CUL("Culinarian", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/7/73/Carpenter_Icon_9.png", "424242"),
    FSH("Fisher", JobType.DOL, "https://ffxiv.gamerescape.com/w/images/0/0f/Fisher_Icon_9.png", "424242"),
    GSM("Goldsmith", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/1/1f/Goldsmith_Icon_9.png", "424242"),
    LTW("Leatherworker", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/0/06/Leatherworker_Icon_9.png", "424242"),
    MIN("Miner", JobType.DOL, "https://ffxiv.gamerescape.com/w/images/3/33/Miner_Icon_9.png", "424242"),
    WVR("Weaver", JobType.DOH, "https://ffxiv.gamerescape.com/w/images/0/06/Leatherworker_Icon_9.png", "424242");

    private String name;
    private JobType type;
    private String imgUrl;
    private String colorHex;

    Job(String name, JobType type, String imgUrl, String colorHex) {
        this.name = name;
        this.type = type;
        this.imgUrl = imgUrl;
        this.colorHex = colorHex;
    }

    public String getName() {
        return name;
    }

    public JobType getType() {
        return type;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getColorHex() {
        return colorHex;
    }

    private enum JobType {
        DOW("Disciple of War"),
        DOM("Disciple of Magic"),
        DOH("Disciple of the Hand"),
        DOL("Disciple of the Land");

        private String name;

        JobType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
