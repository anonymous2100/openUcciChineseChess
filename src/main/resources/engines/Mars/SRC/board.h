/********************************************************************

	----------------------------------------------------------------
    ������֤ �� GPL
	��Ȩ���� (C) 2005-2008 �����˹������о�
	----------------------------------------------------------------
	��һ����������������������������������������GNU ͨ�ù�����
	��֤�������޸ĺ����·�����һ���򡣻��������֤�ĵڶ��棬���ߣ���
	�����ѡ�����κθ��µİ汾��

    ������һ�����Ŀ����ϣ�������ã���û���κε���������û���ʺ��ض�
	Ŀ�ص������ĵ���������ϸ����������GNUͨ�ù������֤��

    ��Ӧ���Ѿ��ͳ���һ���յ�һ��GNUͨ�ù������֤�ĸ�������Ŀ¼
	GPL.txt�ļ����������û�У�д�Ÿ���
    The Free Software Foundation, Inc.,  675  Mass Ave,  Cambridge,
    MA02139,  USA
	----------------------------------------------------------------
	�������ʹ�ñ����ʱ��ʲô������飬�������µ�ַ������ȡ����ϵ��

			http://www.jsmaster.com

	���ŵ���

			jschess##163.com
	----------------------------------------------------------------
	���ļ���;��	��
	
	  
	���ļ���д�ˣ�	
				�˽���			jschess##163.com
		
	���ļ��汾��	03
	����޸��ڣ�	2006-1-16
		  
	ע������E-Mail��ַ�е�##����@�滻����������Ϊ�˵��ƶ����E-Mail
	��ַ�ռ������
	----------------------------------------------------------------
	������ʷ��
			
		  2006-1		��һ�淢��

********************************************************************/
#pragma once
#include "colour.h"
#include "piece.h"
#include "util.h"
#include "square.h"

const int Empty = 0;
const int Edge = Bishop90; // HACK: uncoloured knight

const int FlagsNone = 0;
const int StackSize = 4096;

extern int piece_material[ColourNb]; // Thomas
extern int Square[SquareNb];         //����λ����
extern sq_t Piece[32];               //��������0~15Ϊ�죬16~31Ϊ�ڣ�ֵΪ0ʱ����ʾ�����Ѿ�����
extern int Number[16];               // ǰ14λΪ������ͳ��
extern uint16 BitRanks[16];         // λ�����飬ע���÷���"bitRanks[Squares >> 4]"
extern uint16 BitFiles[16];         // λ�����飬ע���÷���"bitRanks[Squares & 0xf]"
extern int Turn;                     //0Ϊ�죬1Ϊ��
extern int Opening;
extern int Endgame;
extern unsigned long Key;           // Zobrist��ֵ
extern uint64 Lock;                 // ZobristУ����
extern uint64 Stack[StackSize];
extern int Piece_size[ColourNb];
extern int Sp;
extern int Piece_nb;
extern int Ply_nb;

extern const int BitPieceTypes[48];
extern bool board_is_check();
extern bool board_is_mate();
extern void board_clear();
extern void board_init_list();
//extern void board_copy(board_t * dst, const board_t * src);
