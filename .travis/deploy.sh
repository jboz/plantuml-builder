#!/bin/bash

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "master" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

MAKE_RELEASE='false'

if [[ "${TRAVIS_COMMIT_MESSAGE}" =~ ^make\ release ]]; then 
    echo "commit message indicate that a release must be create"
    MAKE_RELEASE='true'
fi

if [ "$MAKE_RELEASE" = 'true' ]; then
    echo "create release from actual SNAPSHOT"
    if ! mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion} versions:commit; then
        err "set release version failed"
        exit 1
    fi
else
    echo "keep snapshot version in pom.xml"
fi

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ]; then
    echo "deploy version to maven centrale"
    if ! mvn deploy --settings .travis/settings.xml -DperformRelease=true -DskipTests=true -B -U; then
        err "maven deploy failed"
        exit 1
    fi
fi

if [ "$MAKE_RELEASE" = 'true' ]; then
    git config user.name "Travis CI"
    git config user.email "travis-ci@ifocusit.ch"
    PROJECT_VERSION=`mvn -q exec:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive`
    if [[ $? != 0 || ! $PROJECT_VERSION ]]; then
        err "failed to parse project version"
        return 1
    fi
    GIT_TAG=v$PROJECT_VERSION
    echo "create git tag $GIT_TAG"
    if ! git tag "$GIT_TAG" -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"; then
        err "failed to create git tag"
        exit 1
    fi

    echo "set next development version"
    if ! mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}-SNAPSHOT versions:commit; then
        err "set next dev version failed"
        exit 1
    fi
    
    # push new version
    if ! git push --quiet --tags "https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG" > /dev/null 2>&1; then
        err "failed to push git tags"
        exit 1
    fi
fi
