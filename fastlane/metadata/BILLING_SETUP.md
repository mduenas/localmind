# In-App Purchase Setup Guide

## Product Definitions

| Field | Lifetime | Monthly |
|-------|----------|---------|
| **Product ID** | `premium_lifetime` | `premium_monthly` |
| **Type** | Non-consumable (Play) / Non-Consumable (App Store) | Subscription (Play) / Auto-Renewable Subscription (App Store) |
| **Reference Name** | LocalMind Premium Lifetime | LocalMind Premium Monthly |
| **Price** | $29.99 (suggested: $29–$49) | $4.99/month |
| **Grace Period** | N/A | 16 days (Play) / auto (App Store) |

### Feature Entitlements (both products unlock the same set)
- On-device LLM task parsing
- JSON task export
- Priority support & future premium features

---

## Google Play Console Setup

### 1. Create In-App Product (Lifetime)
1. Go to **Monetize > Products > In-app products**
2. Click **Create product**
3. Fill in:
   - **Product ID**: `premium_lifetime`
   - **Name**: LocalMind Premium (Lifetime)
   - **Description**: Unlock on-device AI parsing, JSON export, and all future premium features with a one-time purchase.
   - **Default price**: $29.99
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
   - **Price**: $4.99
   - **Grace period**: 16 days
   - **Account hold**: 30 days
5. Set status to **Active**

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
   - **Price**: Tier 30 ($29.99) — adjust per pricing strategy
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
   - **Price**: Tier 5 ($4.99)
4. Add localization:
   - **Display Name**: LocalMind Premium
   - **Description**: On-device AI parsing, JSON export, and all premium features. Cancel anytime.
5. Add a screenshot of the paywall (required for review)
6. Set status to **Ready to Submit**

### 3. Sandbox Testing
1. Go to **Users and Access > Sandbox > Testers**
2. Create sandbox test accounts
3. On device: **Settings > App Store > Sandbox Account** — sign in with test account
4. Sandbox subscriptions renew on accelerated schedule (1 month = 5 minutes)

### 4. App Store Review Notes
Include in the review notes field:
> In-app purchases: The app offers a one-time "Premium Lifetime" purchase ($29.99) and a "Premium Monthly" subscription ($4.99/mo). Both unlock the same features: on-device LLM parsing and JSON export. The app works fully offline with no network calls — StoreKit is the only external API used. Test account credentials: [provide sandbox account].

---

## Code Reference

Product IDs are defined in:
```
composeApp/src/commonMain/kotlin/com/markduenas/localmind/billing/ProductIds.kt
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

Per the spec, pricing ranges are:
- **Lifetime**: $29–$49 one-time (start at $29.99, can increase later)
- **Monthly**: ~$5/month (start at $4.99)

Consider launching at the lower end and raising prices after establishing reviews and download velocity. Both stores allow price changes without app updates.
