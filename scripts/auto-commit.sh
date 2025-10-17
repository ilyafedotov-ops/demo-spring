#!/usr/bin/env bash
set -euo pipefail

# Load configuration from file if it exists
CONFIG_FILE="$(dirname "${BASH_SOURCE[0]}")/../.auto-commit-config"
if [[ -f "$CONFIG_FILE" ]]; then
    source "$CONFIG_FILE"
else
    # Default configuration
    MONITORED_DIRS=("src" "docs" "scripts")
    EXCLUDE_PATTERNS=("*.tmp" "*.log" ".git/*" "target/*" "node_modules/*")
    COMMIT_INTERVAL=300  # 5 minutes in seconds
    AUTO_COMMIT_PREFIX="[auto-commit]"
fi

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
        print_info "No changes detected"
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

# Function to run in continuous mode
run_continuous() {
    print_info "Starting continuous monitoring (interval: ${COMMIT_INTERVAL}s)"
    print_info "Press Ctrl+C to stop"
    
    while true; do
        commit_changes
        sleep "$COMMIT_INTERVAL"
    done
}

# Function to run once
run_once() {
    print_info "Checking for changes..."
    commit_changes
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
    
    # Parse command line arguments
    case "${1:-once}" in
        "once")
            run_once
            ;;
        "continuous")
            run_continuous
            ;;
        "help"|"-h"|"--help")
            echo "Usage: $0 [once|continuous|help]"
            echo "  once       - Check for changes and commit once (default)"
            echo "  continuous - Run continuously, checking for changes at intervals"
            echo "  help       - Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"