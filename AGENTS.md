# AGENTS.md

## Search and Read Constraints
- Before starting file operations in a turn, check the root `.gitignore` once to confirm whether target paths are ignored.
- If `.gitignore` has already been checked in the current turn and the scope has not changed, do not re-check it for each file operation.
- Treat `.gitignore` as the default exclusion source for this repository.
- Never scan, search, read, modify, create, or rely on files or directories ignored by `.gitignore`.
- This applies to `find`, `grep`, `rg`, globbing, code indexing, and exploratory file reads.
- Only access an ignored path if the user explicitly provides the exact path and asks to inspect it.

## Search Policy
- Prefer searching only in relevant source directories such as `src/main`, `src/test`, `app/src`, or paths explicitly named by the user.
- Before any broad search, narrow the scope to non-ignored source directories first.
- Prefer search tools and command patterns that support explicit exclude rules for ignored paths.
- When using shell search tools, explicitly exclude ignored directories whenever the tool supports it.

## Ignored Artifact Policy
- Treat common ignored paths in this repository as out of scope by default, including `target/`, `build/`, `.idea/`, `.vscode/`, `.settings/`, `.sts4-cache/`, generated archives, logs, and compiled artifacts.
- Skip build outputs, caches, generated files, dependency directories, IDE metadata, and any other paths ignored by `.gitignore`.
- Do not use ignored files as implementation references, debugging context, or evidence unless the user explicitly requests them.
- If a broad search might enter ignored paths, narrow the search or add explicit exclude rules first.
