# Original User Request

## 2026-08-21T12:50:01Z

# Teamwork Project Prompt — Draft

> Status: Launched
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Implement an iOS-style Control Center experience for HyperOS by modifying the SystemUI/ControlCenter hooks in the HyperCeiler Xposed module. The project is a capability showcase.

Working directory: /data/data/com.termux/files/home/HyperCeilerModded
Integrity mode: demo

## Requirements

### R1. Adjustable Grid & Padding
Implement hooks for `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (or equivalent) to allow customizable grid columns (default 4, horizontal), padding between tiles, and edge padding. Include the necessary XSharedPreferences toggles and UI sliders in the HyperCeiler settings app.

### R2. Dual Data Usage Panel
Hook into the existing MIUI Data Usage text view (header/footer of Control Center). Modify the injected text to display both Mobile and WiFi data (e.g., "Mobile: 5GB | WiFi: 10GB"). Override the long-press action on this text view to open the Security app's Data usage stats page.

### R3. Custom Shapes & Sizes
Provide sliders in the HyperCeiler Settings to globally adjust the Corner Radius (Shape) and Scale (Size) of all small QS tiles. Hook `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` to apply these layout parameters dynamically.

### R4. iOS-Style Connectivity Platter (Folder)
Create a predefined, expandable "Connectivity Folder" platter within the Control Center. This platter should group the WiFi, Bluetooth, and Mobile Data tiles, expanding to show more options (like Airplane Mode or Hotspot) when long-pressed, mimicking iOS behavior.

## Acceptance Criteria

### Compilation & Hooks
- [ ] The module compiles successfully via `./gradlew build` without syntax errors.
- [ ] Xposed hooks correctly target `com.android.systemui` and `miui.systemui.controlcenter` classes without crashing the SystemUI process.

### Feature Verification
- [ ] Grid column counts, padding values, tile scale, and corner radius reflect dynamically based on HyperCeiler preference values.
- [ ] Data usage text displays both WiFi and Mobile data in the format "Mobile: X | WiFi: Y".
- [ ] Long-pressing the data usage text successfully broadcasts the intent to launch the Security app's data usage activity.
- [ ] The iOS-style Connectivity Platter renders correctly and expands upon interaction.

---
*Next: when approved → delegate via invoke_subagent (see Delegation Protocol)*
