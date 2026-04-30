@echo off
echo 启动玩家1...
start "Player1" java -jar target\openUcciChineseChess-0.0.1-SNAPSHOT-jar-with-dependencies.jar
timeout /t 2 /nobreak >nul
echo 启动玩家2...
start "Player2" java -jar target\openUcciChineseChess-0.0.1-SNAPSHOT-jar-with-dependencies.jar
echo 两个实例已启动，请在玩家1窗口中先启动本地服务器。