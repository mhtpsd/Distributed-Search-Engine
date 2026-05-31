variable "gcp_project_id" {
  description = "The GCP project ID where all resources will be deployed."
  type        = string
}

variable "gcp_region" {
  description = "The GCP region for regional resources (e.g. Cloud Run, Pub/Sub)."
  type        = string
  default     = "us-central1"
}

variable "gcp_zone" {
  description = "The GCP zone for zonal resources (e.g. Bigtable cluster)."
  type        = string
  default     = "us-central1-a"
}

variable "bigtable_instance_id" {
  description = "The Bigtable instance ID for the search engine index store."
  type        = string
  default     = "search-engine"
}
