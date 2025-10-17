#!/usr/bin/env bash
set -euo pipefail

# Configuration
MONITORED_DIRS=("src" "docs" "scripts")
EXCLUDE_PATTERNS=("*.tmp" "*.log" ".git/*" "target/*" "node_modules/*")
AUTO_COMMIT_PREFIX="[auto-commit]"
DEBOUNCE_TIME=5  # Seconds to wait before committing after a change

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a file should be excluded
should_exclude() {
    local file="$1"
    for pattern in "${EXCLUDE_PATTERNS[@]}"; do
        if [[ "$file" == $pattern ]]; then
            return 0
        fi
    done
    return 1
}

# Function to get changed files
get_changed_files() {
    local changed_files=()
    
    # Check if this is the first run (no previous commit)
    if ! git rev-parse HEAD >/dev/null 2>&1; then
        print_info "No commits found, adding all files"
        find "${MONITORED_DIRS[@]}" -type f 2>/dev/null | while read -r file; do
            if ! should_exclude "$file"; then
                echo "$file"
            fi
        done
        return
    fi
    
    # Get untracked files
    git ls-files --others --exclude-standard "${MONITORED_DIRS[@]}" | while read -r file; do
        if ! should_exclude "$file"; then
            echo "$file"
        fi
    done
    
    # Get modified files
    git diff --name-only HEAD "${MONITORED_DIRS[@]}" | while read -r file; do
        if ! should_exclude "$file"; then
            echo "$file"
        fi
    done
}

# Function to generate a commit message based on changes
generate_commit_message() {
    local changed_files="$1"
    local message=""
    
    # Categorize changes
    local java_files=$(echo "$changed_files" | grep -E "\.java$" | wc -l)
    local yaml_files=$(echo "$changed_files" | grep -E "\.(yml|yaml)$" | wc -l)
    local md_files=$(echo "$changed_files" | grep -E "\.md$" | wc -l)
    local xml_files=$(echo "$changed_files" | grep -E "\.xml$" | wc -l)
    local sh_files=$(echo "$changed_files" | grep -E "\.sh$" | wc -l)
    local other_files=$(echo "$changed_files" | wc -l)
    
    # Create a descriptive commit message
    if [[ $java_files -gt 0 ]]; then
        message+="Java code"
    fi
    
    if [[ $yaml_files -gt 0 ]]; then
        if [[ -n "$message" ]]; then message+=", "; fi
        message+="configuration"
    fi
    
    if [[ $md_files -gt 0 ]]; then
        if [[ -n "$message" ]]; then message+=", "; fi
        message+="documentation"
    fi
    
    if [[ $xml_files -gt 0 ]]; then
        if [[ -n "$message" ]]; then message+=", "; fi
        message+="XML files"
    fi
    
    if [[ $sh_files -gt 0 ]]; then
        if [[ -n "$message" ]]; then message+=", "; fi
        message+="scripts"
    fi
    
    if [[ -z "$message" ]]; then
        message="files"
    fi
    
    # Count total changes
    local total_changes=$(echo "$changed_files" | wc -l)
    
    echo "${AUTO_COMMIT_PREFIX} Update ${message} (${total_changes} file(s))"
}

# Function to commit changes
commit_changes() {
    local changed_files=$(get_changed_files)
    
    if [[ -z "$changed_files" ]]; then
        return 0
    fi
    
    print_info "Changes detected:"
    echo "$changed_files" | while read -r file; do
        echo "  - $file"
    done
    
    local commit_message=$(generate_commit_message "$changed_files")
    
    print_info "Committing changes with message: $commit_message"
    
    # Add all changed files
    echo "$changed_files" | while read -r file; do
        git add "$file"
    done
    
    # Commit changes
    git commit -m "$commit_message"
    
    print_info "Changes committed successfully"
}

# Function to handle file changes
handle_change() {
    print_info "File change detected, waiting ${DEBOUNCE_TIME}s for more changes..."
    
    # Reset the timer
    if [[ -n ${timer_pid:-} ]]; then
        kill "$timer_pid" 2>/dev/null || true
    fi
    
    # Start a new timer
    (
        sleep "$DEBOUNCE_TIME"
        commit_changes
    ) &
    
    timer_pid=$!
}

# Function to monitor changes using inotify
monitor_changes() {
    print_info "Starting file monitoring (using inotify)"
    print_info "Press Ctrl+C to stop"
    
    # Build the inotifywait command
    local inotify_cmd=("inotifywait" "-m" "-r" "-e" "modify,create,delete,move")
    
    # Add exclude patterns
    for pattern in "${EXCLUDE_PATTERNS[@]}"; do
        inotify_cmd+=("--exclude" "$pattern")
    done
    
    # Add monitored directories
    inotify_cmd+=("${MONITORED_DIRS[@]}")
    
    # Check if inotifywait is available
    if ! command -v inotifywait >/dev/null 2>&1; then
        print_error "inotifywait is not installed. Please install inotify-tools."
        print_info "On Debian/Ubuntu: sudo apt-get install inotify-tools"
        print_info "On RHEL/CentOS: sudo yum install inotify-tools"
        exit 1
    fi
    
    # Run inotifywait and handle events
    "${inotify_cmd[@]}" | while read -r line; do
        print_info "Detected: $line"
        handle_change
    done
}

# Function to run in fallback polling mode
run_polling_mode() {
    print_info "Running in polling mode (interval: 30s)"
    while true; do
        commit_changes
        sleep 30
    done
}

# Main function
main() {
    # Check if we're in a git repository
    if ! git rev-parse --git-dir >/dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi
    
    # Check if git user is configured
    if ! git config user.name >/dev/null 2>&1 || ! git config user.email >/dev/null 2>&1; then
        print_warning "Git user not configured. Setting default values..."
        git config user.name "Auto Commit"
        git config user.email "auto-commit@example.com"
    fi
    
    # Check which monitoring mode to use
    if command -v inotifywait >/dev/null 2>&1; then
        monitor_changes
    else
        print_warning "inotifywait not available, falling back to polling mode"
        run_polling_mode
    fi
}

# Run main function
main