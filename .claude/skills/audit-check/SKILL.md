---
name: audit-check
description: Check the codebase against a Zone01 audit file. Use when asked to verify readiness for mr-jenk, safe-zone, buy-02, or nexus.
---
Argument: project name.
1. Read docs/zone01/audits/<project>.md. Until that folder exists, read the "# <project>" section of docs/zone01/audits.md.
2. For each audit question, inspect the code, configs, and docs to decide: PASS, FAIL, or UNCLEAR.
3. Output a table: question, verdict, evidence (file paths), fix needed.
4. End with the top 5 fixes ranked by audit risk.
Do not modify files.