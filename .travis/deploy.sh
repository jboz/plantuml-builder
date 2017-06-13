#!/bin/bash

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "master" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

MAKE_RELEASE='false'

echo "commit message: $TRAVIS_COMMIT_MESSAGE"

if [[ "${TRAVIS_COMMIT_MESSAGE}" =~ ^make\ release ]]; then 
    echo "commit message indicate that a release must be create"
    MAKE_RELEASE='true'
fi

echo "will generate release ? $MAKE_RELEASE"

if [ "$MAKE_RELEASE" = 'true' ]; then
    echo "create release from actual SNAPSHOT"
    mvn --settings .travis/settings.xml build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion} versions:commit
else
    echo "keep snapshot version in pom.xml"
fi

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ]; then
    echo "deploy version to maven centrale"
    mvn deploy --settings .travis/settings.xml -DperformRelease=true -DskipTests=true -B -U
    exit $?
fi

if [ "$MAKE_RELEASE" = 'true' ]; then
    git config user.name "Travis CI"
    git config user.email "travis-ci@ifocusit.ch"
    #c8a1526c7ec595d2fca457de79893f9b8d631276
    PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version | grep -v "^\[")
    GIT_TAG=v$PROJECT_VERSION
    echo "create git tag $GIT_TAG"
    git tag "$GIT_TAG" -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"   

    echo "set next development version"
    mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}-SNAPSHOT versions:commit
    
    # push new version
    git push --quiet --tags "https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG" > /dev/null 2>&1
fi
