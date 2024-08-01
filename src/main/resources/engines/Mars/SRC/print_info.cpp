
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
#include ".\print_info.h"
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "move.h"
#include "search.h"

static const char ChessName[14][4] = 
{
	"��","��","��","��","�e","��","��", 
    "��","ʿ","��","�R","܇","��","��"
};

static const char PostionName[2][9][4] = 
{ 
	{"��","��","��","��","��","��","��","��","һ"},
	{"��","��","��","��","��","��","��","��","��"}
};
/*
static const char ChessName32[32][4] = 
{
	"˧","��","��","��","��","��","��", 
    "��","ʿ","��","��","��","��","��"
};*/

char* get_step_name(int move)
{
	int from,to;
	int piece,capture;
	int r0,f0,r1,f1;
	int me,opp,pos;
	
	static char step_name[12];	// �����þ�̬�����������ܷ���

    from = MOVE_FROM(move) ;
	to = MOVE_TO(move);


	piece = Square[from] & (~31);
	capture =Square[to] & (~31);

    r0 = (from >> 4) - 3;  //��
    f0 = (from & 0xf) - 3; //��
   
	r1 = (to >> 4) - 3;
    f1 = (to & 0xf) - 3;
    
    me=PIECE_COLOUR(piece);
	opp=COLOUR_OPP(me);

	pos=Square[from] & 31;

	pos=PieceTo32[pos];

	strcpy( step_name, ChessName[pos] );
	strcat( step_name, PostionName[me][f0] );
	
	//������x0�Ƿ������һ�ųɶԵ�����.
	int y,chess; //y:��
	for(y=0;y<10;y++)
	{
		chess = Square[(y+3)*16+f0+3]; 

		if(chess==0)											// ���Ӽ�������
			continue;

        chess= chess & ~31;

		if(PIECE_IS_ADVISOR(piece) || PIECE_IS_BISHOP(piece))		// ��ʿ��������
			continue;
		if(y==r0)												// ��ͬһ������, ��������.
			continue;

		if(piece==chess)
		{
			if(me)			// ����
			{
				if(r0>y)
					strcpy( step_name, "ǰ" );
				else
					strcpy( step_name, "��" );
			}
			else				// ����
			{
				if(r0>y)
					strcpy( step_name, "��" );
				else
					strcpy( step_name, "ǰ" );
			}

			strcat( step_name, ChessName[pos] );
			break;
		}
	}

	//int piece = PieceTypes[piece]-7*Player;

	//��, ��, ƽ
	if(r0==r1)
	{
		strcat( step_name, "ƽ" );
		strcat( step_name, PostionName[me][f1]);					// ƽ���κ����Ӷ��Ծ���λ�ñ�ʾ
	}
	else if((me && r0<r1) || (!me && r0>r1))
	{
		strcat( step_name, "��" );

		if(PIECE_IS_ADVISOR(piece) || PIECE_IS_BISHOP(piece) ||PIECE_IS_KNIGHT(piece))						// ����ʿ�þ���λ�ñ�ʾ
			strcat( step_name, PostionName[me][f1] );			
		else if(me)												    // ���������ڡ��������λ�ñ�ʾ
			strcat( step_name, PostionName[1][r1-r0-1] );			// �ڷ�
		else
			strcat( step_name, PostionName[0][9-r0+r1] );			// �췽
	}
	else
	{
		strcat( step_name, "��" );

		if(PIECE_IS_ADVISOR(piece) || PIECE_IS_BISHOP(piece) ||PIECE_IS_KNIGHT(piece))						// ����ʿ�þ���λ�ñ�ʾ
			strcat( step_name, PostionName[me][f1] );			
		else if(me)												    // ���������ڡ��������λ�ñ�ʾ
			strcat( step_name, PostionName[1][r0-r1-1] );			// �췽
		else
			strcat( step_name, PostionName[0][9-r1+r0] );			// �ڷ�		
	}

	return(step_name);
}


void print_board()
{
	int sq,pos,piece;
	uint16 move;
	//char f,t;

	printf("------------print-board-v0.3---------------\n");
   
	for(sq=0;sq<90;++sq)
	{
		move=SQUARE_FROM_90(sq);
		//f=(move & 0xf) + 'a' - 3;
		//t='9' + 3-(move>>4);
		printf("%x ",move);
		if( (((sq+1)%9)==0))
		{
			printf("\n");
		}
	}
	 printf("a  b  c  d  e  f  g  h  i\n");
    printf("\n");
	
	for(sq=0;sq<90;++sq)
	{

		piece=Square[SQUARE_FROM_90(sq)];// & 31;
		pos=Square[SQUARE_FROM_90(sq)] & 31;
		if(piece==0)
		{
			printf("0  ");
		}else
		{
            printf("%s ",ChessName[PieceTo32[pos]]);
		}
		//printf("%x=%.3d ",SQUARE_FROM_90(sq),Square[SQUARE_FROM_90(sq)]);
		if( (((sq+1)%9)==0))
		printf("\n");
	}
	printf("\n");
	/*
	for(sq=0;sq<90;++sq)
	{
		
		printf("%.3x ",Square[SQUARE_FROM_90(sq)]>>7);
		if( (((sq+1)%9)==0))
		printf("\n");
	}*//*
    for(sq=0;sq<32;++sq)
	{
		printf("%.2x,",Piece[sq]);
		if( (((sq+1)%16)==0))
		printf("\n");

	}
	printf("Key=%d\n",Key);
	printf("Lock=%d\n",Lock);
	*/
}



void print_list(list_t* list)
{
	int sq,move_str,size;
	//board_t bb[1];

	
    size=list->size;

	//
    //size=5;
	printf("-----------print-list-v0.1---------------\n");
	for(sq=0;sq<size;sq++)
	{	

		move_str=move_to_string(list->move[sq]);
		printf("%x=%.4s=%s  value=%d",list->move[sq],(const char *) &move_str,get_step_name(list->move[sq]),list->value[sq]);//);
		printf("\n");
	}
    
	printf("size=%d\n",list->size);

}
void print_move(int move)
{
	int move_str;
	move_str=move_to_string(move);
	printf("%.4s ",(const char *)&move_str);

}

void print_cn_move(int move)
{
	int move_str;
	move_str=move_to_string(move);
	printf("%.4s=%s ",(const char *)&move_str,get_step_name(move));
}
void save_file(char * file_name)
{
	int sq,pos,piece;
//	uint16 move;
	int move_str;
	mv_t * move_ptr;

	FILE *out = fopen(file_name, "w+");

	fprintf(out, "-------------------������Ϣ-------------------\n\n");
    //fprintf(out,"������� %d\n",SearchCurrent->max_depth);
    //fprintf(out,"seldepth %d time %.0f nodes " S64_FORMAT " nps %.0f",SearchCurrent->max_depth,SearchCurrent->time*1000.0,SearchCurrent->node_nb,SearchCurrent->speed);
	//fprintf(out,"node_qs_n=%d",SearchCurrent->node_qs_nb);

	fprintf(out,"------------print-board-v0.3---------------\n");
   
	for(sq=0;sq<90;++sq)
	{

		piece=Square[SQUARE_FROM_90(sq)];// & 31;
		pos=Square[SQUARE_FROM_90(sq)] & 31;
		if(piece==0)
		{
			fprintf(out,"0  ");
		}else
		{
            fprintf(out,"%s ",ChessName[PieceTo32[pos]]);
		}
		if( (((sq+1)%9)==0))
		fprintf(out,"\n");
	}
	fprintf(out,"\n");

	fprintf(out,"info current depth %d seldepth %d time %.0f nodes " S64_FORMAT " nps %.0f\n",SearchBest->depth,SearchCurrent->max_depth,SearchCurrent->time*1000.0,SearchCurrent->node_nb,SearchCurrent->speed);
    fprintf(out,"----------��Ҫ�������-----------\n");
	
	move_ptr=SearchBest->pv;
	while (*move_ptr!= MoveNone)
	{
		  move_str=move_to_string(*move_ptr);
		  fprintf(out, " %.4s", (const char *)&move_str);
	      move_ptr++;
	 }
	fprintf(out,"\n");


	fclose(out);
}