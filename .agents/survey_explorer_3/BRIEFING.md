# BRIEFING — 2026-08-21T12:55:00Z

## Mission
Investigate Requirement R2 (Dual Data Usage Panel) and Requirement R4 (iOS-Style Connectivity Platter) for HyperCeilerModded, identify hooks, classes, method signatures, intents, and architecture.

## 🔒 My Identity
- Archetype: explorer
- Roles: investigator, synthesizer
- Working directory: /data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_3/
- Original parent: b83761d6-52ba-4de0-acaf-7d9a88818c8e
- Milestone: survey

## 🔒 Key Constraints
- Read-only investigation — do NOT implement / modify source code in project
- Target R2 (Dual Data Usage Panel) and R4 (iOS-Style Connectivity Platter)
- Produce comprehensive survey report and 5-component handoff report

## Current Parent
- Conversation ID: b83761d6-52ba-4de0-acaf-7d9a88818c8e
- Updated: 2026-08-21T12:55:00Z

## Investigation State
- **Explored paths**: `library/hook`, `library/common-ui`, `NewPluginHelperKt.kt`, `HideDelimiter.java`, `CustomCardTiles.java`, `TileUtils.java`, `NewNetworkSpeed.kt`, `AppRestrict.java`, `system_ui_control_center.xml`, `TileSettings.java`, `ControlCenterSettings.java`.
- **Key findings**: 
  - R2: Target `ControlCenterStatusBar` / `MiuiQSHeaderView` carrier/flare `TextView`. Query `NetworkStatsManager.querySummaryForDevice` (with `TYPE_MOBILE` and `TYPE_WIFI`), format as `"Mobile: X | WiFi: Y"`, and set long-click listener to launch `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity`.
  - R4: Hook `miui.systemui.plugin` via `NewPluginHelperKt.kt` (`QSController.getCardStyleTileSpecs` / `QSCardsController`). Provide 2x2 `ConnectivityPlatterView` for Wi-Fi, BT, Cell, Airplane Mode with long-press expansion into 6-item modal `ConnectivityExpandDialog`.
- **Unexplored areas**: None for survey scope.

## Key Decisions Made
- Completed technical survey and architectural design for R2 and R4.
- Created `survey_report.md` and `handoff.md`.

## Artifact Index
- DISPATCH.md — record of task instructions
- progress.md — liveness heartbeat
- survey_report.md — detailed technical survey findings for R2 and R4
- handoff.md — 5-component handoff report
