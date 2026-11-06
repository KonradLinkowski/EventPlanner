-- Creates the per-service database and role used by event-service in local dev.
-- Additional services (booking, payment, notification) add their own DB/user
-- blocks here in later stages.
CREATE USER event_service WITH PASSWORD 'event_service';
CREATE DATABASE event_db OWNER event_service;
