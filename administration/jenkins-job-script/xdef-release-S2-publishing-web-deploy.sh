: "deploy xdef-web.war na tomcat-11 na www.xdef.org"
suffix="release"
server="xdeforg" #docasna hodnota, viz /etc/hosts
webapps_dir="/opt/tomcat/webapps-context-conf"
scp -i "${BUILDER_SSH_IDENTFILE}" "xdef-web/target/xdef-web.war" "${BUILDER_SSH_USERNAME}@${server}:${webapps_dir}/xdef-web-${suffix}.war.tmp"
ssh -i "${BUILDER_SSH_IDENTFILE}" "${BUILDER_SSH_USERNAME}@${server}" "{ cd ${webapps_dir}; mv xdef-web-${suffix}.war.tmp xdef-web-${suffix}.war; }"
