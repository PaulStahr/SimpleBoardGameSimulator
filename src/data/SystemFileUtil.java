package data;

public class SystemFileUtil {
    private static String folder = "cardgamesimulator";

	public static final String defaultProgramDirectory()
	{
		return SystemFileUtil.defaultDirectory() + '/' + '.' + folder;
	}
	
	public static final String defaultDirectory(){
        final String os = System.getProperty("os.name").toUpperCase();
        if (os.contains("WIN"))
            return System.getenv("APPDATA");
        if (os.contains("MAC"))
            return System.getProperty("user.home") + "/Library/Application Support";
        if (os.contains("NUX"))
            return System.getProperty("user.home");
        return System.getProperty("user.dir");
    }
}
