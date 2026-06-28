#!/usr/bin/env bash
set -euo pipefail

flutter build appbundle --release --dart-define-from-file=config/production.json
