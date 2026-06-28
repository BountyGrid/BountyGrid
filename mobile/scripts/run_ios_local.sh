#!/usr/bin/env bash
set -euo pipefail

flutter run --dart-define-from-file=config/local_ios.json
