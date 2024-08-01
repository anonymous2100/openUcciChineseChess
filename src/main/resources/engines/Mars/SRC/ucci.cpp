/* �ļ���ucci.cpp
 * ���ݣ�ElephantEyeԴ�����1���֡���UCCIָ�����ģ��
 * ��ʶ��Լ����
 * c_��������
 * e_��ѡ�
 * g_���ŷ�Ԥ�������飻
 * p_������(ChessPosition)�ĳ�Ա������
 * s_����̬(ȫ��)������
 */

/* ��������������������ʶ�𹤾ߣ�UCCIָ���ǳ������Э���������ʹ����Щ����
 * ������ӵ��Ǽ���Ƿ����������"CheckInput()"������Windows��UnixϵͳҪ����Դ�
 * ������������Ͽ�����Crafty��Դ������ʵ��������������WinBoard�����UCI���涼����ô���ģ�����Ͳ���ϸ˵����
 */

#ifdef _WIN32

#include <windows.h>

int /* bool */ CheckInput(int &BytesLeft) {
  static int /* bool */ s_Init = false;
  static BOOL s_ConsoleMode;
  static HANDLE s_InputHandle;
  DWORD data;
  if (BytesLeft > 0) {
    return true;
  }
  if (!s_Init) {
    s_Init = true;
    s_InputHandle = GetStdHandle(STD_INPUT_HANDLE);
    s_ConsoleMode = GetConsoleMode(s_InputHandle, &data);
    if (s_ConsoleMode) {
      SetConsoleMode(s_InputHandle, data & ~(ENABLE_MOUSE_INPUT | ENABLE_WINDOW_INPUT));
      FlushConsoleInputBuffer(s_InputHandle);
    }
  }
  if (s_ConsoleMode) {
    GetNumberOfConsoleInputEvents(s_InputHandle, &data);
    return data > 1;
  } else {
    if (!PeekNamedPipe(s_InputHandle, NULL, 0, NULL, &data, NULL)) {
      return true;
    }
    BytesLeft = data;
    return data != 0;
  }
}

#else

#include <sys/select.h>
#include <time.h>
#include <unistd.h>

int /* bool */ CheckInput(int &) {
  fd_set readfds;
  timeval tv;
  int /* bool */ data;
  FD_ZERO(&readfds);
  FD_SET(0, &readfds);
  tv.tv_sec = 0;
  tv.tv_usec = 0;
  select(16, &readfds, 0, 0, &tv);
  data = FD_ISSET(0, &readfds);
  return data;
}

#endif

#include <stdio.h>
#include <string.h>
#include "ucci.h"

// ��δ�����idle.h�еĴ��룬ֱ��Ų������************
//#include "idle.h"
#ifdef _WIN32
  //#include <windows.h>
  inline void Idle(void) {
    Sleep(1);
  }
#else
  //#include <time.h>
  inline void Idle(void) {
    timespec tv;
    tv.tv_sec = 0;
    tv.tv_nsec = 1000000;
    nanosleep(&tv, NULL);
  }
#endif
// **************************************************

/* ��ȡһ�еĳ�������"CheckInput()"����������Windows��Unix�Ĵ����һ����
 * �����ж�ȡ���������У��������������ھ�̬����"s_LineStr"�У���������ʱ���Ƿ���"s_LineStr"�����򷵻�NULL
 * ����һ����̬������"s_BytesLeft"����ʵֻ����Windows�У�����ԭ�����"CheckInput()"����
 */
char *ReadInput(void) {
  const int c_MaxInputBuff = 1024;
  static char s_LineStr[c_MaxInputBuff];
  static int s_BytesLeft = 0;
  char *RetVal;
  if (CheckInput(s_BytesLeft)) {
    RetVal = fgets(s_LineStr, c_MaxInputBuff, stdin);
    if (RetVal != NULL) {
      if (s_BytesLeft > 0) {
        s_BytesLeft -= (int)strlen(RetVal);
      }
      RetVal = strchr(s_LineStr, '\n');
      *RetVal = '\0';
      RetVal = s_LineStr;
    }
    return RetVal;
  } else {
    return NULL;
  }
}

/* ��ȡĳ���ַ��е����֣�ͬʱ�ƶ��ַ�����ָ�룬�����޶����ִ�С
 * ԭ��ǳ��򵥣����ﲻ��˵��
 */
int ReadDigit(const char *&LineStr, int MaxValue) {
  int RetValue;
  RetValue = 0;
  while (1) {
    if (*LineStr >= '0' && *LineStr <= '9') {
      RetValue *= 10;
      RetValue += *LineStr - '0';
      LineStr ++;
      if (RetValue > MaxValue) {
        RetValue = MaxValue;
      }
    } else {
      break;
    }
  }
  return RetValue;
}

/* Ȼ��������UCCIָ�������
 * ���е�һ��������"BootLine()"��򵥣�ֻ������������������ĵ�һ��ָ��
 * ����"ucci"ʱ�ͷ���"e_CommUcci"������һ�ɷ���"e_CommNone"
 * ǰ�������������ȴ��Ƿ������룬���û��������ִ�д���ָ��"Idle()"
 * ��������������("BusyLine()"��ֻ��������˼��ʱ)����û������ʱֱ�ӷ���"e_CommNone"
 */
CommEnum BootLine(void) {
  const char *LineStr;
  LineStr = ReadInput();
  while (LineStr == NULL) {
    Idle();
    LineStr = ReadInput();
  }
  if (strcmp(LineStr, "ucci") == 0) {
    return e_CommUcci;
  } else {
    return e_CommNone;
  }
}

CommEnum IdleLine(CommDetail &Command, int /* bool */ Debug) {
  static long s_CoordList[256];
  int i;
  const char *LineStr;
  CommEnum RetValue;
  LineStr = ReadInput();
  while (LineStr == NULL) {
    Idle();
    LineStr = ReadInput();
  }
  if (Debug) {
    printf("info string %s\n", LineStr);
    fflush(stdout);
  }
  // "IdleLine()"����ӵ�UCCIָ����������������UCCIָ����������ͣ�������

  // 1. "isready"ָ��
  if (strcmp(LineStr, "isready") == 0) {
    return e_CommIsReady;

  // 2. "setoption <option> [<arguments>]"ָ��
  } else if (strncmp(LineStr, "setoption ", 10) == 0) {
    LineStr += 10;

    // (i) "batch"ѡ��
    if (strncmp(LineStr, "batch ", 6) == 0) {
      LineStr += 6;
      Command.Option.Type = e_OptionBatch;
      if (strncmp(LineStr, "on", 2) == 0) {
        Command.Option.Value.Check = e_CheckTrue;
      } else if (strncmp(LineStr, "true", 4) == 0) {
        Command.Option.Value.Check = e_CheckTrue;
      } else {
        Command.Option.Value.Check = e_CheckFalse;
      } // ����"batch"ѡ��Ĭ���ǹرյģ�����ֻ���趨"on"��"true"ʱ�Ŵ򿪣���ͬ

    // (ii) "debug"ѡ��
    } else if (strncmp(LineStr, "debug ", 6) == 0) {
      LineStr += 6;
      Command.Option.Type = e_OptionDebug;
      if (strncmp(LineStr, "on", 2) == 0) {
        Command.Option.Value.Check = e_CheckTrue;
      } else if (strncmp(LineStr, "true", 4) == 0) {
        Command.Option.Value.Check = e_CheckTrue;
      } else {
        Command.Option.Value.Check = e_CheckFalse;
      }

    // (iii) "bookfiles"ѡ��
    } else if (strncmp(LineStr, "bookfiles ", 10) == 0) {
      Command.Option.Type = e_OptionBookFiles;
      Command.Option.Value.String = LineStr + 10;

    // (iv) "egtbpaths"ѡ��
    } else if (strncmp(LineStr, "egtbpaths ", 10) == 0) {
      Command.Option.Type = e_OptionEgtbPaths;
      Command.Option.Value.String = LineStr + 10;

    // (v) "hashsize"ѡ��
    } else if (strncmp(LineStr, "hashsize ", 9) == 0) {
      LineStr += 9;
      Command.Option.Type = e_OptionHashSize;
      Command.Option.Value.Spin = ReadDigit(LineStr, 1024);

    // (vi) "threads"ѡ��
    } else if (strncmp(LineStr, "threads ", 8) == 0) {
      LineStr += 8;
      Command.Option.Type = e_OptionThreads;
      Command.Option.Value.Spin = ReadDigit(LineStr, 32);

    // (vii) "drawmoves"ѡ��
    } else if (strncmp(LineStr, "drawmoves ", 10) == 0) {
      LineStr += 10;
      Command.Option.Type = e_OptionDrawMoves;
      Command.Option.Value.Spin = ReadDigit(LineStr, 200);

    // (viii) "repetition"ѡ��
    } else if (strncmp(LineStr, "repetition ", 11) == 0) {
      LineStr += 11;
      Command.Option.Type = e_OptionRepetition;
      if (strncmp(LineStr, "alwaysdraw", 10) == 0) {
        Command.Option.Value.Repetition = e_RepetitionAlwaysDraw;
      } else if (strncmp(LineStr, "checkban", 8) == 0) {
        Command.Option.Value.Repetition = e_RepetitionCheckBan;
      } else if (strncmp(LineStr, "asianrule", 9) == 0) {
        Command.Option.Value.Repetition = e_RepetitionAsianRule;
      } else if (strncmp(LineStr, "chineserule", 11) == 0) {
        Command.Option.Value.Repetition = e_RepetitionChineseRule;
      } else {
        Command.Option.Value.Repetition = e_RepetitionChineseRule;
      }

    // (ix) "pruning"ѡ��
    } else if (strncmp(LineStr, "pruning ", 8) == 0) {
      LineStr += 8;
      Command.Option.Type = e_OptionPruning;
      if (strncmp(LineStr, "none", 4) == 0) {
        Command.Option.Value.Scale = e_ScaleNone;
      } else if (strncmp(LineStr, "small", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleSmall;
      } else if (strncmp(LineStr, "medium", 6) == 0) {
        Command.Option.Value.Scale = e_ScaleMedium;
      } else if (strncmp(LineStr, "large", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleLarge;
      } else {
        Command.Option.Value.Scale = e_ScaleLarge;
      }

    // (x) "knowledge"ѡ��
    } else if (strncmp(LineStr, "knowledge ", 10) == 0) {
      LineStr += 10;
      Command.Option.Type = e_OptionKnowledge;
      if (strncmp(LineStr, "none", 4) == 0) {
        Command.Option.Value.Scale = e_ScaleNone;
      } else if (strncmp(LineStr, "small", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleSmall;
      } else if (strncmp(LineStr, "medium", 6) == 0) {
        Command.Option.Value.Scale = e_ScaleMedium;
      } else if (strncmp(LineStr, "large", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleLarge;
      } else {
        Command.Option.Value.Scale = e_ScaleLarge;
      }

    // (xi) "selectivity"ѡ��
    } else if (strncmp(LineStr, "selectivity ", 12) == 0) {
      LineStr += 12;
      Command.Option.Type = e_OptionSelectivity;
      if (strncmp(LineStr, "none", 4) == 0) {
        Command.Option.Value.Scale = e_ScaleNone;
      } else if (strncmp(LineStr, "small", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleSmall;
      } else if (strncmp(LineStr, "medium", 6) == 0) {
        Command.Option.Value.Scale = e_ScaleMedium;
      } else if (strncmp(LineStr, "large", 5) == 0) {
        Command.Option.Value.Scale = e_ScaleLarge;
      } else {
        Command.Option.Value.Scale = e_ScaleNone;
      }

    // (xii) "style"ѡ��
    } else if (strncmp(LineStr, "style ", 6) == 0) {
      LineStr += 6;
      Command.Option.Type = e_OptionStyle;
      if (strncmp(LineStr, "solid", 5) == 0) {
        Command.Option.Value.Style = e_StyleSolid;
      } else if (strncmp(LineStr, "normal", 6) == 0) {
        Command.Option.Value.Style = e_StyleNormal;
      } else if (strncmp(LineStr, "risky", 5) == 0) {
        Command.Option.Value.Style = e_StyleRisky;
      } else {
        Command.Option.Value.Style = e_StyleNormal;
      }

    // (xiii) "loadbook"ѡ��
    } else if (strncmp(LineStr, "loadbook", 8) == 0) {
      Command.Option.Type = e_OptionLoadBook;

    // (xiv) �޷�ʶ���ѡ�����������
    } else {
      Command.Option.Type = e_OptionNone;
    }
    return e_CommSetOption;

  // 3. "position {<special_position> | fen <fen_string>} [moves <move_list>]"ָ��
  } else if (strncmp(LineStr, "position ", 9) == 0) {
    LineStr += 9;

    // �����ж��Ƿ����������(����涨��5��)������������ֱ��ת���ɶ�Ӧ��FEN��
    if (strncmp(LineStr, "startpos", 8) == 0) {
      Command.Position.FenStr = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
    } else if (strncmp(LineStr, "midgamepos", 10) == 0) {
      Command.Position.FenStr = "2bakab1r/6r2/1cn4c1/p1p1p3p/9/2P3p2/PC2P1n1P/2N1B1NC1/R4R3/3AKAB2 w - - 0 1";
    } else if (strncmp(LineStr, "checkmatepos", 12) == 0) {
      Command.Position.FenStr = "4kar2/4a2rn/4bc3/RN1c5/2bC5/9/4p4/9/4p4/3p1K3 w - - 0 1";
    } else if (strncmp(LineStr, "zugzwangpos", 11) == 0) {
      Command.Position.FenStr = "3k5/4PP3/4r4/3P5/9/9/9/9/9/5K3 w - - 0 1";
    } else if (strncmp(LineStr, "endgamepos", 10) == 0) {
      Command.Position.FenStr = "4k4/4a4/4P4/9/9/9/9/4B4/9/4K4 w - - 0 1";
    // Ȼ���ж��Ƿ�ָ����FEN��
    } else if (strncmp(LineStr, "fen ", 4) == 0) {
      Command.Position.FenStr = LineStr + 4;
    // ������߶����ǣ�����������
    } else {
      return e_CommNone;
    }
    // Ȼ��Ѱ���Ƿ�ָ���˺����ŷ������Ƿ���"moves"�ؼ���
    while (*LineStr != '\0') {
      if (strncmp(LineStr, " moves ", 7) == 0) {
        LineStr += 7;        
        Command.Position.MoveNum = int((strlen(LineStr) + 1) / 5); // ��ʾ��"moves"�����ÿ���ŷ�����4���ַ���1���ո�
        for (i = 0; i < Command.Position.MoveNum; i ++) {
          s_CoordList[i] = *(long *) LineStr; // 4���ַ���ת��Ϊһ��"long"���洢�ʹ�����������
          LineStr += 5;
        }
        Command.Position.CoordList = s_CoordList;
        return e_CommPosition;
      }
      LineStr ++;
    }
    Command.Position.MoveNum = 0;
    return e_CommPosition;

  // 4. "banmoves <move_list>"ָ�����������"position ... moves ..."��һ����
  } else if (strncmp(LineStr, "banmoves ", 9) == 0) {
    LineStr += 9;
    Command.BanMoves.MoveNum = int((strlen(LineStr) + 1) / 5);
    for (i = 0; i < Command.Position.MoveNum; i ++) {
      s_CoordList[i] = *(int *) LineStr;
      LineStr += 5;
    }
    Command.BanMoves.CoordList = s_CoordList;
    return e_CommBanMoves;

  // 5. "go [ponder] {infinite | depth <depth> | time <time> [movestogo <moves_to_go> | increment <inc_time>]}"ָ��
  } else if (strncmp(LineStr, "go ", 3) == 0) {
    LineStr += 3;
    // �����жϵ�����"go"����"go ponder"����Ϊ���߽��ͳɲ�ͬ��ָ��
    if (strncmp(LineStr, "ponder ", 7) == 0) {
      LineStr += 7;
      RetValue = e_CommGoPonder;
    } else {
      RetValue = e_CommGo;
    }
    // Ȼ���жϵ����ǹ̶���Ȼ����趨ʱ��
    if (strncmp(LineStr, "time ", 5) == 0) {
      LineStr += 5;
      Command.Search.DepthTime.Time = ReadDigit(LineStr, 36000);
      // ������趨ʱ�ޣ���Ҫ�ж���ʱ���ƻ��Ǽ�ʱ��
      if (strncmp(LineStr, " movestogo ", 11) == 0) {
        LineStr += 11;
        Command.Search.Mode = e_TimeMove;
        Command.Search.TimeMode.MovesToGo = ReadDigit(LineStr, 100);
        if (Command.Search.TimeMode.MovesToGo < 1) {
          Command.Search.TimeMode.MovesToGo = 1;
        }
      } else if (strncmp(LineStr, " increment ", 11) == 0) {
        LineStr += 11;
        Command.Search.Mode = e_TimeInc;
        Command.Search.TimeMode.Increment = ReadDigit(LineStr, 600);
      // ���û��˵����ʱ���ƻ��Ǽ�ʱ�ƣ����趨Ϊ������1��ʱ����
      } else {
        Command.Search.Mode = e_TimeMove;
        Command.Search.TimeMode.MovesToGo = 1;
      }
    } else if (strncmp(LineStr, "depth ", 6) == 0) {
      LineStr += 6;
      Command.Search.Mode = e_TimeDepth;
      Command.Search.DepthTime.Depth = ReadDigit(LineStr, c_MaxDepth - 1);
    // ���û��˵���ǹ̶���Ȼ����趨ʱ�ޣ��͹̶����Ϊ"c_MaxDepth"
    } else {
      Command.Search.Mode = e_TimeDepth;
      Command.Search.DepthTime.Depth = c_MaxDepth - 1;
    }
    return RetValue;

  // 5. "stop"ָ��
  } else if (strcmp(LineStr, "stop") == 0) {
    return e_CommStop;

  // 6. "quit"ָ��
  } else if (strcmp(LineStr, "quit") == 0) {
    return e_CommQuit;

  // 7. �޷�ʶ���ָ��
  } else {
    return e_CommNone;
  }
}

CommEnum BusyLine(int /* bool */ Debug) {
  const char *LineStr;
  LineStr = ReadInput();
  if (LineStr == NULL) {
    return e_CommNone;
  } else {
    if (Debug) {
      printf("info string %s\n", LineStr);
      fflush(stdout);
    }
    // "BusyLine"ֻ�ܽ���"isready"��"ponderhit"��"stop"������ָ��
    if (strcmp(LineStr, "isready") == 0) {
      return e_CommIsReady;
    } else if (strcmp(LineStr, "ponderhit") == 0) {
      return e_CommPonderHit;
    } else if (strcmp(LineStr, "stop") == 0) {
      return e_CommStop;
    } else {
      return e_CommNone;
    }
  }
}
