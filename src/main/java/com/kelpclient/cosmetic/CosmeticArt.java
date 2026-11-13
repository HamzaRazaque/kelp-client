package com.kelpclient.cosmetic;

import java.util.HashMap;
import java.util.Map;

/** Pixel art for the 3D cosmetics (generated). '.' is empty. */
final class CosmeticArt {
	static final Map<Character, Integer> SWORD_PAL = new HashMap<>(), GILL_PAL = new HashMap<>();
	static final String[] SWORD = {
		"..............jg",
		".............igg",
		".......gjg..ihi.",
		".......ggjgihi..",
		"........ggiihi..",
		".......cbgiig...",
		"......cbbaggjg..",
		".....cbbabbggj..",
		"....cbbabbc.gg..",
		"...cbbabbc......",
		"..cbbabbc.......",
		".cfffbbc........",
		"cfeefbc.........",
		"feeefc..........",
		"eeefc...........",
		"eeec............"
	};
	static final String[] GILL = {
		".....aaab",
		"...aacaab",
		"..aaaa...",
		"aaaaaaaaa",
		"bbaaacaab",
		"bbaaa...b",
		"bbaacaa..",
		"...aaaaa.",
		".....abb."
	};
	static final Map<Character, Integer> DRAGON_PAL = new HashMap<>();
	static final String[] DRAGON = {
		"................pppp",
		".........ppppppppppp",
		".....ppppqppppppppm.",
		"....pppppqppnpppmmm.",
		"...pppmnnqnppnmppp..",
		"...pppmnnqnnpmmmmpp.",
		"..pppmmnnqnnppmmmmm.",
		"..pppmmmmqmmmpmmmmm.",
		".pppmmmnnqnnnppm....",
		".ppmmmmnnqnnmmp.....",
		"pppmmmmmnqoommmp....",
		"ppmmmmmmmqo.oop.....",
		"ppmm..mmmq....o.....",
		".m......oq..........",
		"........o..........."
	};
	static final Map<Character, Integer> ANGEL_PAL = new HashMap<>();
	static final String[] ANGEL = {
		"....................",
		"....................",
		"..............aacccd",
		".........accccccaaa.",
		"..cccccccccaaaaaaa..",
		"accccaaaaaaaabcccd..",
		".ccc....bccccccbb...",
		".cc.ccccccbbbbbb....",
		"ccccbbbbbbacccd.....",
		"ccc....cccccaa......",
		"cc.cccccaaaaa.......",
		"ccccaaaaccda........",
		"c....ccccb..........",
		"c.ccccbbb...........",
		"bccbbbbb............"
	};
	static final Map<Character, Integer> BUTTERFLY_PAL = new HashMap<>();
	static final String[] BUTTERFLY = {
		"......oooooooooo....",
		"....oooaaaaaaaaooo..",
		"...ooaaaaaabbbbaaoo.",
		"..oaaaaaabbbbbbwbaao",
		"..oaaaaabbbbbbbbbbao",
		"..oaooaaabbbbbbbbwao",
		"..ooooaaaabbbbbbaaao",
		"..oooaaoooooaaaaaao.",
		".oooooorrrrrowaaoo..",
		"ooooorrrrrrrrrooo...",
		"oooorrrrwrrrrrro....",
		"oo.orrrrrrrwrrro....",
		"o...orrrrrrrrro.....",
		".....oorrrrroo......",
		".......ooooo........"
	};
	static final Map<Character, Integer> RAVEN_PAL = new HashMap<>();
	static final String[] RAVEN = {
		"....................",
		"....................",
		"..............aacccd",
		".........accccccaaa.",
		"..cccccccccaaaaaaa..",
		"accccaaaaaaaabcccd..",
		".ccc....bccccccbb...",
		".cc.ccccccbbbbbb....",
		"ccccbbbbbbacccd.....",
		"ccc....cccccaa......",
		"cc.cccccaaaaa.......",
		"ccccaaaaccda........",
		"c....ccccb..........",
		"c.ccccbbb...........",
		"bccbbbbb............"
	};
	static final Map<Character, Integer> CRYSTAL_PAL = new HashMap<>();
	static final String[] CRYSTAL = {
		"..........cc........",
		".........bcaaaa.....",
		"......bbbbcaaaaacc..",
		".....cbbbbcaaaaacb..",
		"....accbbbcaaaaccb..",
		"....aacbbbcaaaacbb..",
		"...aaaccbbcaaaacbbb.",
		"...aaaac.bcaaaccbbb.",
		"...aaaacc.caaacbbbb.",
		"..aaaaaac.c.aacbbb..",
		"..aaadddddaaaacbb...",
		".dddd...aaaaa.bb....",
		".d.....aaaaaa.......",
		".........aaa........",
		"...................."
	};
	/** Wing styles in the order of the Wardrobe: dragon, angel, butterfly, raven, crystal. */
	static final String[][] WING_ART = {DRAGON, ANGEL, BUTTERFLY, RAVEN, CRYSTAL};
	static final java.util.List<Map<Character, Integer>> WING_PALS = java.util.List.of(DRAGON_PAL, ANGEL_PAL, BUTTERFLY_PAL, RAVEN_PAL, CRYSTAL_PAL);
	static {
		SWORD_PAL.put('a', 0xFFF8D6); SWORD_PAL.put('b', 0xFBDD6B); SWORD_PAL.put('c', 0xE9B22C); SWORD_PAL.put('d', 0xB8841A); SWORD_PAL.put('e', 0xF4B79A); SWORD_PAL.put('f', 0xE58E6E); SWORD_PAL.put('g', 0x9A7524); SWORD_PAL.put('h', 0x6B4E1A); SWORD_PAL.put('i', 0x4A3412); SWORD_PAL.put('j', 0xD9B23A); SWORD_PAL.put('k', 0x2A1D0A);
		GILL_PAL.put('a', 0xE8669A); GILL_PAL.put('b', 0xD13F78); GILL_PAL.put('c', 0xF78FB8);
		DRAGON_PAL.put('m', 0xB3232A); DRAGON_PAL.put('n', 0xD2343B); DRAGON_PAL.put('o', 0x8A171D); DRAGON_PAL.put('p', 0x4E1216); DRAGON_PAL.put('q', 0x701A1F);
		ANGEL_PAL.put('a', 0xF6F9FC); ANGEL_PAL.put('b', 0xDCE5EF); ANGEL_PAL.put('c', 0xAEBCCD); ANGEL_PAL.put('d', 0xFFE9A6);
		BUTTERFLY_PAL.put('o', 0x1E1018); BUTTERFLY_PAL.put('a', 0xFF9D1F); BUTTERFLY_PAL.put('b', 0xFFC94A); BUTTERFLY_PAL.put('w', 0xFFF6DC); BUTTERFLY_PAL.put('r', 0xD9531E);
		RAVEN_PAL.put('a', 0x2A2833); RAVEN_PAL.put('b', 0x3C3A4E); RAVEN_PAL.put('c', 0x14131A); RAVEN_PAL.put('d', 0x7A5BD6);
		CRYSTAL_PAL.put('a', 0x74E8F6); CRYSTAL_PAL.put('b', 0x36B5D2); CRYSTAL_PAL.put('c', 0xD9FCFF); CRYSTAL_PAL.put('d', 0x1C7C9A);
	}
	private CosmeticArt() { }
}
