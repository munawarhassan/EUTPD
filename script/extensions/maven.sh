#!/bin/bash

set -euo pipefail

M2_HOME=~/.m2/repository

get_mvn_property() {
    mvn -N -f "${ROOT_PATH}/pom.xml" -q exec:exec -Dexec.executable=echo -Dexec.args="\${$@}"
}


get_project_version() {
    get_mvn_property "project.version"
}

function add_mvn_profile() {
    local current_profiles=$1
    local new_profile=$2
    if [[ -z "${current_profiles}" ]]; then
        echo "-P ${new_profile}"
    else
        echo "${current_profiles},${new_profile}"
    fi
}

artifact_exists() {
	local group="$1"
    local artifact="$2"
    local version="$3"
    local classifier="${4-}"
    local extension="${5:-jar}"

    local artifactId="$group:$artifact:$version:$extension${classifier:+":$classifier"}"
    local resource="$(tr . / <<<"$group")/$artifact/$version/$artifact-$version${classifier:+"-$classifier"}.$extension"
    local output

    doing "Checking whether $artifactId already exists on..."

    if ! output="$(mvn -e -N org.codehaus.mojo:wagon-maven-plugin:2.0.0:exist -f "${ROOT_PATH}/pom.xml" -Dwagon.serverId="${MAVEN_REPO_ID}" -Dwagon.url="${MAVEN_REPO_URL}" -Dwagon.resource="$resource")"; then
        log "!!! Failed to execute maven command:"
        log "$output"
        exit 1
    fi

    if grep "$resource exists." <<<"$output" >/dev/null; then
        log "- $artifactId already exists"
    else
        log "- $artifactId does not exist"
        return 1
    fi
}


deploy_file() {
    local file="$1"
    local group="$2"
    local artifact="$3"
    local version="$4"
    local classifier="${5-}"
    local extension="${6:-jar}"

    doing "Deploying file '$(basename "$file")'"

    # The 'restricted' repository on artifactory doesn't allow overwriting of existing pom - just skip it in that case
    local generate_pom=true
    if artifact_exists "$group" "$artifact" "$version" "" "pom" 2>/dev/null; then
        log '--- Skipping publishing of POM since it already exists'
        generate_pom=false
    fi

    mvn -N -e deploy:deploy-file -Durl="${MAVEN_REPO_URL}" \
                              -DrepositoryId="${MAVEN_REPO_ID}" \
                              -Dfile="$file" \
                              -DgroupId="$group" \
                              -DartifactId="$artifact" \
                              -Dversion="$version" \
                              ${classifier:+"-Dclassifier=$classifier"} \
                              -Dpackaging="${extension}" \
                              -DgeneratePom="$generate_pom"
}

mvn_install_file() {
    local file="$1"
    local group="$2"
    local artifact="$3"
    local version="$4"
    local classifier="${5-}"
    local extension="${6:-jar}"
    local groupId=$(echo $group | sed -r 's/[\.]+/\//g')
    local m2_file_path="${M2_HOME}/${groupId}/${artifact}/${version}/${artifact}-${version}${classifier:+-$classifier}.${extension}"

    if [[ -f $m2_file_path  ]]; then
        log ">>> ${artifactId}-${version}${classifier:+-$classifier}.${packaging} already exists in m2 local repository"
        return 0;
    fi
  
    doing "Installing file '$(basename "$file")'"
             

    mvn -N -e install:install-file \
                              -Dfile="$file" \
                              -DgroupId="$group" \
                              -DartifactId="$artifact" \
                              -Dversion="$version" \
                              ${classifier:+"-Dclassifier=$classifier"} \
                              -Dpackaging="${extension}"
    log "Add the following to the ${groupId}:${artifactId} pom.xml"
    log ""
    log "<dependency>"
    log "  <groupId>${group}</groupId>"
    log "  <artifactId>${artifact}</artifactId>"
    log "  <version>${version}</version>"
    log "  <classifier>${classifier}</classifier>"
    log "</dependency>"                              
}

