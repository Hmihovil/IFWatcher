#!/bin/bash

./gradlew assembleRelease
version=`cat app/build.gradle  | grep versionCode | sed "s/.*versionCode \(.*\)/\1/g"`
rm -f version.txt
wget "http://valxp.net/IFWatcher/version.txt"
currentVersion=`cat version.txt | head -n 1`
lastChangelog=`cat version.txt | tail -n +2`
echo "Uploading version to $version. Version on server is $currentVersion"
echo "Last changelog :"
echo $lastChangelog
echo ""
read -e -p "Upload new version? (yes/no): "
if [ "$REPLY"  != "yes" ]; then
    echo "Cancelled"
    exit 1
fi 
echo "$version" > version.txt
read -e -p "Add changelog? (yes/no): "
if [ "$REPLY" == "yes" ]; then
    echo -e "Please type changelog (Ctrl+d to finish): "
    cat >> version.txt
fi
cp app/build/apk/app-release.apk IFWatcher.apk
scp IFWatcher.apk version.txt root@valxp.net:./http/IFWatcher/
rm version.txt IFWatcher.apk
echo "Done"
