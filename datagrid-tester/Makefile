# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help


APP_PATH=~/github/rhdg8-client/datagrid-tester


install: ## Install app artifacts

	mvn clean install -Dmaven.test.skip

effective-pom:
	mvn clean help:effective-pom > effective-pom.txt

dependency-tree:
	mvn dependency:tree > dependency-tree.txt



deploy-cluster:
	docker-compose -f ./datagrid-cluster-compose.yml up -d

undeploy-cluster:
	docker-compose -f ./datagrid-cluster-compose.yml down

	
create-caches:

	# Requirements:
	# - Run Datagrid container
	# - Datagrid CLI on PATH
	cli.sh -c localhost:11222 -f ${APP_PATH}/local_cache/create_caches

boot:
	mvn clean spring-boot:run -Dmaven.test.skip
	
test:
	make install
	mvn clean test

run:
	clear
	make install boot
