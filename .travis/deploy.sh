
TO_RELEASE=false

if [ $TRAVIS_COMMIT_MESSAGE =~ ^make release .*$ ] then
    echo "commit message indicate that de release must be create"
    TO_RELEASE=true
fi

if [ "$TO_RELEASE" == 'true' ]; then
    echo "create release from actual SNAPSHOT"
    #mvn --settings .travis/settings.xml build-helper:parse-version versions:set -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion} help:evaluate -DPROJECT_VERSION=project.version
else
    echo "keep snapshot version in pom.xml"
fi

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    echo "deploy version"
    #mvn deploy --settings .travis/settings.xml -DperformRelease=true -DskipTests=true -B -U
    exit $?
fi

if [ "$TO_RELEASE" == 'true' ]; then
    # Save some useful information
    REPO='git config remote.origin.url'
    SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
    COMMIT_AUTHOR_EMAIL='julienboz@gmail.com'

    git config user.name "Travis CI"
    git config user.email "$COMMIT_AUTHOR_EMAIL"
    #c8a1526c7ec595d2fca457de79893f9b8d631276
    export GIT_TAG=v$PROJECT_VERSION
    echo "git tag $GIT_TAG -a -m 'Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER'"
    echo "git push -q $SSH_REPO --tags"

    echo "after release, set next development version"
    mvn build-helper:parse-version versions:set -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}-SNAPSHOT help:evaluate -DPROJECT_VERSION=project.version
    echo "git add -A ."
    echo "git commit -m 'next dev version: $PROJECT_VERSION'"
    echo "git push $SSH_REPO master"
fi