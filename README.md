<h1 align="center">
  <img src="assets/logo.png" alt="ShotSense" width="120"/>
  <br/>ShotSense
</h1>

<p align="center">Your screenshots, understood.</p>

---

ShotSense **reads every screenshot you take — entirely on your device** — and acts on what it finds: verification codes die in minutes, bills get filed and kept, order IDs and tracking numbers become searchable, and the rest cleans itself up on a schedule you control.

## Features

- **Content-aware auto-pilot** — new screenshots are OCR'd on-device and classified automatically:
  - **OTP / verification screenshots** — lifetime shortened to 30 minutes, with a one-tap "Trash now" action on the alert
  - **Bills & receipts** — detected, filed, and protected from auto-delete
  - **Order IDs / tracking / UTR / UPI / PNR screenshots** — codes extracted, screenshot kept for 30 days
- **Codes tab** — every code ever screenshotted, searchable and one-tap copyable, with its thumbnail next to it. Find "that AWB from two weeks ago" in two seconds.
- **Bills tab** — receipts grouped by month with totals, ready when you need a proof of payment.
- **Search by content** — search finds the screenshot whose *text* matches, not just its filename.
- **Trash with undo** — deletions land in a 7-day trash first; restore anytime.
- **Keep Forever** — pins protect important screenshots from every deletion path.
- **Reliable background detection** — revives itself after reboot, app update or system kill.
- **Manual scheduling** — set a custom deletion timer on any screenshot, with batch operations.

## Privacy

Everything — detection, OCR, classification, storage — happens **on your device**.

- Screenshots are never uploaded anywhere. The OCR model ships inside the app and works fully offline.
- Extracted text lives only in the app's private storage and is deleted the moment the screenshot is trashed.
- Notifications never reveal content — you'll see "OTP screenshot", never the code itself.
- No accounts, no analytics, no network access needed.

## Download

| APK | For |
|---|---|
| [ShotSense-v0.1.0-arm64-release.apk](releases/ShotSense-v0.1.0-arm64-release.apk) | Most phones (2016 and later) |
| [ShotSense-v0.1.0-armv7-release.apk](releases/ShotSense-v0.1.0-armv7-release.apk) | Older 32-bit devices |
| [ShotSense-v0.1.0-x86_64-release.apk](releases/ShotSense-v0.1.0-x86_64-release.apk) | Emulators |

## Build

```bash
flutter pub get
flutter build apk --split-per-abi
```

## License

MIT — see [LICENSE](LICENSE)
