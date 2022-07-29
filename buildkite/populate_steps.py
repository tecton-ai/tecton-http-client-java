#!/usr/bin/env python3

import os
import subprocess
import argparse

DEFAULT_BRANCH = "main"


def call(*args):
    subprocess.check_call(args)


def output(*args):
    return subprocess.check_output(args).decode("utf-8")


def get_commits_since_last(commit: str):
    commit_count = output("git", "rev-list", "--count", f"{commit}..HEAD").strip()
    return int(commit_count)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("pipeline_url")
    parser.add_argument("ossrh_password")
    parser.add_argument("signing_password")
    parser.add_argument("signing_key")
    args = parser.parse_args()
    with open('buildkite.yaml', 'r') as file:
        steps = file.read().strip().replace('OSSRH_PASSWORD', args.ossrh_password).replace('SIGNING_PASSWORD',
                                                                                           args.signing_password).replace(
            'SIGNING_KEY', args.signing_key.strip())
    print(steps)


if __name__ == "__main__":
    main()
