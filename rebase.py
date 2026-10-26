#!/usr/bin/env python3
"""Change the site's base path (= GitHub repository name).

usage: python3 rebase.py <new-repo-name>
Run it once inside this folder before uploading. It rewrites every '/skyroute/' reference.
"""
import os, sys

OLD = '/skyroute'
if len(sys.argv) != 2:
    sys.exit('usage: python3 rebase.py <new-repo-name>')
new = '/' + sys.argv[1].strip('/')
if new == OLD:
    sys.exit('already ' + OLD)
here = os.path.dirname(os.path.abspath(__file__))
count = 0
for root, _, files in os.walk(here):
    for f in files:
        if not f.endswith(('.html', '.js', '.css', '.md', '.py')):
            continue
        p = os.path.join(root, f)
        s = open(p, encoding='utf-8', errors='surrogateescape').read()
        t = s.replace(OLD + '/', new + '/').replace("'" + OLD + "'", "'" + new + "'").replace('"' + OLD + '"', '"' + new + '"')
        if t != s:
            open(p, 'w', encoding='utf-8', errors='surrogateescape').write(t)
            count += 1
print(f'{OLD} -> {new}: {count} files updated')
