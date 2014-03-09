@echo off
javaw -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;ct23-server.jar ct23.xtreme.gsregistering.BaseGameServerRegister
exit