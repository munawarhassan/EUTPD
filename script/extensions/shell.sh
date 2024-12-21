#!/bin/bash

set -euo pipefail

red_color='\033[31m'
blue_color='\033[34m'
blue_bg="\033[44m"
nc_color='\033[0m'


function log() { echo -e "$*" >&2; return $? ; }
function doing() { echo -e "${blue_color}>>>${nc_color} $*" >&2; return $? ;  }

function fail() { log "\nERROR: $*\n" ; exit 1 ; }

function qpushd() {
  pushd "$1" &> /dev/null
}

function qpopd() {
  popd &> /dev/null
}

function cmd_exists() {
  command -v "$@" > /dev/null 2>&1
}

function join_by() {
  local d=${1-} f=${2-};
  if shift 2; then
    printf %s "$f" "${@/#/$d}";
  fi;
}

url_encode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""
  local pos c o

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * ) printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  echo "${encoded}"    # You can either set a return variable (FASTER)
  REPLY="${encoded}"   #+or echo the result (EASIER)... or both... :p
}
