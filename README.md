# CouponManager App Features

This document provides an overview of the features available in the CouponManager app.

## Coupon Management

- **Create, Edit, and Archive Coupons:** Users can easily add new coupons, modify existing ones, or archive them when they are no longer in use.
- **Track Coupon Balance:** The app tracks both the initial and current value of each coupon, allowing users to see how much has been used.
- **Set Expiration Dates:** Users can set an expiration date for each coupon to ensure they are used on time.
- **Categorize Coupons:** Coupons can be assigned to user-defined categories, making it easy to organize and find them.
- **Redeem Codes:** Users can add a redeem code to each coupon, which is displayed in the coupon's details.

## Category Management

- **Create and Manage Categories:** Users can create custom categories to organize their coupons. This allows for a more structured and personalized experience.

## History & Restore Feature

### 1. Overview
The **History & Restore** module provides users with a complete, immutable timeline of every action performed on a coupon. It serves as an audit trail for accuracy and a safety net against mistakes, allowing users to revert a coupon to any previous state without data loss.

### 2. Automatic Operation Tracking
The system automatically records an entry in the history log whenever a significant change occurs to a coupon. Users are not required to manually save these versions.

**Tracked Operations:**
- **Creation:** Recorded when a new coupon is first added to the system.
- **Edits:** Recorded when details such as Name, Expiration Date, Category, or Redeem Code are modified.
- **Balance Changes:** Recorded when a balance is manually updated or utilized.
- **Status Changes:** Recorded when a coupon is Archived or Unarchived.

### 3. Viewing History
Users can access a dedicated **History** section within the details view of any specific coupon.

#### Display Logic
- **Sort Order:** The list is displayed in reverse chronological order (newest operations at the top).
- **Entry Details:** Each history row contains:
  - **Action Type:** A clear label indicating the event (e.g., "Balance Updated", "Coupon Edited").
  - **Change Summary:** A descriptive text detailing specific changes (e.g., "Balance changed from $50 to $40").
  - **Timestamp:** The date and time the action occurred.
  - **Redeem Code:** The coupon redeem code
- **Actionability:** Each past entry includes a **"Restore"** option, allowing the user to revert the coupon to the state it held *before* that specific action occurred.

### 4. The "Restore" Functionality
The Restore feature allows users to "time travel" back to a previous version of the coupon.

#### Behavior Rules
1.  **Non-Destructive Reversion:** When a user restores an old version, the app **does not** delete the history between the past date and the present. Instead, it creates a **new** history entry at the top of the list labeled "Restored."

2.  **Unlimited Restoration:** Users can restore from *any* point in the history list, not just the most recent action.

3.  **Forward-Only Timeline:**
    - *Example:* A user changes a name on Monday, then changes the balance on Tuesday.
    - On Wednesday, they choose to restore the Monday version.
    - **Result:** The coupon returns to Monday's state. The history list preserves the record: Monday -> Tuesday -> Wednesday -> **Thursday (Restored to Monday).**

4.  **Safety Net:** Since a "Restore" action is itself recorded as a new history entry, users can effectively "undo a restore" by restoring the version immediately prior to the restore action.

### 5. Visual Feedback
- **Confirmation:** Upon a successful restore, the app displays a temporary notification (e.g., Snackbar) confirming: *"Coupon restored to version from [Date]."*
- **Immediate Reflection:** The coupon details screen immediately updates to reflect the restored values (Name, Balance, Category, etc.) without requiring a page refresh.

### 6. Edge Case Handling
- **Restoring to "Uncategorized":** If a user restores a version that belonged to a Category that has since been deleted, the system will default the coupon to "No Category" (or a generic bucket) rather than failing the operation.

### 7. Archived Coupon
After a coupon is archived, the history operation will be hidden and won't be shown in the history page.
There will be an option to see the history belonging to a specific coupon through the archived page.

## Data Persistence

- **Local Storage:** All coupon and category data is stored locally on the device using a Room database, ensuring that the information is available even when offline.
