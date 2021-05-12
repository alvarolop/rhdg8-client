#!/bin/sh

set -e

# Set your environment variables here
RHDG_NAMESPACE=rhdg8
RHDG_CLUSTER_NAME=rhdg
RHDG_CLIENT_NAME=rhdg-client
RHDG_GIT_REPO=https://github.com/alvarolop/rhdg8-client.git

#############################
## Do not modify anything from this line
#############################

# Print environment variables
echo -e "\n=============="
echo -e "ENVIRONMENT VARIABLES:"
echo -e " * RHDG_NAMESPACE: $RHDG_NAMESPACE"
echo -e " * RHDG_CLUSTER_NAME: $RHDG_CLUSTER_NAME"
echo -e " * RHDG_CLIENT_NAME: $RHDG_CLIENT_NAME"
echo -e " * RHDG_GIT_REPO: $RHDG_GIT_REPO"
echo -e "==============\n"

# Check if the user is logged in 
if ! oc whoami &> /dev/null; then
    echo -e "Check. You are not logged out. Please log in and run the script again."
    exit 1
else
    echo -e "Check. You are correctly logged in. Continue..."
    oc project default # To avoid issues with deleted projects
fi

# Register Proto Schema
echo -e "\n[1/3]Register Book Proto Schema"
RHDG_SERVER_ROUTE=$(oc get routes ${RHDG_CLUSTER_NAME}-external -n $RHDG_NAMESPACE --template='http://{{.spec.host}}')
curl -X POST -u developer:developer $RHDG_SERVER_ROUTE/rest/v2/schemas/book.proto -d '// Generated from : com.alopezme.hotrodtester.configuration.BookSchema

syntax = "proto2";

package com.alopezme.hotrodtester.model;

/**
 * @Indexed
 */
message Book {
   
   /**
    * @Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)
    */
   optional int32 id = 1 [default = 0];
   
   /**
    * @Field(index=Index.YES, store = Store.YES, analyze = Analyze.NO)
    */
   optional string title = 2;
   
   /**
    * @Field(index=Index.YES, store = Store.YES, analyze = Analyze.YES)
    */
   optional string author = 3;
   
   /**
    * @Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)
    */
   optional int32 publicationYear = 4 [default = 0];
}'


# Create RHDG Client configmap
echo -e "\n[2/3]Creating client ConfigMap"
oc create configmap ${RHDG_CLIENT_NAME}-config \
    --from-file=application.properties=src/main/resources/application-k8s.properties \
    --from-file=logback-spring.xml=src/main/resources/logback-spring-k8s.xml -n $RHDG_NAMESPACE

# Deploy the RHDG client
echo -e "\n[3/3]Deploying the RHDG client"
oc process -f templates/rhdg-client.yaml \
    -p APP_NAMESPACE=$RHDG_NAMESPACE \
    -p APPLICATION_NAME=$RHDG_CLIENT_NAME \
    -p GIT_REPOSITORY=$RHDG_GIT_REPO \
    -p RHDG_CLUSTER_NAME=$RHDG_CLUSTER_NAME | oc apply -f -

# Wait for DeploymentConfig
echo -n "Waiting for pods ready..."
while [[ $(oc get pods -l app=$RHDG_CLIENT_NAME -n $RHDG_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done; echo -n -e "  [OK]\n"


RHDG_CLIENT_ROUTE=$(oc get routes $RHDG_CLIENT_NAME -n $RHDG_NAMESPACE --template='http://{{.spec.host}}')

echo -e "\RHDG Client information:"
echo -e " * URL: $RHDG_CLIENT_ROUTE"
