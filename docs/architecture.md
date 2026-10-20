# Architecture

Browser (React SPA) --/api, session cookie--> Spring Boot (controller → service → repository) --> MongoDB

- **Collections:** `users`, `ngo_profiles`, `food_listings` (indexes on email, userId, donorId, status, expiryAt, district).
- **Security:** Spring Security filter chain; `/api/donor|ngo|admin/**` restricted by role; ownership enforced in services.
- **Matching:** `GeoUtil.distanceKm` (Haversine, R = 6371 km) filters POSTED, unexpired listings by the NGO's service radius.
- **State changes:** atomic `findAndModify` queries in `ListingService` so two NGOs cannot accept the same donation.
- **Districts:** `Districts.ALL` in the backend is the single source; the frontend loads it from `GET /api/districts`.
