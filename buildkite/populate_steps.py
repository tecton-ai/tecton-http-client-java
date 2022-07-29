#!/usr/bin/env python3

import os
import subprocess

import requests

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
    ossrh_password = os.environ['OSSRH_TOKEN']
    signing_password = os.environ['CLIENT_GPG_PASSPHRASE']
    signing_key = os.environ['CLIENT_GPG_KEY']
    pipeline_url = os.environ['PIPELINE_URL']
    should_release, reason = should_publish_snapshot(pipeline_url)
    if should_release:
        with open('buildkite/buildkite.yaml', 'r') as file:
            steps = file.read().strip().replace('OSSRH_PASSWORD', ossrh_password).replace('SIGNING_PASSWORD',
                                                                                          signing_password).replace(
                'SIGNING_KEY', signing_key)
        annotation = f"Publishing Client Snapshot. Reason: {reason}"
    else:
        annotation = f"Not Publishing Snapshot. Reason: {reason}"
    print(steps)
    call("buildkite-agent", "annotate", "--context", "ctx-decision", "--style", "info", annotation)


if __name__ == "__main__":
    main()
