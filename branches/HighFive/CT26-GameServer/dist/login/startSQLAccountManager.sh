#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*:ct26-login.jar ct26.xtreme.tools.accountmanager.SQLAccountManager
