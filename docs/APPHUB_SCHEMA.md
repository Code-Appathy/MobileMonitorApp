# AppHub shared project schema v1
Each monitored PC repository should eventually contain `.apphub/project.json` and `.apphub/GUIDE.md`.

`project.json` is machine-readable. Core concepts: project, status, milestones, tasks, features, relations, updatedAt.
Statuses: `planned`, `in_progress`, `completed`, `blocked`.
Relations: `depends_on`, `provides_to`, `manages`, `shares_data_with`, `planned_integration`.
Progress is calculated from milestone/task completion rather than entered as an arbitrary percentage.

`GUIDE.md` is the friendly explanation source. It should explain purpose, features, connections and future work in short, simple Japanese suitable for a child to understand. Feature IDs in JSON may point to guide sections.
