terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# Pub/Sub topics
resource "google_pubsub_topic" "seed_urls" {
  name = "seed-urls"
}

resource "google_pubsub_topic" "new_urls" {
  name = "new-urls"
}

# Pub/Sub subscriptions
resource "google_pubsub_subscription" "seed_urls_sub" {
  name  = "seed-urls-sub"
  topic = google_pubsub_topic.seed_urls.id
}

resource "google_pubsub_subscription" "new_urls_sub" {
  name  = "new-urls-sub"
  topic = google_pubsub_topic.new_urls.id
}

# Bigtable instance
resource "google_bigtable_instance" "search_engine" {
  name         = var.bigtable_instance_id
  cluster {
    cluster_id   = "search-engine-cluster"
    zone         = var.gcp_zone
    num_nodes    = 3
    storage_type = "SSD"
  }
  deletion_protection = false
}

# Bigtable tables
resource "google_bigtable_table" "pages" {
  name          = "pages"
  instance_name = google_bigtable_instance.search_engine.name
  column_family {
    family = "cf"
  }
}

resource "google_bigtable_table" "inverted_index" {
  name          = "inverted_index"
  instance_name = google_bigtable_instance.search_engine.name
  column_family {
    family = "cf"
  }
}
