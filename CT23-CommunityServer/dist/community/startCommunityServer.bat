@echo off
title Community Server Console
echo Starting Br Xtreme Community Server.
echo.
java -Xms128m -Xmx128m -cp ./../libs/*;ct23cb.jar ct23.xtreme.communityserver.L2CommunityServer
pause
