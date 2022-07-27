#!/bin/bash

PIPELINE=$(cat << EOF
steps:
  - command: "./gradlew clean build"
    label: "build that"
  - wait
  - command: "echo Deploy!"
    label: ":rocket:"
EOF
)

echo "$PIPELINE"
