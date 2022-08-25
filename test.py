import subprocess
from typing import Optional
import json
from datetime import datetime, timedelta
import pytz

response = {
    "ARN": "arn:aws:secretsmanager:us-west-2:472542229217:secret:tecton-staging/JAVA_CLIENT_GPG_KEY-nnnJ3A",
    "Name": "tecton-staging/JAVA_CLIENT_GPG_KEY",
    "VersionId": "571d38fd-909c-4272-b6a0-012651ffb017",
    "SecretString": "{\"tecton-staging/JAVA_CLIENT_GPG_KEY\":\"-----BEGIN PGP PRIVATE KEY BLOCK-----  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 =+hdQ -----END PGP PRIVATE KEY BLOCK-----\"}",
    "VersionStages": [
        "AWSCURRENT"
    ],
    "CreatedDate": "2022-07-25T17:17:45.312000-07:00"
}


def call(*args, gitdir: Optional[str] = None):
    if gitdir is None:
        subprocess.check_call(("git",) + args)
    else:
        subprocess.check_call(("git", "-C", gitdir) + args)


def output(*args, gitdir: Optional[str] = None):
    if gitdir is None:
        return subprocess.check_output(("git",) + args).decode("utf-8")
    else:
        return subprocess.check_output(("git", "-C", gitdir) + args).decode("utf-8")


# target_branch = "release"
# source_branch = "main"
# checkout_command = "git checkout "+source_branch
# call("checkout", "main")
# commit="6547590058958575fh55"
# commit_count = output("rev-list", "--count", f"{commit}..HEAD").strip()
# print(commit_count)
# if int(commit_count) > 0:
#     print("Do release")
# else:
#     print("Skip")

timezone = pytz.timezone("America/Los_Angeles")
dt = datetime.now(timezone) - timedelta(days=3)

prefix = f"origin/release/{dt.strftime('%Y-%m-%d')}/*"
print(prefix)
subprocess.check_call("fetch", "origin")
op = output("branch", "-a", "--list", prefix)
branches = [line for line in op.split('\n') if line.strip() != '']
print(len(branches))
