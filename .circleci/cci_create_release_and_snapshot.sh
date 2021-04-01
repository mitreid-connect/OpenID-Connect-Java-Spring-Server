#!/bin/bash

REPOSITORY=https://github.com/gresham-computing/openid-connect-server
MASTER_BRANCH=1.3.x

function get_version {
    local currentVersion=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
    IFS='-' read -r -a parts <<< "$currentVersion"

    local NEXT_NUMBER="$((${parts[1]} + 1))"
    RELEASE_VERSION="${parts[0]}"-"${parts[1]}"
    NEXT_SNAPSHOT_VERSION="${parts[0]}"-$NEXT_NUMBER-SNAPSHOT
}

function bump_to_release {
    mvn -s gresham-nexus-settings/ctc.plugins.settings.xml versions:set -DnewVersion=$RELEASE_VERSION
    git tag v$RELEASE_VERSION
    echo -e "\nopenid-connect-server release: $RELEASE_VERSION\n"
}

function bump_to_next_snapshot {
    mvn -s gresham-nexus-settings/ctc.plugins.settings.xml versions:set -DnewVersion=$NEXT_SNAPSHOT_VERSION
    echo -e "\nopenid-connect-server snapshot: $NEXT_SNAPSHOT_VERSION\n"
}

function commit_changes {
    git commit -a -m "$1"
}

function push_changes {
    git push $REPOSITORY $MASTER_BRANCH --tags
}

get_version
bump_to_release
commit_changes "New openid-connect-server release: ${RELEASE_VERSION}"
push_changes
bump_to_next_snapshot
commit_changes "Next openid-connect-server snapshot: $NEXT_SNAPSHOT_VERSION"
push_changes
