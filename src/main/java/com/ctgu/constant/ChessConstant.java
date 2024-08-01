package com.ctgu.constant;

import java.awt.Color;

/**
 * 一些常量定义
 */
public class ChessConstant
{
	public static final String[] DEFAULT_FONT = new String[] { "Table.font", "TableHeader.font", "CheckBox.font", "Tree.font", "Viewport.font",
			"ProgressBar.font", "RadioButtonMenuItem.font", "ToolBar.font", "ColorChooser.font", "ToggleButton.font", "Panel.font", "TextArea.font",
			"Menu.font", "TableHeader.font", "OptionPane.font", "MenuBar.font", "Button.font", "Label.font", "PasswordField.font", "ScrollPane.font",
			"MenuItem.font", "ToolTip.font", "List.font", "EditorPane.font", "Table.font", "TabbedPane.font", "RadioButton.font", "CheckBoxMenuItem.font",
			"TextPane.font", "PopupMenu.font", "TitledBorder.font", "ComboBox.font" };
	public static final int GRID_WIDTH = 68;
	public static final int CHESSBOARD_MARGIN = GRID_WIDTH;
	public static final Integer ARROW_HEIGHT = 20;
	public static final Integer ARROW_LENGTH = 10;
	public static final int RANGE_X = 9;
	public static final int RANGE_Y = 10;
	public static final int PIECE_WIDTH = 57;
	public static final int PIECE_HEIGHT = PIECE_WIDTH;
	public static final int X_INIT = 28 - PIECE_WIDTH / 2;
	public static final int Y_INIT = 28 - PIECE_WIDTH / 2;
	public static final String INIT_BOARD_FEN = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
	public static final String[] xIndex = { "0", "1", "2", "3", "4", "5", "6", "7", "8" };
	public static final String[] yIndex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	public static final String[] blackMarkNumbers = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	public static final String[] redMarkNumbers = { "九", "八", "七", "六", "五", "四", "三", "二", "一" };
	public static final String[] iccsHorizontalNumbers = { "a", "b", "c", "d", "e", "f", "g", "h", "i" };
	public static final String[] iccsVerticalNumbers = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	public static final String[] PIECE_ARRAY = { "rr", "rn", "rb", "ra", "rk", "rc", "rp", "br", "bn", "bb", "ba", "bk", "bc", "bp", "oos", "oos2" };
	public static final String[] MANUAL_NAME = { ".pgn", "fen", ".xqf" };

	public static final String[] ENGINE_NAME = { "ElephantEye\\ELEEYE.EXE", "云中象\\KOU.EXE", "兵河五四\\Binghewusi.exe", "决战象棋\\UFX.EXE", "十六人\\sixteen.exe",
			"棋思\\THT.EXE", "棋王\\King.exe", "象棋精灵\\XQSPIRIT.EXE" };
	public static final String[] BOARD_NAME = { "一鸣惊人.png", "三星堆.png", "云心鹤眼.png", "兰亭集序.png", "兰亭集序无字.png", "凤舞九天.png", "卯兔添福.png", "叱咤风云.png", "君临天下.png",
			"壁画古梦.png", "大闹天宫.png", "天山共色.png", "女神之约.png", "小吕飞刀.png", "山海绘卷.png", "护眼绿.png", "星河璀璨.png", "桃花源记.png", "武侠江湖.png", "清悠茶道.png", "游园惊梦.png",
			"牛转乾坤.png", "皓月无痕.png", "盲棋神迹.png", "空城计.png", "竹波烟雨.png", "经典木棋盘.png", "象甲征程.png", "金牛降世.png", "金虎贺岁.png", "金虎贺岁（王天一签名版）.png", "鎏金岁月.png",
			"霸王别姬.png", "鸿门宴.png", "龙腾天坛.png", "bg_JJ背景.png", "bg_一鸣惊人.png", "bg_三星堆.png", "bg_云心鹤眼.png", "bg_兰亭集序.png", "bg_凤舞九天.png", "bg_卯兔添福.png",
			"bg_叱咤风云.png", "bg_君临天下.png", "bg_壁画古梦.png", "bg_大闹天宫.png", "bg_天山共色.png", "bg_女神之约.png", "bg_小吕飞刀.png", "bg_山海绘卷.png", "bg_星河璀璨.png",
			"bg_桃花源记.png", "bg_武侠江湖.png", "bg_浅灰色.png", "bg_清悠茶道.png", "bg_游园惊梦.png", "bg_牛转乾坤.png", "bg_皓月无痕.png", "bg_盲棋神迹.png", "bg_空城计.png", "bg_竹波烟雨.png",
			"bg_纵横四海.png", "bg_经典木棋盘.png", "bg_背景.png", "bg_自制.png", "bg_茶道.png", "bg_象甲征程.png", "bg_象甲重燃.png", "bg_金牛降世.png", "bg_金虎贺岁.png",
			"bg_金虎贺岁（王天一签名版）.png", "bg_鎏金岁月.png", "bg_霸王别姬.png", "bg_鸿门宴.png", "bg_龙腾天坛.png" };
	public static final String[] PIECES_NAME = { "经典木质棋子", "经典抛光棋子", "经典精致棋子", "魏碑体象棋", "精品红黑", "精美黄金杉木棋子" };

	public static final String[] COORDINATE_NAME = { "传统方式", "ICCS格式", "Swing坐标系" };
	public static final String[] CHESS_NAMES = { "帅仕相马车炮兵", "将士象马车炮卒" };
	public static final String[] DIGITS = { "九八七六五四三二一", "１２３４５６７８９", "一二三四五六七八九" };
	public static final String GAME_STATE_FILE_NAME = "chess_game_state.dat";
	public static final char[] DIRECTION_NAMES = { '前', '中', '后', '进', '退', '平' };
	public static final Color LINE_COLOR = Color.BLACK;
	public static final Color WORD_COLOR = Color.BLACK;
	public static final String[] FONT_NAME = { "黑体", "宋体", "微软雅黑" };
	public static final int MSG_BEST_MOVE = 1;
	public static final int MSG_NO_BEST_MOVE = 2;
	public static final int MSG_ENGINE_THINKING = 3;
	public static final int MSG_HUMAN_INPUT = 4;
	public static final int[] ADVISOR_RED_RANGE = { 66, 68, 76, 84, 86 };
	public static final int[] ADVISOR_BLACK_RANGE = { 3, 5, 13, 21, 23 };
	public static final int[] BISHOP_RED_RANGE = { 47, 51, 63, 67, 71, 83, 87 };
	public static final int[] BISHOP_BLACK_RANGE = { 2, 6, 18, 22, 26, 38, 42 };
	public static final int[] RED_KING_RANGE = { 66, 67, 68, 75, 76, 77, 84, 85, 86 };
	public static final int[] BLACK_KING_RANGE = { 3, 4, 5, 12, 13, 14, 21, 22, 23 };

}
