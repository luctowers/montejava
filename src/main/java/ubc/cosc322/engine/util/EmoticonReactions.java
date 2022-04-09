package ubc.cosc322.engine.util;

import java.util.Random;

public class EmoticonReactions {

	private static Random random = new Random();
	private static int lastReaction = -1;

	private static String[] neutral = new String[] {
		"(-_-;)",
		"(~_~;)",
		"((d[-_-]b))",
		"(・_・;)",
		"(@_@)",
		"(^0_0^)",
		"☉_☉",
		"¯\\(°_o)/¯",
		"(゜-゜)",
		"(・_・ヾ",
		"o_O",
		"(¬_¬)",
		"(?_?)"
	};

	private static String[] happy = new String[] {
		"(^_^;)",
		"(/◕ヮ◕)/",
		"\\(^o^)/",
		"(｡◕‿◕｡)",
		"(づ｡◕‿‿◕｡)づ",
		"~(˘▾˘~)",
		"ヘ( ^o^)ノ",
		"(. ❛ ᴗ ❛.)",
		"｡^‿^｡"
	};

	private static String[] angry = new String[] {
		"(ಠ益ಠ)",
		"(ಠ_ಠ)",
		"ლ(ಠ益ಠ)ლ",
		"(╬⓪益⓪)",
		"(☄ฺ◣д◢)☄ฺ",
		"(; ･`д･´)"
	};

	private static String[] sad = new String[] {
		"(T_T)",
		"(;_;)",
		"Q_Q",
		"(｡•́︿•̀｡)"
	};

	private static String[] tableFlip = new String[] {
		"(╯°□°）╯︵ ┻━┻",
		"┻━┻ ︵ \\( °□° )// ︵ ┻━┻",
		"ʕノ•ᴥ•ʔノ ︵ ┻━┻",
		"(ﾉಥ益ಥ）ﾉ ┻━┻",
		"(╯°Д°）╯︵ /(.□ . \\)"
	};

	public static String generateReaction(double condfidence, double confidenceDelta) {
		if (confidenceDelta <= -0.05) {
			return randomReaction(tableFlip);
		}
		if (condfidence <= 0.30) {
			return randomReaction(sad);
		}
		if (condfidence <= 0.45) {
			return randomReaction(angry);
		}
		if (condfidence >= 0.55) {
			return randomReaction(happy);
		}
		return randomReaction(neutral);
	}

	private static String randomReaction(String[] array) {
		int index;
		do {
			index = random.nextInt(array.length);;
		} while (index == lastReaction);
		lastReaction = index;
		return array[index];
	}
	
}
