#!/usr/bin/env python3

import os
import subprocess
import requests
import argparse
import yaml

DEFAULT_BRANCH = "main"


def call(*args):
    subprocess.check_call(args)


def output(*args):
    return subprocess.check_output(args).decode("utf-8")


def get_commits_since_last(commit: str):
    commit_count = output("git", "rev-list", "--count", f"{commit}..HEAD").strip()
    return int(commit_count)


def should_publish_snapshot(pipeline_url: str):
    token = os.environ["BUILDKITE_TOKEN"]
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(pipeline_url, headers=headers)
    response.raise_for_status()
    builds = response.json()
    if len(builds) == 0:
        return True, "Found no previous builds"
    most_recent = builds[0]
    state = most_recent["state"]
    last_commit = most_recent["commit"]
    if state != "passed":
        return state != "running", f"Most recent build has state `{state}`"
    commit_count = get_commits_since_last(last_commit)
    if commit_count > 0:
        return True, f"There are {commit_count} new commits since the last release"
    return False, f"There are no new commits since the last release"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("pipeline_url")
    parser.add_argument("ossrh_password")
    parser.add_argument("signing_password")
    parser.add_argument("signing_key")
    args = parser.parse_args()
    release, reason = should_publish_snapshot(args.pipeline_url)
    steps = []
    if release:
        steps.append(
            {
                "label": "Build Java Client",
                "command": f"./gradlew clean build",
            }
        )
        steps.append("wait")
        steps.append(
            {
                "label": ":rocket: Publish Snapshot Jar",
                "command": f"./gradlew publish -i -Possrh.password={args.ossrh_password} -PsigningPassword={args.signing_password} -PsigningKey={args.signing_key.strip()}",
            }
        )
        steps.append("wait")
        print(yaml.dump(steps))


if __name__ == "__main__":
    main()
