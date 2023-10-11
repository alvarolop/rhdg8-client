#!/bin/sh

set -e

# Set your environment variables here
RHDG_NAMESPACE=rhdg8
RHDG_CLUSTER=rhdg
RHDG_APP_NAME=rhdg-client
RHDG_AUTH_ENABLED=true
RHDG_SSL_ENABLED=true
GRAFANA_NAMESPACE=grafana
GRAFANA_DASHBOARD_NAME="grafana-dashboard-rhdg8-client"

#############################
## Do not modify anything from this line
#############################

# Print environment variables
echo -e "\n=============="
echo -e "ENVIRONMENT VARIABLES:"
echo -e " * RHDG_NAMESPACE: $RHDG_NAMESPACE"
echo -e " * RHDG_CLUSTER: $RHDG_CLUSTER"
echo -e " * RHDG_APP_NAME: $RHDG_APP_NAME"
echo -e " * RHDG_AUTH_ENABLED: $RHDG_AUTH_ENABLED"
echo -e " * RHDG_SSL_ENABLED: $RHDG_SSL_ENABLED"
echo -e "==============\n"

if ! $RHDG_AUTH_ENABLED; then
    OCP_APP_TEMPLATE="01-rhdg-client-basic"
    HTTP_SCHEME="http"
    RHDG_SECURITY=""
else 
    if ! $RHDG_SSL_ENABLED; then
        OCP_APP_TEMPLATE="01-rhdg-client-basic"
        HTTP_SCHEME="http"
        RHDG_SECURITY="--digest -u admin:password"
    else
        OCP_APP_TEMPLATE="01-rhdg-client-ssl"
        HTTP_SCHEME="https"
        RHDG_SECURITY="--digest -u admin:password"
    fi
fi

# Check if the user is logged in 
if ! oc whoami &> /dev/null; then
    echo -e "Check. You are not logged in. Please log in and run the script again."
    exit 1
else
    echo -e "Check. You are correctly logged in. Continue..."
    oc project $RHDG_NAMESPACE
fi

# Register Proto Schema
echo -e "\n[1/5]Register Book Proto Schema"
RHDG_SERVER_ROUTE=$(oc get routes $RHDG_CLUSTER-external -n $RHDG_NAMESPACE --template="$HTTP_SCHEME://{{ .spec.host }}")

# echo "curl -X POST -k -v $RHDG_SECURITY $RHDG_SERVER_ROUTE/rest/v2/schemas/book.proto -d @protos/book.proto"
curl -X POST -k $RHDG_SECURITY $RHDG_SERVER_ROUTE/rest/v2/schemas/book.proto --data-binary @protos/book.proto

# Create RHDG Client configmap
echo -e "\n[2/5]Creating client ConfigMap"
if oc get configmap $RHDG_APP_NAME-config -n $RHDG_NAMESPACE &> /dev/null; then
    echo -e "Check. There was a previous configuration. Deleting..."
    oc delete configmap ${RHDG_APP_NAME}-config -n $RHDG_NAMESPACE
fi

oc create configmap $RHDG_APP_NAME-config \
    --from-file=application.properties=src/main/resources/application-k8s.properties \
    --from-file=logback-spring.xml=src/main/resources/logback-spring-k8s.xml -n $RHDG_NAMESPACE

# Deploy the RHDG client
echo -e "\n[3/5]Deploying the RHDG client"
oc process -f openshift/$OCP_APP_TEMPLATE.yaml \
    -p APP_NAMESPACE=$RHDG_NAMESPACE \
    -p APP_NAME=$RHDG_APP_NAME \
    -p RHDG_CLUSTER=$RHDG_CLUSTER \
    -p DATAGRID_AUTH_ENABLED=$RHDG_AUTH_ENABLED \
    -p DATAGRID_SSL_ENABLED=$RHDG_SSL_ENABLED | oc apply -f -

# Wait for DeploymentConfig
echo -n "Waiting for pods ready..."
while [[ $(oc get pods -l app=$RHDG_APP_NAME -n $RHDG_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done; echo -n -e "  [OK]\n"


# Configure Prometheus to monitor RHDG Client
echo -e "\n[4/5]Configure Prometheus to monitor RHDG Client"
oc process -f openshift/02-rhdg-service-monitor.yaml \
    -p APP_NAME=$RHDG_APP_NAME \
    -p APP_NAMESPACE=$RHDG_NAMESPACE | oc apply -f -


# Create a Grafana dashboard
echo -e "\n[5/5]Create Grafana Dashboard"
oc process -f https://raw.githubusercontent.com/alvarolop/quarkus-observability-app/main/openshift/grafana/40-dashboard.yaml \
  -p DASHBOARD_GZIP="$(cat openshift/grafana-dashboard-rhdg8-client.json | gzip | base64 -w0)" \
  -p DASHBOARD_NAME=${GRAFANA_DASHBOARD_NAME} | oc apply -f -


APP_URL=$(oc get routes $RHDG_APP_NAME -n $RHDG_NAMESPACE --template="$HTTP_SCHEME://{{ .spec.host }}")

echo -e "\nRHDG Client information:"
echo -e " * URL: $APP_URL"
