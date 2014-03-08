@echo off
title Login Server Console

:start
echo Starting BR Xtreme Login Server.
echo.

java -Xms128m -Xmx256m -cp ./../libs/*;ct26-login.jar ct26.xtreme.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Login Server.
echo.
goto start

:error
echo.
echo Login Server terminated abnormally!
echo.

:end
echo.
echo Login Server Terminated.
echo.
pause