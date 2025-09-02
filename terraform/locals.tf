locals {
  project_name     = "wv"
  azs              = slice(data.aws_availability_zones.available.names, 0, 3)
  region           = "us-east-1"
  vpc_cidr         = "10.0.0.0/16"
  public_subnets   = [for k, v in local.azs : cidrsubnet(local.vpc_cidr, 8, k)]
  private_subnets  = [for k, v in local.azs : cidrsubnet(local.vpc_cidr, 8, k + 3)]
  database_subnets = [for k, v in local.azs : cidrsubnet(local.vpc_cidr, 8, k + 6)]
  domain_name      = "bnour.xyz"
  app_name         = "vehicle-catalog"
  database_name    = "vehiclecatalog"
  github_repo_name = "bnour-corrae"
  ecs_cluster_name = "wv"
  container_port   = 8080
  common_tags = {
    ManagedBy = "Terraform"
    Project = "local.project_name"
  }
}
