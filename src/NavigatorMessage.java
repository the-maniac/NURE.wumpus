import aima.core.environment.wumpusworld.WumpusPercept;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NavigatorMessage extends WumpusPercept {
    public ArrayList<String> BREEZE = new ArrayList<String>(Arrays.asList("I feel breeze here", "There is a breeze", "It’s a cool breeze here"));
    public ArrayList<String> STENCH = new ArrayList<String>(Arrays.asList("The indescribable stench", "Horrible stench", "The cave emits a stench"));
    public ArrayList<String> GLITTER = new ArrayList<String>(Arrays.asList("glitter of diamonds", "I see glitter in the cave", "magic crystal glitter seen"));
    public ArrayList<String> BUMP = new ArrayList<String>(Arrays.asList("There was a gentle bump", "The bump sounded again", "Maybe I'll bump"));
    public ArrayList<String> SCREAM = new ArrayList<String>(Arrays.asList("I heard a gurgled scream", "scream was inhuman", "Some scream"));

    public NavigatorMessage(String message) {
        if (message.toLowerCase().contains("Stench".toLowerCase())) {
            this.setStench();
        }
        if (message.toLowerCase().contains("Breeze".toLowerCase())) {
            this.setBreeze();
        }
        if (message.toLowerCase().contains("Glitter".toLowerCase())) {
            this.setGlitter();
        }
        if (message.toLowerCase().contains("Bump".toLowerCase())) {
            this.setBump();
        }
        if (message.toLowerCase().contains("Scream".toLowerCase())) {
            this.setScream();
        }
    }

    @Override
	public String toString() {
		ArrayList<String> result = new ArrayList<String>();
		Random r = new Random();
		int i = r.nextInt(3);
		if (isStench())
			result.add(STENCH.get(i));
		if (isBreeze())
			result.add(BREEZE.get(i));
		if (isGlitter())
			result.add(GLITTER.get(i));
		if (isBump())
			result.add(BUMP.get(i));
		if (isScream())
			result.add(SCREAM.get(i));

		if (!result.isEmpty() && result.size() > 1) {
		    return String.join(" AND ", result);
        }

		if (!result.isEmpty()) {
		    return String.join("", result);
        }
		return "I feel nothing";
	}
}
