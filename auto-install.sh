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

# Create RHDG Client configmap
echo -e "\n[1/2]Creating client ConfigMap"
oc create configmap ${RHDG_CLIENT_NAME}-config \
--from-file=application.properties=src/main/resources/application-k8s.properties \
--from-file=logback-spring.xml=src/main/resources/logback-spring-k8s.xml -n $RHDG_NAMESPACE

# Deploy the RHDG client
echo -e "\n[2/2]Deploying the RHDG client"
oc process -f templates/rhdg-client.yaml -p APP_NAMESPACE=$RHDG_NAMESPACE -p APPLICATION_NAME=$RHDG_CLIENT_NAME -p GIT_REPOSITORY=$RHDG_GIT_REPO -p RHDG_CLUSTER_NAME=$RHDG_CLUSTER_NAME | oc apply -f -

# Wait for DeploymentConfig
echo -n "Waiting for pods ready..."
while [[ $(oc get pods -l app=$RHDG_CLIENT_NAME -n $RHDG_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done; echo -n -e "  [OK]\n"


RHDG_CLIENT_ROUTE=$(oc get routes $RHDG_CLIENT_NAME -n $RHDG_NAMESPACE --template='https://{{.spec.host}}')

echo -e "\RHDG Client information:"
echo -e " * URL: $RHDG_CLIENT_ROUTE"

