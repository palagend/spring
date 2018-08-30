#!/bin/sh
default_profile="test"
if [-z ${RANCHER_ENABLE:+x}]; then
    source /opt/init.sh
fi

if [ -z ${IDS_PROFILE:+x} ]; then 
	echo "IDS_PROFILE is empty. IDS_PROFILE $default_ip will be used as default."; 
else
	echo "Environment Variable is set to IDS_PROFILE = '$IDS_PROFILE'."; 
fi

if [ ! -d /opt/ids/config ]; then
	mkdir -p /opt/ids/config
fi

if [ -z ${IDS_SET_EXTERNAL_CONFIG+x} ]; then 
	echo 'IDS_SET_EXTERNAL_CONFIG is not set, will use INTERNAL config'
	java -jar /opt/app.jar --spring.profiles.active=${IDS_PROFILE:-$default_profile}
else
	echo 'IDS_SET_EXTERNAL_CONFIG is set, YOU should MOUNT EXTERNAL DIRECTORY on "/opt/ids/config"'
	java -jar /opt/app.jar --spring.profiles.active=${IDS_PROFILE:-$default_profile} --spring.cloud.bootstrap.location=/opt/ids/config/
fi
