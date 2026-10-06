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

## Version policy
- Use MAJOR.MINOR.PATCH.
- PATCH: normal updates, fixes, small improvements.
- MINOR: large feature additions or removals. Reset PATCH to 0 after a MINOR bump.
- MAJOR: application-wide redesign or purpose/architecture generation change.
- Keep Android versionName, versionCode, `.apphub/project.json`, `.apphub/releases.json`, and GitHub Release tag consistent.
- Never reuse or decrease Android versionCode.

## Android app distribution
- Android apps intended for My App Store use GitHub Releases as the distribution source.
- Keep pushed/build_succeeded/release_published/apk_available/installed/device_verified distinct.
- New apps should have an automatically generated semantic icon based on app name, purpose, features and ecosystem role.

## Android release signing
- Never commit a keystore, signing password, token, or private key.
- Release APKs are signed only in GitHub Actions using repository Secrets.
- Required Secrets: ANDROID_KEYSTORE, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_PASSWORD, ANDROID_KEY_ALIAS.
- `ANDROID_KEYSTORE` is the base64 text of the JKS file, never the raw binary.
- A GitHub Release is created only from a version tag `vMAJOR.MINOR.PATCH`.
- The tag version must exactly match Android `versionName`.
- Release assets must include the signed APK and `SHA256SUMS.txt`.
- Debug APKs are development artifacts and must not be published as store releases.
