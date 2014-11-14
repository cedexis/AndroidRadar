#!/usr/bin/env python3

"""
A tool used to build android-radar.jar

This jar will include all extracted dependencies
"""

import os
import subprocess
import tempfile
import shutil

java_radar_tag = 'v0.0.7'

def get_build_dir(root_dir):
    return os.path.join(root_dir, 'build')

def prep_directory(root_dir):
    build_dir = get_build_dir(root_dir)
    if os.path.isdir(build_dir):
        shutil.rmtree(build_dir)
    os.makedirs(build_dir)

def execute_command(name, args):
    print(name + '...')
    result = subprocess.check_output(args)
    if 0 < len(result):
        print(result.decode())
    else:
        print('Command complete')

def extract_dep_jar(jar_path):
    cmd_args = [
        'jar',
        'xf',
        jar_path,
    ]
    execute_command('Extracting {}'.format(jar_path), cmd_args)
    shutil.rmtree('META-INF')

def build_jar():
    root_dir = os.path.dirname(os.path.abspath(__file__))
    classes_dir = os.path.join(root_dir, 'bin/classes/com')
    prep_directory(root_dir)
    output_file_path = os.path.join(get_build_dir(root_dir), 'android-radar.jar')
    temp_dir = tempfile.mkdtemp()
    temp_dir_bin = os.path.join(temp_dir, 'bin')
    os.makedirs(temp_dir_bin)

    # https://github.com/cedexis/java-radar.git

    try:
        os.chdir(temp_dir)
        print('Working directory: {}'.format(os.getcwd()))

        cmd_args = [
            'curl',
            'https://github.com/cedexis/java-radar/releases/download/{}/java-radar.jar'.format(java_radar_tag),
            '-O',
            '-L',
        ]
        execute_command('Downloading java-radar.jar', cmd_args)

        jar_file_path = os.path.join(temp_dir, 'java-radar.jar')
        os.chdir(temp_dir_bin)
        extract_dep_jar(jar_file_path)
        os.chdir(root_dir)

        # Merge the classes from the Android project into those of the dependencies
        cmd_args = [
            'cp',
            '-r',
            classes_dir,
            temp_dir_bin
        ]
        execute_command(
            "Merging classes from android-radar with extracted dependencies",
            cmd_args)

        cmd_args = [
            'jar',
            'cf',
            output_file_path,
            '-C',
            temp_dir_bin,
            '.',
        ]
        execute_command('Creating android-radar.jar', cmd_args)

    finally:
        shutil.rmtree(temp_dir)

if __name__ == '__main__':
    build_jar()
