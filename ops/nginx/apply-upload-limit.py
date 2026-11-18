#!/usr/bin/env python3
"""Adjust only BackflowPath nginx blocks, with validation and rollback."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

HOSTS = {"backflowpath.com", "www.backflowpath.com"}
TOKEN = re.compile(r'''"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\#[^\n]*|[{};]|[^\s{};\#]+''')


def patch_config(text):
    tokens = [match for match in TOKEN.finditer(text) if not match.group().startswith("#")]
    edits, matches = [], 0
    index = 0
    while index + 1 < len(tokens):
        if tokens[index].group() != "server" or tokens[index + 1].group() != "{":
            index += 1
            continue
        opening = index + 1
        depth, end = 1, opening + 1
        while end < len(tokens) and depth:
            if tokens[end].group() == "{":
                depth += 1
            elif tokens[end].group() == "}":
                depth -= 1
            end += 1
        if depth:
            raise ValueError("Unbalanced nginx server block")
        block = tokens[opening + 1:end - 1]
        names, limits = set(), []
        depth, server_limit = 1, False
        for position, token in enumerate(block):
            value = token.group()
            if value == "{":
                depth += 1
            elif value == "}":
                depth -= 1
            elif value == "server_name" and depth == 1:
                for name in block[position + 1:]:
                    if name.group() == ";":
                        break
                    names.add(name.group().strip("\"'"))
            elif value == "client_max_body_size":
                if position + 2 >= len(block) or block[position + 2].group() != ";":
                    raise ValueError("Unexpected client_max_body_size syntax")
                limits.append(block[position + 1])
                if depth == 1:
                    server_limit = True
        if names & HOSTS:
            matches += 1
            for value in limits:
                if value.group().lower() != "16m":
                    edits.append((value.start(), value.end(), "16m"))
            if not server_limit:
                edits.append((tokens[opening].end(), tokens[opening].end(), "\n    client_max_body_size 16m;"))
        index = end
    for start, end, replacement in sorted(edits, reverse=True):
        text = text[:start] + replacement + text[end:]
    return text, matches


def run(*args):
    return subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, check=True)


def write_atomic(path, data):
    descriptor, temporary = tempfile.mkstemp(prefix=".backflow-nginx-", dir=str(path.parent))
    try:
        with os.fdopen(descriptor, "wb") as stream:
            stream.write(data)
        shutil.copystat(str(path), temporary)
        if hasattr(os, "chown"):
            original = path.stat()
            os.chown(temporary, original.st_uid, original.st_gid)
        os.replace(temporary, str(path))
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


def restore(backup):
    manifest = backup / "manifest.json"
    if not manifest.exists():
        return
    for entry in json.loads(manifest.read_text()):
        write_atomic(Path(entry["path"]), (backup / entry["backup"]).read_bytes())
    run("nginx", "-t")
    run("systemctl", "reload", "nginx")
    print("Restored BackflowPath nginx upload settings.")


def apply(backup):
    dump = run("nginx", "-T").stdout
    files = sorted(set(Path(path).resolve() for path in re.findall(r"^# configuration file (.+):$", dump, re.M)))
    changes, total = [], 0
    for path in files:
        original = path.read_bytes()
        updated, matches = patch_config(original.decode("utf-8"))
        total += matches
        if updated.encode("utf-8") != original:
            changes.append((path, original, updated.encode("utf-8")))
    if not total:
        raise RuntimeError("No explicit BackflowPath nginx server_name found; no configuration was changed.")
    backup.mkdir(parents=True, exist_ok=True)
    manifest = []
    for path, original, _ in changes:
        name = hashlib.sha256(str(path).encode()).hexdigest() + ".conf"
        (backup / name).write_bytes(original)
        manifest.append({"path": str(path), "backup": name})
    (backup / "manifest.json").write_text(json.dumps(manifest))
    try:
        for path, _, updated in changes:
            write_atomic(path, updated)
        run("nginx", "-t")
        if changes:
            run("systemctl", "reload", "nginx")
    except Exception:
        restore(backup)
        raise
    print("BackflowPath nginx upload limit verified: 16m across {} server block(s); {} file(s) changed.".format(total, len(changes)))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--backup-dir", required=True)
    parser.add_argument("--restore", action="store_true")
    args = parser.parse_args()
    if args.restore:
        restore(Path(args.backup_dir))
    else:
        apply(Path(args.backup_dir))
