#!/bin/bash

# Script bash de déploiement des artefact dans maven centrale
# suivant différentes conditions ce sera une version realease ou snapshot

if [ "$TRAVIS_PULL_REQUEST" != "false" ] || [ "$TRAVIS_BRANCH" != "master" ]; then
    echo "no release on PR or non master branch"
    exit 0
fi

echo "running deployment script..."

fixReleaseVersion() {
    echo "set release version"
    if ! mvn -f pom.xml -q build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion} versions:commit; then
        echo "set release version failed"
        exit 1
    fi
}

setNextDevVersion() {
    echo "set next development version"
    if ! mvn -f pom.xml -q build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}-SNAPSHOT versions:commit; then
        echo "set next development version failed"
        exit 1
    fi
}

tag() {
    echo "reading project version..."
    local version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }'`
    local tag="release/v$version"
    echo "create git tag $tag"
    git tag "$tag" -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"
}

deploy() {
    echo "deploying version to maven centrale..."
    if ! mvn -f pom.xml deploy --settings $GPG_DIR/settings.xml -Prelease -DskipTests=true -B; then
        echo "maven deploy failed"
        exit 1
    fi
}

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

hasForceMessage() {
    if [[ "$TRAVIS_COMMIT_MESSAGE" =~ "force release" ]];then
        echo 1
    else
        echo 0
    fi
}

# create a release version
fixReleaseVersion
# deploy release
deploy

if [[ MAKE_RELEASE = 1 ]]; then
    # reconnect master to origin
    git checkout master

    git config user.name "Travis CI"
    git config user.email "travis-ci@ifocusit.ch"

    tag
    setNextDevVersion
    NEXT_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }'`

    echo "next development version will be $NEXT_VERSION"

    # Commit the "changes", i.e. the new version.
    git add -A .
    git commit -m "set next development version to $NEXT_VERSION"

    echo "pushing new development version..."
    git push "https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG.git" --follow-tags

    echo "release done, tag pushed, next development version set"
fi
