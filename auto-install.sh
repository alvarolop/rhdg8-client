#!/bin/sh

set -e

# Set your environment variables here
RHDG_NAMESPACE=my-rhdg8
RHDG_CLUSTER_NAME=rhdg
RHDG_CLIENT_NAME=rhdg-client
RHDG_GIT_REPO=https://github.com/alvarolop/rhdg8-client.git
RHDG_AUTH_ENABLED=false
RHDG_SSL_ENABLED=false

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
echo -e " * RHDG_AUTH_ENABLED: $RHDG_AUTH_ENABLED"
echo -e " * RHDG_SSL_ENABLED: $RHDG_SSL_ENABLED"
echo -e "==============\n"

if ! $RHDG_SSL_ENABLED; then
    OCP_CLUSTER_TEMPLATE="rhdg-client"
    SERVICE_MONITOR_HTTP_SCHEME="http"
else
    OCP_CLUSTER_TEMPLATE="rhdg-client-ssl"
    SERVICE_MONITOR_HTTP_SCHEME="https"
    RHDG_SECURITY="-u developer:developer"
fi

# Check if the user is logged in 
if ! oc whoami &> /dev/null; then
    echo -e "Check. You are not logged in. Please log in and run the script again."
    exit 1
else
    echo -e "Check. You are correctly logged in. Continue..."
    oc project rhdg8
fi

# Register Proto Schema
echo -e "\n[1/3]Register Book Proto Schema"
RHDG_SERVER_ROUTE=$(oc get routes ${RHDG_CLUSTER_NAME}-external -n $RHDG_NAMESPACE --template="${SERVICE_MONITOR_HTTP_SCHEME}://{{.spec.host}}")
curl -X POST -k -v $RHDG_SECURITY $RHDG_SERVER_ROUTE/rest/v2/schemas/book.proto -d '// Generated from : com.alopezme.hotrodtester.configuration.BookSchema

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
if oc get cm ${RHDG_CLIENT_NAME}-config -n $RHDG_NAMESPACE &> /dev/null; then
    echo -e "Check. There was a previous configuration. Deleting..."
    oc delete configmap ${RHDG_CLIENT_NAME}-config -n $RHDG_NAMESPACE
fi

oc create configmap ${RHDG_CLIENT_NAME}-config \
    --from-file=application.properties=src/main/resources/application-k8s.properties \
    --from-file=logback-spring.xml=src/main/resources/logback-spring-k8s.xml -n $RHDG_NAMESPACE

# Deploy the RHDG client
echo -e "\n[3/3]Deploying the RHDG client"
oc process -f templates/${OCP_CLUSTER_TEMPLATE}.yaml \
    -p APP_NAMESPACE=$RHDG_NAMESPACE \
    -p APPLICATION_NAME=$RHDG_CLIENT_NAME \
    -p GIT_REPOSITORY=$RHDG_GIT_REPO \
    -p RHDG_CLUSTER_NAME=$RHDG_CLUSTER_NAME \
    -p DATAGRID_AUTH_ENABLED=$RHDG_AUTH_ENABLED \
    -p DATAGRID_SSL_ENABLED=$RHDG_SSL_ENABLED | oc apply -f -

# Wait for DeploymentConfig
echo -n "Waiting for pods ready..."
while [[ $(oc get pods -l app=$RHDG_CLIENT_NAME -n $RHDG_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done; echo -n -e "  [OK]\n"


RHDG_CLIENT_ROUTE=$(oc get routes $RHDG_CLIENT_NAME -n $RHDG_NAMESPACE --template="${SERVICE_MONITOR_HTTP_SCHEME}://{{.spec.host}}")

echo -e "\RHDG Client information:"
echo -e " * URL: $RHDG_CLIENT_ROUTE"
