# Apna Bazaar — Phase-wise Build Plan

This project was built in 5 phases. Each phase maps back to a stage in the original PRD.

| Our Phase | What it builds | PRD Stage |
|---|---|-----------|
| Phase 1 | Core search MVP — setup, seed data, Gemini semantic search, `/search` endpoint, ordering-window status | Stage 1   |
| Phase 2 | Excel ingestion — admin uploads a daily menu file instead of manual seeding | Stage 1   |
| Phase 3 | Broadcast/nudge system — organic nudges, AI-suggested nudges, seller-paid promos | Stage 1   |
| Phase 4 | Admin analytics dashboard + weekly AI insights | Stage 1   |
| Phase 5 | In-app pre-orders — Reserve button, `order_slot` table, enforced cutoffs, honest ratings | Stage 2   |

---

## Phase 1 — Core Search MVP
*Foundation: prove the core idea works.*

- Set up Spring Boot + PostgreSQL + Flyway, get the project running.
- Core tables: `community`, `provider`, `search_event`, `zero_result_log`, plus `offering` and `daily_line_item` kept as two separate tables — `offering` holds the seller's permanent catalog entry with a rich description (what Gemini actually searches against), `daily_line_item` holds today's specific price/availability/timing, linked back to it.
- `provider.rating` exists in the schema but stays unused/null in this phase — no order data exists yet to make a rating honest, so no rating is shown on buyer cards.
- A handful of sellers seeded manually, `status = active` (required immediately — `/search` only returns active sellers, so this isn't optional even for test data).
- Gemini semantic search wired up: buyer's query → Gemini reads the catalog → matched sellers returned.
- `/search` endpoint returning seller result cards, with ordering-window status attached (*"Pre-order open — closes 9AM"*, etc.).
- Every search auto-logged — hits into `search_event`, misses into `zero_result_log`.
- Basic chatbot frontend (HTML/JS) to test the whole thing end-to-end.

---

## Phase 2 — Excel Ingestion
*Replace manual seeding with a real, repeatable admin workflow.*

```bash
curl.exe -X POST "http://localhost:8080/admin/upload?communitySlug=tridasa" \
  -F "file=@sample-data/daily-upload-sample.xlsx"
```

- Admin fills a structured Excel sheet daily (seller, flat, WhatsApp, item, price, timing) — no rating column in this sheet, since ratings are buyer feedback, not something an admin can honestly transcribe.
- Upload endpoint parses the file (Apache POI).
- Existing sellers (matched by WhatsApp number) get today's listing updated; brand-new sellers are auto-created directly as active — no separate approval step, since the admin already verified them while filling the sheet.
- Yesterday's listings auto-deactivate when a new upload comes in.

---

## Phase 3 — Broadcast & Nudge System
*Pull buyers back in, don't just wait for them to search.*

- New `broadcast` table to store outgoing messages.
- **Organic nudges** — admin writes and schedules a message manually (now / scheduled / recurring).
- **AI-generated nudges** — system reads `zero_result_log` + time of day, asks Gemini for 4 nudge options, admin picks one.
- **Seller-paid promos** — a seller pays for a sponsored nudge, tracked separately.
- Every broadcast tracks its own performance: searches triggered, WhatsApp taps.

---

## Phase 4 — Admin Analytics Dashboard
*Turn all the quiet logging from Phases 1–3 into something you can see and act on.*

- Separate admin-only frontend page, calling a separate admin-only backend endpoint (`/admin/dashboard`).
- Aggregates data already sitting in the tables: total searches today, zero-result queries, peak search time, active sellers today, top queries, per-seller click-through rate.
- Weekly automated job (Monday 2 AM) generates plain-English AI insights, stored in `demand_insight`.
- Still no rating anywhere in this phase — the dashboard reports on search/click behaviour, not seller quality.

---

## Phase 5 — In-App Pre-orders 
*Capture real order intent, enforce cutoffs, and this is where rating finally becomes honest.*

- New `order_slot` table to record real reservations.
- "Reserve" button added before the WhatsApp redirect on each seller card.
- Ordering-window cutoffs now **enforced**, not just displayed — a reserve attempt after cutoff gets blocked.
- Rating, introduced properly for the first time: since `order_slot` is now a real, non-fake signal ("this buyer intended to order from this seller"), the next time that same buyer opens the chatbot, a soft prompt appears — *"You reserved from Lakshmi 2 days ago — how was it?"* — and only actual submitted answers update `provider.rating`. This is the first point in the whole build where a rating is genuinely earned instead of guessed, and only from here does it start showing on buyer-facing cards.
