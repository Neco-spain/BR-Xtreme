@echo off
title BR Xtreme - Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;ct26-login.jar ct26.xtreme.tools.gsregistering.BaseGameServerRegister -c
pause