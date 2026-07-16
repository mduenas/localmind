#!/usr/bin/env python3
"""Google Play Developer API helper for LocalMind's IAP setup.

Creates/updates the `premium_lifetime` managed product and the
`premium_monthly` subscription + `monthly` base plan described in
fastlane/metadata/BILLING_SETUP.md.

IMPORTANT: the Play Developer API has no field for scheduling a future
price change (unlike App Store Connect's scheduled price changes). Console's
"Schedule new price" is a Console-only feature. `set-price` below applies a
price change immediately. To hit the phase dates in
composeApp/src/commonMain/kotlin/com/markduenas/localmind/billing/PricingSchedule.kt
either:
  - run `set-price` by hand (or via a scheduled cron/GitHub Action) on the
    phase start dates, or
  - use Play Console's own scheduler for the one-time setup, since it's a
    fire-and-forget UI action anyway.

Requires: pip install google-api-python-client google-auth

Field names for the monetization.subscriptions resource have changed across
API versions -- verify against the current Play Developer API reference
(https://developers.google.com/android-publisher) before relying on this
in production.

Usage:
  export GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH=/path/to/key.json
  export ANDROID_PACKAGE_NAME=com.markduenas.localmind

  python3 scripts/play_billing_setup.py create-lifetime --price-cents 2499
  python3 scripts/play_billing_setup.py create-subscription --price-cents 399
  python3 scripts/play_billing_setup.py set-price --product lifetime --price-cents 2999
  python3 scripts/play_billing_setup.py set-price --product monthly --price-cents 499
  python3 scripts/play_billing_setup.py show --product lifetime
"""

from __future__ import annotations

import argparse
import os
import sys

from google.oauth2 import service_account
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]

LIFETIME_SKU = "premium_lifetime"
MONTHLY_SKU = "premium_monthly"
MONTHLY_BASE_PLAN_ID = "monthly"

DEFAULT_REGION = "US"
DEFAULT_CURRENCY = "USD"


def get_service_account_path() -> str:
    path = (
        os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH")
        or os.environ.get("PLAY_SERVICE_ACCOUNT")
    )
    if not path:
        sys.exit(
            "Set GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH or PLAY_SERVICE_ACCOUNT "
            "to the path of your service account JSON key."
        )
    if not os.path.isfile(path):
        sys.exit(f"Service account JSON not found: {path}")
    return path


def get_package_name() -> str:
    return os.environ.get("ANDROID_PACKAGE_NAME", "com.markduenas.localmind")


def build_client():
    creds = service_account.Credentials.from_service_account_file(
        get_service_account_path(), scopes=SCOPES
    )
    return build("androidpublisher", "v3", credentials=creds)


def create_lifetime(service, package_name: str, price_cents: int) -> None:
    body = {
        "packageName": package_name,
        "sku": LIFETIME_SKU,
        "status": "active",
        "purchaseType": "managedUser",
        "defaultLanguage": "en-US",
        "defaultPrice": {
            "priceMicros": str(price_cents * 10_000),
            "currency": DEFAULT_CURRENCY,
        },
        "listings": {
            "en-US": {
                "title": "LocalMind Premium (Lifetime)",
                "description": (
                    "Unlock on-device AI parsing, JSON export, and all "
                    "future premium features with a one-time purchase."
                ),
            }
        },
    }
    result = (
        service.inappproducts()
        .insert(packageName=package_name, body=body)
        .execute()
    )
    print(f"Created in-app product: {result.get('sku')}")


def create_subscription(service, package_name: str, price_cents: int) -> None:
    body = {
        "packageName": package_name,
        "productId": MONTHLY_SKU,
        "listings": [
            {
                "languageCode": "en-US",
                "title": "LocalMind Premium",
                "description": (
                    "On-device AI parsing, JSON export, and all premium "
                    "features. Cancel anytime."
                ),
            }
        ],
        "basePlans": [
            {
                "basePlanId": MONTHLY_BASE_PLAN_ID,
                "state": "ACTIVE",
                "autoRenewingBasePlanType": {
                    "billingPeriodDuration": "P1M",
                    "gracePeriodDuration": "P16D",
                },
                "regionalConfigs": [
                    {
                        "regionCode": DEFAULT_REGION,
                        "price": {
                            "currencyCode": DEFAULT_CURRENCY,
                            "units": str(price_cents // 100),
                            "nanos": (price_cents % 100) * 10_000_000,
                        },
                        "newSubscriberAvailability": True,
                    }
                ],
            }
        ],
    }
    result = (
        service.monetization()
        .subscriptions()
        .create(packageName=package_name, productId=MONTHLY_SKU, body=body)
        .execute()
    )
    print(f"Created subscription: {result.get('productId')}")


def set_price(service, package_name: str, product: str, price_cents: int) -> None:
    if product == "lifetime":
        existing = (
            service.inappproducts()
            .get(packageName=package_name, sku=LIFETIME_SKU)
            .execute()
        )
        existing["defaultPrice"] = {
            "priceMicros": str(price_cents * 10_000),
            "currency": DEFAULT_CURRENCY,
        }
        result = (
            service.inappproducts()
            .update(packageName=package_name, sku=LIFETIME_SKU, body=existing)
            .execute()
        )
        print(f"Updated {LIFETIME_SKU} price to {price_cents / 100:.2f} {DEFAULT_CURRENCY}")
        return

    if product == "monthly":
        existing = (
            service.monetization()
            .subscriptions()
            .get(packageName=package_name, productId=MONTHLY_SKU)
            .execute()
        )
        for base_plan in existing.get("basePlans", []):
            if base_plan.get("basePlanId") != MONTHLY_BASE_PLAN_ID:
                continue
            for regional_config in base_plan.get("regionalConfigs", []):
                if regional_config.get("regionCode") == DEFAULT_REGION:
                    regional_config["price"] = {
                        "currencyCode": DEFAULT_CURRENCY,
                        "units": str(price_cents // 100),
                        "nanos": (price_cents % 100) * 10_000_000,
                    }
        result = (
            service.monetization()
            .subscriptions()
            .patch(
                packageName=package_name,
                productId=MONTHLY_SKU,
                updateMask="basePlans",
                body=existing,
            )
            .execute()
        )
        print(f"Updated {MONTHLY_SKU} price to {price_cents / 100:.2f} {DEFAULT_CURRENCY}")
        return

    sys.exit(f"Unknown product: {product} (expected 'lifetime' or 'monthly')")


def show(service, package_name: str, product: str) -> None:
    if product == "lifetime":
        result = (
            service.inappproducts()
            .get(packageName=package_name, sku=LIFETIME_SKU)
            .execute()
        )
    elif product == "monthly":
        result = (
            service.monetization()
            .subscriptions()
            .get(packageName=package_name, productId=MONTHLY_SKU)
            .execute()
        )
    else:
        sys.exit(f"Unknown product: {product} (expected 'lifetime' or 'monthly')")
    import json

    print(json.dumps(result, indent=2))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    p_lifetime = sub.add_parser("create-lifetime", help="Create the premium_lifetime managed product")
    p_lifetime.add_argument("--price-cents", type=int, required=True)

    p_sub = sub.add_parser("create-subscription", help="Create the premium_monthly subscription + monthly base plan")
    p_sub.add_argument("--price-cents", type=int, required=True)

    p_price = sub.add_parser("set-price", help="Update the price of an existing product (applies immediately)")
    p_price.add_argument("--product", choices=["lifetime", "monthly"], required=True)
    p_price.add_argument("--price-cents", type=int, required=True)

    p_show = sub.add_parser("show", help="Print the current product/subscription definition")
    p_show.add_argument("--product", choices=["lifetime", "monthly"], required=True)

    args = parser.parse_args()
    service = build_client()
    package_name = get_package_name()

    if args.command == "create-lifetime":
        create_lifetime(service, package_name, args.price_cents)
    elif args.command == "create-subscription":
        create_subscription(service, package_name, args.price_cents)
    elif args.command == "set-price":
        set_price(service, package_name, args.product, args.price_cents)
    elif args.command == "show":
        show(service, package_name, args.product)


if __name__ == "__main__":
    main()
