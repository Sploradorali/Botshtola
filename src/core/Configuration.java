package core;

public class Configuration {
	private String token;
	private String dbPath;
    private String server;
    private boolean check_status;
    private String check_channel;
    private int check_frequency;

    /**
     * Creates a new configuration instance using config file
     *
     * @return
     */
    public static Configuration getConfig() {
    	
        Configuration config = new Configuration();

        File configFile = new File("../Botshtola/config.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String current;
            while ((current = br.readLine()) != null) {
                if (current.startsWith("#token")) {
                    config.setToken(current.substring(current.indexOf(":") + 1));
                } else if (current.startsWith("#dbpath")) {
                	config.setDbPath(current.substring(current.indexOf(":") + 1));
                } else if (current.startsWith("#server")) {
                    config.setServer(current.substring(current.indexOf(":") + 1));
                } else if (current.startsWith("#check_status")) {
                    config.setCheck_status(Boolean.valueOf(current.substring(current.indexOf(":") + 1)));
                } else if (current.startsWith("#check_channel")) {
                    config.setCheck_channel(current.substring(current.indexOf(":") + 1));
                } else if (current.startsWith("#check_frequency")) {
                    config.setCheck_frequency(Integer.parseInt(current.substring(current.indexOf(":") + 1)));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isCheck_status() {
        return check_status;
    }

    public void setCheck_status(boolean check_status) {
        this.check_status = check_status;
    }

    public String getCheck_channel() {
        return check_channel;
    }

    public void setCheck_channel(String check_channel) {
        this.check_channel = check_channel;
    }

    public int getCheck_frequency() {
        return check_frequency;
    }

    public void setCheck_frequency(int check_frequency) {
        this.check_frequency = check_frequency;
    }

    public String getDbPath() {
		return dbPath;
	}

    public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}
}
