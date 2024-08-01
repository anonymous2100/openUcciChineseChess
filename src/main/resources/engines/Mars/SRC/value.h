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

/*
const int ValueKing   = 10000; // was 10000
const int ValueAdvisor= 180;
const int ValueBishop = 160;   // was 300
const int ValueKnight = 450;    // was 300
const int ValueRook   = 1050;   // was 500
const int ValueCannon = 500;    // was 900
const int ValuePawn   = 100;    // was 100
*/
const int ValueKing   = 1000; // was 10000
const int ValueAdvisor= 20;
const int ValueBishop = 20;   // was 300
const int ValueKnight = 45;    // was 300
const int ValueRook   = 100;   // was 500
const int ValueCannon = 45;    // was 900
const int ValuePawn   = 10;    // was 100

const int ValueNone    = -32767;
const int ValueDraw    = 0;
const int ValueMate    = 30000;
const int ValueInf     = ValueMate;
const int ValueEvalInf = ValueMate - 256; // handle mates upto 255 plies

// macros

#define VALUE_MATE(height) (-ValueMate+(height))
#define VALUE_PIECE(piece) (ValuePiece[piece])

extern const int MaterialPiece[32];
extern const int ValuePiece[32];

extern bool value_is_ok      (int value);
extern bool range_is_ok      (int min, int max);

extern bool value_is_mate    (int value);

extern int  value_to_trans   (int value, int height);
extern int  value_from_trans (int value, int height);

extern int  value_to_mate    (int value);