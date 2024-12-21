#!/bin/bash

set -e

function log() { echo -e "$*" >&2; return $? ; }

function get_path() {
  local path=$1
  local file_name=$(basename ${path})
  local len=$((${#path} - ${#file_name}))
  echo "${path:0:$len}"
}

function get_upload_pmode_usage() {
  log ""
  log "Usage:  pmode [OPTIONS]"
  log ""
  log " -F|--force         Enfoce update of pmode"
  log " -s=|--server=      Domibus server to use"
  log " -u=|--username=    username to use"
  log " -p=|--password=    password to use"
  log " -f=|--filename=    filename to use"
  log " -h|--help          Display usage of command"
  log ""
}

JSESSIONID=""
XSRFTOKEN=""

# returns the session id stored in cookie file
function session_id() { echo `grep JSESSIONID /tmp/cookie.txt |  cut -d$'\t' -f 7` ; }
# returns the csrf token stored in cookie file
function xsrf_token() { echo `grep XSRF-TOKEN /tmp/cookie.txt |  cut -d$'\t' -f 7` ; }

#----------------------------------------------------------------------------------
# Autenticate the current connection.
#----------------------------------------------------------------------------------
function auth() {
  local server=$1
  local username=$2
  local password=$3

  if [[ -f /tmp/cookie.txt ]]; then
    return
  fi

  log ">>> Logging to domibus to obtain cookies"
  log ""

  curl -s -o /dev/null "${server}/rest/security/authentication" \
  -H "Content-Type: application/json" \
  -d '{"username":"'${username}'","password":"'${password}'"}' \
  -c /tmp/cookie.txt

  log ">>> JSESSIONID=$(session_id)"
  log ">>> XSRFTOKEN=$(xsrf_token)"
  log ">>> X-XSRF-TOKEN=$(xsrf_token)"
}

#----------------------------------------------------------------------------------
# Indicates whether Domibus has a current pmode (is initialized)
#----------------------------------------------------------------------------------
function has_current_pmode() {
  local server=$1
  local username=$2
  local password=$3

  auth "$server" "$username" "$password"

  local response=$(curl -s "${server}/rest/pmode/current" \
  -b /tmp/cookie.txt \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $(xsrf_token)")
  if [[ ${#response} -gt 0 ]]; then
    log "--- Server ${server} contains current PMode"
    echo "true"
  else
    log "--- Server ${server} doesn't contain current PMode"
    echo "false"
  fi
}

#----------------------------------------------------------------------------------
# Upload pmode on Domibus server. See get_upload_pmode_usage function for usage.
#----------------------------------------------------------------------------------
function upload_pmode() {
  local server
  local username
  local password
  local filename
  local force=0

  for i in "$@"
  do
    case $i in
      -F|--force)
      force=1
      shift
      ;;
      -s=*|--server=*)
      server="${i#*=}"
      shift
      ;;
      -u=*|--username=*)
      username="${i#*=}"
      shift
      ;;
      -p=*|--password=*)
      password="${i#*=}"
      ;;
      -f=*|--filename=*)
      filename="${i#*=}"
      ;;
      -h|--help)
      get_upload_pmode_usage
      exit 0
      ;;
    esac
  done

  [ -z "${server}" ] && log "server is not set correctly" && exit 1;
  [ -z "${username}" ] && log "username is not set correctly" && exit 1;
  [ -z "${filename}" ] && log "filename is not set correctly" && exit 1;

  auth "$server" "$username" "$password"
  local pmode_path=$(get_path $filename)

  if [[ $(has_current_pmode "${server}" "${username}" "${password}") = "true" && $force -eq 0 ]]; then
    return
  fi

  pushd ${pmode_path}

  log ">>>   Uploading Pmode : ${filename}"

  curl "${server}/rest/pmode" \
  -b /tmp/cookie.txt \
  -H "X-XSRF-TOKEN: $(xsrf_token)" \
  --form "description=na" --form "file=@$filename" \
  -c /tmp/cookie.txt

  popd
}

#----------------------------------------------------------------------------------
# check to see if this file is being run or sourced from another script
#----------------------------------------------------------------------------------
_is_sourced() {
	# https://unix.stackexchange.com/a/215279
	[ "${#FUNCNAME[@]}" -ge 2 ] \
		&& [ "${FUNCNAME[0]}" = '_is_sourced' ] \
		&& [ "${FUNCNAME[1]}" = 'source' ]
}

_main() {
  if [ "$1" = 'pmode' ]; then
    shift
    upload_pmode "$@"
    exit 0
	fi
  if [ "$1" = "has_current_pmode" ]; then
    shift
    if [[ $(has_current_pmode "$@") ]] ; then
      log "There is a current pmode"
    else
      log "There current pmode is missing"
    fi
    exit 0
	fi
  rm /tmp/cookie.txt ||:

	exec "$@"
}

if ! _is_sourced; then
	_main "$@"
fi
