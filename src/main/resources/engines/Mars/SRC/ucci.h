#ifndef UCCI_H
#define UCCI_H

/* �ļ���ucci.h
 * ���ݣ�ElephantEyeԴ�����1���֡���UCCIָ�����ģ��
 * ��ʶ��Լ����
 * c_��������
 * e_��ѡ�
 * g_���ŷ�Ԥ�������飻
 * p_������(ChessPosition)�ĳ�Ա������
 * s_����̬(ȫ��)������
 */

const int c_MaxDepth = 32; // UCCI����˼���ļ������

// ��UCCIָ���йؼ����йص�ѡ��
enum OptionEnum {
  e_OptionNone, e_OptionBatch, e_OptionDebug, e_OptionBookFiles, e_OptionEgtbPaths, e_OptionHashSize, e_OptionThreads, e_OptionDrawMoves,
  e_OptionRepetition, e_OptionPruning, e_OptionKnowledge, e_OptionSelectivity, e_OptionStyle, e_OptionLoadBook
}; // ��"setoption"ָ����ѡ��
enum CheckEnum {
  e_CheckFalse, e_CheckTrue
}; // ѡ������Ϊ"check"���趨ֵ��ֻ�й�(off��false)��(on��true)����
enum RepetitionEnum {
  e_RepetitionAlwaysDraw, e_RepetitionCheckBan, e_RepetitionAsianRule, e_RepetitionChineseRule
};
enum ScaleEnum {
  e_ScaleNone, e_ScaleSmall, e_ScaleMedium, e_ScaleLarge
};
enum StyleEnum {
  e_StyleSolid, e_StyleNormal, e_StyleRisky
}; // ѡ��style���趨ֵ
enum TimeEnum {
  e_TimeDepth, e_TimeMove, e_TimeInc
}; // ��"go"ָ��ָ����ʱ��ģʽ���ֱ��ǹ̶����(�������൱�����Ϊ"c_MaxDepth")��ʱ����(�������ڱ�����ɼ���)�ͼ�ʱ��(ʣ��ʱ����٣������ⲽ��Ӽ���)
enum CommEnum {
  e_CommNone, e_CommPonderHit, e_CommStop, e_CommUcci, e_CommIsReady, e_CommSetOption, e_CommPosition, e_CommBanMoves, e_CommGo, e_CommGoPonder, e_CommQuit
}; // UCCIָ������

// UCCIָ����Խ��ͳ������������Ľṹ
union CommDetail {

  /* �ɵõ�������Ϣ��UCCIָ��ֻ������4������
   *
   * 1. "setoption"ָ��ݵ���Ϣ���ʺ���"e_CommSetOption"ָ������
   *    "setoption"ָ�������趨ѡ����������ܵ�����Ϣ�С�ѡ�����͡��͡�ѡ��ֵ��
   *    ���磬setoption batch on��ѡ�����;���"e_OptionDebug��ֵ(Value.Spin)����"e_CheckTrue"
   */
  struct {
    OptionEnum Type;             // ѡ������
    union {                      // ѡ��ֵ
      int Spin;                  // "spin"���͵�ѡ���ֵ
      CheckEnum Check;           // "check"���͵�ѡ���ֵ
      RepetitionEnum Repetition;
      ScaleEnum Scale;
      StyleEnum Style;           // "combo"���͵�ѡ���ֵ
      const char *String;        // "string"���͵�ѡ���ֵ
    } Value;                     // "button"����û��ֵ
  } Option;

  /* 2. "position"ָ��ݵ���Ϣ���ʺ���"e_CommPosition"ָ������
   *    "position"ָ���������þ��棬������ʼ������ͬ�����ŷ����ɵľ���
   *    ���磬position startpos moves h2e2 h9g8��FEN������"startpos"�����FEN�����ŷ���(MoveNum)����2
   */
  struct {
    const char *FenStr; // FEN�����������(��"startpos"��)Ҳ�ɽ���������ת����FEN��
    int MoveNum;        // �����ŷ���
    long *CoordList;    // �����ŷ���ָ�����"IdleLine()"�е�һ����̬���飬�����԰�"CoordList"����������
  } Position;

  /* 3. "banmoves"ָ��ݵ���Ϣ���ʺ���"e_CommBanMoves"ָ������
   *    "banmoves"ָ���������ý�ֹ�ŷ������ݽṹʱ������"position"ָ��ĺ����ŷ�����û��FEN��
   */
  struct {
    int MoveNum;
    long *CoordList;
  } BanMoves;

  /* 4. "go"ָ��ݵ���Ϣ���ʺ���"e_CommGo"��"e_CommGoPonder"ָ������
   *    "go"ָ��������˼��(����)��ͬʱ�趨˼��ģʽ�����̶���ȡ�ʱ���ƻ��Ǽ�ʱ��
   */
  struct {
    TimeEnum Mode;   // ˼��ģʽ
    union {          
      int Depth;     // ����ǹ̶���ȣ����ʾ���(��)
      int Time;      // ������޶�ʱ�䣬���ʾʱ��(��)
    } DepthTime;
    union {
      int MovesToGo; // ����Ǽ�ʱ�ƣ����޶�ʱ����Ҫ�߶��ٲ���
      int Increment; // �����ʱ���ƣ����ʾ����ò����޶�ʱ��Ӷ�����
    } TimeMode;
  } Search;
};

// �������������ɱ�UCCIָ���������ǳ������Э�����������
char *ReadInput(void);                             // ��ȡһ��
int ReadDigit(const char *&LineStr, int MaxValue); // ��ȡĳ���ַ��е�����

// ��������������������UCCIָ��������ڲ�ͬ����
CommEnum BootLine(void);                                      // UCCI���������ĵ�һ��ָ�ֻ����"ucci"
CommEnum IdleLine(CommDetail &Command, int /* bool */ Debug); // �������ʱ����ָ��
CommEnum BusyLine(int /* bool */ Debug);                      // ����˼��ʱ����ָ�ֻ�������"stop"��"ponderhit"

int /* bool */ QhInputLine(int /* bool */ Busy); // ǳ������Э��Ľ��չ���

#endif
