output "bigtable_instance_name" {
  description = "The name of the Bigtable instance used by the search engine."
  value       = google_bigtable_instance.search_engine.name
}

output "pubsub_seed_urls_topic_id" {
  description = "The Pub/Sub topic ID for seed URLs."
  value       = google_pubsub_topic.seed_urls.id
}

output "pubsub_new_urls_topic_id" {
  description = "The Pub/Sub topic ID for newly discovered URLs."
  value       = google_pubsub_topic.new_urls.id
}
