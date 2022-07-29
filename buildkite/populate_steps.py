#!/usr/bin/env python3

import os
import subprocess

DEFAULT_BRANCH = "main"

CLIENT_GPG_KEY_SECRET_NAME = "tecton-staging/JAVA_CLIENT_GPG_KEY"


def call(*args):
    subprocess.check_call(args)


def output(*args):
    return subprocess.check_output(args).decode("utf-8")


def get_commits_since_last(commit: str):
    commit_count = output("git", "rev-list", "--count", f"{commit}..HEAD").strip()
    return int(commit_count)


def main() -> None:
    ossrh_password = os.environ['OSSRH_TOKEN']
    signing_password = os.environ['CLIENT_GPG_PASSPHRASE']
    signing_key = os.environ['CLIENT_GPG_KEY']
    with open('buildkite/buildkite.yaml', 'r') as file:
        steps = file.read().strip().replace('OSSRH_PASSWORD', ossrh_password).replace('SIGNING_PASSWORD',
                                                                                      signing_password).replace(
            'SIGNING_KEY', signing_key)
    print(steps)


if __name__ == "__main__":
    main()
