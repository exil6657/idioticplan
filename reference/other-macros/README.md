# Upload your other macro jar here

Place file(s) here:

- `reference/other-macros/other-macros.jar`  <- the obfuscated jar you mentioned
- Optionally decompiled sources: `reference/other-macros/decompiled/...`

Acceptable:
- .jar (ideally <100MB, GitHub limit)
- .zip of decompiled .java
- Single .java snippets if jar too big

How to upload:
1. Via Arena chat: drag-drop the .jar onto the chat — it will appear in workspace root, then move to here.
2. Via git locally:
   ```
   cp /path/to/other.jar reference/other-macros/other-macros.jar
   git add reference/
   git commit -m "reference: add other macro jar for comparison (do not merge)"
   git push origin arena/019f6b02-idioticplan
   ```

I (agent) will then:
- `jar tf` list classes
- `java -jar vineflower.jar other-macros.jar -d decompiled/`
- Diff your `KeySimulator`, `ZenithEyes.RotationExecutor`, `AStarPathfinder`, `AuctionHouseExecutor`, `BazaarExecutor`, `SignInputHandler` vs theirs
- Write `docs/REFERENCE_COMPARISON.md`

Delete before merge:
```
git rm -r reference/
echo "reference/" >> .gitignore
```
