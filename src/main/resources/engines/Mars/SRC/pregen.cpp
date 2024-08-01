#include "pregen.h"

const char c_InBoard[256] = {
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};
const char c_InCity[256] = {
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

/* ���ȱ����������ж��ĸ������ȡ�
 * ���ɣ���� from �ߵ� to�����ȵĸ����ǣ�Src + c_HorseLegTab[to - from + 256]��
 */
const char c_HorseLegTab[512] = {
                               0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,-16,  0,-16,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0, -1,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0, -1,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0, 16,  0, 16,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   0,  0,  0,  0,  0,  0,  0
};

uint16 g_BitRankMask[256];
uint16 g_BitFileMask[256];

// ������λ�С��͡�λ�С����ɳ����ŷ����жϳ����ŷ������Ե�Ԥ�����飬��"PreMoveGen()"��������
slide_move_t g_RankMoveTab[9][512];   // 36,864 �ֽ�
slide_move_t g_FileMoveTab[10][1024]; // 81,920 �ֽ�
slide_mask_t g_RankMaskTab[9][512];   // 36,864 �ֽ�
slide_mask_t g_FileMaskTab[10][1024]; // 81,920 �ֽ�
                                 // ����:  237,568 �ֽ�

// ��������(���ʺ��á�λ�С��͡�λ�С�)���ŷ�Ԥ�������飬��"PreMoveGen()"��������
uint8 g_KingMoves[256][8];
uint8 g_AdvisorMoves[256][8];
uint8 g_BishopMoves[256][8];
uint8 g_ElephantEyes[256][4];
uint8 g_KnightMoves[256][12];
uint8 g_HorseLegs[256][8];
uint8 g_PawnMoves[2][256][4];

// ���ĸ����������ж����ӵ����ӷ�������Ϊ�����ǣ�to = from + c_KnightMoveTab[i]
const int c_KingMoveTab[4] = {-16, -1, 1, 16};
const int c_AdvisorMoveTab[4] = {-17, -15, 15, 17};
const int c_BishopMoveTab[4] = {-34, -30, 30, 34};
const int c_KnightMoveTab[8] = {-33, -31, -18, -14, 14, 18, 31, 33};

void pre_move_gen(void) {
  int from, to, index, i, j, k;
  slide_move_t slide_move_tab;
  slide_mask_t slide_mask_tab;
  for (from = 0; from < 256; from ++) {
    if (c_InBoard[from]) {
      g_BitRankMask[from] = 1 << ((from & 0xf) - 3);
      g_BitFileMask[from] = 1 << ((from >> 4) - 3);
    } else {
      g_BitRankMask[from] = 0;
      g_BitFileMask[from] = 0;
    }    
  }

  // �������ɳ����ŷ���Ԥ������(�����Ӧ�ò���"pregen.h")
  for (i = 0; i < 9; i ++) {
    for (j = 0; j < 512; j ++) {
      // ��ʼ�������ڡ�λ�С��ĳ����ڵ��ŷ�Ԥ�������飬�������¼������裺
      // 1. ��ʼ����ʱ����"slide_move_tab"������û���ŷ�������ʼ�����
      slide_move_tab.non_cap[0] = slide_move_tab.non_cap[1] = slide_move_tab.rook_cap[0] = slide_move_tab.rook_cap[1] =
      slide_move_tab.cannon_cap[0] = slide_move_tab.cannon_cap[1] = slide_move_tab.super_cap[0] = slide_move_tab.super_cap[1] = i + 3;
      slide_mask_tab.non_cap = slide_mask_tab.rook_cap = slide_mask_tab.cannon_cap = slide_mask_tab.super_cap = 0;
      // ��ʾ������"pregen.h"��...[0]��ʾ���һ�������ƶ������ƶ�����[0]����֮��Ȼ
      // 2. ���������ƶ���Ŀ������...[0]��
      for (k = i + 1; k <= 8; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.rook_cap[0] = k + 3;
          slide_mask_tab.rook_cap |= 1 << k;
          break;
        }
        slide_move_tab.non_cap[0] = k + 3;
        slide_mask_tab.non_cap |= 1 << k;
      }
      for (k ++; k <= 8; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.cannon_cap[0] = k + 3;
          slide_mask_tab.cannon_cap |= 1 << k;
          break;
        }
      }
      for (k ++; k <= 8; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.super_cap[0] = k + 3;
          slide_mask_tab.super_cap |= 1 << k;
          break;
        }
      }
      // 3. ���������ƶ���Ŀ������...[1]
      for (k = i - 1; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.rook_cap[1] = k + 3;
          slide_mask_tab.rook_cap |= 1 << k;
          break;
        }
        slide_move_tab.non_cap[1] = k + 3;
        slide_mask_tab.non_cap |= 1 << k;
      }
      for (k --; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.cannon_cap[1] = k + 3;
          slide_mask_tab.cannon_cap |= 1 << k;
          break;
        }
      }
      for (k --; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.super_cap[1] = k + 3;
          slide_mask_tab.super_cap |= 1 << k;
          break;
        }
      }
      // 4. ����ʱ����"slide_move_tab"�������ŷ�Ԥ����������
      g_RankMoveTab[i][j] = slide_move_tab;
      g_RankMaskTab[i][j] = slide_mask_tab;
    }
  }

  for (i = 0; i < 10; i ++) {
    for (j = 0; j < 1024; j ++) {
      // ��ʼ�������ڡ�λ�С��ĳ����ڵ��ŷ�Ԥ�������飬�������¼������裺
      // 1. ��ʼ����ʱ����"slide_move_tab"������û���ŷ�������ʼ�����
      slide_move_tab.non_cap[0] = slide_move_tab.non_cap[1] = slide_move_tab.rook_cap[0] = slide_move_tab.rook_cap[1] =
      slide_move_tab.cannon_cap[0] = slide_move_tab.cannon_cap[1] = slide_move_tab.super_cap[0] = slide_move_tab.super_cap[1] = (i + 3) << 4;
      slide_mask_tab.non_cap = slide_mask_tab.rook_cap = slide_mask_tab.cannon_cap = slide_mask_tab.super_cap = 0;
      // 2. ���������ƶ���Ŀ������...[0]
      for (k = i + 1; k <= 9; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.rook_cap[0] = (k + 3) << 4;
          slide_mask_tab.rook_cap |= 1 << k;
          break;
        }
        slide_move_tab.non_cap[0] = (k + 3) << 4;
        slide_mask_tab.non_cap |= 1 << k;
      }
      for (k ++; k <= 9; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.cannon_cap[0] = (k + 3) << 4;
          slide_mask_tab.cannon_cap |= 1 << k;
          break;
        }
      }
      for (k ++; k <= 9; k ++) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.super_cap[0] = (k + 3) << 4;
          slide_mask_tab.super_cap |= 1 << k;
          break;
        }
      }
      // 3. ���������ƶ���Ŀ������...[1]
      for (k = i - 1; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.rook_cap[1] = (k + 3) << 4;
          slide_mask_tab.rook_cap |= 1 << k;
          break;
        }
        slide_move_tab.non_cap[1] = (k + 3) << 4;
        slide_mask_tab.non_cap |= 1 << k;
      }
      for (k --; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.cannon_cap[1] = (k + 3) << 4;
          slide_mask_tab.cannon_cap |= 1 << k;
          break;
        }
      }
      for (k --; k >= 0; k --) {
        if ((j & (1 << k)) != 0) {
          slide_move_tab.super_cap[1] = (k + 3) << 4;
          slide_mask_tab.super_cap |= 1 << k;
          break;
        }
      }
      // 4. ����ʱ����"slide_move_tab"�������ŷ�Ԥ����������
      g_FileMoveTab[i][j] = slide_move_tab;
      g_FileMaskTab[i][j] = slide_mask_tab;
    }
  }

  // �����������ŷ�Ԥ�������飬��ͬ����Ԥ������
  for (from = 0; from < 256; from ++) {
    if (c_InCity[from]) {
      // ����˧(��)���ŷ�Ԥ��������
      index = 0;
      for (i = 0; i < 4; i ++) {
        to = from + c_KingMoveTab[i];
        if (c_InCity[to]) {
          g_KingMoves[from][index] = to;
          index ++;
        }
      }
      g_KingMoves[from][index] = 0;
      // ������(ʿ)���ŷ�Ԥ��������
      index = 0;
      for (i = 0; i < 4; i ++) {
        to = from + c_AdvisorMoveTab[i];
        if (c_InCity[to]) {
          g_AdvisorMoves[from][index] = to;
          index ++;
        }
      }
      g_AdvisorMoves[from][index] = 0;
    } else {
      g_KingMoves[from][0] = 0;
      g_AdvisorMoves[from][0] = 0;
    }
    if (c_InBoard[from]) {
      // ������(��)���ŷ�Ԥ�������飬������������
      index = 0;
      for (i = 0; i < 4; i ++) {
        to = from + c_BishopMoveTab[i];
        if (c_InBoard[to] && ((from ^ to) & 0x80) == 0) {
          g_BishopMoves[from][index] = to;
          g_ElephantEyes[from][index] = (from + to) >> 1;
          index ++;
        }
      }
      g_BishopMoves[from][index] = 0;
      // ��������ŷ�Ԥ�������飬������������
      index = 0;
      for (i = 0; i < 8; i ++) {
        to = from + c_KnightMoveTab[i];
        if (c_InBoard[to]) {
          g_KnightMoves[from][index] = to;
          g_HorseLegs[from][index] = from + c_HorseLegTab[to - from + 256];
          index ++;
        }
      }
      g_KnightMoves[from][index] = 0;
      // ���ɱ�(��)���ŷ�Ԥ��������
      for (i = 0; i < 2; i ++) {
        index = 0;
        to = (i == 0 ? from - 16 : from + 16);
        if (c_InBoard[to]) {
          g_PawnMoves[i][from][index] = to;
          index ++;
        }
        if (i == 0 ? (from & 0x80) == 0 : (from & 0x80) != 0) {
          for (j = -1; j <= 1; j += 2) {
            to = from + j;
            if (c_InBoard[to]) {
              g_PawnMoves[i][from][index] = to;
              index ++;
            }
          }
        }
        g_PawnMoves[i][from][index] = 0;
      }
    }
  }
}


