# ClipIt Redesign: Apple-Neutral — Design Spec

**Status:** Approved 2026-10-11
**Prototype:** https://claude.ai/artifact/54tdLfBs9WZMrTHzhNxYeV (interactive HTML; the motion values below were tuned there)

## Decisions (from brainstorming)

| Question | Decision |
|---|---|
| Scope | Full overhaul, shipped in phases, one PR per phase |
| Visual direction | Apple-like neutral: grey grouped ground, white cards, one green accent |
| Tapping a coupon card | Opens a detail sheet that morphs out of the card |
| Long-pressing a coupon card | Opens the redeem code view: QR + CODE_128 barcode + code text, screen brightness raised |
| Swipe vs. tab paging | Card swipes win; `HorizontalPager` tab paging is removed |

## Problems this fixes (audit of the current UI)

- `Theme.kt` only sets primary/secondary/background/surface, so `tertiaryContainer`, `secondaryContainer`,
  `surfaceVariant` and `error` fall back to Material's baseline purple.
- `Type.kt` overrides only `bodyLarge`; money uses proportional digits.
- Not edge-to-edge; `window.statusBarColor` is deprecated.
- Card tap does nothing; the redeem code is behind an undiscoverable long press with no hint.
- Archive and one-time redeem block on confirmation dialogs; everything else is in a ⋮ menu.
- Bottom bar has `indication = null` (no press feedback) and is an opaque slab.
- Screen transitions are fixed-duration slides; detail screens don't originate from their source.

## Foundations

- **Color:** neutral surfaces with a slight green bias, one accent. Every Material 3 color role gets an explicit
  value in both themes. Semantic warn (amber) and critical (red) are separate from the accent. Category colors
  appear only as an 8dp dot.
  - Light: background `#F2F2EF`, surface/card `#FFFFFF`, onSurface `#141615`, onSurfaceVariant `#5D6460`,
    outline `#969D99`, primary `#128255`, warn `#A86400`, error `#CC3426`.
  - Dark: background `#0A0B0A`, surface/card `#1A1C1B`, sheet `#1E201F`, onSurface `#F1F4F2`,
    onSurfaceVariant `#A2AAA6`, outline `#6A716D`, primary `#3CCB8A`, warn `#F2AB3C`, error `#FF6D60`.
  - The redeem pass is white in both themes (scanners need dark-on-light).
- **Type:** system font. Large title 34sp/1.08, −0.024em, bold. Display balance 46sp, −0.032em, tabular figures.
  Card name 16.5sp semibold −0.012em. Meta 13sp. Caption 12sp semibold, +0.06em, uppercase. Sizes in `sp`.
- **Edge-to-edge** with content scrolling under translucent bars.

## Home

- Card: name + balance (tabular) on row 1; category dot, relative expiry, "of ₪X" on row 2; 4dp balance bar.
  Relative expiry: "Expires today" / "Expires tomorrow" / "N days left" (amber when ≤ 7, red when expired),
  "Until 14 Mar" beyond 30 days. Sort: soonest expiry first, expired last.
- Press: scale 0.97 on touch-down (100ms ease-out). Held: continues to 0.95 as a long-press hint; fires at 450ms
  with a haptic.
- Swipe right = Use (opens sheet focused on amount) / Redeem for single-use; swipe left = Archive.
  10dp hysteresis, 1:1 tracking, rubber-band past 55% width, commit line 32% width decided by
  projected position `x + (v/1000)·d/(1−d)`, `d = 0.998`. Haptic tick when crossing the commit line.
  Return spring damping 0.8 / response 0.38; fly-off damping 1.0 / response 0.3.
- Undo snackbar (5s) replaces archive and redeem confirmations. Delete keeps an inline second-tap confirmation.
- AI-detected SMS coupons are a "Found in your SMS" section at the top of the list (Add / Dismiss with undo).
- Large title collapses into an inline title; header material appears only once content scrolls under it.

## Detail sheet

- Shared-element morph from the card (Compose `SharedTransitionLayout`). Open: damping 1.0, response 0.38.
- Drag down from the header; dismiss when projected drag > 28% of sheet height; dismiss spring damping 0.86
  with velocity handed off. Can be caught while closing. Background scales to 0.945 with a scrim.
- Contents: balance display, bar, expiry, code (tap to copy), "Show QR & barcode", redemption link,
  Use amount (quick chips ₪25/₪50/₪100/All, inline validation), history, original message, edit, archive, delete.

## Navigation & chrome

- Tabs: Coupons / Insights / Settings, no paging, translucent bar (blur on API 31+, solid fallback),
  press feedback.
- All non-gesture transitions are critically damped springs; bounce only after a flick.
- Reduce motion (system animator scale 0 or in-app) replaces springs with ~200ms cross-fades.

## Phases

0. Foundation: library upgrade (Compose 1.7 for shared elements), edge-to-edge.
1. Theme, color and type system.
2. Coupon card, swipe actions, Undo, long-press redeem view.
3. Detail sheet + shared-element transition.
4. Tab bar, collapsing header, motion pass, reduce motion.
5. Add/Edit as bottom sheets; Insights (Dashboard) and Settings restyle.
