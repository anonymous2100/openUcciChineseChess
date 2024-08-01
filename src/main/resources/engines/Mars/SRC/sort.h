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

#include "attack.h"
#include "board.h"
#include "list.h"
#include "util.h"

struct sort_t {
   int depth;
   int height;
   int trans_killer;
   int killer_1;
   int killer_2;
   int killer_3;
   int killer_4;
   int gen;
   int test;
   int pos;
   int value;
   const attack_t * attack;
   list_t list[1];
   list_t bad[1];
};

// functions

extern void sort_init    ();

extern void sort_init    (sort_t * sort,  const attack_t * attack, int depth, int height, int trans_killer);
extern int  sort_next    (sort_t * sort);

extern void sort_init_qs (sort_t * sort,  const attack_t * attack, bool check);
extern int  sort_next_qs (sort_t * sort);

extern void good_move    (int move,  int depth, int height);

extern void history_good (int move);
extern void history_bad  (int move);

extern void note_moves   (list_t * list,  int height, int trans_killer);


extern void test_gen_good_capture(list_t * list);
extern void test_sort(sort_t * sort);

//extern bool capture_is_good   (int move);
