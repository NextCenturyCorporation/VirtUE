#!/usr/bin/env python3
#

from pathlib import Path
import io
import sys
import argparse
import re
import os

DEFAULT_COPYRIGHT_REGEX = r'.*Copyright \([Cc]\) [0-9]*.'
TEMPLATES_DIRNAME = 'templates'
REQUIRED_TEMPLATE_PROPERTIES = ['match', 'body']
COPYRIGHT_LINES = 50


progname = sys.argv[0]


def warn(message):
    global progname
    print('{}: warning: {}'.format(progname, message), file=sys.stderr)


def read_templates():
    global TEMPLATES_DIRNAME, progname
    templates_dir = Path(progname).parent.joinpath(TEMPLATES_DIRNAME)
    templates = {}
    template_files = templates_dir.glob('*.py')
    for template_file in template_files:
        template_dict = {}
        exec(io.open(template_file, mode='r').read(), None, template_dict)
        template_ok = True
        for prop in REQUIRED_TEMPLATE_PROPERTIES:
            if prop not in template_dict:
                warn('template "{}" does not set required property "{}". It will be ignored.'.format(template_file, prop))
                template_ok = False
        if template_ok:
            template_dict['_filename'] = template_file
            templates[template_file.stem] = template_dict
    if len(templates) == 0:
        warn('no templates found in template directory "{}"'.format(templates_dir))
    return templates


def dump_templates(templates, out):
    for template in sorted(templates.keys()):
        print(template, end='\t')
        print(templates[template])


def lookup_template(templates, filename):
    file_path = Path(filename)
    for template_name in sorted(templates.keys()):
        template = templates[template_name]
        if file_path.match(template.get('match')):
            return template
    return None


def emit_notice(out, template, notice_lines):
    head = template.get('head')
    if head is not None:
        out.write(head)
        out.write('\n')
    body = template.get('body')
    for line in notice_lines:
        if body is not None:
            line = body.format(notice=line)
        out.write(line)
    tail = template.get('tail')
    if tail is not None:
        out.write(tail)
        out.write('\n')


def process_file(templates, filename, notice_lines, replace, copyright_regex):
    template = lookup_template(templates, filename)
    print('Processing "{}" with template "{}"'.format(filename, template['_filename']))
    if template is not None:
        contents = io.open(filename, mode='r').readlines()
        new_filename = filename + '-new'
        new_file = io.open(new_filename, mode='w')
        line_number = 0
        head_skip = template.get('head_skip')
        if head_skip is not None:
            while re.match(head_skip, contents[line_number]):
                new_file.write(contents[line_number])
                line_number += 1
        # replace (by skipping) the current notice, if any
        if replace:
            copyright_line = -1
            for i in range(line_number, line_number+COPYRIGHT_LINES):
                if re.match(copyright_regex, contents[i]):
                    copyright_line = i
                    break
            # print("copyright line: {}:{}".format(filename, copyright_line))
            if copyright_line != -1:
                comment_start = -1
                for i in range(copyright_line, line_number-1, -1):
                    if re.match(template['start_regex'], contents[i]):
                        comment_start = i
                        break
                if comment_start == -1:
                    warn('cannot find comment start for copyright at {}:{}. The existing notice will not be removed'.
                         format(filename, copyright_line))
                else:
                    comment_end = -1
                    for i in range(copyright_line, len(contents)+1):
                        if re.match(template['end_regex'], contents[i]):
                            comment_end = i
                            break
                    if comment_end == -1:
                        warn('cannot find comment end for copyright at {}:{}. The existing notice will not be removed'.
                             format(filename, copyright_line))
                    else:
                        new_file.writelines(contents[line_number:comment_start])
                        line_number = comment_end+1
        emit_notice(new_file, template, notice_lines)
        new_file.writelines(contents[line_number:])
        new_file.close()
        os.replace(new_filename, filename)


def path_matches_patterns(path, patterns):
    for pattern in patterns:
        if path.match(pattern):
            return True
    return False


def process_files(templates, files, notice_lines, args):
    for f in files:
        path = Path(f)
        if args.exclude is None or not path_matches_patterns(path, args.exclude):
            if path.is_dir():
                process_files(templates, path.iterdir(), notice_lines, args)
            elif args.include is None or path_matches_patterns(path, args.include):
                process_file(templates, f, notice, args.replace, args.copyright_regex)


parser = argparse.ArgumentParser()
parser.add_argument('files', help='Files (or directories) to replace', nargs='*')
parser.add_argument('--notice', help='File containing copyright notice', type=argparse.FileType('r'))
parser.add_argument('--copyright-regex', help='Regular expression matching a copyright line',
                    default=DEFAULT_COPYRIGHT_REGEX)
parser.add_argument('--include', action='append', metavar='PATTERN', help='Include matching files (if not excluded)')
parser.add_argument('--exclude', action='append', metavar='PATTERN', help='Exclude matching files (even if included)')
parser.add_argument('--replace', action='store_true', help='Replace any existing copyright notice')
parser.add_argument('--dump', action='store_true', help='Dump the templates database')

templates = read_templates()

args = parser.parse_args()
if args.dump:
    dump_templates(templates, sys.stdout)
else:
    if len(args.files) == 0:
        parser.error('no file specified')
    notice = args.notice.readlines()
    process_files(templates, args.files, notice, args)
