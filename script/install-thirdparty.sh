#!/bin/bash

set +x

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source ${dir}/setenv.sh

ROOT_PATH=${dir}/..
cache_path=${dir}/../.cache

tomcat_version="9.0.85"
tomcat_major_version="9"
tomcat_groupId="org.apache.tomcat"
tomcat_artifactId="apache-tomcat"
  
jdk_version="11.0.21_9"
jdk_version_enc="11.0.21%2B9"
jdk_groupId="net.adoptium"
jdk_artifactId="jdk-hotspot"

tomcat_install() {
  local version=$1
  local groupId=$tomcat_groupId
  local artifactId=$tomcat_artifactId
  local classifier=$2
  local packaging=$3

  local file_path="${cache_path}/${artifactId}-${version}${classifier:+-$classifier}.${packaging}"
  local download_path="https://dlcdn.apache.org/tomcat/tomcat-${tomcat_major_version}/v${version}/bin/apache-tomcat-${version}${classifier:+-$classifier}.${packaging}"

  if [[ -f $file_path  ]]; then
    log ">>> ${artifactId}-${version}${classifier:+-$classifier}.${packaging} already exists in cache"
  elif [[ ! -f $file_path  ]]; then
    wget -P ${cache_path} $download_path
  fi

  mvn_install_file $file_path "${groupId}" "${artifactId}" "${version}" "${classifier}" "${packaging}"

}

openjdk_hotspot_install() {
  local download_url=$1
  local version=$2
  local classifier=$3
  local packaging=$4
  local groupId=$jdk_groupId
  local artifactId=$jdk_artifactId

  local file_path="${cache_path}/$(basename $download_url)"

  if [[ -f $file_path  ]]; then
    log ">>> ${artifactId}-${version}${classifier:+-$classifier}.${packaging} already exists in cache"
  elif [[ ! -f $file_path  ]]; then
    wget -P ${cache_path} $download_url
  fi

  mvn_install_file $file_path "${groupId}" "${artifactId}" "${version}" "${classifier}" "${packaging}"


}



tomcat_install "${tomcat_version}" "" "zip"
tomcat_install "${tomcat_version}" "windows-x64" "zip"


openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-${jdk_version_enc}/OpenJDK11U-jdk_aarch64_linux_hotspot_${jdk_version}.tar.gz" ${jdk_version} "linux-x64" "tar.gz"
openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-${jdk_version_enc}/OpenJDK11U-jdk_x64_mac_hotspot_${jdk_version}.tar.gz" ${jdk_version} "mac-x64" "tar.gz"
openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-${jdk_version_enc}/OpenJDK11U-jdk_x64_windows_hotspot_${jdk_version}.zip" ${jdk_version} "windows-x64" "zip"




