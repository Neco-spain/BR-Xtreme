#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*:ct23-server.jar:ct23-login.jar ct23.xtreme.configurator.ConfigUserInterface
