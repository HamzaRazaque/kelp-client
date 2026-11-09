package com.kelpclient.cosmetic;

import java.util.HashMap;
import java.util.Map;

/** Pixel art for the 3D cosmetics (generated). '.' is empty. */
final class CosmeticArt {
	static final Map<Character, Integer> SWORD_PAL = new HashMap<>(), WING_PAL = new HashMap<>(), CAPE_PAL = new HashMap<>();
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
	static final String[] WING = {
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
	static final String[] CAPE = {
		"11111111111111111111",
		"12222222222222222221",
		"12333333333333333321",
		"12344444444444444321",
		"12354545454545454321",
		"12344444464464444321",
		"12544464666644444521",
		"12344466777666444321",
		"12344777777766644321",
		"12546667777666664521",
		"12366666666666664321",
		"12366899999999886321",
		"12566999999999986521",
		"12348kkk9999kkk84321",
		"1234699kk99kk9964321",
		"12546kkkk99kkkk64521",
		"123889wkw99wkw988321",
		"12388999999999988321",
		"1254899999u999984521",
		"123448999u9999844321",
		"12344489kkkkk8444321",
		"12544448999984444521",
		"12344444888884444321",
		"12344rrr88888r444321",
		"1254rrrruuuuurr44521",
		"123rrrrrrrtrrrrr4321",
		"123rsrrrrrtrrrssr321",
		"123rssrrrrtrrssrr321",
		"123rrsrrrrtrrsrrr321",
		"12rrrssrrrtrssrrrr21",
		"12rrrrsrrrtrsrrrrr21",
		"11rrrrrsrrtrsrrrrr11"
	};
	static {
		SWORD_PAL.put('a', 0xFFF8D6); SWORD_PAL.put('b', 0xFBDD6B); SWORD_PAL.put('c', 0xE9B22C); SWORD_PAL.put('d', 0xB8841A); SWORD_PAL.put('e', 0xF4B79A); SWORD_PAL.put('f', 0xE58E6E); SWORD_PAL.put('g', 0x9A7524); SWORD_PAL.put('h', 0x6B4E1A); SWORD_PAL.put('i', 0x4A3412); SWORD_PAL.put('j', 0xD9B23A); SWORD_PAL.put('k', 0x2A1D0A);
		WING_PAL.put('m', 0xB3232A); WING_PAL.put('n', 0xD2343B); WING_PAL.put('o', 0x8A171D); WING_PAL.put('p', 0x4E1216); WING_PAL.put('q', 0x701A1F);
		CAPE_PAL.put('1', 0x3C0A0E); CAPE_PAL.put('2', 0xA82832); CAPE_PAL.put('3', 0x7C1118); CAPE_PAL.put('4', 0x911620); CAPE_PAL.put('5', 0xD2565E); CAPE_PAL.put('6', 0x4A2A1A); CAPE_PAL.put('7', 0x6E4326); CAPE_PAL.put('8', 0xDDA270); CAPE_PAL.put('9', 0xF2C493); CAPE_PAL.put('w', 0xF7EEDF); CAPE_PAL.put('k', 0x24140F); CAPE_PAL.put('r', 0xE23B3B); CAPE_PAL.put('s', 0xB02A2E); CAPE_PAL.put('t', 0x8A1E22); CAPE_PAL.put('u', 0xC98A60);
	}
	private CosmeticArt() { }
}
