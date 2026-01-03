CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS lost_reports (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  category TEXT NOT NULL,
  location TEXT NOT NULL,
  date_lost TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TEXT NOT NULL,
  FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS found_reports (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  staff_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  category TEXT NOT NULL,
  location TEXT NOT NULL,
  date_found TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TEXT NOT NULL,
  FOREIGN KEY(staff_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS custody_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  found_report_id INTEGER NOT NULL UNIQUE,
  shelf_code TEXT NOT NULL,
  intake_at TEXT NOT NULL,
  current_status TEXT NOT NULL,
  FOREIGN KEY(found_report_id) REFERENCES found_reports(id)
);

CREATE TABLE IF NOT EXISTS claims (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  found_report_id INTEGER NOT NULL,
  proof_text TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TEXT NOT NULL,
  decided_at TEXT,
  decided_by INTEGER,
  FOREIGN KEY(user_id) REFERENCES users(id),
  FOREIGN KEY(found_report_id) REFERENCES found_reports(id),
  FOREIGN KEY(decided_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS handovers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  claim_id INTEGER NOT NULL UNIQUE,
  custody_item_id INTEGER NOT NULL,
  otp_code TEXT NOT NULL,
  delivered_at TEXT,
  delivered_by INTEGER,
  receiver_name TEXT,
  FOREIGN KEY(claim_id) REFERENCES claims(id),
  FOREIGN KEY(custody_item_id) REFERENCES custody_items(id),
  FOREIGN KEY(delivered_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  actor_user_id INTEGER NOT NULL,
  action TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id INTEGER NOT NULL,
  created_at TEXT NOT NULL,
  FOREIGN KEY(actor_user_id) REFERENCES users(id)
);
