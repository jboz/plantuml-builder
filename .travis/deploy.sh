#!/bin/bash

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "master" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

# reconnect master to origin
git checkout master

MAKE_RELEASE='false'

if [[ "${TRAVIS_COMMIT_MESSAGE}" =~ ^make\ release ]]; then 
    echo "commit message indicate that a release must be create"
    MAKE_RELEASE='true'
fi

if [ "$MAKE_RELEASE" = 'true' ]; then
    echo "create release from actual SNAPSHOT"
    if ! mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion} versions:commit; then
        echo "set release version failed"
        exit 1
    fi
else
    echo "keep snapshot version in pom.xml"
fi

echo "reading project version..."
PROJECT_VERSION=`mvn -q exec:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive`

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ]; then
    echo "deploying version to maven centrale..."
    if ! mvn deploy --settings .travis/settings.xml -DperformRelease=true -DskipTests=true -B -U gpg:sign; then
        echo "maven deploy failed"
        exit 1
    fi
fi

if [ "$MAKE_RELEASE" = 'true' ]; then
    git config user.name "Travis CI"
    git config user.email "travis-ci@ifocusit.ch"
    GIT_TAG=v$PROJECT_VERSION
    echo "create git tag $GIT_TAG"
    git tag "$GIT_TAG" -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"

    echo "preparing next version..."
    mvn -q build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}-SNAPSHOT versions:commit

    NEXT_VERSION=`mvn -q exec:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive`
    echo "next development version will be $NEXT_VERSION"

    # Commit the "changes", i.e. the new version.
    git add -A .
    git commit -m "set next development version to $NEXT_VERSION"
    
    REPO=`git config remote.origin.url`
    echo "pushing new development version..."
    git push --tags "https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG.git"

    ls target
fi
