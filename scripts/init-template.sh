#!/usr/bin/env bash
# Turns the template into your mod: renames packages, ids, names and links, then removes itself.
# Run once from the repository root. Needs bash, perl and coreutils (Git Bash on Windows is fine).
set -euo pipefail

usage() {
    cat <<USAGE
Usage: scripts/init-template.sh --id <mod_id> --name "<Mod Name>" --group <maven.group> --author "<Author>" [options]

Required
  --id       Fabric mod id: lowercase letters, digits, - or _, 2 to 64 chars, starts with a letter. Example: souls
  --name     Human readable name. Example: "Thrill of the Hunt"
  --group    Maven group and root package. Example: io.github.henryennis
  --author   Shown in fabric.mod.json and the LICENSE.

Optional
  --repo         GitHub owner/name used for links and releases. Default: your-github/<id>
  --description  One sentence for fabric.mod.json.
  --dry-run      Print what would change and exit.
USAGE
}

ID="" NAME="" GROUP="" AUTHOR="" REPO="" DESCRIPTION="" DRY_RUN=0
while [ $# -gt 0 ]; do
    case "$1" in
        --id) ID="$2"; shift 2 ;;
        --name) NAME="$2"; shift 2 ;;
        --group) GROUP="$2"; shift 2 ;;
        --author) AUTHOR="$2"; shift 2 ;;
        --repo) REPO="$2"; shift 2 ;;
        --description) DESCRIPTION="$2"; shift 2 ;;
        --dry-run) DRY_RUN=1; shift ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown argument: $1" >&2; usage; exit 2 ;;
    esac
done

[ -n "$ID" ] && [ -n "$NAME" ] && [ -n "$GROUP" ] && [ -n "$AUTHOR" ] || { usage; exit 2; }
[[ "$ID" =~ ^[a-z][a-z0-9_-]{1,63}$ ]] || { echo "Invalid mod id: $ID" >&2; exit 2; }
[[ "$GROUP" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$ ]] || { echo "Invalid group: $GROUP" >&2; exit 2; }
[ -f gradle.properties ] && [ -d src/main/kotlin/com/example/examplemod ] || { echo "Run from the template root; it looks already initialised." >&2; exit 2; }

REPO="${REPO:-your-github/$ID}"
ID_PKG="${ID//-/}"
PACKAGE="$GROUP.$ID_PKG"
PACKAGE_PATH="${PACKAGE//.//}"
CLASS="$(printf "%s" "$NAME" | perl -pe "s/[^A-Za-z0-9 ]//g; s/(\\w+)/\\u\$1/g; s/ //g")"
[[ "$CLASS" =~ ^[A-Za-z] ]] || CLASS="Mod$CLASS"
YEAR="$(date +%Y)"

cat <<SUMMARY
Mod id        $ID
Name          $NAME
Class prefix  $CLASS
Package       $PACKAGE
Group         $GROUP
Author        $AUTHOR
Repository    $REPO
SUMMARY
[ "$DRY_RUN" -eq 1 ] && exit 0

case "$PACKAGE_PATH/" in com/example/examplemod/*) echo "The new package sits inside the placeholder package; pick a different group." >&2; exit 2 ;; esac

# 1. Move package directories.
for base in src/main/kotlin src/main/java src/client/kotlin src/client/java src/datagen/kotlin src/gametest/kotlin src/test/kotlin; do
    old="$base/com/example/examplemod"
    [ -d "$old" ] || continue
    new="$base/$PACKAGE_PATH"
    [ "$old" = "$new" ] && continue
    mkdir -p "$new"
    (shopt -s dotglob nullglob; mv "$old"/* "$new"/)
    rmdir -p "$old" 2>/dev/null || true
done

# 2. Rename files and directories that carry the placeholder names (deepest first).
find src -depth -name "*ExampleMod*" | while read -r path; do
    mv "$path" "$(dirname "$path")/$(basename "$path" | perl -pe "s/ExampleMod/$CLASS/g")"
done
find src -depth -name "*examplemod*" | while read -r path; do
    mv "$path" "$(dirname "$path")/$(basename "$path" | perl -pe "s/examplemod/$ID/g")"
done

# 3. Replace placeholders in text files, most specific first.
replace() {
    local from="$1" to="$2"
    find . -type f \
        ! -path "./.git/*" ! -path "./build/*" ! -path "./.gradle/*" ! -path "./.idea/*" ! -path "./run/*" \
        ! -name "*.jar" ! -name "*.png" ! -path "./scripts/init-template.sh" -print0 \
        | xargs -0 perl -pi -e "BEGIN { \$f = shift; \$t = shift } s/\\Q\$f\\E/\$t/g" "$from" "$to"
}
replace "your-github/examplemod" "$REPO"
replace "com.example.examplemod" "$PACKAGE"
replace "com/example/examplemod" "$PACKAGE_PATH"
# Mixin handler names carry the mod id as a prefix; Java identifiers cannot contain a dash.
replace 'examplemod$' "$ID_PKG\$"
replace "ExampleMod" "$CLASS"
replace "Example Mod" "$NAME"
replace "examplemod" "$ID"
replace "com.example" "$GROUP"
replace "Your Name" "$AUTHOR"
replace "Copyright (c) 2026 Henry Ennis-Thomas" "Copyright (c) $YEAR $AUTHOR"
[ -n "$DESCRIPTION" ] && replace "A Cobblemon side-mod built from the Fabric template." "$DESCRIPTION"

# 4. Fresh changelog and README for the new mod.
cat > CHANGELOG.md <<CHANGELOG
# Changelog

The release workflow publishes the newest versioned section below as the release notes.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Project created from the Cobblemon Fabric mod template.
CHANGELOG

cat > README.md <<README
# $NAME

${DESCRIPTION:-A Cobblemon side-mod for Fabric.}

Built from [cobblemon-fabric-mod-template](https://github.com/henryennis/cobblemon-fabric-mod-template).

## Develop

\`\`\`bash
./gradlew build          # format check, unit tests, jar in build/libs
./gradlew runClient      # Minecraft client with the mod and Cobblemon
./gradlew runServer      # dedicated server
./gradlew runGametest    # in-game tests on a headless server
./gradlew runDatagen     # regenerate src/main/generated
./gradlew spotlessApply  # fix formatting
\`\`\`

See \`AGENTS.md\` for the project map and rules, and \`docs/\` for Cobblemon hook points, conventions and releasing.
README

# 5. The new package name changes import order, so let the formatter settle it.
if ./gradlew -q spotlessApply; then
    FORMAT_NOTE="Formatting applied."
else
    FORMAT_NOTE="Formatting could not run (offline?). Run ./gradlew spotlessApply before building."
fi

rm -f scripts/init-template.sh
rmdir scripts 2>/dev/null || true

cat <<DONE

Done. $FORMAT_NOTE Next:
  1. Review the diff, then: ./gradlew build
  2. Commit: git add -A && git commit -m "Initialise $NAME from the Cobblemon Fabric template"
  3. Fill in modrinth_project_id or curseforge_project_id in gradle.properties when you are ready to publish.
DONE
