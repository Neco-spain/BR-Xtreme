@echo off
color 17
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;ct23-login.jar ct23.xtreme.gsregistering.BaseGameServerRegister -c
exit
