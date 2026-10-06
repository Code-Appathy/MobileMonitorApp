# AppHub v1 common contract

`.apphub` separates semantic project knowledge from GitHub's objective development facts.

Files: `project.json`, `ontology.json`, `features.json`, `milestones.json`, `relations.json`, `decisions.json`, `changelog.json`, `visualization.json`, `GUIDE.md`.

GitHub provides commits, changed files, issues/PRs and Actions results. Codex updates semantic metadata only when the code/design change affects it. Android renders both developer and child-friendly views from the same data.

Ontology changes are first-class events. Increment `ontologyVersion`, preserve a history entry, state the reason, and update affected features/relations/visualization semantics.

Progress must be deterministic. Default milestone weights: planning 10, design 15, foundation 20, features 30, testing 15, production 10.
