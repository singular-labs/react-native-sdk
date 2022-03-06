#!/usr/bin/env python3
import subprocess
import os
from os import path


def run_script(script, stdin=None):
    """Returns (stdout, stderr), raises error on non-zero return code"""
    # Note: by using a list here (['bash', ...]) you avoid quoting issues, as the
    # arguments are passed in exactly this order (spaces, quotes, and newlines won't
    # cause problems):
    proc = subprocess.Popen(['bash', '-c', script],
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                            stdin=subprocess.PIPE)
    stdout, stderr = proc.communicate()
    if proc.returncode:
        raise ScriptException(proc.returncode, stdout, stderr, script)
    return stdout, stderr


class ScriptException(Exception):
    def __init__(self, returncode, stdout, stderr, script):
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr
        Exception.__init__('Error in script')

def download_sdk_tools():
    repo_path = 'sdk-tools/'
    if path.exists(repo_path):
        os.chdir(repo_path)
        os.system("git reset --hard")
        os.system("git clean -fxd")
        os.system("git checkout master")
        os.system("git pull")
        os.chdir("../")
    else:
        os.system("git clone git@github.com:singular-labs/{0}.git".format(repo_path.replace('/', '')))

    os.system("chmod +xwr ./sdk-tools/update_zendesk_articles.py")


def update_docs():
    print("Updating documentation")
    platform = "react-native"
    output, err = run_script("git describe --abbrev=0 --tags `git rev-list --tags --skip=1  --max-count=1`")
    old_version = output.decode('utf-8').strip()
    output, err = run_script("git describe --tags --abbrev=0")
    new_version = output.decode('utf-8').strip()
    os.chdir('sdk-tools/')
    command = "./update_zendesk_articles.py --platform={0} --old-version={1} --new-version={2}".format(platform, old_version, new_version)
    os.system(command)
    os.chdir('..')


def release_react_native_sdk():
    os.system("npm publish")

    download_sdk_tools()
    update_docs()
    output, err = run_script("git describe --tags --abbrev=0")
    new_version = output.decode('utf-8').strip()
    with open(new_version, 'w') as fp:
        pass

    print('Syncing latest version to s3')
    s3_location = 's3://maven.singular.net/react-native/{0}'.format(new_version)
    run_script('s3cmd put {0} {1}'.format(new_version, s3_location))

    print('Done!')


if __name__ == '__main__':
    release_react_native_sdk()
