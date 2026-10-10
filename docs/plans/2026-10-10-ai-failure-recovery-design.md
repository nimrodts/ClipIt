# AI failure recovery — design

## Goal

An SMS that passed the whitelist and on-device coupon check but could not go through Gemini
(503 "high demand", timeout, network, quota, invalid key, other API errors) must not be lost or
silently turned into a weak regex guess. It waits in the Pending Coupons dialog, where the user can
re-run the AI or fill it in by hand, and the app also retries it in the background.

## Data

`Coupon` gains four nullable columns (Room migration 18 → 19):

| Column | Meaning |
|---|---|
| `sourceSms` | Original SMS body, always kept for coupons created from SMS |
| `sourceSender` | SMS sender, used as the name fallback and in notifications |
| `aiStatus` | `null` = no AI problem, `FAILED_RETRYING` = background retries active, `FAILED` = no more automatic retries |
| `aiError` | Last AI error message, shown in the Pending dialog |

## Flow

1. `CouponExtractionWorker` no longer retries itself. It always saves a pending coupon (regex guess
   as the starting point when AI failed), built by `PendingCouponBuilder`.
   - Transient error or quota → `FAILED_RETRYING`, notify "Coupon saved — AI unavailable", schedule
     `AiRetryWorker`.
   - Invalid key or other error → `FAILED`, notify "Coupon saved — AI failed".
   - Success → as before, the notification names the store and the sender.
2. `AiRetryWorker` (unique per coupon, needs network): first run after 15 minutes, then linear
   backoff of 1h, 2h, 3h … capped at 5h by WorkManager, for 8 runs (about a day).
   - Success → coupon fields replaced with the AI result, `aiStatus = null`, notify
     "Coupon updated by AI".
   - Still failing on the last run → `aiStatus = FAILED`, no notification.
3. Manual "Retry with AI" in the Pending dialog runs the same retry immediately. On failure only
   `aiError` is updated; a coupon that is still `FAILED_RETRYING` keeps its background retries.

## Rules

- A background retry does nothing if the coupon was deleted, approved (edit-and-save approves too),
  or is no longer `FAILED_RETRYING` (e.g. a manual retry already succeeded). Its job ends then.
- The coupon is re-read right before writing; if it changed during the AI call, the result is
  dropped so user edits are never overwritten.
- If the AI result's redeem code already belongs to another coupon, the pending coupon is kept
  unchanged and marked `FAILED` with a "duplicate" error.

## Testing

JVM unit tests for `PendingCouponBuilder` (status mapping, field mapping, review-required draft) and
`CouponAiRetrier` (all outcomes above, using fakes for the parser and coupon store).
