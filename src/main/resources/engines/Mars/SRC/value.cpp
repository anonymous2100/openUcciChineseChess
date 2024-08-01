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
#include ".\value.h"
#include "util.h"

const int MaterialPiece[32]=
{
	0,
	400,400,
	400,400,
	880,880,
	2000,2000,
	960,960,
	90,90,90,90,90,
	0,
	400,400,
	400,400,
	880,880,
	2000,2000,
	960,960,
	90,90,90,90,90
};
const int ValuePiece[32]=
{
ValueKing ,
ValueAdvisor,ValueAdvisor,
ValueBishop ,ValueBishop ,
ValueKnight ,ValueKnight ,
ValueRook  ,ValueRook  ,
ValueCannon ,ValueCannon ,
ValuePawn ,ValuePawn ,ValuePawn ,ValuePawn ,ValuePawn ,
ValueKing ,
ValueAdvisor,ValueAdvisor,
ValueBishop ,ValueBishop ,
ValueKnight ,ValueKnight ,
ValueRook  ,ValueRook  ,
ValueCannon ,ValueCannon ,
ValuePawn ,ValuePawn ,ValuePawn ,ValuePawn ,ValuePawn
};

// value_is_ok()

bool value_is_ok(int value) {

   if (value < -ValueInf || value > +ValueInf) return false;

   return true;
}

// range_is_ok()

bool range_is_ok(int min, int max) {

   if (!value_is_ok(min)) return false;
   if (!value_is_ok(max)) return false;

   if (min >= max) return false; // alpha-beta-like ranges cannot be null

   return true;
}

// value_is_mate()

bool value_is_mate(int value) {

   

   if (value < -ValueEvalInf || value > +ValueEvalInf) return true;

   return false;
}

// value_to_trans()

int value_to_trans(int value, int height) {

   
   ASSERT(height_is_ok(height));

   if (value < -ValueEvalInf) {
      value -= height;
   } else if (value > +ValueEvalInf) {
      value += height;
   }

   

   return value;
}

// value_from_trans()

int value_from_trans(int value, int height) {

   
   ASSERT(height_is_ok(height));

   if (value < -ValueEvalInf) {
      value += height;
   } else if (value > +ValueEvalInf) {
      value -= height;
   }

   

   return value;
}

// value_to_mate()

int value_to_mate(int value) {

   int dist;

   

   if (value < -ValueEvalInf) {

      dist = (ValueMate + value) / 2;
      ASSERT(dist>0);

      return -dist;

   } else if (value > +ValueEvalInf) {

      dist = (ValueMate - value + 1) / 2;
      ASSERT(dist>0);

      return +dist;
   }

   return 0;
}

