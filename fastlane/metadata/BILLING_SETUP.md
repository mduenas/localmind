# In-App Purchase Setup Guide

## Product Definitions

| Field | Lifetime | Monthly |
|-------|----------|---------|
| **Product ID** | `premium_lifetime` | `premium_monthly` |
| **Type** | Non-consumable (Play) / Non-Consumable (App Store) | Subscription (Play) / Auto-Renewable Subscription (App Store) |
| **Reference Name** | LocalMind Premium Lifetime | LocalMind Premium Monthly |
| **Price (current)** | $24.99 | $3.99/month |
| **Grace Period** | N/A | 16 days (Play) / auto (App Store) |

### Feature Entitlements (both products unlock the same set)
- On-device LLM task parsing
- JSON task export
- Priority support & future premium features

---

## Pricing Schedule (Implemented)

| Phase | Dates | Lifetime | Monthly |
|-------|-------|----------|---------|
| **Launch Intro** | 2026-07-16 to 2026-10-01 | $24.99 | $3.99 |
| **Standard** | 2026-10-02 to 2027-01-31 | $29.99 | $4.99 |
| **Mature** | 2027-02-01 onward | $34.99 | $5.99 |

Use local midnight in each store region when scheduling automatic price changes.

---

## Google Play Console Setup

### 1. Create In-App Product (Lifetime)
1. Go to **Monetize > Products > In-app products**
2. Click **Create product**
3. Fill in:
   - **Product ID**: `premium_lifetime`
   - **Name**: LocalMind Premium (Lifetime)
   - **Description**: Unlock on-device AI parsing, JSON export, and all future premium features with a one-time purchase.
   - **Default price**: $24.99
4. Set status to **Active**

### 2. Create Subscription (Monthly)
1. Go to **Monetize > Subscriptions**
2. Click **Create subscription**
3. Fill in:
   - **Product ID**: `premium_monthly`
   - **Name**: LocalMind Premium
4. Add a **base plan**:
   - **Base plan ID**: `monthly`
   - **Renewal type**: Auto-renewing
   - **Billing period**: 1 month
   - **Price**: $3.99
   - **Grace period**: 16 days
   - **Account hold**: 30 days
5. Set status to **Active**

### 2b. Schedule Play Price Changes
1. In each product/base plan, open **Price > Schedule new price**
2. Add:
   - 2026-10-02: lifetime $29.99, monthly $4.99
   - 2027-02-01: lifetime $34.99, monthly $5.99
3. Enable rollout to all regions (or custom regional overrides)

### 3. Licensing Testing
1. Go to **Settings > License testing**
2. Add your test Gmail accounts
3. These accounts can make purchases without being charged

### 4. Play Console Declarations
- No additional encryption declarations needed (SQLCipher is exempt)
- In-app purchases must be declared in the store listing content rating questionnaire

---

## Apple App Store Connect Setup

### 1. Create Non-Consumable (Lifetime)
1. Go to **App > Features > In-App Purchases**
2. Click **+** and select **Non-Consumable**
3. Fill in:
   - **Reference Name**: LocalMind Premium Lifetime
   - **Product ID**: `premium_lifetime`
   - **Price**: Tier 25 ($24.99)
4. Add localization:
   - **Display Name**: LocalMind Premium (Lifetime)
   - **Description**: Unlock on-device AI parsing, JSON export, and all future premium features with a one-time purchase.
5. Add a screenshot of the paywall (required for review)
6. Set status to **Ready to Submit**

### 2. Create Auto-Renewable Subscription (Monthly)
1. Go to **App > Features > Subscriptions**
2. Create a **Subscription Group**: "LocalMind Premium"
3. Add subscription:
   - **Reference Name**: LocalMind Premium Monthly
   - **Product ID**: `premium_monthly`
   - **Subscription Duration**: 1 Month
   - **Price**: Tier 4 ($3.99)
4. Add localization:
   - **Display Name**: LocalMind Premium
   - **Description**: On-device AI parsing, JSON export, and all premium features. Cancel anytime.
5. Add a screenshot of the paywall (required for review)
6. Set status to **Ready to Submit**

### 2b. Schedule App Store Price Changes
1. Open each IAP/subscription and add scheduled price changes:
   - 2026-10-02: Tier 30 ($29.99) and Tier 5 ($4.99)
   - 2027-02-01: Tier 35 ($34.99) and Tier 6 ($5.99)
2. Confirm preserving price for existing subscribers is **off** for monthly plan.

### 3. Sandbox Testing
1. Go to **Users and Access > Sandbox > Testers**
2. Create sandbox test accounts
3. On device: **Settings > App Store > Sandbox Account** — sign in with test account
4. Sandbox subscriptions renew on accelerated schedule (1 month = 5 minutes)

### 4. App Store Review Notes
Include in the review notes field:
> In-app purchases: The app offers a one-time "Premium Lifetime" purchase and a "Premium Monthly" subscription. Both unlock the same features: on-device LLM parsing and JSON export. Current launch prices are $24.99 lifetime and $3.99/month, with scheduled increases already configured in App Store Connect. The app works fully offline with no network calls — StoreKit is the only external API used. Test account credentials: [provide sandbox account].

---

## Code Reference

Product IDs are defined in:
```
composeApp/src/commonMain/kotlin/com/markduenas/localmind/billing/BillingProduct.kt
```

```kotlin
object ProductIds {
    const val PREMIUM_LIFETIME = "premium_lifetime"
    const val PREMIUM_MONTHLY = "premium_monthly"
}
```

These IDs **must match exactly** between the code and both store consoles.

---

## Pricing Strategy Notes

Schedule rationale:
- Lower launch prices reduce purchase friction while early reviews accumulate.
- First lift at 2026-10-02 aligns with post-launch validation window.
- Second lift at 2027-02-01 aligns with mature feature set and higher perceived value.
