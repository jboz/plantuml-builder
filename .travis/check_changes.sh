#!/bin/bash

hasChange() {
    local check_path=$1

    echo "Checking changes since ${TRAVIS_COMMIT_RANGE} against ${check_path}"
    GITDIFF=$(git diff --name-only ${TRAVIS_COMMIT_RANGE} | grep ${check_path} | tr -d '[:space:]')
    if [[ "$GITDIFF" == "" ]]; then
        echo "No code changes, skipped"
        return 0
    fi
    echo "Code changes"
    return 1
}

changes=0

# can have many directory as paramters
for path in "$@"; do
    hasChange ${path}
    result=$?
    if [[ ${result} = 1 ]]; then
        changes=1
    fi
done

if [[ ${changes} = 0 ]]; then
    # at leat one directory contains changes
    exit 1
fi
