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
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "ucci.h"
#include "pregen.h"
#include "board.h"
#include "fen.h"
#include "list.h"
#include "move_gen.h"
#include "move.h"
#include "attack.h"
#include "move_do.h"
#include "piece.h"
#include "square.h"
#include "move_evasion.h"
#include "move_check.h"
#include "search.h"
#include "sort.h"
#include "trans.h"
#include "material.h"
#include "print_info.h"
#include <time.h>

//board_t g_board[1];
list_t  g_list[1];
sort_t  g_sort[1];
undo_t  g_undo[1];
//attack_t g_attack[1];

//PieceTo32

FILE * OutFile;

int main(int argc, char * argv[])
{
	const char *BoolValue[2] = { "false", "true" };
    printf("Mars V0.3 UCCI based on Fruit 2.1 by Fabien Letouzey\n");
	printf("���ߣ��˽���\n");
    printf("��վ��www.jsmaster.com\n");
	printf("�����ucciָ��......\n");
    //position fen rnbakab2/9/2c1c1n2/p1p1p3p/6p2/2PN3r1/P3P1P1P/1C2C1N2/9/1RBAKAB1R w - - 0 7   
	//position fen 1rbakab2/9/1cn5n/pR4p1p/2p1p4/4c1P2/P1P4rP/2N1C1NC1/4A4/2B1KABR1 w - - 0 10
	//9/3ka4/3R5/5r2p/p5p2/4N4/P5P1P/9/4A2c1/2BK2B2 b - - 0 36
	CommEnum IdleComm;
	CommDetail Command;
	int move,n;
	undo_t undo[1];
 
	if(BootLine() == e_CommUcci)
	{

		zobrist_gen();
	    pre_move_gen();
	    trans_init(Trans);
	    trans_alloc(Trans);
        material_init();
        material_alloc(); 
		OutFile = stdout;
        board_from_fen("rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR r - - 0 1");
		//board_from_fen("3k5/9/9/9/9/4c4/9/9/4n4/4K4 r - - 0 1");
		//board_from_fen("3k5/9/9/9/9/4c4/3C5/2n1n4/4K4/9 r - - 0 1");
		printf("position fen rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR r - - 0 1\n\n");
		print_board();
		printf("\n");
		printf("id name Mars v0.3\n");
		fflush(stdout);
		printf("id copyright ��Ȩ����(C) 2005-2008\n");
		fflush(stdout);
		printf("id author �˽���\n");
		fflush(stdout);
		printf("id user δ֪\n\n");
		fflush(stdout);
/*
				printf("option batch type check default %s\n", BoolValue[ThisSearch.bBatch]);
		fflush(stdout);

		// option debug �����������ϸ��������Ϣ�����������ĵ���ģʽ��
		printf("option debug type check default %s\n", BoolValue[ThisSearch.Debug]);
		fflush(stdout);

		// ָ�����ֿ��ļ������ƣ���ָ��������ֿ��ļ����÷ֺš�;���������粻������ʹ�ÿ��ֿ⣬���԰�ֵ��ɿ�
		ThisSearch.bUseOpeningBook = ThisSearch.m_Hash.LoadBook(BookFile);
		if(ThisSearch.bUseOpeningBook)
			printf("option bookfiles type string default %s\n", BookFile);
		else
			printf("option bookfiles type string default %s\n", 0);
		fflush(stdout);

		// �оֿ�����
		printf("option egtbpaths type string default null\n");
		fflush(stdout);

		// ��ʾHash��Ĵ�С
		printf("option hashsize type spin default %d MB\n", ThisSearch.m_Hash.nHashSize*2*sizeof(CHashRecord)/1024/1024);
		fflush(stdout);

		// ������߳���
		printf("option threads type spin default %d\n", 0);
		fflush(stdout);

		// ����ﵽ��Ȼ���ŵİ�غ���
		printf("option drawmoves type spin default %d\n", ThisSearch.NaturalBouts);
		fflush(stdout);

		// ���
		printf("option repetition type spin default %d 1999��桶�й����徺������\n", e_RepetitionChineseRule);
		fflush(stdout);

		// ���Ųü��Ƿ��
		printf("option pruning type check %d\n", ThisSearch);
		fflush(stdout);

		// ��ֵ������ʹ�����
		printf("option knowledge type check %d\n", ThisSearch);
		fflush(stdout);

		// ָ��ѡ����ϵ����ͨ����0,1,2,3�ĸ����𡣸���ֵ�����Ӽ�һ����Χ�ڵ��������������ÿ���߳�����ͬ���塣
		printf("option selectivity type spin min 0 max 3 default %d\n", ThisSearch.nSelectivity);
		fflush(stdout);

		// ָ������ķ��ͨ����solid(����)��normal(����)��risky(ð��)����
		printf("option style type combo var solid var normal var risky default %s\n", ChessStyle[ThisSearch.nStyle]);
		fflush(stdout);		
*/
		// copyprotection ��ʾ��Ȩ�����Ϣ(���ڼ�飬��Ȩ��Ϣ��ȷ���Ȩ��Ϣ����)�� 
		printf("copyprotection ok\n\n");
		fflush(stdout);

		// ucciok ����ucciָ������һ��������Ϣ����ʾ�����Ѿ�������UCCIЭ��ͨѶ��״̬��
		printf("ucciok\n\n");
		fflush(stdout);
        
		//FILE * out=fopen("info.txt","w+");
		do 
		{
			IdleComm = IdleLine(Command, 0);
			switch (IdleComm) 
			{
				// isready ��������Ƿ��ھ���״̬���䷴����Ϣ����readyok����ָ����������������ġ�ָ����ջ��������Ƿ�����������ָ�
				// readyok �������洦�ھ���״̬(���ɽ���ָ���״̬)���������洦�ڿ���״̬����˼��״̬��
				case e_CommIsReady:
					//fprintf(out,"readyok\n");
					printf("readyok\n");
					fflush(stdout);
					break;

				// stop �ж������˼����ǿ�Ƴ��š���̨˼��û������ʱ�����ø�ָ������ֹ˼����Ȼ������������档
				case e_CommStop:
					//ThisSearch.bStopThinking = 1;
					printf("nobestmove\n");
					printf("score 0\n");
					fflush(stdout);
					break;

				// position fen ���á��������̡��ľ��棬��fen��ָ��FEN��ʽ����moves�������������߹����ŷ�
				case e_CommPosition:
					// �����洫����Fen��ת��Ϊ�����Ϣ
					// board_clear();
					//fprintf(out,"%s\n",Command.Position.FenStr);
                    //fprintf(out,"%d",Command.Position.MoveNum);
					//fprintf(out,"%s\n",Command.Position.CoordList);
					 //trans_clear(Trans);
					 board_from_fen(Command.Position.FenStr);
					 print_board();
					 for(n=0; n<Command.Position.MoveNum; n++)
					{
						move = move_from_string(Command.Position.CoordList[n]);
						//fprintf(out,"%x ",move);
						if( !move )
							break;

						move_do(move,undo);
						//ThisSearch.StepRecords[ThisSearch.nCurrentStep-1] |= ThisSearch.Checking(ThisSearch.Player) << 24;
					}
					// �������ߵ���ǰ����Ҫ��Ϊ�˸����ŷ���¼������ѭ����⡣
					break;

				// banmoves Ϊ��ǰ�������ý��֣��Խ�������޷�����ĳ������⡣�����ֳ������ʱ�����ֿ��Բٿؽ��������淢������ָ�
				case e_CommBanMoves:
					break;

				// setoption ����������ֲ���
				case e_CommSetOption:
					switch(Command.Option.Type) 
					{
						// setoption batch %d
						case e_OptionBatch:
							batch=(Command.Option.Value.Check == e_CheckTrue);
							printf("option batch type check default %s\n", BoolValue[batch]);
							fflush(stdout);
							break;

						// setoption debug %d �����������ϸ��������Ϣ�����������ĵ���ģʽ��
						case e_OptionDebug:
							
							break;

						// setoption bookfiles %s  ָ�����ֿ��ļ������ƣ���ָ��������ֿ��ļ����÷ֺš�;���������粻������ʹ�ÿ��ֿ⣬���԰�ֵ��ɿ�
						case e_OptionBookFiles:
							
							break;

						// setoption egtbpaths %s  ָ���оֿ��ļ������ƣ���ָ������оֿ�·�����÷ֺš�;���������粻������ʹ�òоֿ⣬���԰�ֵ��ɿ�
						case e_OptionEgtbPaths:
							// ����Ŀǰ��֧�ֿ��ֿ�
							
							break;

						// setoption hashsize %d  ��MBΪ��λ�涨Hash��Ĵ�С��-1��ʾ�������Զ�����Hash��1��1024MB
						// �󱤽����и�Bug��ÿ����������ʱ���������Ӧ�ڿ��ֿ��ǰ��
						case e_OptionHashSize:
							// -1MB(�Զ�), 0MB(�Զ�), 1MB(16), 2MB(17), 4MB(18), 8MB(19), 16MB(20), 32MB(21), 64MB(22), 128MB(23), 256MB(24), 512MB(25), 1024MB(26)
							if( Command.Option.Value.Spin <= 0)
								n = 22;		// ȱʡ����£������Զ�����(1<<22)*16=64MB�������������˫����һ�롣
							else
							{
								n = 15;											// 0.5 MB = 512 KB �Դ�Ϊ����
								while( Command.Option.Value.Spin > 0 )
								{
									Command.Option.Value.Spin >>= 1;			// ÿ�γ���2��ֱ��Ϊ0
									n ++;
								}
							}								

							break;

						// setoption threads %d	      ������߳�����Ϊ�ദ���������������
						case e_OptionThreads:
							// ThisSearch.nThreads = Command.Option.Value.Spin;		// 0(auto),1,2,4,8,16,32
							printf("option drawmoves type spin default %d\n", 0);
							fflush(stdout);
							break;

						// setoption drawmoves %d	  �ﵽ��Ȼ���ŵĻغ���:50,60,70,80,90,100�����Ѿ��Զ�ת��Ϊ��غ���
						case e_OptionDrawMoves:
							
							break;

						// setoption repetition %d	  ����ѭ������棬Ŀǰֻ֧�֡��й��������1999��
						case e_OptionRepetition:
							// ThisSearch.nRepetitionStyle = Command.Option.Value.Repetition;
							// e_RepetitionAlwaysDraw  ��������
							// e_RepetitionCheckBan    ��ֹ����
							// e_RepetitionAsianRule   ���޹���
							// e_RepetitionChineseRule �й�����ȱʡ��
							
							break;

						// setoption pruning %d����������ǰ�ü����Ƿ��
						case e_OptionPruning:
							//ThisSearch.bPruning = Command.Option.Value.Scale;
							//printf("option pruning type check %d\n", ThisSearch);
							//fflush(stdout);
							break;

						// setoption knowledge %d����ֵ������ʹ��
						case e_OptionKnowledge:
							//ThisSearch.bKnowledge = Command.Option.Value.Scale;
							//printf("option knowledge type check %d\n", ThisSearch);
							//fflush(stdout);
							break;

						// setoption selectivity %d  ָ��ѡ����ϵ����ͨ����0,1,2,3�ĸ�����
						case e_OptionSelectivity:
							/*switch (Command.Option.Value.Scale)
							{
								case e_ScaleNone:
									ThisSearch.SelectMask = 0;
									break;
								case e_ScaleSmall:
									ThisSearch.SelectMask = 1;
									break;
								case e_ScaleMedium:
									ThisSearch.SelectMask = 3;
									break;
								case e_ScaleLarge:
									ThisSearch.SelectMask = 7;
									break;
								default:
									ThisSearch.SelectMask = 0;
									break;
							}
							printf("option selectivity type spin min 0 max 3 default %d\n", ThisSearch.SelectMask);
							fflush(stdout);*/
							break;

						// setoption style %d  ָ������ķ��ͨ����solid(����)��normal(����)��risky(ð��)����
						case e_OptionStyle:
							//ThisSearch.nStyle = Command.Option.Value.Style;
							//printf("option style type combo var solid var normal var risky default %s\n", ChessStyle[Command.Option.Value.Style]);
							//fflush(stdout);
							break;						

						// setoption loadbook  UCCI����ElephantBoard��ÿ���½����ʱ���ᷢ������ָ��
						case e_OptionLoadBook:
							/*ThisSearch.m_Hash.ClearHashTable();
							ThisSearch.bUseOpeningBook = ThisSearch.m_Hash.LoadBook(BookFile);
							
							if(ThisSearch.bUseOpeningBook)
								printf("option loadbook succeed. %s\n", BookFile);		// �ɹ�
							else
								printf("option loadbook failed! %s\n", "Not found file BOOK.DAT");				// û�п��ֿ�
							fflush(stdout);
							printf("\n\n");
							fflush(stdout);
							*/
							break;

						default:
							break;
					}
					break;

				// Prepare timer strategy according to "go depth %d" or "go ponder depth %d" command
				case e_CommGo:
				case e_CommGoPonder:
					switch (Command.Search.Mode)
					{
						// �̶����Command.Search.DepthTime.Depth
						case e_TimeDepth:
							ponder = 2;
							search(Command.Search.DepthTime.Depth,0,0);
							break;

						// ʱ���ƣ� ����ʱ�� = ʣ��ʱ�� / Ҫ�ߵĲ���
						case e_TimeMove:
							ponder = (IdleComm == e_CommGoPonder ? 1 : 0);
							search(32,Command.Search.DepthTime.Time * 1000 / Command.Search.TimeMode.MovesToGo, Command.Search.DepthTime.Time * 1000);
						//	ThisSearch.Ponder = (IdleComm == e_CommGoPonder ? 1 : 0);
						//	printf("%d\n", Command.Search.TimeMode.MovesToGo);
						//	ThisSearch.MainSearch(127, Command.Search.DepthTime.Time * 1000 / Command.Search.TimeMode.MovesToGo, Command.Search.DepthTime.Time * 1000);
							break;

						// ��ʱ�ƣ� ����ʱ�� = ÿ�����ӵ�ʱ�� + ʣ��ʱ�� / 20 (��������ֻ���20���ڽ���)
						case e_TimeInc:
                            ponder = (IdleComm == e_CommGoPonder ? 1 : 0);
							search(32,(Command.Search.DepthTime.Time + Command.Search.TimeMode.Increment * 20) * 1000 / 20, Command.Search.DepthTime.Time * 1000);
						 //	ThisSearch.Ponder = (IdleComm == e_CommGoPonder ? 1 : 0);
						 //	ThisSearch.MainSearch(127, (Command.Search.DepthTime.Time + Command.Search.TimeMode.Increment * 20) * 1000 / 20, Command.Search.DepthTime.Time * 1000);
							break;

						default:
							break;
					}
					break;
			}
		} while (IdleComm != e_CommQuit);
        //fprintf(out,"bye");
		//fclose(out);
		printf("bye\n");
		fflush(stdout);
		//getchar();
	}

	//getchar();
	return 0;
}