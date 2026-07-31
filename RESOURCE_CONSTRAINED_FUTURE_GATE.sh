#!/bin/sh
# Unverified future gate for the maintained Brief Android application.
set -eu

repo_name=Brief
repo_root=$(CDPATH= cd "$(dirname "$0")" && pwd -P)
verify=0
allow_network=0
owner_device=0
owner_integration=0
owner_deploy=0

die() {
    printf '%s\n' "future gate: $*" >&2
    exit 1
}

usage() {
    printf '%s\n' "usage: ./RESOURCE_CONSTRAINED_FUTURE_GATE.sh --verify --allow-network [--owner-approved-integration] [--owner-approved-device] [--owner-approved-deploy]"
}

while [ "$#" -gt 0 ]; do
    case "$1" in
        --verify) verify=1 ;;
        --allow-network) allow_network=1 ;;
        --owner-approved-integration) owner_integration=1 ;;
        --owner-approved-device) owner_device=1 ;;
        --owner-approved-deploy) owner_deploy=1 ;;
        --help) usage; exit 0 ;;
        *) die "unknown option: $1" ;;
    esac
    shift
done

[ "$(basename "$repo_root")" = "$repo_name" ] || die "expected repository basename $repo_name"
git_root=$(git -C "$repo_root" rev-parse --show-toplevel 2>/dev/null) || die "not a Git worktree"
[ "$git_root" = "$repo_root" ] || die "script must remain at the Git worktree root"

if [ "$verify" -ne 1 ]; then
    usage
    printf '%s\n' "plan only: requires a clean checkout, JDK 17, Android SDK, and explicit --allow-network for a clean dependency resolution."
    exit 0
fi

[ "$allow_network" -eq 1 ] || die "a clean Gradle checkout may resolve dependencies; rerun with --allow-network"
[ -z "$(git -C "$repo_root" status --porcelain)" ] || die "a clean checkout is required"
git -C "$repo_root" diff --check
command -v java >/dev/null 2>&1 || die "JDK 17 is required"

cd "$repo_root"
run_gradle() {
    if [ -x "$repo_root/gradlew" ]; then
        "$repo_root/gradlew" --no-daemon "$@"
    elif [ -f "$repo_root/gradle/wrapper/gradle-wrapper.jar" ] && [ -f "$repo_root/gradle/wrapper/gradle-wrapper.properties" ]; then
        java -classpath "$repo_root/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain --no-daemon "$@"
    else
        die "Gradle wrapper launcher or wrapper JAR and properties are required"
    fi
}

run_gradle :app:assembleDebug
run_gradle :app:test
run_gradle :app:ktlintCheck :app:detekt

if [ "$owner_integration" -eq 1 ]; then
    printf '%s\n' "Integration gate: on an authorized test device, verify Wikipedia current-events fetch, Room cache, bookmarks, search, offline reading, and WorkManager background sync."
fi
if [ "$owner_device" -eq 1 ]; then
    command -v adb >/dev/null 2>&1 || die "Android SDK platform tools are required for the device gate"
    run_gradle :app:connectedDevDebugAndroidTest
    printf '%s\n' "Device gate: manually inspect the dev build against Android API 28+ behavior and offline cache recovery."
fi
if [ "$owner_deploy" -eq 1 ]; then
    printf '%s\n' "No local Play publishing configuration is present. Owner must approve a separately documented release and store submission; this script does not publish."
fi

printf '%s\n' "UNVERIFIED: future gate commands completed only if every invoked command above succeeds."
