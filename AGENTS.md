# MobileMonitorApp / AppHub v1 Codex rules

Codex must treat source code and GitHub as development facts, and `.apphub/` as semantic metadata.

After meaningful work, inspect the change and update only affected metadata:
- Feature behavior/status -> `.apphub/features.json`
- Milestone/task state -> `.apphub/milestones.json`
- Domain concepts/relationships -> `.apphub/ontology.json`; increment `ontologyVersion` and append a reasoned history item
- Cross-project relationships -> `.apphub/relations.json`
- Important architectural/product choice -> `.apphub/decisions.json`
- User-facing meaning changes -> `.apphub/GUIDE.md` and, when needed, `.apphub/visualization.json`
- Every meaningful metadata change -> append `.apphub/changelog.json`

Never invent a completion percentage. Progress is calculated from weighted milestones/tasks.
Never mark build/test/device verification complete without evidence.
Keep these states distinct: file created, pushed, build succeeded, installed, device verified.
