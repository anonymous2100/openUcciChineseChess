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

********************************************************************/#pragma once

#include "board.h"
#include "util.h"

// constants

const int ListSize = 256;

// macros

#define LIST_CLEAR(list)     ((list)->size=0)
#define LIST_ADD(list,mv)    ((list)->move[(list)->size++]=(mv))

#define LIST_IS_EMPTY(list)  ((list)->size==0)
#define LIST_SIZE(list)      ((list)->size)

#define LIST_MOVE(list,pos)  ((list)->move[pos])
#define LIST_VALUE(list,pos) ((list)->value[pos])

// types

struct list_t {
   int size;
   uint16 move[ListSize];
   sint16 value[ListSize];
};

typedef bool (*move_test_t) (int move);

// functions

extern bool list_is_ok    (const list_t * list);

extern void list_remove   (list_t * list, int pos);

extern void list_copy     (list_t * dst, const list_t * src);

extern void list_sort     (list_t * list);

extern bool list_contain  (const list_t * list, int move);
extern void list_note     (list_t * list);

extern void list_filter   (list_t * list,  move_test_t test, bool keep);
