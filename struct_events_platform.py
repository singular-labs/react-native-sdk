#!/usr/bin/env python3

import os
import json
import shutil
import argparse
import sys

import importlib.util

from singular_struct_events_common import StructEventsCommon
dir_path = os.path.dirname(os.path.realpath(__file__))
structEventsCommon = StructEventsCommon(dir_path + "/../")



def format_var_name(data_type, data):
    return structEventsCommon.camel_case(data)


def generate_code(data, data_type ):
    ret = 'export const ' + data_type.capitalize() + ' = {\n'
    count = 1
    for elem in data:
        ret = ret +'\t' + format_var_name(data_type, elem) + ":"+ "\"{}\"".format(elem)
        if not count == len(data):
            ret = ret + ',\n'
        else:
            ret = ret + '\n'
        count = count+1
    ret = ret +'}'
    file_name = data_type.capitalize()+'.js'
    structEventsCommon.write_file(ret, dir_path+ '/'+file_name)
    #print(ret)




if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-skip_repo", "--skip_repo", help = "skip cloning source json repo and asume it exists")
    parser.add_argument("-tmp_dir", "--tmp_dir", help = "tmp dir")
    args = parser.parse_args()
    if not args.tmp_dir:
        print('no tmp dir provided')
        sys.exit(-1)
    if not args.skip_repo:
        structEventsCommon.clone_repo(args.tmp_dir)
    events_data = structEventsCommon.read_data("events",args.tmp_dir)
    attributes_data = structEventsCommon.read_data("attributes",args.tmp_dir)
    generate_code(events_data, "events")
    generate_code(attributes_data, "attributes")
    structEventsCommon.update_zendesk_structured_events("6568295258395", events_data, attributes_data, format_var_name )
    if not args.skip_repo:
        structEventsCommon.delete_repo(args.tmp_dir)