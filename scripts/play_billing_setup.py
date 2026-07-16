#!/usr/bin/env python3
"""Google Play Developer API helper for LocalMind's IAP setup.

Creates/updates the `premium_lifetime` one-time product (via
monetization.onetimeproducts -- the legacy inappproducts endpoint returns
403 "Please migrate to the new publishing API" on this account) and the
`premium_monthly` subscription base plan, per
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

The service account also needs write permission granted in Play Console
(Users and permissions) beyond the default deploy role -- read calls (`show`)
can succeed with a reporting-only role while writes (`set-price`,
`create-*`) 403 until "Manage store presence" / monetization edit access is
granted explicitly.

MONTHLY_BASE_PLAN_ID and LIFETIME_PURCHASE_OPTION_ID below must match the
IDs already live in Play Console -- they are arbitrary strings set at
creation time, not derived from the product SKU. Run `show` first to check.

REGIONS_VERSION must match the regionsVersion Play already has on file for
the product (visible in a onetimeproducts `show`; not present on the
subscriptions resource, so use the same value for both). Guessing an old
version causes currency-mismatch 400s as Play's regional currency mappings
change over time (e.g. Bulgaria's BGN -> EUR transition).

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
# These must match the IDs already live in Play Console -- check with `show`
# before assuming a value; they are not auto-derived from the SKU name.
MONTHLY_BASE_PLAN_ID = "localminder-premium"
LIFETIME_PURCHASE_OPTION_ID = "premium-lifetime"

DEFAULT_REGION = "US"
DEFAULT_CURRENCY = "USD"
# Regions/currencies snapshot version Play expects on regional pricing configs.
# Must match the regionsVersion already on the existing product/subscription --
# check via `show` before assuming a value. See
# https://support.google.com/googleplay/android-developer/answer/10532353
REGIONS_VERSION = "2025/03"


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


def money(price_cents: int) -> dict:
    return {
        "currencyCode": DEFAULT_CURRENCY,
        "units": str(price_cents // 100),
        "nanos": (price_cents % 100) * 10_000_000,
    }


def create_lifetime(service, package_name: str, price_cents: int) -> None:
    """Create/update the lifetime one-time product via the current
    monetization.onetimeproducts API (the legacy inappproducts endpoint
    returns 403 "Please migrate to the new publishing API" on this account).
    """
    body = {
        "packageName": package_name,
        "productId": LIFETIME_SKU,
        "listings": [
            {
                "languageCode": "en-US",
                "title": "LocalMind Premium (Lifetime)",
                "description": (
                    "Unlock on-device AI parsing, JSON export, and all "
                    "future premium features with a one-time purchase."
                ),
            }
        ],
        "purchaseOptions": [
            {
                "purchaseOptionId": LIFETIME_PURCHASE_OPTION_ID,
                "state": "ACTIVE",
                "buyOption": {
                    "legacyCompatible": True,
                },
                "regionalPricingAndAvailabilityConfigs": [
                    {
                        "regionCode": DEFAULT_REGION,
                        "price": money(price_cents),
                        "availability": "AVAILABLE",
                    }
                ],
            }
        ],
    }
    result = (
        service.monetization()
        .onetimeproducts()
        .patch(
            packageName=package_name,
            productId=LIFETIME_SKU,
            allowMissing=True,
            regionsVersion_version=REGIONS_VERSION,
            body=body,
        )
        .execute()
    )
    print(f"Created/updated one-time product: {result.get('productId')}")


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
        .create(
            packageName=package_name,
            productId=MONTHLY_SKU,
            regionsVersion_version=REGIONS_VERSION,
            body=body,
        )
        .execute()
    )
    print(f"Created subscription: {result.get('productId')}")


def set_price(service, package_name: str, product: str, price_cents: int) -> None:
    if product == "lifetime":
        existing = (
            service.monetization()
            .onetimeproducts()
            .get(packageName=package_name, productId=LIFETIME_SKU)
            .execute()
        )
        for purchase_option in existing.get("purchaseOptions", []):
            if purchase_option.get("purchaseOptionId") != LIFETIME_PURCHASE_OPTION_ID:
                continue
            for regional_config in purchase_option.get("regionalPricingAndAvailabilityConfigs", []):
                if regional_config.get("regionCode") == DEFAULT_REGION:
                    regional_config["price"] = money(price_cents)
        result = (
            service.monetization()
            .onetimeproducts()
            .patch(
                packageName=package_name,
                productId=LIFETIME_SKU,
                updateMask="purchaseOptions",
                regionsVersion_version=REGIONS_VERSION,
                body=existing,
            )
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
                regionsVersion_version=REGIONS_VERSION,
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
            service.monetization()
            .onetimeproducts()
            .get(packageName=package_name, productId=LIFETIME_SKU)
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
