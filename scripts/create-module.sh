#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PYTHON_SCRIPT="$ROOT_DIR/.agents/skills/create-module/scripts/create_module.py"

if [ ! -f "$PYTHON_SCRIPT" ]; then
    echo "Error: Không tìm thấy script tại $PYTHON_SCRIPT"
    exit 1
fi

python3 "$PYTHON_SCRIPT" "$@"
