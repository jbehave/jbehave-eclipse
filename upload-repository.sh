#!/bin/bash

VERSION=$1

if [ "$VERSION" == "" ] ; then
  echo "usage: upload-repository.sh <version>"
  exit;
fi

ARTIFACT="jbehave-repository"
QUALIFIER="updates"
REFERENCE="/var/www/jbehave.org/reference"
GROUP_ID="org.jbehave.eclipse"
ARTIFACT_ID="org.jbehave.eclipse.repository"
CLASSIFIER=""
RELATIVE_PATH="eclipse"

ARTIFACT_FULL="$GROUP_ID:$ARTIFACT_ID:$VERSION:zip:$CLASSIFIER"
VERSIONED_ARTIFACT="uploads/$ARTIFACT-$VERSION"
ZIPPED_ARTIFACT="$ARTIFACT.zip"
if [ "$RELATIVE_PATH" != "" ] ; then
  REFERENCE="$REFERENCE/$RELATIVE_PATH"
fi
VERSIONED_REFERENCE="$REFERENCE/$VERSION"

mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:get -Dartifact=$ARTIFACT_FULL -Dtransitive=false -Ddest=target/$ZIPPED_ARTIFACT

scp target/$ZIPPED_ARTIFACT jbehave.org:uploads/
ssh jbehave.org "rm -rf $VERSIONED_ARTIFACT; unzip -q -d $VERSIONED_ARTIFACT uploads/$ZIPPED_ARTIFACT; rm -r $VERSIONED_REFERENCE; mv $VERSIONED_ARTIFACT $VERSIONED_REFERENCE; cd $REFERENCE; rm $QUALIFIER; ln -s $VERSION $QUALIFIER"
