# Auto-Commit Scripts

This document explains how to use the auto-commit scripts that monitor changes in your project and automatically create git commits.

## Overview

The project includes two auto-commit scripts:

1. `scripts/auto-commit.sh` - A polling-based script that checks for changes at intervals
2. `scripts/auto-commit-watch.sh` - A real-time script that uses inotify to detect file changes immediately

Both scripts automatically:
- Monitor configured directories for changes
- Generate meaningful commit messages based on the types of files changed
- Create commits with the detected changes
- Exclude temporary files and build artifacts

## Configuration

The scripts can be configured using the `.auto-commit-config` file in the project root. The default configuration monitors the `src`, `docs`, and `scripts` directories.

## Usage

### Basic Polling Script

```bash
# Run once to check for changes and commit
./scripts/auto-commit.sh once

# Run continuously, checking for changes every 5 minutes
./scripts/auto-commit.sh continuous

# Show help
./scripts/auto-commit.sh help
```

### Real-time Watch Script

```bash
# Start real-time monitoring (requires inotify-tools)
./scripts/auto-commit-watch.sh
```

### Prerequisites

For the watch script, you need to have `inotify-tools` installed:

- On Debian/Ubuntu: `sudo apt-get install inotify-tools`
- On RHEL/CentOS: `sudo yum install inotify-tools`
- On macOS: `brew install inotify-tools`

If `inotify-tools` is not available, the watch script will automatically fall back to polling mode.

## Integration with Git Hooks

You can integrate these scripts with git hooks to automate commits at different stages of development.

### Pre-push Hook Example

Create a `.git/hooks/pre-push` file:

```bash
#!/bin/bash
# Run auto-commit before pushing
./scripts/auto-commit.sh once
```

## How Commit Messages Are Generated

The scripts generate commit messages based on the types of files changed:

- Java files: "Java code"
- YAML files: "configuration"
- Markdown files: "documentation"
- XML files: "XML files"
- Shell scripts: "scripts"

Example commit messages:
- `[auto-commit] Update Java code (3 file(s))`
- `[auto-commit] Update configuration, documentation (2 file(s))`

## Excluding Files

The scripts automatically exclude common temporary and build files. You can customize the exclusion patterns in the `.auto-commit-config` file.

## Troubleshooting

### Script doesn't detect changes

1. Check if the directories you're changing are in the `MONITORED_DIRS` configuration
2. Verify your changes aren't matching any `EXCLUDE_PATTERNS`
3. Ensure the files have been saved to disk

### Git user not configured

The scripts will automatically set a default git user if one isn't configured. You can customize this in the `.auto-commit-config` file.

### Too frequent commits

Increase the `DEBOUNCE_TIME` in the configuration file to wait longer between commits, or use the polling script with a longer interval.

## Security Considerations

These scripts automatically commit changes to your repository. Be aware of:

- Sensitive information might be accidentally committed
- Commits are made with the configured git user identity
- The scripts will commit any changes in the monitored directories

Always review your changes and the auto-commit configuration to ensure they align with your project's security requirements.