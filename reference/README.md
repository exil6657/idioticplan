# Reference Material — DO NOT MERGE TO MAIN

This folder is for temporary reference jars / decompiled sources used to improve accuracy of Zenith Client.

Contents here:
- Are **only** on `arena/*` branches
- Must be **removed before merging to `main`** (run `git rm -r reference/` + add `reference/` to .gitignore)
- Are not included in the mod jar build (not jar-in-jar)
- May be copyrighted — do not publish on public `main`, do not distribute

Purpose: compare rotation, pathfinding, GUI click, Bazaar/AH sign handling vs another macro mod to fill [RESEARCH NEEDED] gaps.

After analysis, agent writes `docs/REFERENCE_COMPARISON.md` and patches engines, then reference files are deleted.
