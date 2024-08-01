#pragma once
#include "random.h"
#include "util.h"

extern const char  c_InBoard[256];     // ���������
extern const char  c_InCity[256];      // �Ź����б�
extern const char  c_HorseLegTab[512]; // ���ȱ�

struct slide_move_t {
  uint8 non_cap[2];      // ���������ߵ������һ��/��Сһ��
  uint8 rook_cap[2];     // ���������ߵ������һ��/��Сһ��
  uint8 cannon_cap[2];   // �ڳ������ߵ������һ��/��Сһ��
  uint8 super_cap[2];    // ������(�����ӳ���)���ߵ������һ��/��Сһ��
};

struct slide_mask_t {
  uint16 non_cap, rook_cap, cannon_cap, super_cap;
};

extern uint16 g_BitRankMask[256]; // ÿ�����ӵ�λ�е�����λ
extern uint16 g_BitFileMask[256]; // ÿ�����ӵ�λ�е�����λ


extern slide_move_t g_RankMoveTab[9][512];   // 36,864 �ֽ�
extern slide_move_t g_FileMoveTab[10][1024]; // 81,920 �ֽ�
extern slide_mask_t g_RankMaskTab[9][512];   // 36,864 �ֽ�
extern slide_mask_t g_FileMaskTab[10][1024]; // 81,920 �ֽ�
                                        // ����:  237,568 �ֽ�

extern uint8 g_KingMoves[256][8];
extern uint8 g_AdvisorMoves[256][8];
extern uint8 g_BishopMoves[256][8];
extern uint8 g_ElephantEyes[256][4];
extern uint8 g_KnightMoves[256][12];
extern uint8 g_HorseLegs[256][8];
extern uint8 g_PawnMoves[2][256][4];

void pre_move_gen(void);
