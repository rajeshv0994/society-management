# SocietyHub Design System

The public website and all four role applications use the same visual language.
Role applications consume the shared implementation from
`packages/society_ui/lib/src/theme.dart`.

## Core tokens

| Token | Value | Usage |
| --- | --- | --- |
| Navy | `#17213F` | Primary text, dark surfaces, snackbars |
| Dark navy | `#0E1530` | Deep brand surfaces |
| Society blue | `#3157D5` | Primary actions, focus, selected navigation |
| Teal | `#12A594` | Community accent and supporting decoration |
| Warm canvas | `#FFFBF5` | Page background |
| Muted blue | `#F1F5FF` | Selected states and quiet surfaces |
| Border | `#E5E8F1` | Cards, inputs, navigation separation |
| Muted text | `#66708C` | Supporting copy |

Semantic colors are success `#14866D`, warning `#B66500`, and danger `#BD3551`.

## Role accents

The primary brand/action color remains SocietyHub blue. A secondary accent identifies the
workspace:

| Application | Accent |
| --- | --- |
| Platform Admin | Purple `#6E42BD` |
| Society Admin | Blue `#3157D5` |
| Staff | Teal `#087E8B` |
| Member | Terracotta `#D14D25` |

## Component rules

- Page backgrounds use the warm canvas.
- Cards use white surfaces, 20-pixel corners, a subtle border, and no heavy elevation.
- Inputs use white fill, 14-pixel corners, leading icons where useful, and a blue focus ring.
- Primary actions use SocietyHub blue; role accents identify the role rather than replacing
  primary action hierarchy.
- Side navigation uses white with a border and blue selected states.
- Login screens use the website's soft cream-to-blue background, decorative brand glows,
  SocietyHub mark, role icon, and a single focused form card.
- Page spacing reduces from 22–24 pixels to 16 pixels on narrow screens.
- Status colors must use the semantic tokens, not arbitrary Material colors.

## Flutter usage

```dart
MaterialApp(
  theme: SocietyTheme.light(accent: const Color(0xff087e8b)),
  home: const StaffApp(),
)
```

Reusable public components include `SocietyBrandMark`, `SocietyLoginBackground`,
`SocietyColors`, and `SocietyRoleTheme`.
