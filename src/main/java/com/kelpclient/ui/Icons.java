package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

/** 16x16 pixel-art icons (outlined and bevel-shaded), drawn with fills so the mod needs no texture files. */
public final class Icons {
	private static final Map<String, String[]> M = new HashMap<>();
	private static final Map<Character, Integer> P = new HashMap<>();

	private static void def(String k, String... rows) { M.put(k, rows); }

	static {
		P.put('A', 0xFF0A1411);
		P.put('B', 0xFF9BF5B4);
		P.put('C', 0xFF3CD16B);
		P.put('D', 0xFF1E8A45);
		P.put('E', 0xFFC99A5B);
		P.put('F', 0xFFA6D8FF);
		P.put('G', 0xFF3C5547);
		P.put('H', 0xFF4AA3F0);
		P.put('I', 0xFF15221E);
		P.put('J', 0xFF2B6CB0);
		P.put('K', 0xFFFF9A9E);
		P.put('L', 0xFFE5484D);
		P.put('M', 0xFF9B2226);
		P.put('N', 0xFFEFF5F2);
		P.put('O', 0xFFFFEB8A);
		P.put('P', 0xFFF2C230);
		P.put('Q', 0xFFB8860B);
		P.put('R', 0xFFFFFFFF);
		P.put('S', 0xFFB4C2BC);
		P.put('T', 0xFFE2E9E6);
		P.put('U', 0xFFA9B4B0);
		P.put('V', 0xFF66716D);
		P.put('W', 0xFF25382F);
		P.put('X', 0xFF8B5A2B);
		P.put('Y', 0xFF5A3818);
		P.put('Z', 0xFFE0BEFF);
		P.put('a', 0xFFA855F7);
		P.put('b', 0xFF6B2FB8);
		P.put('c', 0xFFFFD6E8);
		P.put('d', 0xFFF7A8C8);
		P.put('e', 0xFFD9588A);
		P.put('f', 0xFFFFC080);
		P.put('g', 0xFFE9822B);
		P.put('h', 0xFFA8501A);
		P.put('i', 0xFFC02A2A);
		P.put('j', 0xFFFFF3B0);
		P.put('k', 0xFFE6C25A);
		P.put('l', 0xFFE24A4A);
		P.put('m', 0xFF8A6A55);
		P.put('n', 0xFF3A2519);
		P.put('o', 0xFFFFE5CB);
		P.put('p', 0xFFF2C9A0);
		P.put('q', 0xFFC98F63);
		P.put('r', 0xFF7A1414);
		P.put('s', 0xFF4A5258);
		P.put('t', 0xFF30363A);
		P.put('u', 0xFF1A1E20);
		def("kelp", "......AA........", ".....ABBA.......", "....ABCCA..AA...", "...ABCCDA.ABBA..", "...ADCCA.ABCCA..", "....ADCBAACCDA..", ".....ACCBACCA...", "....ABCCDACCA...", "...ABCCDAADCBA..", "...ADCCA..ACCBA.", "....ADCBA.ACCDA.", ".....ADCBABCDA..", "......ACCBCDA...", "......ADDDDA....", ".....AEEEEEA....", "......AAAAA.....");
		def("shirt", "................", "................", "....AAAAAAAA....", ".AAAFFGGGGFFAAA.", "AFFFHHIIIIHHFFFA", "FHHHHHFJJFHHHHHF", "JHHHHHHHHHHHHHHJ", "AJJJHHHHHHHHJJJA", ".AAAHHHHHHHHAAA.", "...AHHHHHHHHA...", "...AHHHHHHHHA...", "...AHHHHHHHHA...", "...AHHHHHHHHA...", "...AHHHHHHHHA...", "...AJJJJJJJJA...", "....AAAAAAAA....");
		def("pin", ".....AAAAAA.....", "....AKKKKKKA....", "...AKLLLLLLKA...", "..AKLLMMMMLLKA..", "..ALLLNNNNLLLA..", "..ALLLNNNNLLLA..", "..ALLLNNNNLLLA..", "..ALLLNNNNLLLA..", "..AMLLKKKKLLMA..", "...AMLLLLLLMA...", "....AMMMLLMA....", ".....AAALMA.....", "......AKLA......", "......ALLA......", "......AMMA......", ".......AA.......");
		def("person", "................", "......AAAA......", ".....AOOOOA.....", "....AOPPPPOA....", "....APPPPPPA....", "....APPPPPPA....", "....AQPPPPQA....", "....AAPPPPAA....", "...AOOPPPPOOA...", "..AOPPPPPPPPOA..", "..APPPPPPPPPPA..", ".AOPPPPPPPPPPOA.", ".APPPPPPPPPPPPA.", ".APPPPPPPPPPPPA.", ".AQQQQQQQQQQQQA.", "..AAAAAAAAAAAA..");
		def("arrows", "................", ".........AA.....", "........ABBAA...", ".AAAAAAAACCBBA..", "ABBBBBBBBCCCCBA.", "ADDDDDDDDCCDDA..", ".AAAAAAAADDAA...", "......A..AA.....", ".....ARA........", "....ARNAAAAAAAA.", "...ARNNRRRRRRRRA", "....ASNSSSSSSSSA", ".....ASAAAAAAAA.", "......A.........", "................", "................");
		def("gear", ".......AA.......", "......ATTA......", "...AAAAUUAAAA...", "..ATTTTUUTTTTA..", "..AUUUUUUUUUUA..", "..AUUUVVVVUUUA..", ".AAUUUGGGGUUUAA.", "ATTUUUWWWWUUUTTA", "AVVUUUWWWWUUUVVA", ".AAUUUIIIIUUUAA.", "..AUUUTTTTUUUA..", "..AUUUUUUUUUUA..", "..AVVVVUUVVVVA..", "...AAAAUUAAAA...", "......AVVA......", ".......AA.......");
		def("monitor", "................", ".AAAAAAAAAAAAAA.", "ATTTTTTTTTTTTTTA", "AUGGGGGGGGGGGGUA", "AUWWWIIWWIICCWUA", "AUWWICCIICCGGWUA", "AUWICGGCCCGWWWUA", "AUWCGWWGCGWWWWUA", "AUWCWWWWGWWWWWUA", "AUIGIIIIIIIIIIUA", "AVTTTTTTTTTTTTVA", ".AAAAAAUUAAAAAA.", "....AAAUUAAA....", "...ATTTVVTTTA...", "....AAAAAAAA....", "................");
		def("sword", ".............AA.", "............ARRA", "...........ARNNA", "..........ARNSSA", ".........ARNSAA.", "........ARNSA...", ".......ARNSA....", "...A..ARNSA.....", "..AOAARNSA......", "...AORSSA.......", "...AEEEA........", "..AEXYYA........", ".AEXYAAOA.......", ".AYXA..AOA......", "AOOYA...A.......", ".AAA............");
		def("cross", ".......AA.......", "......ABBA......", "......ACCA......", "......ACCA......", "......ACCA......", "......ADDA......", ".AAAAA.AA.AAAAA.", "ABBBBBAKKABBBBBA", "ADDDDDAMMADDDDDA", ".AAAAA.AA.AAAAA.", "......ABBA......", "......ACCA......", "......ACCA......", "......ACCA......", "......ADDA......", ".......AA.......");
		def("heart", "................", "...AAA..AAA.....", "..AKKKAAKKKA....", ".AKLKLKKLLLKA...", "AKLKLLLLLLLLKA..", "AMLKLLLLLLLLMA..", ".AMLLLLLLLLMA...", "..AMLLLLLLMA....", "...AMLLLLMA.....", "....AMLLMA......", ".....AMMA.......", "......AA........", "................", "................", "................", "................");
		def("clock", "................", ".....AAAAAA.....", "...AATTTTTTAA...", "..ATTVRRRRVTTA..", "..AURRNNWNRRUA..", ".ATVNNNNWNNNVTA.", ".AURNNNNWNNNRUA.", ".AUNNNNNWSSSNUA.", ".AUNNNNNLWWWNUA.", ".AUSNNNNRRRRSUA.", ".AVTNNNNNNNNTVA.", "..AUSSNNNNSSUA..", "..AVTTSSSSTTVA..", "...AAVTTTTVAA...", ".....AAAAAA.....", "................");
		def("compass", "......AAAA......", "....AATTTTAA....", "...ATTVVVVTTA...", "..ATVGGGGGGVTA..", ".ATVGWWLLWWGVTA.", ".AUGWWILLIWWGUA.", "ATUWWWLLLLWWWUTA", "AUUWWWLLLLWWWUUA", "AUUWWWNNPNWWWUUA", "AVUWWWNNNNWWWUVA", ".AUIWWGNNGWWIUA.", ".AVTIWWNNWWITVA.", "..AVTIIGGIITVA..", "...AVTTTTTTVA...", "....AAVVVVAA....", "......AAAA......");
		def("chip", "....A..A..A.....", "...ATAATAATA....", "...AVAAVAAVAA...", ".AAGGGGGGGGGGAA.", "ATTWWIIIIIIWWTTA", ".AAWWBBBBBBWWAA.", ".AAWWCCDDCCWWAA.", "ATTWWCCGGCCWWTTA", ".AAWWCCIICCWWAA.", ".AAWWCCBBCCWWAA.", "ATTWWDDDDDDWWTTA", ".AAWWGGGGGGWWAA.", "..AIIIIIIIIIIA..", "...ATAATAATAA...", "...AVAAVAAVA....", "....A..A..A.....");
		def("mouse", ".....AAAAAA.....", "....ATTTTTTA....", "...ATUULLUUTA...", "...AUUULLUUUA...", "...AUUULLUUUA...", "...AUUULLUUUA...", "...AUUULLUUUA...", "...AUUUAAUUUA...", "...AUUUTTUUUA...", "...AUUUUUUUUA...", "...AUUUUUUUUA...", "...AUUUUUUUUA...", "...AUUUUUUUUA...", "...AVUUUUUUVA...", "....AVVVVVVA....", ".....AAAAAA.....");
		def("list", "................", "................", "..AAA.AAAAAAAA..", ".ABBBARRRRRRRRA.", ".ADDDASSSSSSSSA.", "..AAA.AAAAAAAA..", "..AAA.AAAAAAAA..", ".ABBBARRRRRRRRA.", ".ADDDASSSSSSSSA.", "..AAA.AAAAAAAA..", "..AAA.AAAAAAAA..", ".ABBBARRRRRRRRA.", ".ADDDASSSSSSSSA.", "..AAA.AAAAAAAA..", "................", "................");
		def("keys", "................", "................", "AAAAAAAAAAAAAAAA", "GGGGGGGGGGGGGGGG", "WUUWUUWUUWUUWUUW", "WUUWUUWUUWUUWUUW", "WGGWGGWGGWGGWGGW", "WUUWUUWUUWUUWUUW", "WUUWUUWUUWUUWUUW", "WGGIGGIGGIGGIGGW", "WWWUUUUUUUUUUWWW", "WWWUUUUUUUUUUWWW", "IIIGGGGGGGGGGIII", "AAAAAAAAAAAAAAAA", "................", "................");
		def("bolt", ".......AAAA.....", "......AOOOOA....", ".....AOPPQA.....", "....AOPPPAA.....", "...AOQPPPOOA....", "....AAPPPQA.....", "....AOPPQA......", "....APQQA.......", "...AOQAA........", "..AOQA..........", "...AA...........", "................", "................", "................", "................", "................");
		def("sun", ".......AA.......", "......APPA......", "...A...AA..A....", "..APA..AA.APA...", "...A.AAOOAAA....", "....AOOPPOOA....", ".A..APPPPPPA.A..", "APAAOPPPPPPOAPA.", ".A.AQPPPPPPQAA..", "....APPPPPPA....", "...AAQQPPQQA....", "..APAAAQQAAPA...", "...A...AA..A....", "......APPA......", ".......AA.......", "................");
		def("moon", "................", "......AA........", "....AAFFA.......", "...AFFJA........", "..AFHHA.........", "..AHHHA.........", ".AFHHHA.........", ".AHHHHA.........", ".AHHHHFA........", ".AJHHHHFA.......", "..AHHHHHFAAAA...", "..AJHHHHHFFFFA..", "...AJJHHHHJJA...", "....AAJJJJAA....", "......AAAA......", "................");
		def("eye", "................", "................", "................", "....AAAAAA......", "..AARRRRRRAA....", ".ARRNSSSSNRRA...", "ARNNSFFFFSNNRA..", "RNNNFHGGHFNNNRA.", "SNNNJHIIHJNNNSA.", "ASNNRJFFJRNNSA..", ".ASSNRRRRNSSA...", "..AASSSSSSAA....", "....AAAAAA......", "................", "................", "................");
		def("cube", ".......AA.......", ".....AARRAA.....", "...AARRNNRRAA...", ".AARRNNNNNNRRA..", "ARRNNNNNNNNNNRA.", "AFFSSNNNNNNSSTA.", "AHHFFSSNNSSTTUA.", "AHHHHFFSSTTUUUA.", "AHHHHHHFTUUUUUA.", "AHHHHHHHUUUUUUA.", "AHHHHHHHUUUUUUA.", "AJJHHHHHUUUUUVA.", ".AAJJHHHUUUVVA..", "...AAJJHUVVAA...", ".....AAJVAA.....", ".......AA.......");
		def("food", "................", ".........AA.....", ".......AABBA....", ".....AAEEDDBA...", "....AKKYYKKA....", "...AKLLKKLLKA...", "..AKLLLLLLLLKA..", "..ALLKLLLLLLLA..", "..ALLKLLLLLLLA..", "..ALLLLLLLLLLA..", "..ALLLLLLLLLLA..", "..AMLLLLLLLLMA..", "...AMLLLLLLMA...", "....AMMMMMMA....", ".....AAAAAA.....", "................");
		def("star", ".......AA.......", "......AOOA......", ".....AOPPOA.....", ".AAAAOPPPPOAAAA.", "AOOOOPPPPPPOOOOA", ".AQPPPPPPPPPPQA.", "..AQPPPPPPPPQA..", "...APPPPPPPPA...", "...APPPQQPPPA...", "..AOPQQAAQQPOA..", ".AOQQAA..AAQQOA.", "..AAA......AAA..", "................", "................", "................", "................");
		def("bars", "................", "................", "................", "..........AAA...", ".........ABBBA..", ".........ACCCA..", "......AAAACCCA..", ".....ABBBACCCA..", ".....ACCCACCCA..", "..AAAACCCACCCA..", ".ABBBACCCACCCA..", ".ACCCACCCACCCA..", ".ACCCACCCACCCA..", ".ACCCACCCACCCA..", ".ADDDADDDADDDA..", "..AAA.AAA.AAA...");
		def("chat", "................", ".AAAAAAAAAAAAAA.", "ARRRRRRRRRRRRRRA", "ANNNNNNNNNNNNNNA", "ANNNSSSSSSSSNNNA", "ANNNHHHHHHHHNNNA", "ANNNRRRRRRRRNNNA", "ANNNHHHHHHNNNNNA", "ANNNRRRRRRNNNNNA", "ANNNNNNNNNNNNNNA", "ASSNNNSSSSSSSSSA", ".AANNNAAAAAAAAA.", "..ANSSA.........", "..ASAA..........", "...A............", "................");
		def("armor", "................", "..AAAA.AA.AAAA..", ".ATTTTAGGATTTTA.", ".AUUUUAWWAUUUUA.", ".AUUUUTIITUUUUA.", ".AUUUUUTTUUUUUA.", ".AUUUUUUUUUUUUA.", ".AVVUUVVVVUUVVA.", "..AAUUHHHHUUAA..", "...AUUHHHHUUA...", "...AUUTTTTUUA...", "...AUUUUUUUUA...", "...AUUUUUUUUA...", "...AVVVVVVVVA...", "....AAAAAAAA....", "................");
		def("potion", "......AAAA......", ".....AEEEEA.....", ".....AYYYYA.....", "......ARRA......", "......ANNA......", "......ASSA......", ".....AZZZZA.....", "....AZaaaaZA....", "...AZaaaaaaZA...", "..AZaZaaaaaaZA..", "..AaaZaaaaaaaA..", "..AbaaaaaaaabA..", "...AbaaaaaabA...", "....AbbbbbbA....", ".....AAAAAA.....", "................");
		def("tree", "......AAAA......", "....AABBBBAA....", "...ABBCCCCBBA...", "..ABCCCCCCCCBA..", ".ABCCCCCCCCCCBA.", ".ADCCCCCCCCCCDA.", "..ADCCCCCCCCDA..", "...ADDDDDDDDA...", "....AAAAAAAA....", ".......AA.......", "......AEEA......", "......AXXA......", "......AXXA......", "......AXXA......", "......AYYA......", ".......AA.......");
		def("boot", "....AAAAA.......", "...ATTTTTA......", "...AVVVVVA......", "...AEEEEEA......", "...AXXXXXA......", "...AXXXXXA......", "...AXXXXXA......", "...AXXXXXA......", "...AXXXXXAAAA...", "...AXXXXXEEEEA..", "...AXXXXXXXXXEA.", "...AXXXXXXXXXXA.", "...AYYYYYYYYYYA.", "...AGGGGGGGGGGA.", "....AAAAAAAAAA..", "................");
		def("camera", "................", ".....AAAAAA.....", "....ATTTTTTA....", ".AAAAUUUUUUAAAA.", "ATTTTUUUUUUTTTTA", "AUUUUUVVVVUUVUUA", "AUUUUVGGGGVULUUA", "AUUUUGIFFIGUTUUA", "AUUUUWFFHFWUUUUA", "AUUUUWJHHJWUUUUA", "AUUUUIGJJGIUUUUA", "AUUUUTIGGITUUUUA", "AUUUUUTTTTUUUUUA", "AVVVVVVVVVVVVVVA", ".AAAAAAAAAAAAAA.", "................");
		def("x", "................", "................", "...AA......AAA..", "..AKKA....AKKKA.", "..AMLKA..AKLMMA.", "...AMLKAAKLMAA..", "....AMLKKLMA....", ".....ALLLLA.....", ".....ALLLLA.....", "....AKLMMLKA....", "...AKLMAAMLKA...", "..AKLMA..AMLKA..", "..ALLA....AMLKA.", "..AMMA.....AMMA.", "...AA.......AA..", "................");
		def("axolotl", "................", "................", ".AA..........AA.", "ALLA........ALLA", "ALLLAAAAAAAALLLA", ".ALLccccccccLLA.", ".ALLdeeddeedLLA.", "..AAdWWddWWdAA..", ".ALLdWWddWWdLLA.", ".ALLdccddccdLLA.", "..AAdddeedddAA..", "...AddddddddA...", "...AeeeeeeeeA...", "....AAAAAAAA....", "................", "................");
		def("panda", "................", "................", "...AAA....AAA...", "..AfffA..AfffA..", "..AgNgA..AgNgA..", "..AgfgAAAAgfgA..", "..AgghffffhggA..", "..AggWWhhWWggA..", "..AghWWRRWWhgA..", "..AgRRRSSRRRgA..", "..AgNNNWWNNNgA..", "..AgNNNWWNNNgA..", "..AgSSSRRSSSgA..", "..AhffffffffhA..", "...AAAAAAAAAA...", "................");
		def("wings", ".A............A.", "AiA..........AiA", "AKiA........AiKA", "ALKiA......AiKLA", "ALLKiA....AiKLLA", "ALLLKiA..AiKLLLA", "AMLLLKiAAiKLLLMA", ".AMMMMKiiKMMMMA.", ".AiiiiiiiiiiiiA.", ".AKKiiKAAKiiKKA.", ".AMiiKLAALKiiMA.", ".AiAAAMAAMAAAiA.", ".AiA..A..A..AiA.", "..A..........A..", "................", "................");
		def("gsword", "...........AAAA.", "..........AOORRA", ".........AOARjjA", "........AOARjPPA", ".......AOARjPAA.", "......AOARjPA...", ".....AOARjPA....", "...AAOARjPA.....", "..AkOARjPA......", "...AkRPPA.......", "...AEEEA........", "..AEXYYA........", ".AEXYAAkA.......", ".AXXA..AkA......", ".AYYA...A.......", "..AA............");
		def("cape", "...AAAAAAAAAA...", "..AllllllllllA..", "..AiKKKKKKKKiA..", "..AiLmmmmmmLiA..", "..AiLnnnnnnLiA..", "..AiLoWWoWWLiA..", "..AiLpoopooLiA..", "..AiLqqqqqqLiA..", "..AiLKKKKKKLiA..", "..AiLLLLLLLLiA..", "..AiLLLLLLLLiA..", "..AiLLLLLLLLiA..", "..AiLLLLLLLLiA..", "..AiMMMMMMMMiA..", "..ArllllllllrA..", "...AAAAAAAAAA...");
		def("tophat", "................", "....AAAAAAAA....", "...AssssssssA...", "...AttttttttA...", "...AttttttttA...", "...AttttttttA...", "...AttttttttA...", "...AuuuuuuuuA...", "...ALLLLLLLLA...", ".AAAssssssssAAA.", "AsssttttttttsssA", "AttttttttttttttA", "AuuuuuuuuuuuuuuA", ".AAAAAAAAAAAAAA.", "................", "................");
		def("heartmini", ".xx...xx.", "xxxx.xxxx", "xxxxxxxxx", "xxxxxxxxx", ".xxxxxxx.", "..xxxxx..", "...xxx...", "....x....");
	}

	public static boolean has(String k) { return M.containsKey(k); }
	public static int w(String k) { String[] r = M.get(k); if (r == null) return 0; int m = 0; for (String s : r) m = Math.max(m, s.length()); return m; }
	public static int h(String k) { String[] r = M.get(k); return r == null ? 0 : r.length; }

	public static void draw(DrawContext c, String k, int x, int y, int s) { draw(c, k, x, y, s, 0, 99); }

	/** tint != 0 paints every pixel in that colour; maxCols limits columns (used for half hearts). */
	public static void draw(DrawContext c, String k, int x, int y, int s, int tint, int maxCols) {
		String[] r = M.get(k);
		if (r == null) return;
		for (int row = 0; row < r.length; row++) {
			String line = r[row];
			int col = 0;
			while (col < line.length() && col < maxCols) {
				char ch = line.charAt(col);
				if (ch == '.') { col++; continue; }
				int start = col;
				while (col < line.length() && col < maxCols && line.charAt(col) == ch) col++;
				Integer color = tint != 0 ? Integer.valueOf(tint) : P.get(ch);
				if (color == null) continue;
				c.fill(x + start * s, y + row * s, x + col * s, y + (row + 1) * s, color);
			}
		}
	}

	public static void drawCentered(DrawContext c, String k, int cx, int cy, int s) { drawCentered(c, k, cx, cy, s, 0); }

	public static void drawCentered(DrawContext c, String k, int cx, int cy, int s, int tint) {
		draw(c, k, cx - w(k) * s / 2, cy - h(k) * s / 2, s, tint, 99);
	}

	/** Largest whole-number scale at which the icon fits inside a box of the given size. */
	public static int fit(String k, int box) { return Math.max(1, box / Math.max(1, Math.max(w(k), h(k)))); }
}
