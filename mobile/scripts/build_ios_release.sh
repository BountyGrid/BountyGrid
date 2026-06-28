#!/usr/bin/env bash
set -euo pipefail

flutter build ipa --release --dart-define-from-file=config/production.json
