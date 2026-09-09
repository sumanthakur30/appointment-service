# appointment-service branches

This GitHub repo is **appointments only** (`spring.application.name=appointment-service`, port **8093**).

| Branch | Meaning |
|--------|---------|
| `master` | Appointment integration (same as shop `dev` for this repo) |
| `dev` | **Must match appointment `master`.** Do not put IPD here. |
| `ipd/dev` | **Hospital IPD history** that was mistakenly on `dev`. Use until `ipd-service` has its own GitHub repo. |
| `feature/observability-ipd` | IPD observability (same IPD line as `ipd/dev`) |

Local folders:

- `D:\sugamFlow\appointment-service` → this repo, `dev` / `master`
- `D:\sugamFlow\ipd-service` → IPD sources; keep tracking `ipd/dev` (or a future `sumanthakur30/ipd-service`)

Compose builds `appointment-service` from `appointment-service/Dockerfile` and IPD from `ipd-service/Dockerfile`. Do not merge IPD into `dev` again.
