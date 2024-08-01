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

#include "board.h"
#include "util.h"

struct undo_t {

   bool capture;

   int capture_square;
   int capture_piece;
   //int capture_pos;

   //int pawn_pos;
   //int from;

   int turn;
   int flags;
   int ep_square;
   int ply_nb;

   int cap_sq;

   int opening;
   int endgame;

   //uint16 bit_ranks[16];         // λ�����飬ע���÷���"bitRanks[Squares >> 4]"
   //uint16 bit_files[16];         // λ�����飬ע���÷���"bitRanks[Squares & 0xf]"

   unsigned long key;      // Zobrist��ֵ
   uint64 lock;            // ZobristУ����
};


extern void move_do        ( int move, undo_t * undo);
extern void move_undo      ( int move, const undo_t * undo);

extern void move_do_null   ( undo_t * undo);
extern void move_undo_null ( const undo_t * undo);
