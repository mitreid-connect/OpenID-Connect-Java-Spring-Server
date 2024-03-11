#!/bin/bash

if [[ -z "${CIRCLE_TOKEN}" ]]; then
  echo Cannot trigger release workflow. CircleCI user token not found.
  exit 1
fi

BRANCH=1.3.x

echo -e "\nTriggering release workflow on branch: ${BRANCH}.\n" 

status_code=$(curl --request POST \
  --url https://circleci.com/api/v2/project/github/gresham-computing/openid-connect-server/pipeline \
  --header 'Circle-Token: '${CIRCLE_TOKEN}'' \
  --header 'content-type: application/json' \
  --data '{"branch":"'${BRANCH}'","parameters":{"release": true}}' \
  -o response.json \
  -w "%{http_code}")
  
  if [ "${status_code}" -ge "200" ] && [ "${status_code}" -lt "300" ]; then
      echo -e "\nAPI call succeeded [${status_code}]. Response:\n"
      cat response.json
      rm response.json
  else
      echo -e "\nAPI call failed [${status_code}]. Response:\n"
      cat response.json
      rm response.json
      exit 1
  fi
