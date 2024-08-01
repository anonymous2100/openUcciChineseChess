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
#include "pregen.h"

struct attack_t {
   int dn;
   int ds[8];
//   int di[2+1];
};

#define ATTACK_IN_CHECK(attack)           ((attack)->dn!=0)

extern const char  AttackTab[512];
//extern void attack_init();

extern const char ProtectTab[512];

extern bool is_attacked   ( int to, int colour);

//extern bool line_is_empty ( int from, int to);

extern bool is_pinned(int colour);
extern bool is_piece_pinned     (int square, int colour);
extern bool is_cannon_pinned(int square, int colour);
extern bool is_knight_pinned(int move, int colour);
extern bool is_filter_pinned(int move, int colour);

//extern bool attack_is_ok  (const attack_t * attack);
extern void attack_set    (attack_t * attack);//�����ŷ�

extern bool is_in_check(int colour);//�����ж�
//extern bool piece_attack_king ( int piece, int from, int king);

extern bool is_checked(int to,int colour);