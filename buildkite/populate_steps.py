#!/usr/bin/env python3

import os


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
