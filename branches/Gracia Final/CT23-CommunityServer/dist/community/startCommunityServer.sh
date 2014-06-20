#!/bin/sh
java -Xms128m -Xmx128m -cp ./../libs/*:ct23cb.jar ct23.xtreme.communityserver.L2CommunityServer > log/stdout.log 2>&1