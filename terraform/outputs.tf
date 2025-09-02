output "alb_dns_name" {
  value = module.alb.dns_name
}

output "cloudfront_domain_name" {
  value = module.cloudfront.cloudfront_distribution_domain_name
}

output "vehicle_catalog_repository" {
  value = module.ecr.repository_name
}

output "db_address" {
  value = module.db.db_instance_address
}

output "s3_regional_domain_name" {
  value = module.vehicle_catalog_images.s3_bucket_bucket_regional_domain_name
}

output "tasks_iam_role_arn" {
  value = module.service.tasks_iam_role_arn
}

output "task_exec_iam_role_arn" {
  value = module.service.task_exec_iam_role_arn
}
